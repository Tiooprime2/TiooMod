package net.tioo.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.tioo.mod.gui.TiooGui;
import net.tioo.mod.modules.combat.ShieldDisabler;
import net.tioo.mod.modules.combat.TriggerBot;
import org.lwjgl.glfw.GLFW;

public class TiooMod implements ClientModInitializer {

    public static final String MOD_ID   = "tioomod";
    public static final String MOD_NAME = "Tioo";

    // Singleton
    public static TiooMod INSTANCE;

    // Modules
    public static TriggerBot    triggerBot;
    public static ShieldDisabler shieldDisabler;

    // GUI
    public static TiooGui gui;

    // Keybind buka GUI (default: RIGHT_SHIFT)
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;

        // Init modules
        triggerBot     = new TriggerBot();
        shieldDisabler = new ShieldDisabler();

        // Init GUI
        gui = new TiooGui();

        // Register keybind RIGHT_SHIFT
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.tioomod.open_gui",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.tioomod"
        ));

        // Tick event — cek keybind + jalankan modules
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Buka GUI saat RIGHT_SHIFT ditekan
            while (openGuiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(gui);
                }
            }

            // Tick modules
            if (client.world != null && client.player != null) {
                if (triggerBot.isEnabled())     triggerBot.onTick(client);
                if (shieldDisabler.isEnabled()) shieldDisabler.onTick(client);
            }
        });

        System.out.println("[Tioo] Mod initialized! Made by Tiooprime2");
    }
}
