package net.jasper.mod.automation;

import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.ClientHelpers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import static net.jasper.mod.util.HUDTextures.BLOCK_MENU_ICON;

/**
 * Class to track state and implement menu prevention that allows for replays to work in background
 */
public class MenuPrevention {

    public static boolean preventToBackground = false;
    private static boolean registered = false;

    public static void register() {
        registered = true;
    }

    public static void renderIcon(DrawContext context) {
        // Rendering the texture to show that menu opening is blocked anda the mouse can be used freely
        // This is always on and should never be effected by showHUDOption
        if (preventToBackground) {
            MinecraftClient client = MinecraftClient.getInstance();
            context.getMatrices().push();
            // Texture is 24x24. Scale it with guiScale
            int scaledSizeBlockMenu = 24 * ClientHelpers.getGuiScale();
            int xBlockMenu = client.getWindow().getScaledWidth() / 2 - scaledSizeBlockMenu / 2;
            int yBlockMenu = client.getWindow().getScaledHeight() / 2 - scaledSizeBlockMenu / 2;
            context.drawTexture(BLOCK_MENU_ICON, xBlockMenu, yBlockMenu, 0, 0, scaledSizeBlockMenu, scaledSizeBlockMenu, scaledSizeBlockMenu, scaledSizeBlockMenu);
            context.getMatrices().pop();
        }
    }

    public static void toggleBackgroundPrevention() {
        if (!registered) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        // Do not allow background prevention to be toggled while in screen to prevent typing to toggle feature!
        if (client.currentScreen instanceof ChatScreen) {
            return;
        }

        preventToBackground = !preventToBackground;

        if (PlayerAutomaOptionsScreen.writeStateToChatOption.getValue()) {
            ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.menuPreventionToggle").append(preventToBackground ? ScreenTexts.ON : ScreenTexts.OFF));
        }

        int mouseMode = preventToBackground ? InputUtil.GLFW_CURSOR_NORMAL : InputUtil.GLFW_CURSOR_DISABLED;
        InputUtil.setCursorParameters(client.getWindow().getHandle(), mouseMode, client.mouse.getX(), client.mouse.getY());
        // The icon is rendered accordingly in mod.gui.PlayerAutomaHUD
    }
}
