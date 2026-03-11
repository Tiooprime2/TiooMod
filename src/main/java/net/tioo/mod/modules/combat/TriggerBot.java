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

public class TriggerBot extends Module {

    // Settings
    private boolean whileAscend   = false;
    private boolean allEntities   = false;
    private boolean swingHand     = true;
    private boolean onlyCritSword = false;
    private boolean onlyCritAxe   = false;
    private boolean checkShield   = false;

    // Delay — sama seperti Argon
    private final int swordMinDelay = 540;
    private final int swordMaxDelay = 550;
    private final int axeMinDelay   = 780;
    private final int axeMaxDelay   = 800;

    // State
    private long lastAttackTime = 0;
    private long currentDelay   = 545;

    public TriggerBot() {
        super("Trigger Bot", "Automatically hits players for you");
    }

    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        // While ascending check — sama kayak Argon
        if (!whileAscend) {
            boolean ascending = !mc.player.isOnGround() && mc.player.getVelocity().y > 0;
            boolean notFalling = !mc.player.isOnGround() && mc.player.fallDistance <= 0.0f;
            if (ascending || notFalling) return;
        }

        // Delay check
        long now = System.nanoTime() / 1000000L; // pakai nanoTime seperti Argon TimerUtils
        if (now - lastAttackTime < currentDelay) return;

        // Strict crosshair only
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity entity = hit.getEntity();
        if (entity == null || entity == mc.player) return;

        // Entity filter
        if (!allEntities && !(entity instanceof PlayerEntity)) return;

        // Check shield
        if (checkShield && entity instanceof PlayerEntity player) {
            if (player.isBlocking()) return;
        }

        // Item check
        var item = mc.player.getMainHandStack().getItem();
        boolean isSword = item instanceof SwordItem;
        boolean isAxe   = item instanceof AxeItem;

        // Only crit checks
        if (onlyCritSword && isSword && mc.player.fallDistance <= 0.0f) return;
        if (onlyCritAxe   && isAxe   && mc.player.fallDistance <= 0.0f) return;

        // Attack cooldown — pakai 0.5F seperti Argon WorldUtils.isCrit()
        // Lebih akurat dari 0f yang biasa dipakai
        float cooldown = mc.player.getAttackCooldownProgress(0.5f);
        if (cooldown < 0.9f) return;

        // ATTACK
        mc.interactionManager.attackEntity(mc.player, entity);
        if (swingHand) mc.player.swingHand(Hand.MAIN_HAND);

        // Reset delay — random range tergantung item
        lastAttackTime = now;
        if (isAxe) {
            currentDelay = axeMinDelay + (long)(Math.random() * (axeMaxDelay - axeMinDelay + 1));
        } else {
            currentDelay = swordMinDelay + (long)(Math.random() * (swordMaxDelay - swordMinDelay + 1));
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
