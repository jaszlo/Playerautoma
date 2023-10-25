package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.gui.FileChooser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec2f;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InputRecorder {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    private static Recording record = new Recording();

    private static boolean isRecording = false;
    private static boolean isReplaying = false;

    public static boolean looping = false;

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
            record.add(currentKeyMap, new Vec2f(pitch, yaw), client.player.getInventory().selectedSlot);
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

        int index = 0;
        for (List<Boolean> keyMap : record.keysPressed) {
            List<Float> lookDir = record.lookingDirections.get(index);
            Vec2f currentLookingDirection = new Vec2f(lookDir.get(0), lookDir.get(1));
            int selectedSlot = record.slotSelections.get(index);
            index++;
            PlayerAutomaClient.tasks.add("Apply KeyMap", () -> {
                // Update looking direction
                float pitch = currentLookingDirection.x; float yaw = currentLookingDirection.y;
                client.player.setPitch(pitch); client.player.setYaw(yaw);

                // Update inventory slot
                client.player.getInventory().selectedSlot = selectedSlot;

                // Update keys pressed
                int i = 0;
                for (KeyBinding k : client.options.allKeys) {
                    k.setPressed(keyMap.get(i++));
                }
            });
        }
        PlayerAutomaClient.tasks.add("Finish Replay", () -> {
            isReplaying = false;PlayerController.writeToChat("Replay Done!");
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

    public static void storeRecord() {
        LOGGER.info("storing recording");
        new Thread(() -> {
            String selected = FileChooser.getPath(FileChooser.Operation.STORE);

            try {
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(selected));
                objectOutputStream.writeObject(record);
            } catch(IOException e) {
                e.printStackTrace();
                LOGGER.info("Failed to create ouputstream for selected file");
            }
            PlayerController.writeToChat("Stored Recording");
        }).start();

    }


    public static void loadRecord() {
        LOGGER.info("loading recording");
        new Thread(() -> {
            String selected = FileChooser.getPath(FileChooser.Operation.LOAD);

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(selected));
                record = (Recording) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.info("Could not load record");
                PlayerController.writeToChat("Invalid file");

            }
            PlayerController.writeToChat("Loaded Recording");
        }).start();

    }

}
