package net.jasper.mod.automation;

import net.minecraft.util.math.Vec2f;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recording implements Serializable {

    protected final List<List<Boolean>> keysPressed = new ArrayList<>();
    protected final List<List<Float>> lookingDirections = new ArrayList<>();
    protected final List<Integer> slotSelections = new ArrayList<>();
    protected boolean isEmpty = true;

    public Recording() {}

    public void clear() {
        keysPressed.clear();
        lookingDirections.clear();
        slotSelections.clear();
        isEmpty = true;
    }

    public void add(List<Boolean> currentKeyMap, Vec2f lookingDir, int selectedSlot) {
        this.keysPressed.add(currentKeyMap);
        this.lookingDirections.add(List.of(lookingDir.x, lookingDir.y));
        this.slotSelections.add(selectedSlot);
        isEmpty = false;
    }
}
