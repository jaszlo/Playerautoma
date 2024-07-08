package net.jasper.mod.mixins.accessors;

import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantScreen.class)
public interface MerchantScreenAccessor {

    @Accessor("selectedIndex")
    void setSelectedIndex(int index);

    @Invoker("syncRecipeIndex")
    void setSyncRecipeIndexInvoker();
}
