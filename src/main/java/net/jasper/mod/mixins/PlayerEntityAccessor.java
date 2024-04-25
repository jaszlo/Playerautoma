package net.jasper.mod.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface PlayerEntityAccessor {
    @Accessor("type")
    @Mutable
    void setType(EntityType<?> type);
}
