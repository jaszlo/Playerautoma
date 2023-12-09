package net.jasper.mod.util.data;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec2f;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Recording implements Serializable {

    public final List<List<Boolean>> keysPressed = new ArrayList<>();
    public final List<List<Float>> lookingDirections = new ArrayList<>();
    public final List<Integer> slotSelections = new ArrayList<>();
    public final List<Optional<SlotClick>> slotClicked = new ArrayList<>();
    public final List<Screen> currentScreen = new ArrayList<>();


    public int size = 0;
    public boolean isEmpty = true;

    public Recording() {}

    public void clear() {
        keysPressed.clear();
        lookingDirections.clear();
        slotSelections.clear();
        slotClicked.clear();
        currentScreen.clear();
        size = 0;
        isEmpty = true;
    }

    public void add(List<Boolean> currentKeyMap, Vec2f lookingDir, int selectedSlot, Optional<SlotClick> lastSlotClicked, Screen currentScreen) {
        this.keysPressed.add(currentKeyMap);
        this.lookingDirections.add(List.of(lookingDir.x, lookingDir.y));
        this.slotSelections.add(selectedSlot);
        this.slotClicked.add(lastSlotClicked);
        this.currentScreen.add(currentScreen);
        isEmpty = false;
        size++;
    }
}
