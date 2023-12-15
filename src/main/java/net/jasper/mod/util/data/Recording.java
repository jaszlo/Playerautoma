package net.jasper.mod.util.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Main-Class for storing all data required for a recording
 */
public class Recording implements Serializable {
    public record RecordEntry(
            List<Boolean> keysPressed,
            LookingDirection lookingDirection,
            int slotSelection,
            SlotClick slotClicked, Class<?> currentScreen
    ) implements Serializable {}

    public final List<RecordEntry> entries = new ArrayList<>();

    public Recording() {}

    public void clear() {
        entries.clear();
    }

    public void add(RecordEntry entry) {
        entries.add(entry);
    }
    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
