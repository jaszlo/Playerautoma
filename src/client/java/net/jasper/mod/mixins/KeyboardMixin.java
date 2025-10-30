package net.jasper.mod.mixins;

import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerautomaOptionsScreen;
import net.jasper.mod.mixins.accessors.KeyBindingAccessor;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.PlayerautomaExceptionHandler;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Class implementing menu prevention functionality
 */
@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method="onKey", at=@At("HEAD"), cancellable=true)
    private void injected(long window, int action, KeyInput input, CallbackInfo ci) {
        int key = input.getKeycode();
        int menuPreventionKeyCode = ((KeyBindingAccessor) Constants.PREVENT_MENU).getBoundKey().getCode();
        if (MenuPrevention.preventToBackground && key != menuPreventionKeyCode) {
            ci.cancel();
        }


        int startReplayKeyCode = ((KeyBindingAccessor) Constants.START_REPLAY).getBoundKey().getCode();
        int loopReplayKeyCode = ((KeyBindingAccessor) Constants.LOOP_REPLAY).getBoundKey().getCode();
        if (PlayerRecorder.state.isReplaying() &&
                key != startReplayKeyCode &&
                key != loopReplayKeyCode &&
                Boolean.TRUE.equals(PlayerautomaOptionsScreen.stopReplayOnManualInput.getValue())
        ) {
            PlayerautomaExceptionHandler.callSafe(PlayerRecorder::stopReplay);
            ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.replayStoppedOnManualInput"));
        }
    }
}
