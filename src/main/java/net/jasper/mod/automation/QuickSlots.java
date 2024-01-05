package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.PlayerController;
import net.jasper.mod.util.data.Recording;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * QuickSlots for storing and loading Recordings for the PlayerRecorder
 */
public class QuickSlots {

    public static Recording[] quickSlots = new Recording[9];

    // KeyBinding State
    private static final int[] storeCooldowns = new int[9];
    private static final int[] loadCooldowns = new int[9];
    private static final boolean[] CTRLPressed = new boolean[9];
    private static final boolean[] ALTPressed = new boolean[9];
    private static final int COOLDOWN = 5;

    public static void store(int slot, Recording recording) {
        quickSlots[slot] = recording;
    }
    public static Recording load(int slot) {
        return quickSlots[slot];
    }

    // Initialize State
    static {
        Arrays.fill(quickSlots, null);
        Arrays.fill(ALTPressed, false);
        Arrays.fill(CTRLPressed, false);
        Arrays.fill(storeCooldowns, 0);
        Arrays.fill(loadCooldowns, 0);
    }

    private static void handleQuickSlotKeyPress(long handle, int[] cooldowns, boolean[] pressed) {
        for (int i = 0; i < pressed.length; i++) {
            if (cooldowns[i] > 0) {
                cooldowns[i]--;
                continue;
            }
            pressed[i] = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_1 + i);
            if (pressed[i]) {
                // Fill all cooldowns to prevent double key press
                Arrays.fill(cooldowns, COOLDOWN);
                return;
            }
        }
    }

    private static void consumeKeyPress(KeyBinding key, int limit) {
        int error = 0;
        while (key.wasPressed()) {
            key.setPressed(false);
            if (error++ > limit) {
                PlayerAutomaClient.LOGGER.warn("Could not unset keybinding for QuickSlot");
                break;
            }
        }
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            long handle = client.getWindow().getHandle();

            // Check Store QuickSlot KeyBindings
            if (CTRLPressed(handle)) {
                handleQuickSlotKeyPress(handle, storeCooldowns, CTRLPressed);
            }

            // Check Load QuickSlot KeyBindings
            if (ALTPressed(handle)) {
                handleQuickSlotKeyPress(handle, loadCooldowns, ALTPressed);
            }

            for (int i = 0; i < CTRLPressed.length; i++) {
                // Store Recording to QuickSlot
                if (CTRLPressed[i]) {
                    // Unset key to not change selectedSlot
                    consumeKeyPress(client.options.hotbarKeys[i], 10);
                    // Check if store operation can be done
                    if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
                        PlayerController.writeToChat("Cannot store Recording while Recording or Replaying");
                        continue;
                    } else if (PlayerRecorder.record == null || PlayerRecorder.record.isEmpty()) {
                        PlayerController.writeToChat("No Recording to store");
                        continue;
                    }

                    store(i, PlayerRecorder.record.copy());
                    PlayerController.writeToChat("Stored Recording to QuickSlot " + (i + 1));

                // Load Recording from QuickSlot
                } else if (ALTPressed[i]) {
                    // Unset key to not change selectedSlot
                    consumeKeyPress(client.options.hotbarKeys[i], 10);
                    Recording r = load(i);
                    // Check if load operation can be done
                    if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
                        PlayerController.writeToChat("Cannot load Recording while Recording or Replaying");
                        continue;
                    } else if (r == null || r.isEmpty()) {
                        PlayerController.writeToChat("No Recording in QuickSlot " + (i + 1));
                        continue;
                    }

                    PlayerRecorder.record = r;
                    PlayerController.writeToChat("Loaded Recording to QuickSlot " + (i + 1));
                }
            }

            // Reset Keys pressed
            Arrays.fill(ALTPressed, false);
            Arrays.fill(CTRLPressed, false);
        });
    }

    private static boolean CTRLPressed(long handle) {
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean ALTPressed(long handle) {
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
