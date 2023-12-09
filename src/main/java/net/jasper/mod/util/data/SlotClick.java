package net.jasper.mod.util.data;

import net.minecraft.screen.slot.SlotActionType;

public class SlotClick {

    public int slotId;
    public int button;
    public SlotActionType actionType;

    public SlotClick(int slotId, int button, SlotActionType actionType) {
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
    }

    @Override
    public String toString() {
        return "SlotClick{" +
                "slotId=" + slotId +
                ", button=" + button +
                ", actionType=" + actionType +
                '}';
    }
}
