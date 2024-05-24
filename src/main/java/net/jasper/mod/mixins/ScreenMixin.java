package net.jasper.mod.mixins;

import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.mixins.accessors.KeyBindingAccessor;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.jasper.mod.util.keybinds.Constants.STOP_REPLAY;


@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method="keyPressed", at=@At("HEAD"))
    private void toggleMenuPrevention(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        int code = ((KeyBindingAccessor) Constants.PREVENT_MENU).getBoundKey().getCode();
        if (keyCode == code) {
            MenuPrevention.toggleBackgroundPrevention();
        }
    }

    @Inject(method="render", at=@At("TAIL"))
    private void renderPlayerRecorderIcon(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Renders only if active
        MenuPrevention.renderIcon(context);
    }

    @Inject(method="keyPressed", at=@At("HEAD"))
    private void stopReplay(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (STOP_REPLAY.matchesKey(keyCode, scanCode)) {
            PlayerRecorder.stopReplay();
        }
    }


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
