package net.jasper.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.jasper.mod.automation.InventoryAutomation;
import net.jasper.mod.automation.InputRecorder;
import net.jasper.mod.util.keybinds.PlayerAutomaKeyBinds;
import net.jasper.mod.util.data.TaskQueue;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class PlayerAutomaClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(PlayerAutoma.MOD_ID + "::client");
	public static final String RECORDING_FOLDER_NAME = "Recordings";
	public static final String RECORDING_PATH = Path.of(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), RECORDING_FOLDER_NAME).toString();


	// Will execute one task per tick
	public static final TaskQueue tasks = new TaskQueue();
	public static final TaskQueue inventoryTasks = new TaskQueue();

	@Override
	public void onInitializeClient() {
		// Create folder for recordings if not exists
		File recordingFolder = new File(RECORDING_PATH);
		if (!recordingFolder.exists()) {
			boolean failed = !recordingFolder.mkdir();
			// Do not initialize mod if failed to create folder (should not happen)
			if (failed) return;
		}

		// Initialize New Keybinds
		PlayerAutomaKeyBinds.initialize();

		// Register Inventory Automations
		InventoryAutomation.registerInventoryAutomation();

		// Register Player Recorder
		InputRecorder.registerInputRecorder();



		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			// Run Inventory tasks separately
			if (!inventoryTasks.isEmpty()) {
				inventoryTasks.poll().run();
			}

			// Run one of the assigned tasks in this tick only if inventory task finished
			if (!tasks.isEmpty() && inventoryTasks.isEmpty()) {
				tasks.poll().run();
			}
			PlayerAutomaKeyBinds.handleKeyPresses();
		});
	}
}