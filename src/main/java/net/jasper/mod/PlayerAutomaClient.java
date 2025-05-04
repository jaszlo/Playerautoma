package net.jasper.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.jasper.mod.automation.*;
import net.jasper.mod.gui.PlayerAutomaHUD;
import net.jasper.mod.gui.RecordingSelectorScreen;
import net.jasper.mod.util.keybinds.PlayerAutomaKeyBinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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

	public static String getVersion() {
		return FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
	}


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
		LOGGER.info("Playerautoma {} is starting...", getVersion());
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
		WorldRenderEvents.END.register(context -> {
			var client = MinecraftClient.getInstance();
			var matrices = context.matrixStack();
			var camera = client.getCameraEntity();
			if (client.world == null || client.player == null || matrices == null || camera == null) return;

			matrices.push();
			PlayerEntityRenderer renderer = (PlayerEntityRenderer) client.getEntityRenderDispatcher().getRenderer(client.player);
			var renderState = renderer.createRenderState();
			renderState.x = 1000;
			// Rotation play around
			double degree = 45;
			float cosDegree = (float) Math.cos(degree);
			float sinDegree = (float) Math.sin(degree);
			// X Rotation
			Matrix4f rotationMatrixX = new Matrix4f(
					new Vector4f(1f, 0f, 0f, 0f),
					new Vector4f(0f, cosDegree, sinDegree, 0f),
					new Vector4f(0f, -sinDegree, cosDegree, 0f),
					new Vector4f(0f, 0f, 0f, 1f)
			);

			// Y Rotation
			Matrix4f rotationMatrixY = new Matrix4f(
					new Vector4f(cosDegree, 0f, -sinDegree, 0f),
					new Vector4f(0f, 1, 0, 0f),
					new Vector4f(sinDegree, 0, cosDegree, 0f),
					new Vector4f(0f, 0f, 0f, 1f)
			);

			// Z Rotation
			Matrix4f rotationMatrixZ = new Matrix4f(
					new Vector4f(cosDegree, -sinDegree, 0, 0f),
					new Vector4f(sinDegree, cosDegree, 0, 0f),
					new Vector4f(0, 0, 1, 0f),
					new Vector4f(0f, 0f, 0f, 1f)
			);

			//matrices.multiplyPositionMatrix(rotationMatrixX);
			//matrices.multiplyPositionMatrix(rotationMatrixY);
			//matrices.multiplyPositionMatrix(rotationMatrixZ);
			System.out.println(client.player.getX() + ", " + camera.getX() + " => " + (client.player.getX() == camera.getX()));
			System.out.println(client.player.getY() + ", " + camera.getY() + " => " + (client.player.getY() == camera.getY()));
			System.out.println(client.player.getZ() + ", " + camera.getZ() + " => " + (client.player.getZ() == camera.getZ()));

			matrices.translate(100.0 - camera.getX(), 100.0 - camera.getY(), 100.0 - camera.getZ());
			renderState.x = 100.0;
			renderState.y = 100.0;
			renderState.z = 100.0;
			renderState.bodyYaw = 45;
			renderState.pitch = 45;
			renderer.render(renderState, context.matrixStack(), context.consumers(), 150);
			matrices.pop();
		});
	}
}