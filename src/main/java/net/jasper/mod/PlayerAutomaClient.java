package net.jasper.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.jasper.mod.automation.InventoryAutomation;
import net.jasper.mod.automation.InputRecorder;
import net.jasper.mod.util.keybinds.PlayerAutomaKeyBinds;
import net.jasper.mod.util.data.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerAutomaClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(PlayerAutoma.MOD_ID + "::client");

	// Will execute one task per tick
	public static final TaskQueue tasks = new TaskQueue();
	public static final TaskQueue inventoryTasks = new TaskQueue();


	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing mod client");

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