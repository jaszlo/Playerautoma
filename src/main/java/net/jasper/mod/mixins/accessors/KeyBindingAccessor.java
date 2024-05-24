package net.jasper.mod.mixins.accessors;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("boundKey")
    InputUtil.Key getBoundKey();

    @Accessor("timesPressed")
    int getTimesPressed();

    @Accessor("timesPressed")
    void setTimesPressed(int count);

    @Accessor("KEYS_BY_ID")
    static Map<String, KeyBinding> getKeysByID() {
        // Failed to apply mixin for 'KEYS_BY_ID' if null is returned
        return null;
    }

}
