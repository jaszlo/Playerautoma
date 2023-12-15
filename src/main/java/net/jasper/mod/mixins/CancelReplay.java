package net.jasper.mod.mixins;

import net.jasper.mod.automation.InputRecorder;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.jasper.mod.util.keybinds.Constants.CANCEL_REPLAY;

/**
 * Checks if the cancel replay keybinding is pressed when for screen key pressed
 */
@Mixin(Screen.class)
public class CancelReplay {
    @Inject(method="keyPressed", at=@At("HEAD"))
    private void injected(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (CANCEL_REPLAY.matchesKey(keyCode, scanCode)) {
            InputRecorder.stopReplay();
        }
    }
}
