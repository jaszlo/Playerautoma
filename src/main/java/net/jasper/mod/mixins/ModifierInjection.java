package net.jasper.mod.mixins;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class ModifierInjection {


    @Inject(method="hasControlDown", at=@At("HEAD"), cancellable=true)
    private static void injectedCTRL(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerRecorder.state.isReplaying() && PlayerRecorder.pressedModifiers.contains(Constants.CTRL)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method="hasShiftDown", at=@At("HEAD"), cancellable=true)
    private static void injectedSHIFT(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerRecorder.state.isReplaying() && PlayerRecorder.pressedModifiers.contains(Constants.SHIFT)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method="hasControlDown", at=@At("HEAD"), cancellable=true)
    private static void injectedALT(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerRecorder.state.isReplaying() && PlayerRecorder.pressedModifiers.contains(Constants.ALT)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
