package net.jasper.mod.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.data.RecordingThumbnail;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.math.ColorHelper;

import java.util.function.BiConsumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThumbnailHelpers {

    public static final int WIDTH = 64;
    public static final int HEIGHT = 64;

    public static NativeImage scaleDownImage(NativeImage originalImage, int newWidth, int newHeight) {
        // Step 1: Check if the image needs scaling down
            if (originalImage.getWidth() <= newWidth || originalImage.getHeight() <= newHeight) {
            return originalImage;
        }

        // Step 2: Create a new NativeImage with the specified dimensions
        NativeImage scaledImage = new NativeImage(newWidth, newHeight, false);

        // Step 3: Calculate sliding window sizes
        int slidingWindowWidth = originalImage.getWidth() / newWidth;
        int slidingWindowHeight = originalImage.getHeight() / newHeight;

        for (int x = 0; x < newWidth; x++) {
            for (int y = 0; y < newHeight; y++) {
                int avgColor = getAverageColor(originalImage, x, y, slidingWindowWidth, slidingWindowHeight, false);
                scaledImage.setColorArgb(x, y, avgColor);
            }
        }

        // Step 6: Return the resized image
        RecordingThumbnail thumbnail = RecordingThumbnail.createFromNativeImage(scaledImage);
        return thumbnail.toNativeImage();
    }

    private static int getAverageColor(NativeImage originalImage, int scaledImageX, int scaledImageY, int slidingWindowWidth, int slidingWindowHeight, boolean smooth) {
        int totalAlpha = 0;
        int totalRed = 0;
     int totalGreen = 0;
     int totalBlue = 0;
     int count = 0;

        int startX = scaledImageX * slidingWindowWidth;
        int startY = scaledImageY * slidingWindowHeight;

        // Prevent overflow of window via math.min
        int endX = Math.min(startX + scaledImageX + slidingWindowWidth, originalImage.getWidth());
        int endY = Math.min(startY + scaledImageY + slidingWindowHeight, originalImage.getHeight());

        // Just select the color of the startX, startY window
        if (!smooth) {
            return originalImage.getColorArgb(startX, startY);
        }

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                int color = originalImage.getColorArgb(x, y);
                totalAlpha += ColorHelper.getAlpha(color);
                totalRed   += ColorHelper.getRed(color);
                totalGreen += ColorHelper.getGreen(color);
                totalBlue  += ColorHelper.getBlue(color);
                count++;
            }
        }
        count = count == 0 ? 1 : count;
        int a = Math.min(totalAlpha / count, 255);
        int r = Math.min(totalRed   / count, 255);
        int g = Math.min(totalGreen / count, 255);
        int b = Math.min(totalBlue  / count, 255);

        return  ColorHelper.getArgb(a, r, g, b);
    }


    /**
     * Mostly copied from MinecraftClient.takePanorama
     */
    public static void create(BiConsumer<RecordingThumbnail, NativeImage> thumbnailConsumer) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        Framebuffer framebuffer = client.getFramebuffer();

        try {
            client.gameRenderer.setBlockOutlineEnabled(false);
            client.gameRenderer.setRenderingPanorama(true);
            client.gameRenderer.renderWorld(RenderTickCounter.ONE);
            try {
                Thread.sleep(10L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                // Ignore
            }

            ScreenshotRecorder.takeScreenshot(framebuffer, nativeImage -> {
                RecordingThumbnail thumbnail = RecordingThumbnail.createFromNativeImage(scaleDownImage(nativeImage, WIDTH, HEIGHT));
                thumbnailConsumer.accept(thumbnail, nativeImage);
            });
        } catch (Exception exception) {
            PlayerAutomaExceptionHandler.handleException(exception);
            PlayerAutomaClient.LOGGER.error("Couldn't save temporary screenshot image", exception);
        } finally {
            client.gameRenderer.setBlockOutlineEnabled(true);
            client.gameRenderer.setRenderingPanorama(false);
        }
    }

}
