package net.jasper.mod.mixins;


import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.automation.InputRecorder;
import net.jasper.mod.util.data.SlotClick;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(HandledScreen.class)
public class SlotClickedCallbackInjection {

    @Inject(method="onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at=@At("HEAD"))
    private void injected(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        // Register the slot that was clicked if it was a valid slot
        PlayerAutomaClient.LOGGER.info(new SlotClick(slotId, button, actionType).toString());
        InputRecorder.lastSlotClicked = Optional.of(new SlotClick(slotId, button, actionType));
    }
}
