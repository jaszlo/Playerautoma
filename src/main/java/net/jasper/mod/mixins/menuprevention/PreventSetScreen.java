package net.jasper.mod.mixins.menuprevention;

import net.jasper.mod.automation.MenuPrevention;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class PreventSetScreen {
    @Inject(method="setScreen", at=@At("HEAD"), cancellable=true)
    private void injected(Screen screen, CallbackInfo ci) {
        if (MenuPrevention.preventToBackground && screen instanceof GameMenuScreen) {
            ci.cancel();
        }
    }
}
