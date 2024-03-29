package net.jasper.mod.util.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.ModMenu;
import net.jasper.mod.gui.RecordingSelector;
import net.jasper.mod.gui.RecordingStorer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Class storing all KeyBinding-Constants
 */
public class Constants {

    private static final String KEYBINDING_CATEGORY = "PlayerAutoma";

    private static final String[] translations = {
            "playerautoma.keys.startRecording",
            "playerautoma.keys.stopRecording",
            "playerautoma.keys.startReplay",
            "playerautoma.keys.stopReplay",
            "playerautoma.keys.startLoop",
            "playerautoma.keys.storeRecording",
            "playerautoma.keys.loadRecording",
            "playerautoma.keys.pauseReplay",
            "playerautoma.keys.openMenu"
    };

    protected static final int AMOUNT_KEYBINDS = translations.length;

    private static final KeyBinding[] bindings = {
            new KeyBinding(translations[0], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KEYBINDING_CATEGORY),
            new KeyBinding(translations[1], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, KEYBINDING_CATEGORY),
            new KeyBinding(translations[2], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, KEYBINDING_CATEGORY),
            new KeyBinding(translations[3], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, KEYBINDING_CATEGORY),
            new KeyBinding(translations[4], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, KEYBINDING_CATEGORY),
            new KeyBinding(translations[5], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, KEYBINDING_CATEGORY),
            new KeyBinding(translations[6], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, KEYBINDING_CATEGORY),
            new KeyBinding(translations[7], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, KEYBINDING_CATEGORY),
            new KeyBinding(translations[8], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, KEYBINDING_CATEGORY)
    };

    public static final KeyBinding STOP_REPLAY = bindings[3];

    private static final Runnable[] callbackMethods = {
            PlayerRecorder::startRecord,
            PlayerRecorder::stopRecord,
            () -> PlayerRecorder.startReplay(false),
            PlayerRecorder::stopReplay,
            PlayerRecorder::startLoop,
            RecordingStorer::open,
            RecordingSelector::open,
            PlayerRecorder::togglePauseReplay,
            ModMenu::open
    };

    protected static KeyBind[] defaultKeybinds = new KeyBind[AMOUNT_KEYBINDS];

    static {
        for (int i = 0; i < AMOUNT_KEYBINDS; i++) {
            defaultKeybinds[i] = new KeyBind(translations[i], bindings[i], callbackMethods[i]);
            KeyBindingHelper.registerKeyBinding(bindings[i]);
        }
    }
}