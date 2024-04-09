package net.jasper.mod.mixins.menuprevention;

import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.mixins.KeyBindingAccessor;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Screen.class)
public class ScreenInjection {

    @Inject(method="keyPressed", at=@At("HEAD"))
    private void injected(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        int code = ((KeyBindingAccessor) Constants.PREVENT_MENU).getBoundKey().getCode();
        if (keyCode == code) {
            MenuPrevention.toggleBackgroundPrevention();
        }
    }

    @Inject(method="render", at=@At("TAIL"))
    private void injected(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Renders only if active
        MenuPrevention.renderIcon(context);
    }
}
