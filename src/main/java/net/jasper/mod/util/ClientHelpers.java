package net.jasper.mod.util;

import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.data.LookingDirection;
import net.jasper.mod.util.data.SlotClick;
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

    public static void centerPlayer() {
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

        // Center player on current block
        Vec3d playerPos = player.getPos();
        BlockPos blockPos = new BlockPos((int) Math.floor(playerPos.x), (int) Math.floor(playerPos.y) - 1, (int) Math.floor(playerPos.z));
        player.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5);
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
        client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, click.slotId(), click.button(), click.actionType(), client.player);
    }
}
