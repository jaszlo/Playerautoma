package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.gui.PlayerAutomaMenuScreen;
import net.jasper.mod.gui.QuickMenu;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.mixins.accessors.KeyBindingAccessor;
import net.jasper.mod.util.*;
import net.jasper.mod.util.data.*;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static net.jasper.mod.PlayerAutomaClient.PLAYERAUTOMA_RECORDING_PATH;
import static net.jasper.mod.automation.PlayerRecorder.State.*;

/**
 * Class records player input and allows to replay those
 */
public class PlayerRecorder {

    public static Recording record = new Recording(null);
    public static NativeImageBackedTexture thumbnailTexture = null;
    public static final Identifier THUMBNAIL_TEXTURE_IDENTIFIER = new Identifier(PlayerAutomaClient.MOD_ID, "current_recording_thumbnail");

    public static State state = IDLE;

    // Will execute one task per tick
    public static final TaskQueue tasks = new TaskQueue(TaskQueue.MEDIUM_PRIORITY);

    // Gets set in HandledScreenMixin. Should only every contain one item. Else user made more than 50 clicks per second ???
    // Is used to handle asynchronous nature of slot clicks
    public static Queue<SlotClick> lastSlotClicked = new ConcurrentLinkedDeque<>();

    // Gets set in mixin ClientPlayerNetworkHandlerMixin. Should only every contain one item. Else user made more than 50 commands per second ???
    // Is used to handle asynchronous nature of chat input
    public static final Queue<String> lastCommandUsed = new ConcurrentLinkedDeque<>();

    // Keyboard Modifier State for current tick in replay
    public static final Queue<String> pressedModifiers = new ConcurrentLinkedDeque<>();

    public static void register() {
        // Register Task-Queues
        tasks.register("playerActions");
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                // Player not in-game. Therefore, reset recorder by stopping any activity
                stopRecord();
                stopReplay();
            }

            // If not Recording or currently paused prevent actions from being recorded
            if (!state.isRecording() || state.isPausedRecording()) {
                return;
            }

            // Create current KeyMap (Map of which keys to pressed state) and a map of the amount of times they have been pressed
            List<String> pressedKeys = new ArrayList<>();
            Map<String, Integer> timesPressed = new HashMap<>();
            for (KeyBinding k : client.options.allKeys) {
                // Do not record playerautoma KeyBindings
                if (Constants.PLAYERAUTOMA_KEYBINDINGS.contains(k)) continue;

                // Recording keyPress and pressedCounter
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
        if (state.isReplaying() || state.isPausedReplaying()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStartRecordingWhileReplaying"));
            return;
        }

        if (state.isRecording()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStartRecordingWhileRecording"));
            return;
        }

        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.startRecording"));
        clearRecord();

        RecordingThumbnail screenshot = RecordingThumbnailRecorder.create();
        if (screenshot != null) {
            record.thumbnail = screenshot;
            thumbnailTexture = new NativeImageBackedTexture(screenshot.toNativeImage());
            MinecraftClient.getInstance().getTextureManager().registerTexture(THUMBNAIL_TEXTURE_IDENTIFIER, thumbnailTexture);

        }

        if (PlayerAutomaOptionsScreen.resetKeyBindingsOnRecordingOption.getValue()) {
            KeyBinding.unpressAll();
        }

        ClientHelpers.centerPlayer();
        lastSlotClicked.clear();
        lastCommandUsed.clear();
        state = RECORDING;
    }


    public static void stopRecord() {
        if (state.isRecording() || state.isPausedRecording()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.stopRecording"));
            state = IDLE;
            // Creates the thumbnail on stop - only used when stored
        } else {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStopNotStartedRecording"));
        }
    }

