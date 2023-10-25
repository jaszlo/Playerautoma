package net.jasper.mod.util.keybinds;

import net.jasper.mod.PlayerAutomaClient;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;

public class KeyBind {

    private static final Logger LOGGER = PlayerAutomaClient.LOGGER;

    protected String name;
    protected KeyBinding bind;
    protected Runnable callback;
    public KeyBind(String name, KeyBinding bind, Runnable callback) {
        this.name = name;
        this.bind = bind;
        this.callback = callback;
    }

    public void execute() {
        if (this.callback == null || !(this.callback instanceof Runnable)) {
            LOGGER.info("Tried to run callback for Keybinding " + this.name + " but is not of instance runnable!");
            return;
        }
        if (this.bind.wasPressed()) {
            this.callback.run();
        }
    }
}
