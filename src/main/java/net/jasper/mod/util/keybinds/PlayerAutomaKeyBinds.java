package net.jasper.mod.util.keybinds;

import net.minecraft.client.option.KeyBinding;

import java.util.ArrayList;
import java.util.List;

public class PlayerAutomaKeyBinds {


    private static final List<KeyBind> keyBinds = new ArrayList<>();
    public static void initialize() {
        keyBinds.addAll(List.of(Constants.defaultKeybinds));
    }

    public static void addKeyBinding(String name, KeyBinding bind, Runnable callback) {
        keyBinds.add(new KeyBind(name, bind, callback));
    }

    public static void addKeyBinding(KeyBind bind) {
        keyBinds.add(bind);
    }

    public static void handleKeyPresses() {
        for (KeyBind b : keyBinds) {
            b.execute();
        }
    }
}
