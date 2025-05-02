package net.jasper.mod.mixins;

import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "clickButton", at = @At("HEAD"))
    private void mouseClicked(int syncId, int buttonId, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof EnchantmentScreen) {
            PlayerRecorder.lastEnchantmentMade.add(buttonId);
        }
    }
}
