package net.jasper.mod.util.keybinds;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to initialize and handle all keybinds
 */
public class PlayerAutomaKeyBinds {

    private static final List<KeyBind> keyBinds = new ArrayList<>();
    public static void initialize() {
        keyBinds.addAll(List.of(Constants.defaultKeybinds));
    }

    public static void handleKeyPresses() {
        for (KeyBind b : keyBinds) {
            b.execute();
        }
    }
}
