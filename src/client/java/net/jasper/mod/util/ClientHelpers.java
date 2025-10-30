package net.jasper.mod.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerautomaOptionsScreen;
import net.jasper.mod.util.data.LookingDirection;
import net.jasper.mod.util.data.SlotClick;
import net.jasper.mod.util.data.StartingPositionOffset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

/**
 * Utility class for controlling minor aspects of the player and client
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClientHelpers {


    public static int getGuiScale() {
        MinecraftClient client = MinecraftClient.getInstance();
        int scale = client.options.getGuiScale().getValue();
        // Scale was set to 'AUTO' which is represented by 0. Calculate actual scale
        scale = scale == 0 ? client.getWindow().getWidth() / client.getWindow().getScaledWidth() : scale;
        return scale;
    }


    /**
     * Asserts that the player entity is not null and returns the current one
     * @return current PlayerEntity
     */
    public static PlayerEntity getPlayerEntity() {
        var player = MinecraftClient.getInstance().player;
        assert player != null : "MinecraftClient.player was null";
        return player;
    }

    /**
     * Asserts that the interactionManager is not null and returns the current one
     * @return current ClientPlayerInteractionManager
     */
    public static ClientPlayerInteractionManager getInteractionManager() {
        var interactionManager = MinecraftClient.getInstance().interactionManager;
        assert interactionManager != null : "MinecraftClient.interactionManager was null";
        return interactionManager;
    }

    public static void positionPlayer() {
        PlayerEntity player = getPlayerEntity();

        // Only change looking direction if set in options
        if (Boolean.TRUE.equals(PlayerautomaOptionsScreen.useDefaultDirectionOption.getValue())) {
            LookingDirection.Name dirName = PlayerautomaOptionsScreen.setDefaultDirectionOption.getValue();
            // If the looking direction is to be used from the replay nothing needs to be set here
            if (!dirName.equals(LookingDirection.Name.FROM_REPLAY)) {
                LookingDirection dir = dirName.getYawPitch();
                player.setPitch(dir.pitch());
                player.setYaw(dir.yaw());
            }

        }

        // Center player on current block only if enabled in options
        if (Boolean.TRUE.equals(PlayerautomaOptionsScreen.useDefaultStartingPositionOption.getValue())) {
            StartingPositionOffset offset = PlayerautomaOptionsScreen.setDefaultStartingPositionOption.getValue().getOffset();

            Vec3d playerPos = player.getEntityPos();
            BlockPos blockPos = new BlockPos((int) Math.floor(playerPos.x), (int) Math.floor(playerPos.y), (int) Math.floor(playerPos.z));
            player.setPosition(blockPos.getX() + offset.x(), blockPos.getY(), blockPos.getZ() + offset.z());
        }
    }


    public static void writeToActionBar(Text message, boolean tinted) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.inGameHud == null || Boolean.FALSE.equals(PlayerautomaOptionsScreen.writeStateToActionBarOption.getValue())) {
            return;
        }

        client.inGameHud.setOverlayMessage(message, tinted);
    }

    public static void writeToActionBar(Text message) {
        writeToActionBar(message, false);
    }

    public static void clickSlot(SlotClick click) {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            getInteractionManager().clickSlot(getPlayerEntity().currentScreenHandler.syncId, click.slotId(), click.button(), click.actionType(), getPlayerEntity());
        } catch(Exception e) {
            client.currentScreen = null;
            PlayerRecorder.stopReplay();
            writeToActionBar(Text.translatable("playerautoma.messages.error.clickSlotError"));
        }
    }

    public static void writeToChat(Text message) {
        getPlayerEntity().sendMessage(message, false);
    }

    private Optional<Window> getCurrentWindow() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(client.getWindow());
    }

    public static boolean isCtrlPressed() {
        Optional<Window> windowOpt = new ClientHelpers().getCurrentWindow();
        return windowOpt.map(window ->
                InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_LEFT_CONTROL) ||
                InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_RIGHT_CONTROL)
        ).orElse(false);
    }

    public static boolean isShiftPressed() {
        Optional<Window> windowOpt = new ClientHelpers().getCurrentWindow();
        return windowOpt.map(window ->
                InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_LEFT_SHIFT) ||
                InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_RIGHT_SHIFT)
        ).orElse(false);
    }

    public static boolean isAltPressed() {
        Optional<Window> windowOpt = new ClientHelpers().getCurrentWindow();
        return windowOpt.map(window ->
                InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_LEFT_ALT) ||
                InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_RIGHT_ALT)
        ).orElse(false);
    }
}
