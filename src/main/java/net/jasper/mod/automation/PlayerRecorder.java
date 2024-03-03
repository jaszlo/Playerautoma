package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.gui.RecordingStorer;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.data.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import static net.jasper.mod.automation.PlayerRecorder.State.*;

/**
 * Class records player input and allows to replay those
 */
public class PlayerRecorder {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    public static Recording record = new Recording();

    public static State state = IDLE;

    // Will execute one task per tick
    public static final TaskQueue tasks = new TaskQueue(TaskQueue.MEDIUM_PRIORITY);

    // Gets set in mixin SlotClickedCallback. Should only every contain one item. Else user made more than 50 clicks per second ???
    // Is used to handle asynchronous nature of slot clicks
    public static Queue<SlotClick> lastSlotClicked = new ConcurrentLinkedDeque<>();

    public static void registerInputRecorder() {
        // Register Task-Queues
        tasks.register("playerActions");
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                // Player not in-game. Therefore, reset recorder by stopping any activity
                stopRecord();
                stopReplay();
            }

            if (!state.isRecording()) {
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
        if (!state.isAny(IDLE, PAUSED)) {
            return;
        }
        ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.startRecording"));
        clearRecord();
        ClientHelpers.centerPlayer();
        lastSlotClicked.clear();
        state = RECORDING;
    }

    public static void stopRecord() {
        if (state.isRecording()) {
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.stopRecording"));
            state = IDLE;
        }
    }

    public static void clearRecord() {
        record.clear();
    }

    public static void startReplay(boolean looped) {
        if (record.isEmpty()) {
            return;
        }
        if (state.isAny(RECORDING, REPLAYING)) {
            // if state is replaying and has no tasks its looped therefore just continue and if not return
            if (!(state.isReplaying() && tasks.isEmpty())) {
                return;
            }
        }
        // If starting while paused tasks needs to be cleared and resumed
        tasks.clear();
        tasks.resume();
        state = REPLAYING;
        if (!looped) ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.startReplay"));

        ClientHelpers.centerPlayer();
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.interactionManager != null;

        // Check relative/absolute replay and no defaultDirection
        boolean isRelative = !PlayerAutomaOptionsScreen.useDefaultDirectionOption.getValue()
                                && PlayerAutomaOptionsScreen.useRelativeLookingDirectionOption.getValue();

        // Get first RecordEntry Looking direction to calculate difference
        LookingDirection l = record.entries.get(0).lookingDirection();
        float pitchDiff = isRelative ? l.pitch() - client.player.getPitch() : 0;
        float yawDiff = isRelative ? l.yaw() - client.player.getYaw() : 0;

        for (Recording.RecordEntry entry : record.entries) {
            // Get all data for current record tick (i) to replay
            List<Boolean> keyMap = entry.keysPressed();
            LookingDirection currentLookingDirection = entry.lookingDirection();
            int selectedSlot = entry.slotSelection();
            SlotClick clickedSlot = entry.slotClicked();
            Class<?> currentScreen = entry.currentScreen();

            // Replay Ticks
            tasks.add(() -> {

                // Update looking direction
                client.player.setPitch(currentLookingDirection.pitch() - pitchDiff);
                client.player.setYaw(currentLookingDirection.yaw() - yawDiff);

                // Update selected inventory slot
                client.player.getInventory().selectedSlot = selectedSlot;

                // Update keys pressed
                int j = 0;
                for (KeyBinding k : client.options.allKeys) {
                    k.setPressed(keyMap.get(j++));
                }

                // Always attack in replay if something is in the way
                if (client.options.attackKey.isPressed()) {
                    if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                        EntityHitResult entityHitResult = (EntityHitResult) client.crosshairTarget;
                        client.interactionManager.attackEntity(client.player, entityHitResult.getEntity());
                    } else {
                        client.player.swingHand(Hand.MAIN_HAND);
                    }

                    // If in creative break block if possible
                    if (client.player.isCreative() && client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHitResult = (BlockHitResult) client.crosshairTarget;
                        client.interactionManager.attackBlock(blockHitResult.getBlockPos(), blockHitResult.getSide());
                    }
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
                if (clickedSlot != null && PlayerAutomaOptionsScreen.recordInventoryActivitiesOption.getValue()) ClientHelpers.clickSlot(clickedSlot);
            });
        }

        if (!looped) {
            // Finish Replay if not looping
            tasks.add(() -> {
                state = IDLE;
                for (KeyBinding k : client.options.allKeys) {
                    k.setPressed(false);
                }
                ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.replayDone"));
            });
        }

        if (looped) {
            // Replay Again (Looping)
            tasks.add(() -> startReplay(true));
        }
    }

    public static void startLoop() {
        if (state.isAny(RECORDING, REPLAYING) || record.isEmpty()) {
            return;
        }
        ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.startLoopedReplay"));
        startReplay(true);
    }

