package net.jasper.mod;

import net.fabricmc.api.ClientModInitializer;

import net.jasper.mod.automation.*;
import net.jasper.mod.gui.PlayerAutomaHUD;
import net.jasper.mod.util.keybinds.PlayerAutomaKeyBinds;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * Main Class for the Client-Side of the Mod. Registers all Keybinds and Automations
 */
public class PlayerAutomaClient implements ClientModInitializer {

	public static final String MOD_ID = "playerautoma";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID + "::client");
	public static final String RECORDING_FOLDER_NAME = "Recordings";

	public static final String RECORDING_PATH = Path.of(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), RECORDING_FOLDER_NAME).toString();
	@Override
	public void onInitializeClient() {
		// Create folder for recordings if not exists
		File recordingFolder = new File(RECORDING_PATH);
		if (!recordingFolder.exists()) {
			boolean failed = !recordingFolder.mkdir();
			// Do not initialize mod if failed to create folder (should not happen)
			if (failed)  {
				LOGGER.error("Failed to create recording folder - PlayerAutoma will not be initialized");
				return;
			}
		}


		// Initialize New Keybinds
		PlayerAutomaKeyBinds.register();

		// Register Inventory-Automations (Re-Stacking of Blocks)
		InventoryAutomation.register();

		// Register Player-Recorder (Recording & Replaying)
		PlayerRecorder.register();

		// Register HUD element for state of Player-Recorder
		PlayerAutomaHUD.register();

		// Register Quick slots for Player-Recorder, requires KeyBindings to be registered first
		QuickSlots.register();

		// Register Commands to control the Player-Recorder
		Commands.register();

		// Register MenuPrevention. That allows to run Recording with minecraft in the background.
		MenuPrevention.register();

	}
}