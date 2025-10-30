package net.jasper.mod.util.data;

import net.minecraft.client.texture.NativeImage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public record RecordingThumbnail(List<Integer> colors, int width, int height) implements Serializable {

    public static RecordingThumbnail createFromNativeImage(NativeImage image) {
        List<Integer> colors = new ArrayList<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                colors.add(image.getColorArgb(x, y));
            }
        }
        return new RecordingThumbnail(colors, image.getWidth(), image.getHeight());
    }

    public NativeImage toNativeImage() {
        NativeImage result = new NativeImage(width, height, false);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x + y * width;
                result.setColorArgb(x, y, colors.get(index));
            }
        }
        return result;
    }


}
