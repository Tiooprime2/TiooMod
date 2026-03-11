package net.tioo.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.tioo.mod.gui.TiooGui;
import net.tioo.mod.modules.combat.ShieldDisabler;
import net.tioo.mod.modules.combat.TriggerBot;

public class TiooMod implements ClientModInitializer {

    public static final String MOD_ID   = "tioomod";
    public static final String MOD_NAME = "Tioo";

    public static TiooMod     INSTANCE;
    public static TriggerBot  triggerBot;
    public static ShieldDisabler shieldDisabler;
    public static TiooGui     gui;

    @Override
    public void onInitializeClient() {
        INSTANCE       = this;
        triggerBot     = new TriggerBot();
        shieldDisabler = new ShieldDisabler();
        gui            = new TiooGui();

        // ═══════════════════════════════════════════
        // CARA BUKA GUI — intercept chat message
        // Ketik ".tioo" di chat → GUI terbuka
        // Ketik ".tb"   di chat → toggle TriggerBot
        // Ketik ".sd"   di chat → toggle ShieldDisabler
        // ═══════════════════════════════════════════
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            return true; // allow semua chat
        });

        // Intercept SEBELUM chat dikirim ke server
        net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            MinecraftClient mc = MinecraftClient.getInstance();

            if (message.equalsIgnoreCase(".tioo")) {
                mc.execute(() -> mc.setScreen(new TiooGui()));
                return false; // jangan kirim ke server
            }

            if (message.equalsIgnoreCase(".tb")) {
                triggerBot.toggle();
                mc.player.sendMessage(
                    net.minecraft.text.Text.literal(
                        "[Tioo] TriggerBot: " + (triggerBot.isEnabled() ? "§aON" : "§cOFF")
                    ), true); // actionbar
                return false;
            }

            if (message.equalsIgnoreCase(".sd")) {
                shieldDisabler.toggle();
                mc.player.sendMessage(
                    net.minecraft.text.Text.literal(
                        "[Tioo] ShieldDisabler: " + (shieldDisabler.isEnabled() ? "§aON" : "§cOFF")
                    ), true);
                return false;
            }

            return true; // kirim chat normal
        });

        // Tick modules
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null && client.player != null) {
                if (triggerBot.isEnabled())     triggerBot.onTick(client);
                if (shieldDisabler.isEnabled()) shieldDisabler.onTick(client);
            }
        });

        System.out.println("[Tioo] Initialized! by Tiooprime2");
    }
}
