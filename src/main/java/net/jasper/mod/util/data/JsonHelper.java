package net.jasper.mod.util.data;

import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonHelper {

    // Field names
    private static final String LENGTH = "length";
    private static final String KEYS_PRESSED = "keysPressed";
    private static final String PITCH = "pitch";
    private static final String YAW = "yaw";
    private static final String LOOKING_DIRECTION = "lookingDirection";
    private static final String SELECTED_SLOT = "selectedSlot";
    private static final String SLOT_ID = "slotId";
    private static final String BUTTON = "button";
    private static final String ACTION_TYPE = "actionType";
    private static final String SLOT_CLICKED = "slotClicked";
    private static final String ENTRIES = "entries";
    private static final String CURRENT_SCREEN = "currentScreen";

    public static final Map<String, Integer> keyNameToIndex = new HashMap<>();

    static {
        int i = 0;
        for (KeyBinding b : MinecraftClient.getInstance().options.allKeys) {
            keyNameToIndex.put(b.getTranslationKey(), i++);
        }
    }


    public static String serialize(Recording r) {
        MinecraftClient client = MinecraftClient.getInstance();
        JsonObject result = new JsonObject();
        result.addProperty(LENGTH, r.entries.size());

        JsonArray entries = new JsonArray();

        for (Recording.RecordEntry entry : r.entries) {
            JsonObject jsonEntry = new JsonObject();
            // Fill the entry with the actual values
            {
                // Keys
                JsonArray keysPressed = new JsonArray();
                int i = 0;
                for (boolean pressed : entry.keysPressed()) {
                    //Only store keys that are pressed. Also store their index as it will be implicitly known that
                    if (!pressed) {
                        i++;
                        continue;
                    }
                    keysPressed.add(client.options.allKeys[i++].getTranslationKey());
                }
                jsonEntry.add(KEYS_PRESSED, keysPressed);

                // Looking Direction
                JsonObject lookingDirection = new JsonObject();
                lookingDirection.addProperty(PITCH, entry.lookingDirection().pitch());
                lookingDirection.addProperty(YAW, entry.lookingDirection().yaw());
                jsonEntry.add(LOOKING_DIRECTION, lookingDirection);

                // Selected Slot (Hotbar)
                jsonEntry.addProperty(SELECTED_SLOT, entry.slotSelection());

                // Clicked Slot (Inventory)
                if (entry.slotClicked() != null) {
                    JsonObject slotClicked = new JsonObject();
                    slotClicked.addProperty(SLOT_ID, entry.slotClicked().slotId());
                    slotClicked.addProperty(BUTTON, entry.slotClicked().button());
                    slotClicked.addProperty(ACTION_TYPE, entry.slotClicked().actionType().toString());
                    jsonEntry.add(SLOT_CLICKED, slotClicked);
                } else {
                    jsonEntry.addProperty(SLOT_CLICKED, "null");
                }

                // Current Screen
                if (entry.currentScreen() != null) {
                    jsonEntry.addProperty(CURRENT_SCREEN, entry.currentScreen().getName());
                } else {
                    jsonEntry.addProperty(CURRENT_SCREEN, "null");
                }
            }
            entries.add(jsonEntry);
        }

        result.add(ENTRIES, entries);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(result);
    }

    public static Recording deserialize(String s) {
        MinecraftClient client = MinecraftClient.getInstance();
        Recording result = new Recording();

        JsonObject parsed = JsonParser.parseString(s).getAsJsonObject();

        for (JsonElement jsonEntryElement : parsed.get(ENTRIES).getAsJsonArray()) {
            JsonObject jsonEntry = jsonEntryElement.getAsJsonObject();

            List<Boolean> keysPressed = new ArrayList<>();

            // Initialize all key presses as false
            for (int i = 0; i < client.options.allKeys.length; i++) {
                keysPressed.add(false);
            }
            for (JsonElement jsonPressed : jsonEntry.get(KEYS_PRESSED).getAsJsonArray()) {
                String keyName = jsonPressed.getAsString();
                // Not all key presses are stored. Only the ones that were pressed
                // For those stored set to pressed
                int i = keyNameToIndex.getOrDefault(keyName, -1);
                if (i >= 0) {
                    keysPressed.set(i, true);
                }
            }

            JsonObject jsonLookingDirection = jsonEntry.get(LOOKING_DIRECTION).getAsJsonObject();
            LookingDirection lookingDirection = new LookingDirection(
                    jsonLookingDirection.get(YAW).getAsFloat(),
                    jsonLookingDirection.get(PITCH).getAsFloat()
            );

            int selectedSlot = jsonEntry.get(SELECTED_SLOT).getAsInt();

            SlotClick slotclick = null;
            try {
                JsonObject jsonSlotClick = jsonEntry.get(SLOT_CLICKED).getAsJsonObject();
                    slotclick = new SlotClick(
                            jsonSlotClick.get(SLOT_ID).getAsInt(),
                            jsonSlotClick.get(BUTTON).getAsInt(),
                            SlotActionType.valueOf(jsonSlotClick.get(ACTION_TYPE).getAsString())
                    );
            } catch (Exception e) {
                // Do nothing. When this happens the value is null as it should be
                // PlayerAutomaClient.LOGGER.info(e.toString());
            }

            Class<?> currentScreen = null;
            try {
                currentScreen = Class.forName(jsonEntry.get(CURRENT_SCREEN).getAsString());
            } catch (ClassNotFoundException e) {
                // Do nothing. When this happens the value is null as it should be
                //PlayerAutomaClient.LOGGER.info(e.toString());
            }

            Recording.RecordEntry entry = new Recording.RecordEntry(
                keysPressed,
                lookingDirection,
                selectedSlot,
                slotclick,
                currentScreen
            );
            result.add(entry);
        }

        return result;
    }
}
