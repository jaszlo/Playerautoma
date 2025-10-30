package net.jasper.mod.util.data;

import net.minecraft.screen.slot.SlotActionType;

import java.io.Serializable;

/**
 * Small Data-Class to store slot clicks
 */
public record SlotClick(int slotId, int button, SlotActionType actionType) implements Serializable {
    @Override
    public String toString() {
        return "SlotClick{" +
                "slotId=" + slotId +
                ", button=" + button +
                ", actionType=" + actionType +
                '}';
    }
}
