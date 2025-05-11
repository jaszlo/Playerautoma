package net.jasper.mod.util.keybinds;

import net.jasper.mod.PlayerAutomaClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.slf4j.Logger;

/**
 * Custom-Keybinding class that stores a name, a keybinding and a callback all in one
 */
public class KeyBind {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    protected String name;
    protected KeyBinding bind;
    protected Runnable callback;
    public KeyBind(String translationKey, KeyBinding bind, Runnable callback) {
        this.name = Text.translatable(translationKey).toString();
        this.bind = bind;
        this.callback = callback;
    }

    public void execute() {
        if (this.callback == null) {
            LOGGER.info("Tried to run callback for Keybinding {} but is not of instance runnable!", this.name);
            return;
        }
        if (this.bind.wasPressed()) {
            this.callback.run();
        }
    }
}
