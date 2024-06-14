package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.data.Recording;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * QuickSlots for storing and loading Recordings for the PlayerRecorder
 */
public class QuickSlots {

    public static final int QUICKSLOTS_N = 9;

    public static Recording[] quickSlots = new Recording[QUICKSLOTS_N];

    // KeyBinding State
    private static final int[] storeCooldowns = new int[QUICKSLOTS_N];
    private static final int[] loadCooldowns = new int[QUICKSLOTS_N];
    private static final boolean[] CTRLPressed = new boolean[QUICKSLOTS_N];
    private static final boolean[] ALTPressed = new boolean[QUICKSLOTS_N];
    private static final int COOLDOWN = 5;

    private static void store(int slot, Recording recording) {
        if (slot < 0 || slot >= QUICKSLOTS_N) {
            return;
        }

        updateQuickSlotTexture(slot, new NativeImageBackedTexture(recording.thumbnail.toNativeImage()));
        quickSlots[slot] = recording;
    }

    private static Recording load(int slot) {
        return quickSlots[slot];
    }

    public static final Identifier[] THUMBNAIL_IDENTIFIER;


    // Initialize State
    static {
        THUMBNAIL_IDENTIFIER = new Identifier[] {
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_1"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_2"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_3"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_4"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_5"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_6"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_7"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_8"),
                new Identifier(PlayerAutomaClient.MOD_ID, "quick_slot_9"),
        };
        Arrays.fill(quickSlots, new Recording(null));
        Arrays.fill(ALTPressed, false);
        Arrays.fill(CTRLPressed, false);
        Arrays.fill(storeCooldowns, 0);
        Arrays.fill(loadCooldowns, 0);
    }

    public static void clearQuickSlot() {
        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.clearedAllQuickSlot"));
        for (int i = 0; i < QUICKSLOTS_N; i++) {
            quickSlots[i].clear();
        }
    }

    public static void clearQuickSlot(int slot) {
        if (slot >= 0 && slot <= QUICKSLOTS_N) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.clearedOneQuickSlot").append(" " + (slot + 1)));
            quickSlots[slot].clear();
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
                PlayerAutomaClient.LOGGER.warn("Could not unset keybinding for QuickSlot");
                break;
            }
        }
    }

    public static void updateQuickSlotTexture(int slot, NativeImageBackedTexture texture) {
        if (slot < 0 || slot >= QUICKSLOTS_N) {
            return;
        }

        // Destroy old texture, register new one
        MinecraftClient.getInstance().getTextureManager().destroyTexture(THUMBNAIL_IDENTIFIER[slot]);
        MinecraftClient.getInstance().getTextureManager().registerTexture(THUMBNAIL_IDENTIFIER[slot], texture);
    }

    public static void loadRecording(int slot) {
        if (slot < 0 || slot >= QUICKSLOTS_N) {
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

        PlayerRecorder.record = r;
        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.loadQuickslot").append(Text.of("" + (slot + 1))));
    }

    public static void storeRecording(int slot) {
        if (slot < 0 || slot >= QUICKSLOTS_N) {
            return;
        }

        // Check if store operation can be done
        if (PlayerRecorder.state != PlayerRecorder.State.IDLE) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStoreDueToState"));
            return;
        } else if (PlayerRecorder.record == null || PlayerRecorder.record.isEmpty()) {
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.cannotStoreEmpty"));
            return;
        }

        store(slot, PlayerRecorder.record.copy());
        ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.storeQuickslot").append(Text.of("" + (slot  + 1))));
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }

            long handle = client.getWindow().getHandle();

            // Check Store QuickSlot KeyBindings
            if (PlayerAutomaOptionsScreen.useCTRLForQuickSlots.getValue() && CTRLPressed(handle)) {
                handleQuickSlotKeyPress(handle, storeCooldowns, CTRLPressed);
            }

            // Check Load QuickSlot KeyBindings
            if (PlayerAutomaOptionsScreen.useALTForQuickSlots.getValue() && ALTPressed(handle)) {
                handleQuickSlotKeyPress(handle, loadCooldowns, ALTPressed);
            }

            for (int i = 0; i < CTRLPressed.length; i++) {
                // Store Recording to QuickSlot
                if (CTRLPressed[i]) {
                    // Unset key to not change selectedSlot
                    if (PlayerAutomaOptionsScreen.preventSlotChanges.getValue()) {
                        consumeKeyPress(client.options.hotbarKeys[i], 10);
                    }
                    storeRecording(i);

                // Load Recording from QuickSlot
                } else if (ALTPressed[i]) {
                    // Unset key to not change selectedSlot
                    if (PlayerAutomaOptionsScreen.preventSlotChanges.getValue()) {
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

    private static boolean CTRLPressed(long handle) {
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean ALTPressed(long handle) {
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
