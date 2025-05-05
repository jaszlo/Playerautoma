package net.jasper.mod.util;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.data.LookingDirection;
import net.jasper.mod.util.data.SlotClick;
import net.jasper.mod.util.data.StartingPositionOffset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Utility class for controlling minor aspects of the player and client
 */
public class ClientHelpers {


    public static int getGuiScale() {
        MinecraftClient client = MinecraftClient.getInstance();
        int scale = client.options.getGuiScale().getValue();
        // Scale was set to 'AUTO' which is represented by 0. Calculate actual scale
        scale = scale == 0 ? client.getWindow().getWidth() / client.getWindow().getScaledWidth() : scale;
        return scale;
    }

    public static void positionPlayer() {
        // Center Camera
        PlayerEntity player= MinecraftClient.getInstance().player;
        assert player != null;

        // Only change looking direction if set in options
        if (PlayerAutomaOptionsScreen.useDefaultDirectionOption.getValue()) {
            LookingDirection.Name dirName = PlayerAutomaOptionsScreen.setDefaultDirectionOption.getValue();
            LookingDirection dir = dirName.getYawPitch();
            player.setPitch(dir.pitch());
            player.setYaw(dir.yaw());
        }

        // Center player on current block only if enabled in options
        if (PlayerAutomaOptionsScreen.useDefaultStartingPositionOption.getValue()) {
            StartingPositionOffset offset = PlayerAutomaOptionsScreen.setDefaultStartingPositionOption.getValue().getOffset();

            Vec3d playerPos = player.getPos();
            BlockPos blockPos = new BlockPos((int) Math.floor(playerPos.x), (int) Math.floor(playerPos.y), (int) Math.floor(playerPos.z));
            player.setPosition(blockPos.getX() + offset.x(), blockPos.getY(), blockPos.getZ() + offset.z());
        }
    }


    public static void writeToActionBar(Text message, boolean tinted) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.inGameHud == null || !PlayerAutomaOptionsScreen.writeStateToActionBarOption.getValue()) {
            return;
        }

        client.inGameHud.setOverlayMessage(message, tinted);
    }

    public static void writeToActionBar(Text message) {
        writeToActionBar(message, false);
    }

    public static void clickSlot(SlotClick click) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.interactionManager != null;
        try {
            client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, click.slotId(), click.button(), click.actionType(), client.player);
        } catch(Exception e) {
            client.currentScreen = null;
            PlayerRecorder.stopReplay();
            writeToActionBar(Text.translatable("playerautoma.messages.error.clickSlotError"));
        }
    }

    public static void writeToChat(Text message) {
        assert MinecraftClient.getInstance().player != null;
        MinecraftClient.getInstance().player.sendMessage(message, false);
    }
}
