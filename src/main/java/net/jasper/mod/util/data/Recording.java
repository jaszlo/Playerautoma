package net.jasper.mod.util.data;

import net.minecraft.client.option.KeyBinding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main-Class for storing all data required for a recording
 */
public class Recording implements Serializable {
    public record RecordEntry(
            List<Boolean> keysPressed,
            Map<String, Integer> timesPressed,
            LookingDirection lookingDirection,
            int slotSelection,
            SlotClick slotClicked,
            Class<?> currentScreen
    ) implements Serializable {}

    public final List<RecordEntry> entries = new ArrayList<>();

    public String toString() {
       return "Recording[" + this.entries.size() + "]";
    }

    public void clear() {
        entries.clear();
    }

    public void add(RecordEntry entry) {
        entries.add(entry);
    }
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public Recording copy() {
        Recording copy = new Recording();
        copy.entries.addAll(this.entries);
        return copy;
    }
}
