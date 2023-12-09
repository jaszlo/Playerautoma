package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.data.Recording;
import net.jasper.mod.util.data.SlotClick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec2f;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InputRecorder {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    private static Recording record = new Recording();

    private static boolean isRecording = false;
    private static boolean isReplaying = false;
    public static boolean looping = false;

    public static boolean hadScreenOpen = false;

    public static Optional<SlotClick> lastSlotClicked = Optional.empty();

    public static void registerInputRecorder() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Check if recording is possible
            if (!isRecording || client.player == null) {
                return;
            }
            // Get current KeyMap (Map of which keys to pressed state)
            List<Boolean> currentKeyMap = new ArrayList<>();
            for (KeyBinding k : client.options.allKeys) {
                currentKeyMap.add(k.isPressed());
            }

            // Get Player looking direction
            float pitch = client.player.getPitch();
            float yaw = client.player.getYaw();

            // Add recorded data to record
            record.add(currentKeyMap, new Vec2f(pitch, yaw), client.player.getInventory().selectedSlot, lastSlotClicked, client.currentScreen);

            // Clear last slot clicked to prevent double clicks
            lastSlotClicked = Optional.empty();

            // Check if a screen was open
            hadScreenOpen = client.currentScreen != null;
        });
    }

    public static void startRecord() {
        if (isRecording || isReplaying) {
            return;
        }
        LOGGER.info("startRecord");
        PlayerController.writeToChat("Started Recording");
        clearRecord();
        PlayerController.centerPlayer();
        isRecording = true;
    }

    public static void stopRecord() {
        if (isRecording) {
            LOGGER.info("stopRecord");
            PlayerController.writeToChat("Stopped Recording");
            isRecording = false;
        }
    }

    public static void clearRecord() {
        LOGGER.info("clearRecord");
        record.clear();
    }

    public static void startReplay() {
        if (isRecording || isReplaying || record.isEmpty) {
            return;
        }
        isReplaying = true;
        LOGGER.info("startReplay");
        PlayerController.writeToChat("Started Replay");

        PlayerController.centerPlayer();
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null : "client.player was found to be null in InputRecorder.replay()";

        for (int i = 0; i < record.size; i++) {

            // Get all data for current record tick
            List<Boolean> keyMap = record.keysPressed.get(i);
            List<Float> lookDir = record.lookingDirections.get(i);
            Vec2f currentLookingDirection = new Vec2f(lookDir.get(0), lookDir.get(1));
            int selectedSlot = record.slotSelections.get(i);
            Optional<SlotClick> clickedSlot = record.slotClicked.get(i);
            Screen currentScreen = record.currentScreen.get(i);

            // Add task to task queue (where one task is executed per tick)
            PlayerAutomaClient.tasks.add("Apply KeyMap", () -> {

                // Update looking direction
                float pitch = currentLookingDirection.x; float yaw = currentLookingDirection.y;
                client.player.setPitch(pitch); client.player.setYaw(yaw);

                // Update inventory slot
                client.player.getInventory().selectedSlot = selectedSlot;

                // Update keys pressed
                int j = 0;
                for (KeyBinding k : client.options.allKeys) {
                    k.setPressed(keyMap.get(j++));
                }

                // Set current screen
                client.setScreen(currentScreen);

                // Click Slot in inventory
                if (client.currentScreen == null && clickedSlot.isPresent()) {
                    LOGGER.warn("Clicking slot while no screen is open");
                }
                clickedSlot.ifPresent(PlayerController::clickSlot);
            });

        }


        PlayerAutomaClient.tasks.add("Finish Replay", () -> {
            isReplaying = false;
            PlayerController.writeToChat("Replay Done!");
        });

        if (looping) {
            PlayerAutomaClient.tasks.add("Replay again of looping", InputRecorder::startReplay);
        }
    }

    public static void startLoop() {
        if (isRecording || isReplaying || record.isEmpty) {
            return;
        }

        LOGGER.info("startLoop");
        PlayerController.writeToChat("Started Looped Replay");

        looping = true;
        // isReplaying = true will be set in startReplay
        startReplay();
    }

    public static void stopReplay() {
        if (!isReplaying) {
            return;
        }
        LOGGER.info("stopReplay");
        PlayerController.writeToChat("Stopped Replay");

        isReplaying = false;
        looping = false;

        PlayerAutomaClient.tasks.clear();
        PlayerAutomaClient.inventoryTasks.clear();

        // Toggle of all keys to stop player from doing anything
        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            k.setPressed(false);
        }
    }

    public static void storeRecord(String name) {
        LOGGER.info("storing recording");
        new Thread(() -> {
            try {
                // If file already exists create new file with "_new" before file type.
                File selected = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/recordings/" + name);
                String newName = name.substring(0, name.length() - 4) + "_new.rec";
                if (selected.exists()) {
                    selected = new File(MinecraftClient.getInstance().runDirectory.getAbsolutePath() + "/recordings/" + newName);
                }

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(selected));
                objectOutputStream.writeObject(record);
                PlayerController.writeToChat("Stored Recording");
            } catch(IOException e) {
                PlayerController.writeToChat("Failed to store recording");
                LOGGER.info("Failed to create output stream for selected file");
                LOGGER.info(e.getMessage());
            }
        }).start();

    }


    public static void loadRecord(File selected) {
        LOGGER.info("loading recording");
        new Thread(() -> {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(selected));
                record = (Recording) objectInputStream.readObject();
                objectInputStream.close();
                PlayerController.writeToChat("Loaded Recording");
            } catch (Exception e) {
                LOGGER.info("Could not load record");
                LOGGER.info(e.getMessage());
                PlayerController.writeToChat("Invalid file");
            }
        }).start();
    }
}
