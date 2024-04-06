package net.jasper.mod.automation;

import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.ClientHelpers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class MenuPrevention {

    public static boolean preventToBackground = false;
    private static boolean registered = false;

    public static void register() {
        registered = true;
    }

    public static void toggleBackgroundPrevention() {
        if (!registered) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
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
