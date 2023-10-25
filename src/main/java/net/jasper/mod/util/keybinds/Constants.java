package net.jasper.mod.util.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.jasper.mod.automation.InputRecorder;
import net.jasper.mod.gui.PlayerAutomaMenu;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Constants {
    protected static final int AMOUNT_KEYBINDS = 8;

    private static final String keyBindCategory = "Playerautoma";

    private static final String[] names = {
            // Player Recoding Keybinds
            "Start Recording",
            "Stop Recording",
            "Replay Recording",
            "Cancel Replay",
            "Loop Replay",
            "Store Recording",
            "Load Recording",

            // Open Menu
            "Open Mod Menu"
    };

    private static final KeyBinding[] bindings = {

            // Player Recoding Keybinds
            new KeyBinding(names[0], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, keyBindCategory),
            new KeyBinding(names[1], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, keyBindCategory),
            new KeyBinding(names[2], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, keyBindCategory),
            new KeyBinding(names[3], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, keyBindCategory),
            new KeyBinding(names[4], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, keyBindCategory),
            new KeyBinding(names[5], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, keyBindCategory),
            new KeyBinding(names[6], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, keyBindCategory),

            // Open Menu
            new KeyBinding(names[7], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, keyBindCategory)
    };

    private static final Runnable[] callbackMethods = {

            // Player Recording Keybinds
            InputRecorder::startRecord,
            InputRecorder::stopRecord,
            InputRecorder::startReplay,
            InputRecorder::stopReplay,
            InputRecorder::startLoop,
            InputRecorder::storeRecord,
            InputRecorder::loadRecord,

            // Toggle GUI
            PlayerAutomaMenu::open
    };

    protected static KeyBind[] defaultKeybinds = new KeyBind[AMOUNT_KEYBINDS];

    static {
        for (int i = 0; i < AMOUNT_KEYBINDS; i++) {
            defaultKeybinds[i] = new KeyBind(names[i], bindings[i], callbackMethods[i]);
            KeyBindingHelper.registerKeyBinding(bindings[i]);
        }
    }

}
