package net.tioo.mod.modules.combat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.tioo.mod.modules.Module;

/**
 * ShieldDisabler — rewritten from Argon's ShieldDisabler.class
 *
 * Logic:
 * 1. Detect apakah target player sedang memakai Shield
 * 2. Cari Axe pertama di hotbar
 * 3. Switch ke Axe dan attack — disable shield
 * 4. Switch back ke slot semula (opsional)
 */
public class ShieldDisabler extends Module {

    // Settings
    private boolean switchBack    = true;  // kembali ke slot semula setelah attack
    private boolean requireHoldAxe = false; // harus pegang axe dulu
    private int     hitDelay      = 0;     // delay ms sebelum hit
    private int     switchDelay   = 100;   // delay ms sebelum switch back

    // State
    private int  previousSlot   = -1;
    private long lastSwitchTime = 0;
    private long lastHitTime    = 0;
    private boolean switched    = false;

    public ShieldDisabler() {
        super("Shield Disabler", "Automatically disables your opponents shield");
    }

    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        // ═══════════════════════════════════════════
        // SWITCH BACK — kembali ke slot semula
        // ═══════════════════════════════════════════
        long now = System.currentTimeMillis();
        if (switched && switchBack && previousSlot != -1) {
            if (now - lastSwitchTime >= switchDelay) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
                switched     = false;
            }
            return;
        }

        // ═══════════════════════════════════════════
        // RAYCAST — cek target di crosshair
        // ═══════════════════════════════════════════
        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) return;
        if (!(((EntityHitResult) hit).getEntity() instanceof PlayerEntity target)) return;
        if (target == mc.player) return;

        // ═══════════════════════════════════════════
        // SHIELD CHECK — apakah target sedang blocking?
        // mc.player.isBlocking() untuk target
        // ═══════════════════════════════════════════
        if (!isTargetBlocking(target)) return;

        // ═══════════════════════════════════════════
        // FIND AXE — cari axe pertama di hotbar (slot 0-8)
        // ═══════════════════════════════════════════
        int axeSlot = findAxeInHotbar(mc);
        if (axeSlot == -1) return; // tidak ada axe

        // Kalau requireHoldAxe, skip kalau tidak pegang axe
        if (requireHoldAxe && !(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;

        // ═══════════════════════════════════════════
        // HIT DELAY CHECK
        // ═══════════════════════════════════════════
        if (now - lastHitTime < hitDelay) return;

        // ═══════════════════════════════════════════
        // COOLDOWN CHECK — axe harus 100%
        // ═══════════════════════════════════════════
        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < 1.0f) return;

        // ═══════════════════════════════════════════
        // SWITCH TO AXE & ATTACK
        // ═══════════════════════════════════════════
        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = axeSlot;

        // Attack target
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingMainHand();

        // Update state
        switched      = true;
        lastSwitchTime = now;
        lastHitTime    = now;
    }

    /**
     * Cek apakah target sedang blocking dengan shield
     */
    private boolean isTargetBlocking(PlayerEntity target) {
        // isBlocking() return true kalau sedang use shield
        return target.isBlocking();
    }

    /**
     * Cari slot axe pertama di hotbar (0-8)
     */
    private int findAxeInHotbar(MinecraftClient mc) {
        for (int i = 0; i <= 8; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1; // tidak ada axe
    }

    // ─── Settings Getters/Setters ──────────────────────────────────────────
    public boolean isSwitchBack()      { return switchBack; }
    public boolean isRequireHoldAxe()  { return requireHoldAxe; }
    public int     getHitDelay()       { return hitDelay; }
    public int     getSwitchDelay()    { return switchDelay; }

    public void setSwitchBack(boolean v)     { this.switchBack     = v; }
    public void setRequireHoldAxe(boolean v) { this.requireHoldAxe = v; }
    public void setHitDelay(int v)           { this.hitDelay       = v; }
    public void setSwitchDelay(int v)        { this.switchDelay    = v; }
}
