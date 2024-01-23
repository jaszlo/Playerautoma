package net.jasper.mod.mixins;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGameHud.class)
public interface InGameHudDimensions {
    @Accessor("scaledHeight")
    int getScaledHeight();

    @Accessor("scaledWidth")
    int getScaledWidth();
}
