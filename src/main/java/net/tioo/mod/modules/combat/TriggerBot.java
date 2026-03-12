package net.tioo.mod.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.tioo.mod.modules.Module;

import java.util.Random;

public class TriggerBot extends Module {

    // ── Settings ──────────────────────────────────────────────────────────
    private boolean legitMode      = true;   // humanize semua
    private boolean workWhileJump  = true;   // tetap hit saat jump
    private boolean whileAscend    = true;   // hit saat naik jump
    private boolean allEntities    = false;
    private boolean swingHand      = true;
    private boolean checkShield    = false;
    private boolean onlyCritSword  = false;
    private boolean onlyCritAxe    = false;
    private boolean humanizeCool   = true;   // randomize cooldown ±20ms

    // Delay range (ms)
    private int minDelay = 50;
    private int maxDelay = 150;

    // Miss chance (0-100)
    private int missChance = 7; // 7%

    // ── Internal state ────────────────────────────────────────────────────
    private final Random random = new Random();

    // Timer pakai nanoTime seperti Argon
    private long lastAttackTime = 0;
    private long currentDelay   = 100;

    // Untuk "pending attack" — tunda attack sebentar seperti manusia
    private boolean pendingAttack   = false;
    private long    pendingAt       = 0;   // kapan attack akan dieksekusi
    private Entity  pendingTarget   = null;

    // Sword/axe base delay (Argon style)
    private final int swordMin = 540, swordMax = 550;
    private final int axeMin   = 780, axeMax   = 800;

    public TriggerBot() {
        super("Trigger Bot", "Automatically hits players for you");
    }

    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        long now = nanoMs();

        // ── Eksekusi pending attack ────────────────────────────────────────
        if (pendingAttack && pendingTarget != null) {
            if (now >= pendingAt) {
                // Cek masih valid
                if (pendingTarget.isAlive() && mc.crosshairTarget instanceof EntityHitResult hit
                        && hit.getEntity() == pendingTarget) {

                    // Cek cooldown
                    float cooldown = mc.player.getAttackCooldownProgress(0.5f);
                    if (cooldown >= 0.9f) {
                        doAttack(mc, pendingTarget);
                    }
                }
                pendingAttack = false;
                pendingTarget = null;
            }
            return; // tunggu pending selesai dulu
        }

        // ── Movement check ────────────────────────────────────────────────
        if (!workWhileJump) {
            // Kalau workWhileJump OFF → hanya hit saat di tanah
            if (!mc.player.isOnGround()) return;
        } else {
            // Kalau workWhileJump ON tapi whileAscend OFF → skip saat naik
            if (!whileAscend) {
                boolean ascending = !mc.player.isOnGround()
                    && mc.player.getVelocity().y > 0;
                if (ascending) return;
            }
            // Falling dan sprint-jump tetap jalan ✅
        }

        // ── Delay check (base delay Argon) ────────────────────────────────
        if (now - lastAttackTime < currentDelay) return;

        // ── Crosshair check ───────────────────────────────────────────────
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = hit.getEntity();
        if (entity == null || entity == mc.player) return;
        if (!allEntities && !(entity instanceof PlayerEntity)) return;

        if (checkShield && entity instanceof PlayerEntity player)
            if (player.isBlocking()) return;

        var item = mc.player.getMainHandStack().getItem();
        boolean isSword = item instanceof SwordItem;
        boolean isAxe   = item instanceof AxeItem;

        if (onlyCritSword && isSword && mc.player.fallDistance <= 0.0f) return;
        if (onlyCritAxe   && isAxe   && mc.player.fallDistance <= 0.0f) return;

        // Cooldown check
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        if (cooldown < 0.9f) return;

        // ── LEGIT MODE — humanize ─────────────────────────────────────────
        if (legitMode) {

            // 1. Miss chance — kadang skip attack seperti manusia miss click
            if (random.nextInt(100) < missChance) {
                // Skip attack ini, reset delay
                lastAttackTime = now;
                currentDelay   = nextDelay(isAxe);
                return;
            }

            // 2. Gaussian reaction delay — manusia tidak langsung klik
            //    rata-rata (minDelay+maxDelay)/2, variance ±30ms
            long humanDelay = gaussianDelay();

            // Set pending attack — eksekusi setelah humanDelay ms
            pendingAttack  = true;
            pendingTarget  = entity;
            pendingAt      = now + humanDelay;

        } else {
            // Non-legit — langsung attack
            doAttack(mc, entity);
        }

        // Reset delay untuk attack berikutnya
        lastAttackTime = now;
        currentDelay   = nextDelay(isAxe);

        // Humanize cooldown ±20ms
        if (humanizeCool) {
            currentDelay += (long)(random.nextGaussian() * 20);
            if (currentDelay < 50) currentDelay = 50;
        }
    }

    private void doAttack(MinecraftClient mc, Entity entity) {
        mc.interactionManager.attackEntity(mc.player, entity);
        if (swingHand) mc.player.swingHand(Hand.MAIN_HAND);
    }

    // Gaussian delay — mirip distribusi reaksi manusia
    private long gaussianDelay() {
        double mean    = (minDelay + maxDelay) / 2.0;
        double sigma   = (maxDelay - minDelay) / 4.0; // 95% dalam range
        long   delay   = (long)(mean + random.nextGaussian() * sigma);
        // Clamp dalam range
        delay = Math.max(minDelay, Math.min(maxDelay, delay));
        return delay;
    }

    private long nextDelay(boolean isAxe) {
        if (isAxe) return axeMin + random.nextInt(axeMax - axeMin + 1);
        return swordMin + random.nextInt(swordMax - swordMin + 1);
    }

    private long nanoMs() { return System.nanoTime() / 1_000_000L; }

    // ── Getters/Setters ───────────────────────────────────────────────────
    public boolean isLegitMode()      { return legitMode; }
    public boolean isWorkWhileJump()  { return workWhileJump; }
    public boolean isWhileAscend()    { return whileAscend; }
    public boolean isAllEntities()    { return allEntities; }
    public boolean isSwingHand()      { return swingHand; }
    public boolean isCheckShield()    { return checkShield; }
    public boolean isOnlyCritSword()  { return onlyCritSword; }
    public boolean isOnlyCritAxe()    { return onlyCritAxe; }
    public boolean isHumanizeCool()   { return humanizeCool; }
    public int     getMinDelay()      { return minDelay; }
    public int     getMaxDelay()      { return maxDelay; }
    public int     getMissChance()    { return missChance; }

    public void setLegitMode(boolean v)      { this.legitMode      = v; }
    public void setWorkWhileJump(boolean v)  { this.workWhileJump  = v; }
    public void setWhileAscend(boolean v)    { this.whileAscend    = v; }
    public void setAllEntities(boolean v)    { this.allEntities    = v; }
    public void setSwingHand(boolean v)      { this.swingHand      = v; }
    public void setCheckShield(boolean v)    { this.checkShield    = v; }
    public void setOnlyCritSword(boolean v)  { this.onlyCritSword  = v; }
    public void setOnlyCritAxe(boolean v)    { this.onlyCritAxe    = v; }
    public void setHumanizeCool(boolean v)   { this.humanizeCool   = v; }
    public void setMinDelay(int v)           { this.minDelay       = v; }
    public void setMaxDelay(int v)           { this.maxDelay       = v; }
    public void setMissChance(int v)         { this.missChance     = v; }
}
