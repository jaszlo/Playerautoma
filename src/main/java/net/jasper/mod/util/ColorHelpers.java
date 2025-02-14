package net.jasper.mod.util;

import net.minecraft.util.math.ColorHelper;

public class ColorHelpers {

    public static int getRgbWithAlpha(int color, int alpha) {
        return ColorHelper.getArgb(alpha, ColorHelper.getRed(color), ColorHelper.getGreen(color), ColorHelper.getBlue(color));
    }
}
