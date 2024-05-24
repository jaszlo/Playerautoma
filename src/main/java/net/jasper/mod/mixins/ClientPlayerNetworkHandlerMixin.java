package net.jasper.mod.mixins;

import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerNetworkHandlerMixin {

    // sendMessage in ChatScreen.java removes '/'. Prefix therefore can start without it
    @Unique String[] IGNORED_COMMANDS_PREFIX = new String[] {
            "record",
            "replay",
            "say"
    };

    @Inject(method="sendChatCommand", at=@At("HEAD"))
    private void recordCommand(String message, CallbackInfo ci) {
        if (Arrays.stream(IGNORED_COMMANDS_PREFIX).noneMatch(message::startsWith)) {
            PlayerRecorder.lastCommandUsed.add(message);
        }
    }

}
