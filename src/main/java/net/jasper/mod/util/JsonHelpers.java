package net.jasper.mod.util;

import com.google.gson.*;
import net.jasper.mod.util.data.LookingDirection;
import net.jasper.mod.util.data.Recording;
import net.jasper.mod.util.data.RecordingThumbnail;
import net.jasper.mod.util.data.SlotClick;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to help de/serialize recordings from .json files.
 */
public class JsonHelpers {

    // Field names
    private static final String LENGTH = "length";
    private static final String KEYS_PRESSED = "keysPressed";
    private static final String TIMES_PRESSED = "timesPressed";
    private static final String MODIFIERS = "modifiers";
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
    private static final String COMMAND = "command";
    private static final String THUMBNAIL = "thumbnail";
    private static final String THUMBNAIL_COLORS = "colors";
    private static final String THUMBNAIL_WIDTH = "width";
    private static final String THUMBNAIL_HEIGHT = "height";
    private static final String VILLAGER_TRADE = "villagerTrade";


    public static String serialize(Recording r) {
        JsonObject result = new JsonObject();
        result.addProperty(LENGTH, r.entries.size());

        // Add Thumbnail
        JsonObject jsonThumbnail = new JsonObject();
        jsonThumbnail.addProperty(THUMBNAIL_WIDTH, r.thumbnail.width());
        jsonThumbnail.addProperty(THUMBNAIL_HEIGHT, r.thumbnail.height());
        JsonArray colors = new JsonArray();
        r.thumbnail.colors().forEach(colors::add);
        jsonThumbnail.add(THUMBNAIL_COLORS, colors);

        result.add(THUMBNAIL, jsonThumbnail);

        JsonArray entries = new JsonArray();
        for (Recording.RecordEntry entry : r.entries) {
            JsonObject jsonEntry = new JsonObject();
            // Fill the entry with the actual values
            {
                // Keys pressed
                JsonArray keysPressed = new JsonArray();
                for (String pressed : entry.keysPressed()) {
                    keysPressed.add(pressed);
                }
                jsonEntry.add(KEYS_PRESSED, keysPressed);

                // Keys pressed count
                JsonObject timesPressed = new JsonObject();
                Map<String, Integer> timesPressedMap = entry.timesPressed();
                for (String translationKey : timesPressedMap.keySet()) {
                    // Only store keys that are pressed. If not timesPressed is implicitly 0
                    int count = timesPressedMap.get(translationKey);
                    if (count == 0) {
                        continue;
                    }
                    timesPressed.addProperty(translationKey, count);
                }
                jsonEntry.add(TIMES_PRESSED, timesPressed);

                // Modifiers
                JsonArray modifiers = new JsonArray();
                for (String modifier : entry.modifiers()) {
                    modifiers.add(modifier);
                }
                jsonEntry.add(MODIFIERS, modifiers);

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
                }

                // Current Screen
                if (entry.currentScreen() != null) {
                    jsonEntry.addProperty(CURRENT_SCREEN, entry.currentScreen().getName());
                }

                // Current command
                if (entry.command() != null) {
                    jsonEntry.addProperty(COMMAND, entry.command());
                }

                // Villager trade
                if (entry.villagerTrade() != null) {
                    jsonEntry.addProperty(VILLAGER_TRADE, entry.villagerTrade());
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
        Recording result = new Recording(null);
        JsonObject parsed = JsonParser.parseString(s).getAsJsonObject();

        // Read Thumbnail and set for recording
        JsonObject jsonThumbnail = parsed.get(THUMBNAIL).getAsJsonObject();
        List<Integer> colors = new ArrayList<>();
        int width = jsonThumbnail.get(THUMBNAIL_WIDTH).getAsInt();
        int height = jsonThumbnail.get(THUMBNAIL_HEIGHT).getAsInt();
        JsonArray jsonColors = jsonThumbnail.get(THUMBNAIL_COLORS).getAsJsonArray();
        for (JsonElement color : jsonColors) {
            colors.add(color.getAsInt());
        }
        result.thumbnail = new RecordingThumbnail(colors, width, height);

        for (JsonElement jsonEntryElement : parsed.get(ENTRIES).getAsJsonArray()) {
            JsonObject jsonEntry = jsonEntryElement.getAsJsonObject();

            List<String> keysPressed = new ArrayList<>();
            Map<String, Integer> timesPressed = new HashMap<>();
            // Initialize all key presses as false and 0 times pressed
            for (KeyBinding k : client.options.allKeys) {
                timesPressed.put(k.getTranslationKey(), 0);
            }
            // Read keys pressed
            for (JsonElement jsonPressed : jsonEntry.get(KEYS_PRESSED).getAsJsonArray()) {
                keysPressed.add(jsonPressed.getAsString());
            }

            JsonObject jsonTimesPressed = jsonEntry.get(TIMES_PRESSED).getAsJsonObject();
            for (String translationKey : jsonTimesPressed.keySet()) {
                // Get "KeySet" i.e. just the on key that is the
                timesPressed.put(translationKey, jsonTimesPressed.get(translationKey).getAsInt());
            }

            JsonArray jsonModifiers = jsonEntry.get(MODIFIERS).getAsJsonArray();
            List<String> modifiers = new ArrayList<>();
            for (JsonElement modifier : jsonModifiers) {
                modifiers.add(modifier.getAsString());
            }

            JsonObject jsonLookingDirection = jsonEntry.get(LOOKING_DIRECTION).getAsJsonObject();
            LookingDirection lookingDirection = new LookingDirection(
                    jsonLookingDirection.get(YAW).getAsFloat(),
                    jsonLookingDirection.get(PITCH).getAsFloat()
            );

            int selectedSlot = jsonEntry.get(SELECTED_SLOT).getAsInt();

            SlotClick slotclick = null;
            if (jsonEntry.has(SLOT_CLICKED)) {
                JsonObject jsonSlotClick = jsonEntry.get(SLOT_CLICKED).getAsJsonObject();
                    slotclick = new SlotClick(
                            jsonSlotClick.get(SLOT_ID).getAsInt(),
                            jsonSlotClick.get(BUTTON).getAsInt(),
                            SlotActionType.valueOf(jsonSlotClick.get(ACTION_TYPE).getAsString())
                    );
            }

            Class<?> currentScreen = null;
            if (jsonEntry.has(CURRENT_SCREEN)) {
                try {
                currentScreen = Class.forName(jsonEntry.get(CURRENT_SCREEN).getAsString());
                } catch (Exception e) {
                    // Do nothing. When this happens the value is null as it should be and is not present
                    //PlayerAutomaClient.LOGGER.info(e.toString());
                }
            }

            String command = null;
            if (jsonEntry.has(COMMAND)) {
                command = jsonEntry.get(COMMAND).getAsString();
            }

            Integer villagerTrade = null;
            if (jsonEntry.has(VILLAGER_TRADE)) {
                villagerTrade = jsonEntry.get(VILLAGER_TRADE).getAsInt();
            }

            Recording.RecordEntry entry = new Recording.RecordEntry(
                keysPressed,
                timesPressed,
                modifiers,
                lookingDirection,
                selectedSlot,
                slotclick,
                currentScreen,
                command,
                villagerTrade
            );
            result.add(entry);
        }

        return result;
    }
}
