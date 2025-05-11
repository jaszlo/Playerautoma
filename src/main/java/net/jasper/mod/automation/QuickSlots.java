package net.jasper.mod.automation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerautomaClient;
import net.jasper.mod.gui.option.PlayerautomaOptionsScreen;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.IOHelpers;
import net.jasper.mod.util.data.Recording;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.Arrays;

import static net.jasper.mod.PlayerautomaClient.PLAYERAUTOMA_QUICKSLOT_PATH;

/**
 * QuickSlots for storing and loading Recordings for the PlayerRecorder
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuickSlots {

    public static final int SLOTS_AMOUNT = 9;

    public static final Recording[] SLOTS = new Recording[SLOTS_AMOUNT];
    public static final String[] SLOT_FILE_NAMES = {
            "quickslot_1.rec",
            "quickslot_2.rec",
            "quickslot_3.rec",
            "quickslot_4.rec",
            "quickslot_5.rec",
            "quickslot_6.rec",
            "quickslot_7.rec",
            "quickslot_8.rec",
            "quickslot_9.rec"
    };

    // KeyBinding State
    private static final int[] storeCooldowns = new int[SLOTS_AMOUNT];
    private static final int[] loadCooldowns = new int[SLOTS_AMOUNT];
    private static final boolean[] CTRLPressed = new boolean[SLOTS_AMOUNT];
    private static final boolean[] ALTPressed = new boolean[SLOTS_AMOUNT];
    private static final int COOLDOWN = 5;

    private static void store(int slot, Recording recording) {
        if (slot < 0 || slot >= SLOTS_AMOUNT) {
            return;
        }

        SLOTS[slot] = recording;
        NativeImageBackedTexture texture = recording.thumbnail != null ? new NativeImageBackedTexture(() -> SLOT_FILE_NAMES[slot], recording.thumbnail.toNativeImage()) : null;
        updateQuickSlotTexture(slot, texture);
        // I assume that this doesn't fail, and therefore I don't check a return value i created to check this fails ...
        IOHelpers.storeRecordingFile(SLOTS[slot], new File(PLAYERAUTOMA_QUICKSLOT_PATH), SLOT_FILE_NAMES[slot], IOHelpers.RecordingFileTypes.REC, true);
    }

    private static Recording load(int slot) {
        return SLOTS[slot];
    }

    public static final Identifier[] THUMBNAIL_IDENTIFIER;


    // Initialize State
    static {
        THUMBNAIL_IDENTIFIER = new Identifier[] {
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_1"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_2"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_3"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_4"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_5"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_6"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_7"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_8"),
                Identifier.of(PlayerautomaClient.MOD_ID, "quickslot_9"),
        };
        Arrays.fill(SLOTS, new Recording(null));
        Arrays.fill(ALTPressed, false);
        Arrays.fill(CTRLPressed, false);
        Arrays.fill(storeCooldowns, 0);
        Arrays.fill(loadCooldowns, 0);
    }

    public static void clearQuickSlot() {
        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.clearedAllQuickSlot"));
        for (int i = 0; i < SLOTS_AMOUNT; i++) {
            SLOTS[i].clear();
            // Clear file and thumbnail texture
            store(i, SLOTS[i]);
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.clearedAllQuickSlot"));
        }
    }

    public static void clearQuickSlot(int slot) {
        if (slot >= 0 && slot <= SLOTS_AMOUNT) {
            SLOTS[slot].clear();
            // Clear file and thumbnail texture
            store(slot, SLOTS[slot]);
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.clearedOneQuickSlot").append(" " + (slot + 1)));
        }
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

    @SuppressWarnings("SameParameterValue")
    private static void consumeKeyPress(KeyBinding key, int limit) {
        int error = 0;
        while (key.wasPressed()) {
            key.setPressed(false);
            if (error++ > limit) {
                PlayerautomaClient.LOGGER.warn("Could not unset keybinding for QuickSlot");
                break;
            }
        }
    }

    public static void updateQuickSlotTexture(int slot, NativeImageBackedTexture texture) {
        if (slot < 0 || slot >= SLOTS_AMOUNT) {
            return;
        }

        // Destroy old texture, register new one if present
        MinecraftClient.getInstance().getTextureManager().destroyTexture(THUMBNAIL_IDENTIFIER[slot]);
        if (texture != null) {
            MinecraftClient.getInstance().getTextureManager().registerTexture(THUMBNAIL_IDENTIFIER[slot], texture);
        }
    }

    public static void loadRecording(int slot) {
        if (slot < 0 || slot >= SLOTS_AMOUNT) {
            return;
        }

        Recording r = load(slot);
        // Check if load operation can be done
        if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotLoadDueToState"));
            return;
        } else if (r == null || r.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.loadQuickslotMissing").append(Text.of("" + (slot + 1))));
            return;
        }

        PlayerRecorder.recording = r;
        // Destroy old texture, register new one if present
        MinecraftClient.getInstance().getTextureManager().destroyTexture(PlayerRecorder.THUMBNAIL_TEXTURE_IDENTIFIER);
        if (r.thumbnail != null) {
            PlayerRecorder.thumbnailTexture = new NativeImageBackedTexture(() -> SLOT_FILE_NAMES[slot], r.thumbnail.toNativeImage());
            MinecraftClient.getInstance().getTextureManager().registerTexture(PlayerRecorder.THUMBNAIL_TEXTURE_IDENTIFIER, PlayerRecorder.thumbnailTexture);
        }

        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.loadQuickslot").append(Text.of("" + (slot + 1))));
    }

    public static void storeRecording(int slot) {
        if (slot < 0 || slot >= SLOTS_AMOUNT) {
            return;
        }

        // Check if store operation can be done
        if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStoreDueToState"));
            return;
        } else if (PlayerRecorder.recording == null || PlayerRecorder.recording.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStoreEmpty"));
            return;
        }

        // Store quickslots to file to be persistent in store
        store(slot, PlayerRecorder.recording.copy());
        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.storeQuickslot").append(Text.of("" + (slot  + 1))));
    }

    @SuppressWarnings("java:S3776")
    public static void register() {
        // Load persistent quickslots.
        for (int i = 0; i < SLOTS_AMOUNT; i++) {
            Recording r = IOHelpers.loadRecordingFile(new File(PLAYERAUTOMA_QUICKSLOT_PATH), new File(SLOT_FILE_NAMES[i]));
            SLOTS[i] = r;
            if (r.thumbnail != null) {
                int finalI = i;
                MinecraftClient.getInstance().getTextureManager().registerTexture(THUMBNAIL_IDENTIFIER[i], new NativeImageBackedTexture(() -> SLOT_FILE_NAMES[finalI], r.thumbnail.toNativeImage()));
            }
        }

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            long handle = client.getWindow().getHandle();

            // Check Store QuickSlot KeyBindings
            if (Boolean.TRUE.equals(PlayerautomaOptionsScreen.useCTRLForQuickSlots.getValue()) && CTRLPressed(handle)) {
                handleQuickSlotKeyPress(handle, storeCooldowns, CTRLPressed);
            }

            // Check Load QuickSlot KeyBindings
            if (Boolean.TRUE.equals(PlayerautomaOptionsScreen.useALTForQuickSlots.getValue()) && ALTPressed(handle)) {
                handleQuickSlotKeyPress(handle, loadCooldowns, ALTPressed);
            }

            for (int i = 0; i < CTRLPressed.length; i++) {
                // Store Recording to QuickSlot
                if (CTRLPressed[i]) {
                    // Unset key to not change selectedSlot
                    if (Boolean.TRUE.equals(PlayerautomaOptionsScreen.preventSlotChanges.getValue())) {
                        consumeKeyPress(client.options.hotbarKeys[i], 10);
                    }
                    storeRecording(i);

                // Load Recording from QuickSlot
                } else if (ALTPressed[i]) {
                    // Unset key to not change selectedSlot
                    if (Boolean.TRUE.equals(PlayerautomaOptionsScreen.preventSlotChanges.getValue())) {
                        consumeKeyPress(client.options.hotbarKeys[i], 10);
                    }
                    loadRecording(i);
                }
            }

            // Reset Keys pressed
            Arrays.fill(ALTPressed, false);
            Arrays.fill(CTRLPressed, false);
        });
    }

    private static boolean CTRLPressed(long handle) { // NOSONAR
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean ALTPressed(long handle) { // NOSONAR
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
