package net.jasper.mod.mixins.accessors;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.channels.WritableByteChannel;

@Mixin(NativeImage.class)
public interface NativeImageAccessor {

    @Invoker("write")
    boolean writeInvoker(WritableByteChannel channel);
}
