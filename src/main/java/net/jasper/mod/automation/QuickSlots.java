package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.PlayerController;
import net.jasper.mod.util.data.Recording;
import net.jasper.mod.util.data.TaskQueue;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * QuickSlots for storing and loading Recordings for the PlayerRecorder
 */
public class QuickSlots {

    public static Recording[] quickSlots = new Recording[10];

    // KeyBinding State
    private static final int[] storeCooldowns = new int[10];
    private static final int[] loadCooldowns = new int[10];
    private static final boolean[] CTRLPressed = new boolean[10];
    private static final boolean[] ALTPressed = new boolean[10];
    private static final int COOLDOWN = 10;

    private static final TaskQueue changeSelectedSlot = new TaskQueue(TaskQueue.LOW_PRIORITY);

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
            pressed[i] = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_0 + i);
            if (pressed[i]) {
                // Fill all cooldowns to prevent double key press
                Arrays.fill(cooldowns, COOLDOWN);
                return;
            }
        }
    }

    public static void register() {
        changeSelectedSlot.register("changeSelectedSlot");

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            // Store current selected slot to restore it later as it gets changed by the QuickSlot KeyBindings
            int selectedSlotBackup = client.player.getInventory().selectedSlot;
            long handle = client.getWindow().getHandle();

            // Check Store QuickSlot KeyBindings
            if (CTRLPressed(handle)) {
                handleQuickSlotKeyPress(handle, storeCooldowns, CTRLPressed);
            }

            // Store Recording to QuickSlot
            for (int i = 0; i < CTRLPressed.length; i++) {
                if (CTRLPressed[i]) {

                    if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
                        PlayerController.writeToChat("Cannot store Recording while Recording or Replaying");
                        continue;
                    }

                    store(i, PlayerRecorder.record.copy());
                    PlayerController.writeToChat("Stored Recording to QuickSlot " + i);
                }
            }

            // Check Load QuickSlot KeyBindings
            if (ALTPressed(handle)) {
                handleQuickSlotKeyPress(handle, loadCooldowns, ALTPressed);
            }

            // Load Recording from QuickSlot
            for (int i = 0; i < ALTPressed.length; i++) {
                if (ALTPressed[i]) {
                    Recording r = load(i);

                    if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
                        PlayerController.writeToChat("Cannot load Recording while Recording or Replaying");
                        continue;
                    }

                    if (r == null || r.isEmpty()) {
                        PlayerController.writeToChat("No Recording in QuickSlot " + i);
                        continue;
                    }
                    PlayerRecorder.record = r;
                    PlayerController.writeToChat("Loaded Recording to QuickSlot " + i);
                }
            }

            // Reset Keys pressed
            Arrays.fill(ALTPressed, false);
            Arrays.fill(CTRLPressed, false);

            // Restore selected slot
            if (client.player.getInventory().selectedSlot != selectedSlotBackup) {
                changeSelectedSlot.add(() -> {
                    PlayerAutomaClient.LOGGER.info("Restoring selected slot to " + selectedSlotBackup);
                    client.player.getInventory().selectedSlot = selectedSlotBackup;
                });
            }

        });
    }

    private static boolean CTRLPressed(long handle) {
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean ALTPressed(long handle) {
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
