package net.jasper.mod.mixins;

import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.mixins.accessors.KeyBindingAccessor;
import net.jasper.mod.util.keybinds.Constants;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method="onKey", at=@At("HEAD"), cancellable=true)
    private void injected(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        int code = ((KeyBindingAccessor) Constants.PREVENT_MENU).getBoundKey().getCode();
        if (MenuPrevention.preventToBackground && key != code) {
            ci.cancel();
        }
    }

}
