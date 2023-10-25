package net.jasper.mod.util.keybinds;

import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.automation.InputRecorder;
import net.jasper.mod.gui.PlayerAutomaMenu;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class Constants {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;
    protected static final int AMOUNT_KEYBINDS = 8;

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
            new KeyBinding(names[0], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "Player Recorder"),
            new KeyBinding(names[1], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "Player Recorder"),
            new KeyBinding(names[2], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "Player Recorder"),
            new KeyBinding(names[3], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "Player Recorder"),
            new KeyBinding(names[4], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, "Player Recorder"),
            new KeyBinding(names[5], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, "Player Recorder"),
            new KeyBinding(names[6], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, "Player Recorder"),

            // Open Menu
            new KeyBinding(names[7], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "Jasper's Mod")
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
        }
    }

}
