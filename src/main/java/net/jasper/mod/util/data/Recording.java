package net.jasper.mod.util.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main-Class for storing all data required for a recording
 */
public class Recording implements Serializable {
    public record RecordEntry(
            List<String> keysPressed,
            Map<String, Integer> timesPressed,
            List<String> modifiers,
            LookingDirection lookingDirection,
            int slotSelection,
            SlotClick slotClicked,
            Class<?> currentScreen,
            String command,
            Integer villagerTrade
    ) implements Serializable {}

    public final List<RecordEntry> entries;
    public RecordingThumbnail thumbnail;

    public Recording(RecordingThumbnail thumbnail) {
        this.entries = new ArrayList<>();
        this.thumbnail = thumbnail;
    }

    public String toString() {
       return "Recording[" + this.entries.size() + "]";
    }

    public void clear() {
        this.thumbnail = null;
        this.entries.clear();
    }

    public void add(RecordEntry entry) {
        entries.add(entry);
    }
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public Recording copy() {
        Recording copy = new Recording(this.thumbnail);
        copy.entries.addAll(this.entries);
        return copy;
    }
}
