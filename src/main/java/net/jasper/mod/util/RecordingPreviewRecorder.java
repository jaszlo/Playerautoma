package net.jasper.mod.util;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jasper.mod.PlayerAutomaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Identifier;

import java.io.File;
import java.nio.file.Path;


public class RecordingPreviewRecorder {


    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;


    private static boolean init = false;
    private static final Identifier identifier = new Identifier(PlayerAutomaClient.MOD_ID, "new_texture");
    private static NativeImage screenshot;
    private static NativeImageBackedTexture texture;

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
                scaledImage.setColor(x, y, avgColor);
            }
        }

        // Step 6: Return the resized image
        return scaledImage;
    }

private static int getAverageColor(NativeImage originalImage, int scaledImageX, int scaledImageY, int slidingWindowWidth, int slidingWindowHeight, boolean smooth) {
    int totalAlpha = 0, totalRed = 0, totalGreen = 0, totalBlue = 0, count = 0;

    int startX = scaledImageX * slidingWindowWidth;
    int startY = scaledImageY * slidingWindowHeight;

    // Prevent overflow of window via math.min
    int endX = Math.min(startX + scaledImageX + slidingWindowWidth, originalImage.getWidth());
    int endY = Math.min(startY + scaledImageY + slidingWindowHeight, originalImage.getHeight());

    // Just select the color of the startX, startY window
    if (!smooth) {
        return originalImage.getColor(startX, startY);
    }

    for (int x = startX; x < endX; x++) {
        for (int y = startY; y < endY; y++) {
            int color = originalImage.getColor(x, y);
            totalAlpha += (color >> 24) & 0xFF;
            totalRed   += (color >> 16) & 0xFF;
            totalGreen += (color >> 8)  & 0xFF;
            totalBlue  += (color >> 0)  & 0xFF;
            count++;
        }
    }

    int a = Math.min(totalAlpha / count, 255);
    int r = Math.min(totalRed   / count, 255);
    int g = Math.min(totalGreen / count, 255);
    int b = Math.min(totalBlue  / count, 255);

    return  ((a & 0xFF) << 24) |
            ((r & 0xFF) << 16) |
            ((g & 0xFF) << 8)  |
            ((b & 0xFF) << 0);
}


    /**
     * Mostly copied from MinecraftClient.takePanorama
     */
    public static void create() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        HudRenderCallback.EVENT.register(((context, tickDelta) -> {
            if (init) {
                int scaledSize = WIDTH * ClientHelpers.getGuiScale();
                context.drawTexture(identifier, 100, 100, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
            }
        }));

        SimpleFramebuffer framebuffer = new SimpleFramebuffer(width, height, true, false);
        try {
            client.gameRenderer.setBlockOutlineEnabled(false);
            client.gameRenderer.setRenderingPanorama(true);
            client.worldRenderer.reloadTransparencyPostProcessor();
            framebuffer.beginWrite(true);
            client.gameRenderer.renderWorld(1.0f, 0L);
            NativeImage original =  ScreenshotRecorder.takeScreenshot(framebuffer);
            screenshot = scaleDownImage(original, WIDTH, HEIGHT);

            original.writeTo(new File(String.valueOf(Path.of(client.runDirectory.getAbsolutePath(), "original.png"))));
            screenshot.writeTo(new File(String.valueOf(Path.of(client.runDirectory.getAbsolutePath(), "scaled.png"))));

            texture = new NativeImageBackedTexture(screenshot);
            client.getTextureManager().registerTexture(identifier, texture);
        } catch (Exception exception) {
            PlayerAutomaClient.LOGGER.error("Couldn't save temporary screenshot image", exception);
        } finally {
            client.gameRenderer.setBlockOutlineEnabled(true);
            framebuffer.delete();
            client.gameRenderer.setRenderingPanorama(false);
            client.worldRenderer.reloadTransparencyPostProcessor();
            client.getFramebuffer().beginWrite(true);
            init = true;
        }
    }

}
