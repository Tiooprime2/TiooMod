package net.tioo.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.tioo.mod.TiooMod;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercept doAttack() — persis seperti Argon
 *
 * Argon logic:
 * - Cancel doAttack() HANYA kalau mouse kiri tidak ditekan
 * - Kalau mouse kiri ditekan = klik manual = tetap jalan normal
 * - Kalau mouse kiri tidak ditekan = attack dari TriggerBot = cancel MC punya
 *
 * Tanpa ini = TriggerBot attack + MC attack = double attack = banned!
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getWindow() == null) return;

        // Cek apakah TriggerBot atau ShieldDisabler aktif
        boolean tbOn = TiooMod.triggerBot     != null && TiooMod.triggerBot.isEnabled();
        boolean sdOn = TiooMod.shieldDisabler != null && TiooMod.shieldDisabler.isEnabled();

        if (tbOn || sdOn) {
            // Cancel doAttack() bawaan MC kalau mouse kiri TIDAK ditekan
            // (artinya attack datang dari mod, bukan dari player klik)
            boolean mousePressed = GLFW.glfwGetMouseButton(
                mc.getWindow().getHandle(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) == GLFW.GLFW_PRESS;

            if (!mousePressed) {
                cir.setReturnValue(false);
            }
        }
    }
}
