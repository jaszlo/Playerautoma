package net.jasper.mod.mixins;


import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.util.data.SlotClick;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Sets the last slot clicked for the Input-Recorder to store it
 */
@Mixin(HandledScreen.class)
public abstract class SlotClickedCallback {
    @Inject(method="onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at=@At("HEAD"))
    private void injected(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        // Register slot click for InputRecorder if isRecording and not replaying
        if (PlayerRecorder.state.isRecording()) {
            PlayerRecorder.lastSlotClicked.add(new SlotClick(slotId, button, actionType));
        }
    }
}
