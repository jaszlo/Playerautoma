package net.jasper.mod;

import net.fabricmc.api.ClientModInitializer;
import net.jasper.mod.automation.*;
import net.jasper.mod.gui.PlayerAutomaHUD;
import net.jasper.mod.gui.RecordingSelectorScreen;
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

	public static final String PLAYERAUTOMA_FOLDER_PATH = Path.of(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), MOD_ID).toString();
	public static final String PLAYERAUTOMA_RECORDING_PATH = Path.of(PLAYERAUTOMA_FOLDER_PATH, "recordings").toString();
	public static final String PLAYERAUTOMA_QUICKSLOT_PATH = Path.of(PLAYERAUTOMA_FOLDER_PATH, "quickslots").toString();

	public static final String[] REQUIRED_FOLDERS = {
			PLAYERAUTOMA_FOLDER_PATH,
			PLAYERAUTOMA_RECORDING_PATH,
			PLAYERAUTOMA_QUICKSLOT_PATH
	};


	// Will be executed in mixin after client has been fully initialized
	public static void initializeAfterClient() {
		// Loading textures required the texture manager to be created which is not the case when run in "onInitializeClient"

		// Register Quick slots for Player-Recorder, requires KeyBindings to be registered first
		QuickSlots.register();

		// Load thumbnails from stored recordings to prevent lag on first opening of load recording screen
		RecordingSelectorScreen.loadThumbnails();
	}


	@Override
	public void onInitializeClient() {

		// Create all required folders
		for (String path : REQUIRED_FOLDERS) {
			File required = new File(path);
			if (!required.exists()) {
				boolean failed = required.mkdirs();
				// Do not initialize mod if failed to create folder (should not happen)
				if (!failed) {
					LOGGER.error("Failed to create folder {}. Playerautoma will not be initialized", required.getName());
					return;
				}
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

		// Register Commands to control the Player-Recorder
		Commands.register();

		// Register MenuPrevention. That allows to run Recording with minecraft in the background.
		MenuPrevention.register();
	}
}