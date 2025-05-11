package net.jasper.mod.mixins;

import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.PlayerAutomaExceptionHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *  Class implementing menu prevention functionality
 */
@Mixin(Mouse.class)
public class MouseMixin {

    @Unique
    private double previousX = 0;

    @Unique
    private double previousY = 0;

    @Inject(method="updateMouse", at=@At("HEAD"), cancellable=true)
    private void injected(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (MenuPrevention.preventToBackground) {
            ci.cancel();
        }

        double newX = client.mouse.getX();
        double newY = client.mouse.getY();

        if (PlayerRecorder.state.isReplaying() && Boolean.TRUE.equals(PlayerAutomaOptionsScreen.stopReplayOnManualInput.getValue())) {

            if (previousX != newX || previousY != newY) {
                PlayerAutomaExceptionHandler.callSafe(PlayerRecorder::stopReplay);
                ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.replayStoppedOnManualInput"));
            }
        }

        previousX = newX;
        previousY = newY;
    }
}