    public static void clearRecord() {
        record.clear();
        thumbnailTexture = null;
        MinecraftClient.getInstance().getTextureManager().destroyTexture(THUMBNAIL_TEXTURE_IDENTIFIER);
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
        if (state.isRecording() || state.isPausedRecording()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStartReplayWhileRecording"));
            return;
        }

        if (record.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.startEmptyRecording"));
            return;
        }

        if (state.isAny(RECORDING, REPLAYING)) {
            // if state is replaying and has no tasks its looped therefore just continue and if not return
            if (!(state.isReplaying() && tasks.isEmpty())) {
                ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStartReplayWhileReplaying"));
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
        if (!looped) ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.startReplay"));

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
                if (client.currentScreen != null && currentScreen == null && !((client.currentScreen instanceof QuickMenu) || (client.currentScreen instanceof PlayerAutomaMenuScreen))) {
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
                ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.replayDone"));
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
        if (state.isRecording() || state.isPausedRecording()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStartReplayWhileRecording"));
            return;
        }

        if (record.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.startEmptyRecording"));
            return;
        }

        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.startLoopedReplay"));
        startReplay(true);
    }


    public static void togglePauseReplay() {
        if (!state.isAny(REPLAYING, PAUSED_REPLAY) || record.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotTogglePauseReplayWhileInvalidState"));
            return;

        }

        if (state.isPausedReplaying()) {
            tasks.resume();
            state = REPLAYING;
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.resumeReplay"));
        } else if (state.isReplaying()) {
            tasks.pause();
            state = PAUSED_REPLAY;
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.pauseReplay"));
            // Toggle of all keys to stop player from doing anything
            KeyBinding.unpressAll();
        }
    }


    public static void togglePauseRecord() {
        if (!state.isAny(RECORDING, PAUSED_RECORDING) || record.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotTogglePauseRecordingWhileInvalidState"));
            return;
        }

        // Continue Recording
        if (state.isPausedRecording()) {
            state = RECORDING;
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.resumeRecording"));
        // Pause recording
        } else if (state.isRecording()) {
            state = PAUSED_RECORDING;
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.pauseRecording"));
            // Toggle of all keys to stop player from doing anything
            KeyBinding.unpressAll();
        }
    }


    public static void togglePause() {
        if (state.isRecording() || state.isPausedRecording()) {
            togglePauseRecord();
        } else if (state.isReplaying() || state.isPausedReplaying()) {
            togglePauseReplay();
        }
    }


    public static void stopReplay() {
        // Only stop replay if replaying or looping
        if (!state.isAny(REPLAYING, PAUSED_REPLAY)) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStopNotStartedReplay"));
            return;
        }

        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.stopReplay"));
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


    public static void storeRecord(String name) {
        if (record.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStoreEmpty"));
            return;
        } else if (state.isAny(RECORDING, REPLAYING)) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStoreDueToState"));
            return;
        }
        boolean success = IOHelpers.storeRecordingFile(record, new File(PLAYERAUTOMA_RECORDING_PATH), name);
        Text feedback = success ? Text.translatable("playerautoma.messages.storedRecording") : Text.translatable("playerautoma.messages.error.storeFailed");
        ClientHelpers.writeToActionBar(feedback);
    }


    public static void loadRecord(String selected) {
        loadRecord(new File(selected));
    }

    public static void loadRecord(File selected) {
        if (state.isAny(RECORDING, REPLAYING)) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotLoadDueToState"));
            return;
        }

        // Do not load async as we ant the result as fast as possible
        Recording r = IOHelpers.loadRecordingFile(new File(PLAYERAUTOMA_RECORDING_PATH), selected);
        if (r.isEmpty()) ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.loadFailed"));
        else record = r;
    }

    public enum State {
        RECORDING,
        REPLAYING,
        IDLE,
        PAUSED_REPLAY,
        PAUSED_RECORDING;


        public int getColor() {
            return switch (this) {
                case RECORDING, PAUSED_RECORDING -> 0xff0000;   // Red
                case REPLAYING, PAUSED_REPLAY -> 0x0f7302;      // Green
                default -> 0xFFFFFF;                            // White
            };
        }

        public Identifier getIcon() {
            return switch (PlayerRecorder.state) {
                case IDLE -> Textures.HUD.IDLE_ICON;
                case PAUSED_REPLAY -> Textures.HUD.REPLAYING_PAUSED_ICON;
                case PAUSED_RECORDING -> Textures.HUD.RECORDING_PAUSED_ICON;
                case RECORDING -> Textures.HUD.RECORDING_ICON;
                case REPLAYING -> Textures.HUD.REPLAYING_ICON;
            };
        }

        public Text getText() {
            return switch (this) {
                case RECORDING -> Text.translatable("playerautoma.state.recording");
                case REPLAYING -> Text.translatable("playerautoma.state.replaying");
                case PAUSED_REPLAY, PAUSED_RECORDING -> Text.translatable("playerautoma.state.paused");
                default -> Text.translatable("playerautoma.state.idle");
            };
        }

        public boolean isPausedReplaying() {
            return this == PAUSED_REPLAY;
        }

        public boolean isPausedRecording() {
            return this == PAUSED_RECORDING;
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