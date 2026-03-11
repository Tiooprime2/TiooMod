package net.tioo.mod.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.tioo.mod.modules.Module;

/**
 * TriggerBot — rewritten from Argon's TriggerBot.class
 *
 * Logic:
 * 1. Raycast check: cek apakah crosshair mengarah ke LivingEntity
 * 2. Cooldown check: hanya swing saat attack strength = 100%
 * 3. Swing attack ke target
 */
public class TriggerBot extends Module {

    // Settings
    private boolean workInScreen  = false; // trigger meski di dalam screen
    private boolean whileUse      = false; // trigger meski sedang makan/blocking
    private boolean allEntities   = false; // attack semua entity, bukan hanya player
    private boolean onlyOnGround  = false; // hanya attack saat di tanah/jatuh
    private boolean swingHand     = true;  // swing hand saat attack
    private int     minDelay      = 0;     // ms minimum delay antar attack
    private int     maxDelay      = 0;     // ms maximum delay antar attack

    // State
    private long    lastAttackTime = 0;
    private long    currentDelay   = 0;

    public TriggerBot() {
        super("Trigger Bot", "Automatically hits players for you");
    }

    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        // Skip kalau di dalam screen (kecuali workInScreen aktif)
        if (mc.currentScreen != null && !workInScreen) return;

        // Skip kalau sedang use item (kecuali whileUse aktif)
        if (!whileUse && mc.player.isUsingItem()) return;

        // ═══════════════════════════════════════════
        // COOLDOWN CHECK — hanya swing kalau 100%
        // ═══════════════════════════════════════════
        float attackCooldown = mc.player.getAttackCooldownProgress(0f);
        if (attackCooldown < 1.0f) return;

        // ═══════════════════════════════════════════
        // DELAY CHECK
        // ═══════════════════════════════════════════
        long now = System.currentTimeMillis();
        if (now - lastAttackTime < currentDelay) return;

        // ═══════════════════════════════════════════
        // RAYCAST CHECK — crosshair ke LivingEntity?
        // ═══════════════════════════════════════════
        HitResult hit = mc.crosshairTarget;
        if (hit == null) return;
        if (hit.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult entityHit = (EntityHitResult) hit;

        // Cek apakah entity valid
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return;

        // Skip player sendiri
        if (target == mc.player) return;

        // Kalau allEntities false, hanya attack PlayerEntity
        if (!allEntities && !(target instanceof PlayerEntity)) return;

        // Skip mob yang sudah mati
        if (target.isDead() || target.getHealth() <= 0) return;

        // ═══════════════════════════════════════════
        // ONLY ON GROUND / FALLING CHECK
        // ═══════════════════════════════════════════
        if (onlyOnGround) {
            // Skip kalau player sedang naik dari jump
            if (mc.player.getVelocity().y > 0) return;
        }

        // ═══════════════════════════════════════════
        // ATTACK!
        // ═══════════════════════════════════════════
        mc.interactionManager.attackEntity(mc.player, target);
        if (swingHand) {
            mc.player.swingMainHand();
        }

        // Update timing
        lastAttackTime = now;
        // Random delay antara min dan max
        currentDelay = minDelay + (long)(Math.random() * (maxDelay - minDelay + 1));
    }

    // ─── Settings Getters/Setters ──────────────────────────────────────────
    public boolean isWorkInScreen()  { return workInScreen; }
    public boolean isWhileUse()      { return whileUse; }
    public boolean isAllEntities()   { return allEntities; }
    public boolean isOnlyOnGround()  { return onlyOnGround; }
    public boolean isSwingHand()     { return swingHand; }
    public int     getMinDelay()     { return minDelay; }
    public int     getMaxDelay()     { return maxDelay; }

    public void setWorkInScreen(boolean v)  { this.workInScreen  = v; }
    public void setWhileUse(boolean v)      { this.whileUse      = v; }
    public void setAllEntities(boolean v)   { this.allEntities   = v; }
    public void setOnlyOnGround(boolean v)  { this.onlyOnGround  = v; }
    public void setSwingHand(boolean v)     { this.swingHand     = v; }
    public void setMinDelay(int v)          { this.minDelay       = v; }
    public void setMaxDelay(int v)          { this.maxDelay       = v; }
}
