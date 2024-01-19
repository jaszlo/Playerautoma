package net.jasper.mod.util.keybinds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

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
    }

    public static void handleKeyPresses() {
        for (KeyBind b : keyBinds) {
            b.execute();
        }
    }
}
