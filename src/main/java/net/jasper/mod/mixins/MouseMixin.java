package net.jasper.mod.mixins;

import net.jasper.mod.automation.MenuPrevention;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *  Class implementing menu prevention functionality
 */
@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(method="updateMouse", at=@At("HEAD"), cancellable=true)
    private void injected(CallbackInfo ci) {
        if (MenuPrevention.preventToBackground) {
            ci.cancel();
        }
    }
}
