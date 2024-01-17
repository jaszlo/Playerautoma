package net.jasper.mod.mixins;

import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

@SuppressWarnings("InvalidInjectorMethodSignature")
@Mixin(OptionsScreen.class)
public abstract class OptionsAddition {
    @Shadow protected abstract ButtonWidget createButton(Text message, Supplier<Screen> screenSupplier);
    @ModifyVariable(method="init", at=@At("STORE"), ordinal=0)
    private GridWidget.Adder injected(GridWidget.Adder adder) {
        adder.add(this.createButton(Text.translatable("playerautoma.options"), PlayerAutomaOptionsScreen::create));
        return adder;
    }
}
