package net.jasper.mod.util.keybinds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.jasper.mod.gui.QuickMenu;
import net.jasper.mod.mixins.accessors.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to initialize and handle all keybinds
 */
public class PlayerAutomaKeyBinds {

    private static final List<KeyBind> keyBinds = new ArrayList<>();
    public static void register() {
        keyBinds.addAll(List.of(Constants.defaultKeybinds));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            handleKeyPresses();
        });


        WorldRenderEvents.START.register(context -> {
        MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null || client.player == null) {
                return;
            }

            long handle = client.getWindow().getHandle();
            KeyBindingAccessor keyBindingAccessor = (KeyBindingAccessor)Constants.QUICK_MENU;

            // Handle quickMenu
            boolean menuOpenPressed = InputUtil.isKeyPressed(handle, keyBindingAccessor.getBoundKey().getCode());
            if (!(client.currentScreen instanceof QuickMenu || client.currentScreen instanceof GameMenuScreen) && menuOpenPressed && !QuickMenu.wasClosed) {
                QuickMenu.open();
            }

            if (client.currentScreen instanceof QuickMenu && !menuOpenPressed) {
                client.currentScreen.close();
            }

            // Reset flag to enable opening the menu again whenever open key not pressed
            if (!menuOpenPressed) {
                QuickMenu.wasClosed = false;
            }
        });
    }

    public static void handleKeyPresses() {
        for (KeyBind b : keyBinds) {
            b.execute();
        }
    }
}
