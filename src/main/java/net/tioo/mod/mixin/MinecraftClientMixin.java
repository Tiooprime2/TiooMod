package net.tioo.mod.mixin;

import net.minecraft.client.MinecraftClient;
import net.tioo.mod.TiooMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercept doAttack() — sama seperti Argon
 *
 * Tanpa ini: TriggerBot attack + mouse click = double attack = banned!
 * Dengan ini: saat TriggerBot aktif, doAttack() bawaan MC di-cancel
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onAttack(CallbackInfoReturnable<Boolean> cir) {
        // Cancel doAttack() bawaan MC saat TriggerBot aktif
        // Biar tidak double attack
        if (TiooMod.triggerBot != null && TiooMod.triggerBot.isEnabled()) {
            cir.setReturnValue(false);
        }
    }
}
