package net.jasper.mod.mixins;

import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public class MerchantScreenMixin {

    @Shadow
    private int selectedIndex;

    @Inject(method="syncRecipeIndex", at=@At("HEAD"))
    public void recordSyncRecipeIndex(CallbackInfo ci) {
        PlayerRecorder.lastVillagerTradeMade.add(this.selectedIndex);
    }
}
