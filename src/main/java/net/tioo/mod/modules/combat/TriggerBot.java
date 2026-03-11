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

/**
 * TriggerBot — rewritten based on Argon source
 *
 * Key logic dari Argon:
 * - Sword delay: 540-550ms (random)
 * - Axe delay: 780-800ms (random)
 * - Only crit: cek fallDistance <= 0
 * - Check shield: cek target.isBlocking()
 * - Strict crosshairTarget only (bukan kill aura)
 */
public class TriggerBot extends Module {

    // Settings
    private boolean whileAscend  = false; // attack saat naik jump
    private boolean allEntities  = false; // attack semua entity
    private boolean swingHand    = true;  // swing tangan
    private boolean onlyCritSword = false; // hanya crit saat sword
    private boolean onlyCritAxe  = false; // hanya crit saat axe
    private boolean checkShield  = false; // skip kalau target blocking

    // Delay — sama seperti Argon
    private int swordMinDelay = 540;
    private int swordMaxDelay = 550;
    private int axeMinDelay   = 780;
    private int axeMaxDelay   = 800;

    // State
    private long lastAttackTime   = 0;
    private long currentDelay     = 545;

    public TriggerBot() {
        super("Trigger Bot", "Automatically hits players for you");
    }

    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        // While ascending check — sama kayak Argon
        // Argon: skip kalau not on ground DAN velocity y > 0 DAN fallDistance <= 0
        if (!whileAscend) {
            boolean ascending = !mc.player.isOnGround()
                && mc.player.getVelocity().y > 0;
            boolean notFalling = !mc.player.isOnGround()
                && mc.player.fallDistance <= 0.0f;
            if (ascending || notFalling) return;
        }

        // Delay check
        long now = System.currentTimeMillis();
        if (now - lastAttackTime < currentDelay) return;

        // Strict crosshair check
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = hit.getEntity();
        if (entity == null) return;
        if (entity == mc.player) return;

        // Entity filter
        if (!allEntities && !(entity instanceof PlayerEntity)) return;

        // Check shield — skip kalau target blocking
        if (checkShield && entity instanceof PlayerEntity player) {
            if (player.isBlocking()) return;
        }

        // Item check — beda delay sword vs axe
        var item = mc.player.getMainHandStack().getItem();
        boolean isSword = item instanceof SwordItem;
        boolean isAxe   = item instanceof AxeItem;

        // Only crit checks — sama kayak Argon (cek fallDistance)
        if (onlyCritSword && isSword && mc.player.fallDistance <= 0.0f) return;
        if (onlyCritAxe   && isAxe   && mc.player.fallDistance <= 0.0f) return;

        // Attack cooldown check
        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < 1.0f) return;

        // ATTACK!
        mc.interactionManager.attackEntity(mc.player, entity);
        if (swingHand) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        // Update delay — random range tergantung item
        lastAttackTime = now;
        if (isAxe) {
            currentDelay = axeMinDelay
                + (long)(Math.random() * (axeMaxDelay - axeMinDelay + 1));
        } else {
            // Sword atau item lain pakai sword delay
            currentDelay = swordMinDelay
                + (long)(Math.random() * (swordMaxDelay - swordMinDelay + 1));
        }
    }

    // Getters/Setters
    public boolean isWhileAscend()   { return whileAscend; }
    public boolean isAllEntities()   { return allEntities; }
    public boolean isSwingHand()     { return swingHand; }
    public boolean isOnlyCritSword() { return onlyCritSword; }
    public boolean isOnlyCritAxe()   { return onlyCritAxe; }
    public boolean isCheckShield()   { return checkShield; }

    public void setWhileAscend(boolean v)   { this.whileAscend   = v; }
    public void setAllEntities(boolean v)   { this.allEntities   = v; }
    public void setSwingHand(boolean v)     { this.swingHand     = v; }
    public void setOnlyCritSword(boolean v) { this.onlyCritSword = v; }
    public void setOnlyCritAxe(boolean v)   { this.onlyCritAxe   = v; }
    public void setCheckShield(boolean v)   { this.checkShield   = v; }
}
