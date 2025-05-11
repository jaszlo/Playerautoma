package net.jasper.mod.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.util.math.ColorHelper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColorHelpers {

    public static int getRgbWithAlpha(int color, int alpha) {
        return ColorHelper.getArgb(alpha, ColorHelper.getRed(color), ColorHelper.getGreen(color), ColorHelper.getBlue(color));
    }
}
