package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.gui.QuickMenu;
import net.jasper.mod.gui.RecordingStorerScreen;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.mixins.accessors.KeyBindingAccessor;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.JsonHelper;
import net.jasper.mod.util.Textures;
import net.jasper.mod.util.data.*;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static net.jasper.mod.PlayerAutomaClient.RECORDING_PATH;
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

    // Modifier State for current tick in replay
    public static final Queue<String> pressedModifiers = new ConcurrentLinkedDeque<>();

    public static final Queue<String> lastCommandUsed = new ConcurrentLinkedDeque<>();

    public static void register() {
        // Register Task-Queues
        tasks.register("playerActions");
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                // Player not in-game. Therefore, reset recorder by stopping any activity
                stopRecord();
                stopReplay();
            }

            if (!state.isRecording()) {
                return;
            }

            // Create current KeyMap (Map of which keys to pressed state) and a map of the amount of times they have been pressed
            List<String> pressedKeys = new ArrayList<>();
            Map<String, Integer> timesPressed = new HashMap<>();
            for (KeyBinding k : client.options.allKeys) {
                if (k.isPressed()) pressedKeys.add(k.getTranslationKey());
                int count = ((KeyBindingAccessor) k).getTimesPressed();
                if (count > 0) timesPressed.put(k.getTranslationKey(), count);
            }

            // Create List to track which modifiers have been pressed
            List<String> modifiers = new ArrayList<>();
            if (Screen.hasControlDown()) modifiers.add(Constants.CTRL);
            if (Screen.hasShiftDown()) modifiers.add(Constants.SHIFT);
            if (Screen.hasAltDown()) modifiers.add(Constants.ALT);

            // Create a new RecordEntry and add it to the record
            Recording.RecordEntry newEntry = new Recording.RecordEntry(
                pressedKeys,
                timesPressed,
                modifiers,
                new LookingDirection(client.player.getYaw(), client.player.getPitch()),
                client.player.getInventory().selectedSlot,
                lastSlotClicked.poll(),
                client.currentScreen == null ? null : client.currentScreen.getClass(),
                lastCommandUsed.poll()
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

        if (PlayerAutomaOptionsScreen.resetKeyBindingsOnRecordingOption.getValue()) {
            KeyBinding.unpressAll();
        }

        ClientHelpers.centerPlayer();
        lastSlotClicked.clear();
        lastCommandUsed.clear();
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

    /**
     * Starts a replay. Will replay once and then stop.
     */
    public static void startReplay() {
        startReplay(false, Integer.MIN_VALUE);
    }

    /**
     * Start a replay. Will loop indefinitely if told so
     * @param looped Loop indefinitely if true
     */
    public static void startReplay(boolean looped) {
        startReplay(looped, Integer.MIN_VALUE);
    }

    /**
     * Start a replay. Will loop for loopCount times
     * @param loopCount Amount of replay loops
     */
    public static void startReplay(int loopCount) {
        startReplay(true, loopCount);
    }

    /**
     * Starts a replay
     * @param looped if true loops the replay
     * @param loopCount if looped is false count has no effect.
     *              if looped is true and count is negative loop indefinitely
     *              if looped is true and count is positive loop for that amount
     */
    private static void startReplay(boolean looped, int loopCount) {
        if (record.isEmpty()) {
            return;
        }
        if (state.isAny(RECORDING, REPLAYING)) {
            // if state is replaying and has no tasks its looped therefore just continue and if not return
            if (!(state.isReplaying() && tasks.isEmpty())) {
                return;
            }
        }

        // If menu prevention is activated enable it by default if not already enabled
        if (PlayerAutomaOptionsScreen.alwaysPreventMenuOption.getValue() && !MenuPrevention.preventToBackground) {
            MenuPrevention.toggleBackgroundPrevention();
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
        LookingDirection l = record.entries.getFirst().lookingDirection();
        float pitchDiff = isRelative ? l.pitch() - client.player.getPitch() : 0;
        float yawDiff = isRelative ? l.yaw() - client.player.getYaw() : 0;

        for (Recording.RecordEntry entry : record.entries) {
            // Get all data for current record tick (i) to replay
            List<String> keysPressed = entry.keysPressed();
            Map<String, Integer> timesPressed = entry.timesPressed();
            LookingDirection currentLookingDirection = entry.lookingDirection();
            int selectedSlot = entry.slotSelection();
            SlotClick clickedSlot = entry.slotClicked();
            Class<?> currentScreen = entry.currentScreen();
            String command = entry.command();

            // Replay Ticks
            tasks.add(() -> {

                // Update looking direction
                client.player.setPitch(currentLookingDirection.pitch() - pitchDiff);
                client.player.setYaw(currentLookingDirection.yaw() - yawDiff);

                // Update selected inventory slot
                client.player.getInventory().selectedSlot = selectedSlot;

                // Update keys pressed
                KeyBinding.unpressAll();
                Map<String, KeyBinding> keysByID = KeyBindingAccessor.getKeysByID();
                assert keysByID != null : "Failed to apply Mixin for 'KEYS_TO_BINDINGS' Accessor";
                for (String translationKey : keysPressed) {
                    KeyBinding k = keysByID.get(translationKey);
                    k.setPressed(true);
                }

                // Update keys pressed count
                for (String translationKey : timesPressed.keySet()) {
                    ((KeyBindingAccessor) keysByID.get(translationKey)).setTimesPressed(timesPressed.get(translationKey));
                }

                // Toggle modifiers accordingly
                pressedModifiers.clear();
                pressedModifiers.addAll(entry.modifiers());

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

                // KeyStrokes are consumed by screens and not recorded therefore track screen
                // If there is a screen opened and the next currentScreen is null close the current one
                // Also never close the quickMenu or modmenu
                if (client.currentScreen != null && currentScreen == null && !(client.currentScreen instanceof QuickMenu)) {
                    client.currentScreen.close();
                    client.setScreen(null);
                }

                // Click Slot in inventory if possible
                if (clickedSlot != null && PlayerAutomaOptionsScreen.recordInventoryActivitiesOption.getValue()) ClientHelpers.clickSlot(clickedSlot);

                // Execute command if possible
                if (command != null) Objects.requireNonNull(client.getNetworkHandler()).sendChatCommand(command);
            });
        }

        if (!looped) {
            // Finish Replay if not looping
            tasks.add(() -> {
                state = IDLE;
                KeyBinding.unpressAll();
                ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.replayDone"));
            });
        }

        if (looped) {
            // Stop looping
            if (loopCount == 1) {
                tasks.add(PlayerRecorder::stopReplay);
            // Loop until count reaches 0
            } else if (loopCount > 1) {
                tasks.add(() -> startReplay(true, loopCount - 1));
            // Loop indefinitely
            } else /* loopCount < 0 */ {
                tasks.add(() -> startReplay(true, Integer.MIN_VALUE));
            }
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
            KeyBinding.unpressAll();
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

        // Toggle of all keys to stop player from doing anything after finishing replay
        KeyBinding.unpressAll();

        // If default menu prevention is enabled it needs to be disabled here if enabled
        if (PlayerAutomaOptionsScreen.alwaysPreventMenuOption.getValue() && MenuPrevention.preventToBackground) {
            MenuPrevention.toggleBackgroundPrevention();
        }

    }

    private static File createNewFileName(String name) {
        File selected = Path.of(RECORDING_PATH, name).toFile();
        String fileType = RecordingStorerScreen.useJSON.getValue() ? ".json" : ".rec";
        String newName = name;
        while (selected.exists()) {
            newName = newName.substring(0, newName.length() - fileType.length()) + "_new" + fileType;
            selected = Path.of(RECORDING_PATH, newName).toFile();
        }
        return selected;
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
        try {
            // If file already exists create new file with "_new" before file type.
            selected = createNewFileName(name);
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(selected));
            if (objectOutputStream == null) throw new IOException("objectInputStream is null");
            // Store as .json/.rec according to option
            if (RecordingStorerScreen.useJSON.getValue()) {
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
            LOGGER.warn(e.getMessage());
            try {
                if (objectOutputStream != null) objectOutputStream.close();
                LOGGER.info("Deletion of failed file: {}", selected.delete());
            } catch (IOException closeFailed) {
                LOGGER.warn(closeFailed.getMessage());
                LOGGER.warn("Error closing file (storeRecord) in error handling!"); // This should not happen :(
            }
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.storeFailed"));
            LOGGER.info("Failed to create output stream for selected file");
        }
    }


    public static void loadRecord(String name) {
        File selected = Path.of(RECORDING_PATH, name).toFile();
        loadRecord(selected);
    }
    public static void loadRecord(File selected) {
        if (state.isAny(RECORDING, REPLAYING)) {
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.cannotLoadDueToState"));
            return;
        }

        // Try to load file as .json and .rec which ever works use it
        // TODO: When adding more file types add an enum for this and do not brute force
        String[] options = { "json", "rec" };
        boolean success = false;
        for (String option : options) {
            if (option.equals("json")) {
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
                    // success will stay on false and message will be printed after for loop
                    continue;
                }
                success = true;
                break;
            } else if (option.equals("rec")) {
                ObjectInputStream objectInputStream = null;
                try {
                    objectInputStream = new ObjectInputStream(new FileInputStream(selected));
                    // This can happen when a file is selected and then deleted via the file explorer
                    if (objectInputStream == null) throw new IOException("objectInputStream is null");

                    record = (Recording) objectInputStream.readObject();
                    objectInputStream.close();
                    ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.loadedRecording"));
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                    try {
                        if (objectInputStream != null) objectInputStream.close();
                    } catch (IOException closeFailed) {
                        LOGGER.warn(closeFailed.getMessage());
                        LOGGER.warn("Error closing file (loadRecord) in error handling!"); // This should not happen :(
                    }
                    continue;

                }
                success = true;
                break;
            }
        }
        if (!success) ClientHelpers.writeToChat(Text.translatable("playerautoma.message.loadFailed"));

    }

    public enum State {
        RECORDING,
        REPLAYING,
        IDLE,
        PAUSED;


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
                case IDLE -> Textures.HUD.IDLE_ICON;
                case PAUSED -> Textures.HUD.PAUSING_ICON;
                case RECORDING -> Textures.HUD.RECORDING_ICON;
                case REPLAYING -> Textures.HUD.REPLAYING_ICON;
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