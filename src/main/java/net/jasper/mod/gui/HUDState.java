package net.jasper.mod.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.mixins.InGameHudDimensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;

/**
 * Little HUD for Playerautoma to display current state of player recorder
 */
public class HUDState {

    public enum Position {
        TOP_LEFT,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;
        @Override
        public String toString() {
            return switch (this) {
                case TOP_LEFT -> "top_left";
                case TOP_RIGHT -> "top_right";
                case CENTER_LEFT -> "center_left";
                case CENTER_RIGHT -> "center_right";
                case BOTTOM_LEFT -> "bottom_left";
                case BOTTOM_RIGHT -> "bottom_right";
            };
        }

        public static HUDState.Position fromString(String s) {
            return HUDState.Position.valueOf(s.toUpperCase());
        }

        public static Text toText(HUDState.Position n) {
            return switch (n) {
                case TOP_LEFT -> Text.translatable("playerautoma.option.hudPosition.topLeft");
                case TOP_RIGHT -> Text.translatable("playerautoma.option.hudPosition.topRight");
                case CENTER_LEFT -> Text.translatable("playerautoma.option.hudPosition.centerLeft");
                case CENTER_RIGHT -> Text.translatable("playerautoma.option.hudPosition.centerRight");
                case BOTTOM_LEFT -> Text.translatable("playerautoma.option.hudPosition.bottomLeft");
                case BOTTOM_RIGHT -> Text.translatable("playerautoma.option.hudPosition.bottomRight");
            };
        }

        public int[] getPosition() {
            InGameHud hud = MinecraftClient.getInstance().inGameHud;
            InGameHudDimensions dim = (InGameHudDimensions) hud;

            return switch (this) {
                case TOP_LEFT -> new int[]{ 2, 2 };
                case TOP_RIGHT -> new int[]{ dim.getScaledWidth() - 50, 2 };
                case CENTER_LEFT -> new int[]{ 2, dim.getScaledHeight() / 2 };
                case CENTER_RIGHT -> new int[]{ dim.getScaledWidth() - 50, dim.getScaledHeight() / 2};
                case BOTTOM_LEFT -> new int[]{ 2, dim.getScaledHeight() - 10 };
                case BOTTOM_RIGHT -> new int[]{ dim.getScaledWidth() - 50, dim.getScaledWidth() - 10};
            };
        }

    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!PlayerAutomaOptionsScreen.showHudOption.getValue() || MinecraftClient.getInstance().currentScreen != null) {
                return;
            }

            InGameHud hud = MinecraftClient.getInstance().inGameHud;
            TextRenderer r = hud.getTextRenderer();
            context.getMatrices().push();



            int[] pos = PlayerAutomaOptionsScreen.setHudPositionOption.getValue().getPosition();
            int x = pos[0]; int y = pos[1];
            r.draw(
                    /* text            */ PlayerRecorder.state.getText(),
                    /* x               */ x,
                    /* y               */ y,
                    /* color           */ PlayerRecorder.state.getColor(),
                    /* shadow          */ true,
                    /* matrix          */ context.getMatrices().peek().getPositionMatrix(),
                    /* vertexConsumers */ context.getVertexConsumers(),
                    /* layerType       */ TextRenderer.TextLayerType.SEE_THROUGH,
                    /* backgroundColor */ 0xFFFFFF,
                    /* light           */ 0xFFFFFF
            );
            context.getMatrices().pop();

        });
    }
}
