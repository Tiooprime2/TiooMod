package net.tioo.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.tioo.mod.gui.TiooGui;
import net.tioo.mod.modules.combat.ShieldDisabler;
import net.tioo.mod.modules.combat.TriggerBot;

public class TiooMod implements ClientModInitializer {

    public static final String MOD_ID   = "tioomod";
    public static final String MOD_NAME = "Tioo";

    public static TiooMod        INSTANCE;
    public static TriggerBot     triggerBot;
    public static ShieldDisabler shieldDisabler;

    // Flag buat buka GUI di tick berikutnya
    // (tidak bisa setScreen langsung dari chat event)
    private static boolean openGuiNextTick = false;

    @Override
    public void onInitializeClient() {
        INSTANCE       = this;
        triggerBot     = new TriggerBot();
        shieldDisabler = new ShieldDisabler();

        // ═══════════════════════════════════════════
        // Chat commands
        // .tioo  → buka GUI
        // .tb    → toggle TriggerBot
        // .sd    → toggle ShieldDisabler
        // ═══════════════════════════════════════════
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return true;

            switch (message.toLowerCase().trim()) {
                case ".tioo" -> {
                    openGuiNextTick = true; // buka di tick berikutnya
                    return false;
                }
                case ".tb" -> {
                    triggerBot.toggle();
                    mc.player.sendMessage(Text.literal(
                        "§8[§bTioo§8] §fTriggerBot: " +
                        (triggerBot.isEnabled() ? "§aON" : "§cOFF")
                    ), true);
                    return false;
                }
                case ".sd" -> {
                    shieldDisabler.toggle();
                    mc.player.sendMessage(Text.literal(
                        "§8[§bTioo§8] §fShieldDisabler: " +
                        (shieldDisabler.isEnabled() ? "§aON" : "§cOFF")
                    ), true);
                    return false;
                }
            }
            return true;
        });

        // ═══════════════════════════════════════════
        // Tick — jalankan modules + handle GUI open
        // ═══════════════════════════════════════════
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            // Buka GUI di sini (setelah chat event selesai)
            if (openGuiNextTick && client.currentScreen == null) {
                client.setScreen(new TiooGui());
                openGuiNextTick = false;
            }

            // Tick modules
            if (client.world != null && client.player != null) {
                if (triggerBot.isEnabled())     triggerBot.onTick(client);
                if (shieldDisabler.isEnabled()) shieldDisabler.onTick(client);
            }
        });

        System.out.println("[Tioo] Initialized! by Tiooprime2");
    }
}
