package net.jasper.mod.mixins;

import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.automation.MenuPrevention;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Class implementing menu prevention functionality
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method="setScreen", at=@At("HEAD"), cancellable=true)
    private void injected(Screen screen, CallbackInfo ci) {
        if (MenuPrevention.preventToBackground) {
            MinecraftClient client = MinecraftClient.getInstance();
            // When closing a window enable cursor again and if trying to open menu cancel callback
            if (screen == null) {
                InputUtil.setCursorParameters(client.getWindow().getHandle(), GLFW.GLFW_CURSOR_NORMAL, client.mouse.getX(), client.mouse.getY());
            } else if (screen instanceof GameMenuScreen) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "<init>", at=@At("TAIL"))
    private void injected(RunArgs args, CallbackInfo ci) {
        PlayerAutomaClient.initializeAfterClient();
    }
}
