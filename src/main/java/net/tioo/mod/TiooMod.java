package net.tioo.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.tioo.mod.gui.TiooGui;
import net.tioo.mod.modules.combat.ShieldDisabler;
import net.tioo.mod.modules.combat.TriggerBot;
import org.lwjgl.glfw.GLFW;

public class TiooMod implements ClientModInitializer {

    public static final String MOD_ID   = "tioomod";
    public static final String MOD_NAME = "Tioo";

    public static TiooMod        INSTANCE;
    public static TriggerBot     triggerBot;
    public static ShieldDisabler shieldDisabler;

    private static boolean openGuiNextTick = false;
    private boolean keyWasDown = false;

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

            // ── RIGHT_SHIFT → buka/tutup GUI ──────────────────────────
            if (client.getWindow() != null) {
                boolean keyDown = InputUtil.isKeyPressed(
                    client.getWindow().getHandle(),
                    GLFW.GLFW_KEY_RIGHT_SHIFT
                );
                if (keyDown && !keyWasDown) {
                    if (client.currentScreen == null) {
                        openGuiNextTick = true;
                    } else if (client.currentScreen instanceof TiooGui) {
                        client.setScreen(null);
                    }
                }
                keyWasDown = keyDown;
            }

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
