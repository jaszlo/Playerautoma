package net.jasper.mod.util.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.PlayerAutomaMenu;
import net.jasper.mod.gui.RecordingSelector;
import net.jasper.mod.gui.RecordingStorer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Class storing all KeyBinding-Constants
 */
public class Constants {
    protected static final int AMOUNT_KEYBINDS = 8;

    private static final String KEYBINDING_CATEGORY = "Playerautoma";

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
            new KeyBinding(names[0], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KEYBINDING_CATEGORY),
            new KeyBinding(names[1], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, KEYBINDING_CATEGORY),
            new KeyBinding(names[2], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, KEYBINDING_CATEGORY),
            new KeyBinding(names[3], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, KEYBINDING_CATEGORY),
            new KeyBinding(names[4], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, KEYBINDING_CATEGORY),
            new KeyBinding(names[5], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, KEYBINDING_CATEGORY),
            new KeyBinding(names[6], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, KEYBINDING_CATEGORY),

            // Open Menu
            new KeyBinding(names[7], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, KEYBINDING_CATEGORY)
    };

    public static final KeyBinding CANCEL_REPLAY = bindings[3];

    private static final Runnable[] callbackMethods = {

            // Player Recording Keybinds
            PlayerRecorder::startRecord,
            PlayerRecorder::stopRecord,
            PlayerRecorder::startReplay,
            PlayerRecorder::stopReplay,
            PlayerRecorder::startLoop,
            RecordingStorer::open,
            RecordingSelector::open,

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
