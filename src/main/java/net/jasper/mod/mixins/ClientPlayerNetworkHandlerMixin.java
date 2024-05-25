package net.jasper.mod.mixins;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.CommandsToExcludeOption;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Class that track all commands that were send to the server
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayerNetworkHandlerMixin {

    // sendMessage in ChatScreen.java removes '/'. Prefix therefore can start without it
    @Unique String[] userCommandsForbidden = new String[] {
            "record",
            "replay",
            "say",
            "msg",
            "message",
            "alert",
            "email",
            "reply",
            "r "
    };

    @Inject(method="sendChatCommand", at=@At("HEAD"))
    private void recordCommand(String message, CallbackInfo ci) {
        // If not enabled never track commands
        if (!PlayerAutomaOptionsScreen.recordCommands.getValue()) {
            return;
        }

        // When '/' is there remove it if not just check if it starts with current command to ignore
        boolean noMatchWithUserIgnoreList = CommandsToExcludeOption.userCommandsToIgnore.stream().noneMatch(e -> e.startsWith("/") && message.startsWith(e.substring(1)) || message.startsWith(e));
        boolean noMatchWithForbiddenList = Arrays.stream(userCommandsForbidden).noneMatch(message::startsWith);
        if (noMatchWithUserIgnoreList && noMatchWithForbiddenList) {
            PlayerRecorder.lastCommandUsed.add(message);
        }
    }
}
