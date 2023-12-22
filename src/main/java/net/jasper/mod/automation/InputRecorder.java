package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.PlayerController;
import net.jasper.mod.util.data.LookingDirection;
import net.jasper.mod.util.data.Recording;
import net.jasper.mod.util.data.SlotClick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static net.jasper.mod.automation.InputRecorder.State.*;

/**
 * Class records player input and allows to replay those
 */
public class InputRecorder {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    private static Recording record = new Recording();

    public static State state = IDLE;

    // Gets set in mixin SlotClickedCallback. Should only every contain one item. Else user made more than 50 clicks per second ???
    // Is used to handle asynchronous nature of slot clicks
    public static Queue<SlotClick> lastSlotClicked = new ConcurrentLinkedDeque<>();

    public static void registerInputRecorder() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if recording is possible
            if (!state.isRecording() || client.player == null) {
                return;
            }

            // Create current KeyMap (Map of which keys to pressed state)
            List<Boolean> currentKeyMap = new ArrayList<>();
            for (KeyBinding k : client.options.allKeys) {
                currentKeyMap.add(k.isPressed());
            }

            // Create a new RecordEntry and add it to the record
            Recording.RecordEntry newEntry = new Recording.RecordEntry(
                    currentKeyMap,
                    new LookingDirection(client.player.getYaw(), client.player.getPitch()),
                    client.player.getInventory().selectedSlot,
                    lastSlotClicked.poll(),
                    client.currentScreen == null ? null : client.currentScreen.getClass()
            );
            record.add(newEntry);
        });
    }

    public static void startRecord() {
        if (state.isAny(RECORDING, REPLAYING)) {
            return;
        }
        PlayerController.writeToChat("Started Recording");
        clearRecord();
        PlayerController.centerPlayer();
        lastSlotClicked.clear();
        state = RECORDING;
    }

    public static void stopRecord() {
        if (state.isRecording()) {
            PlayerController.writeToChat("Stopped Recording");
            state = IDLE;
        }
    }

    public static void clearRecord() {
        record.clear();
    }

    public static void startReplay() {
        if (state.isAny(RECORDING, REPLAYING) || record.isEmpty()) {
            return;
        }
        state = REPLAYING;
        PlayerController.writeToChat("Started Replay");

        PlayerController.centerPlayer();
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        for (Recording.RecordEntry entry : record.entries) {
            // Get all data for current record tick (i) to replay
            List<Boolean> keyMap = entry.keysPressed();
            LookingDirection currentLookingDirection = entry.lookingDirection();
            int selectedSlot = entry.slotSelection();
            SlotClick clickedSlot = entry.slotClicked();
            Class<?> currentScreen = entry.currentScreen();

            // Replay Ticks
            PlayerAutomaClient.tasks.add(() -> {

                // Update looking direction
                client.player.setPitch(currentLookingDirection.pitch());
                client.player.setYaw(currentLookingDirection.yaw());

                // Update selected inventory slot
                client.player.getInventory().selectedSlot = selectedSlot;

                // Update keys pressed
                int j = 0;
                for (KeyBinding k : client.options.allKeys) {
                    k.setPressed(keyMap.get(j++));
                }


                // Close screen if needed
                if (client.currentScreen != null && currentScreen == null) {
                    client.currentScreen.close();
                    client.setScreen(null);
                }

                // Inventory is not opened via Keybinding therefore open manually if needed
                if (client.currentScreen == null && currentScreen == InventoryScreen.class) {
                    client.setScreen(new InventoryScreen(client.player));
                }

                // Click Slot in inventory if possible
                if (clickedSlot != null) PlayerController.clickSlot(clickedSlot);
            });
        }

        // Finish Replay
        PlayerAutomaClient.tasks.add(() -> {
            state = IDLE;
            PlayerController.writeToChat("Replay Done!");
        });

        if (state.isLooping()) {
            // Replay Again (Looping)
            PlayerAutomaClient.tasks.add(InputRecorder::startReplay);
        }
    }

    public static void startLoop() {
        if (state.isAny(RECORDING, REPLAYING) || record.isEmpty()) {
            return;
        }
        PlayerController.writeToChat("Started Looped Replay");
        state = LOOPING;
        startReplay();
    }

    public static void stopReplay() {
        if (!state.isReplaying()) {
            return;
        }
        PlayerController.writeToChat("Stopped Replay");

        state = IDLE;

        // Clear all tasks to stop replay
        PlayerAutomaClient.tasks.clear();
        PlayerAutomaClient.inventoryTasks.clear();

        // Toggle of all keys to stop player from doing anything
        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            k.setPressed(false);
        }
    }

    public static void storeRecord(String name) {
        if (record.isEmpty()) {
            PlayerController.writeToChat("Cannot store empty recording");
            return;
        } else if (state.isAny(RECORDING, REPLAYING)) {
            PlayerController.writeToChat("Cannot store recording while recording or replaying");
            return;
        }

        File selected = null;
        ObjectOutputStream objectOutputStream = null;
        String basePath = MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/recordings/";
        try {
            // If file already exists create new file with "_new" before file type.
            selected = new File(basePath + name);
            String newName = name.substring(0, name.length() - 4) + "_new.rec";
            if (selected.exists()) {
                selected = new File(basePath + newName);
            }
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(selected));
            if (objectOutputStream == null) throw new IOException("objectInputStream is null");
            objectOutputStream.writeObject(record);
            objectOutputStream.close();
            PlayerController.writeToChat("Stored Recording");
        } catch(IOException e) {
            e.printStackTrace();
            try {
                if (objectOutputStream != null) objectOutputStream.close();
                LOGGER.info("Deletion of failed file: " + selected.delete());
            } catch (IOException closeFailed) {
                closeFailed.printStackTrace();
                LOGGER.warn("Error closing file in error handling!"); // This should not happen :(
            }
            PlayerController.writeToChat("Failed to store recording");
            LOGGER.info("Failed to create output stream for selected file");
        }
    }


    public static void loadRecord(File selected) {
        if (state.isAny(RECORDING, REPLAYING)) {
            PlayerController.writeToChat("Cannot load recording while recording or replaying");
            return;
        }
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(selected));
            // This can happened when a file is selected and then deleted via the file explorer
            if (objectInputStream == null) throw new IOException("objectInputStream is null");
            record = (Recording) objectInputStream.readObject();
            objectInputStream.close();
            PlayerController.writeToChat("Loaded Recording");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (objectInputStream != null) objectInputStream.close();
            } catch (IOException closeFailed) {
                closeFailed.printStackTrace();
                LOGGER.warn("Error closing file in error handling!"); // This should not happen :(
            }
            PlayerController.writeToChat("Invalid file");
        }
    }

    public enum State {
        RECORDING,
        REPLAYING,
        LOOPING,
        IDLE;

        public boolean isLooping() {
            return this == LOOPING;
        }

        public boolean isRecording() {
            return this == RECORDING;
        }

        public boolean isReplaying() {
            return this == REPLAYING;
        }

        public boolean isAny(State... states) {
            for (State state : states) {
                if (this == state) {
                    return true;
                }
            }
            return false;
        }
    }
}
