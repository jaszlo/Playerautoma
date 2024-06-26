package net.jasper.mod.util.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.PlayerAutomaMenuScreen;
import net.jasper.mod.gui.RecordingSelectorScreen;
import net.jasper.mod.gui.RecordingStorerScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

/**
 * Class storing all KeyBinding-Constants
 */
public class Constants {

    public static final String CTRL = "ctrl";
    public static final String SHIFT = "shift";
    public static final String ALT = "alt";

    private static final String KEYBINDING_CATEGORY = "PlayerAutoma";

    private static final String[] translations = {
            "playerautoma.keys.startRecording",
            "playerautoma.keys.stopRecording",
            "playerautoma.keys.startReplay",
            "playerautoma.keys.stopReplay",
            "playerautoma.keys.startLoop",
            "playerautoma.keys.storeRecording",
            "playerautoma.keys.loadRecording",
            "playerautoma.keys.pauseReplayOrRecord",
            "playerautoma.keys.openMenu",
            "playerautoma.keys.menuPrevention",
            "playerautoma.keys.quickMenu"
    };

    protected static final int AMOUNT_KEYBINDS = translations.length;

    private static final KeyBinding[] BINDINGS = {
            new KeyBinding(translations[0], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, KEYBINDING_CATEGORY),
            new KeyBinding(translations[1], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, KEYBINDING_CATEGORY),
            new KeyBinding(translations[2], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, KEYBINDING_CATEGORY),
            new KeyBinding(translations[3], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, KEYBINDING_CATEGORY),
            new KeyBinding(translations[4], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, KEYBINDING_CATEGORY),
            new KeyBinding(translations[5], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U, KEYBINDING_CATEGORY),
            new KeyBinding(translations[6], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_I, KEYBINDING_CATEGORY),
            new KeyBinding(translations[7], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, KEYBINDING_CATEGORY),
            new KeyBinding(translations[8], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, KEYBINDING_CATEGORY),
            new KeyBinding(translations[9], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_0, KEYBINDING_CATEGORY),
            new KeyBinding(translations[10], InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, KEYBINDING_CATEGORY),
    };

    public static final Set<KeyBinding> PLAYERAUTOMA_KEYBINDINGS = Set.of(BINDINGS);

    public static final KeyBinding STOP_REPLAY = BINDINGS[3];
    public static final KeyBinding PREVENT_MENU = BINDINGS[9];
    public static final KeyBinding QUICK_MENU = BINDINGS[10];

    private static final Runnable[] callbackMethods = {
            PlayerRecorder::startRecord,
            PlayerRecorder::stopRecord,
            PlayerRecorder::startReplay,
            PlayerRecorder::stopReplay,
            PlayerRecorder::startLoop,
            RecordingStorerScreen::open,
            RecordingSelectorScreen::open,
            PlayerRecorder::togglePause,
            PlayerAutomaMenuScreen::open,
            MenuPrevention::toggleBackgroundPrevention,
            () -> {} // Do nothing! The quickMenu opens onPress and closes onRelease and needs to be handled differently
    };

    protected static KeyBind[] defaultKeybinds = new KeyBind[AMOUNT_KEYBINDS];

    static {
        for (int i = 0; i < AMOUNT_KEYBINDS; i++) {
            defaultKeybinds[i] = new KeyBind(translations[i], BINDINGS[i], callbackMethods[i]);
            try {
                KeyBindingHelper.registerKeyBinding(BINDINGS[i]);
            } catch (IllegalStateException e) {
                // This happens if playerautoma fails to initialize. Therefore, do nothing and prevent minecraft from crashing.
                break;
            }
        }
    }
}