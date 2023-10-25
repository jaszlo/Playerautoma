package net.jasper.mod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerAutoma implements ModInitializer {

    public static final String MOD_ID = "playerautoma";
    public static final String MOD_VERSION =  "v1.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Mod initialized");
    }
}