    public static void togglePauseReplay() {
        if (!state.isAny(REPLAYING, PAUSED) || record.isEmpty()) {
            return;
        }

        if (state.isPaused()) {
            tasks.resume();
            state = REPLAYING;
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.resumeReplay"));
        } else if (state.isReplaying()) {
            tasks.pause();
            state = PAUSED;
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.pauseReplay"));
            // Toggle of all keys to stop player from doing anything
            for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
                k.setPressed(false);
            }
        }
    }

    public static void stopReplay() {
        // Only stop replay if replaying or looping
        if (!state.isAny(REPLAYING, PAUSED)) {
            return;
        }
        ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.stopReplay"));
        state = IDLE;
        // Clear all tasks to stop replay
        tasks.clear();
        InventoryAutomation.inventoryTasks.clear();

        // Toggle of all keys to stop player from doing anything
        for (KeyBinding k : MinecraftClient.getInstance().options.allKeys) {
            k.setPressed(false);
        }
    }

    public static void storeRecord(String name) {
        if (record.isEmpty()) {
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.cannotStoreEmpty"));
            return;
        } else if (state.isAny(RECORDING, REPLAYING)) {
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.cannotStoreDueToState"));
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
            // Store as .json/.rec according to option
            if (RecordingStorer.useJSON.getValue()) {
                String json = JsonHelper.serialize(record);
                FileWriter fileWriter = new FileWriter(selected);
                BufferedWriter writer = new BufferedWriter(fileWriter);
                writer.write(json);
                writer.close();
                fileWriter.close();
            } else {
                objectOutputStream.writeObject(record);
            }
            objectOutputStream.close();
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.storedRecording"));

        } catch(IOException e) {
            e.printStackTrace();
            try {
                if (objectOutputStream != null) objectOutputStream.close();
                LOGGER.info("Deletion of failed file: " + selected.delete());
            } catch (IOException closeFailed) {
                closeFailed.printStackTrace();
                LOGGER.warn("Error closing file in error handling!"); // This should not happen :(
            }
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.storeFailed"));
            LOGGER.info("Failed to create output stream for selected file");
        }
    }


    public static void loadRecord(File selected) {
        if (state.isAny(RECORDING, REPLAYING)) {
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.cannotLoadDueToState"));
            return;
        }

        // Load as .json/.rec according to option
        if (RecordingStorer.useJSON.getValue()) {
            try {
                FileReader fileReader = new FileReader(selected);
                BufferedReader reader = new BufferedReader(fileReader);
                StringBuilder readFile = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    readFile.append(line);
                }
                record = JsonHelper.deserialize(readFile.toString());
                ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.loadedRecording"));
            } catch(Exception e) {
                ClientHelpers.writeToChat(Text.translatable("playerautoma.message.loadFailed"));
            }
            return;
        }

        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(selected));
            // This can happen when a file is selected and then deleted via the file explorer
            if (objectInputStream == null) throw new IOException("objectInputStream is null");

            record = (Recording) objectInputStream.readObject();
            objectInputStream.close();
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.loadedRecording"));
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (objectInputStream != null) objectInputStream.close();
            } catch (IOException closeFailed) {
                closeFailed.printStackTrace();
                LOGGER.warn("Error closing file in error handling!"); // This should not happen :(
            }
            ClientHelpers.writeToChat(Text.translatable("playerautoma.message.loadFailed"));
        }
    }

    public enum State {
        RECORDING,
        REPLAYING,
        IDLE,
        PAUSED;

        private static final Identifier REPLAYING_ICON = new Identifier(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/replaying.png");
        private static final Identifier RECORDING_ICON = new Identifier(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/recording.png");
        private static final Identifier PAUSING_ICON = new Identifier(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/pausing.png");
        private static final Identifier IDLE_ICON = new Identifier(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/idle.png");


        public int getColor() {
            return switch (this) {
                case RECORDING -> 0xff0000; // Red
                case REPLAYING -> 0x0f7302; // Green
                case PAUSED -> 0x000669;    // Blue
                default -> 0xFFFFFF;        // White
            };
        }

        public Identifier getIcon() {
            return switch (PlayerRecorder.state) {
                case IDLE -> IDLE_ICON;
                case PAUSED -> PAUSING_ICON;
                case RECORDING -> RECORDING_ICON;
                case REPLAYING -> REPLAYING_ICON;
            };
        }

        public Text getText() {
            return switch (this) {
                case RECORDING -> Text.translatable("playerautoma.state.recording");
                case REPLAYING -> Text.translatable("playerautoma.state.replaying");
                case PAUSED -> Text.translatable("playerautoma.state.paused");
                default -> Text.translatable("playerautoma.state.idle");
            };
        }
        public boolean isPaused() {
            return this == PAUSED;
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