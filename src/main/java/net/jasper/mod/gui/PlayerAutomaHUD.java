package net.jasper.mod.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jasper.mod.automation.MenuPrevention;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.mixins.InGameHudDimensions;
import net.jasper.mod.util.ClientHelpers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;

/**
 * Little HUD for Playerautoma to display current state of player recorder
 */
public class PlayerAutomaHUD {

    public enum ShowHUDOption {
        NOTHING,
        TEXT,
        ICON,
        TEXT_AND_ICON;

        @Override
        public String toString() {
            return switch (this) {
                case NOTHING -> "nothing";
                case TEXT -> "text";
                case ICON -> "icon";
                case TEXT_AND_ICON -> "text_and_icon";
            };
        }

        public static PlayerAutomaHUD.ShowHUDOption fromString(String s) {
            return PlayerAutomaHUD.ShowHUDOption.valueOf(s.toUpperCase());
        }

        public static Text toText(PlayerAutomaHUD.ShowHUDOption opt) {
            return switch(opt) {
                case NOTHING -> Text.translatable("playerautoma.option.hudShow.nothing");
                case TEXT -> Text.translatable("playerautoma.option.hudShow.text");
                case ICON -> Text.translatable("playerautoma.option.hudShow.icon");
                case TEXT_AND_ICON -> Text.translatable("playerautoma.option.hudShow.text_and_icon");
            };
        }
    }

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

        public static PlayerAutomaHUD.Position fromString(String s) {
            return PlayerAutomaHUD.Position.valueOf(s.toUpperCase());
        }

        public static Text toText(PlayerAutomaHUD.Position opt) {
            return switch (opt) {
                case TOP_LEFT -> Text.translatable("playerautoma.option.hudPosition.topLeft");
                case TOP_RIGHT -> Text.translatable("playerautoma.option.hudPosition.topRight");
                case CENTER_LEFT -> Text.translatable("playerautoma.option.hudPosition.centerLeft");
                case CENTER_RIGHT -> Text.translatable("playerautoma.option.hudPosition.centerRight");
                case BOTTOM_LEFT -> Text.translatable("playerautoma.option.hudPosition.bottomLeft");
                case BOTTOM_RIGHT -> Text.translatable("playerautoma.option.hudPosition.bottomRight");
            };
        }


        public int[] getPosition(int scaledSize) {
            InGameHud hud = MinecraftClient.getInstance().inGameHud;
            InGameHudDimensions dim = (InGameHudDimensions) hud;

            // Get the longest text offset of the state if text is displayed
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int textOffset = 0;
            PlayerAutomaHUD.ShowHUDOption opt = PlayerAutomaOptionsScreen.showHudOption.getValue();
            if (opt == ShowHUDOption.TEXT || opt == ShowHUDOption.TEXT_AND_ICON) {
                for (PlayerRecorder.State s : PlayerRecorder.State.values()) {
                    textOffset = Math.max(textOffset, textRenderer.getWidth(s.getText()));
                }
            }

            return switch (this) {
                case TOP_LEFT ->     new int[]{ (int)(dim.getScaledWidth() * 0.01),  (int)(dim.getScaledHeight() * 0.01)              };
                case CENTER_LEFT ->  new int[]{ (int)(dim.getScaledWidth() * 0.01),  (int)(dim.getScaledHeight() * 0.5)               };
                case BOTTOM_LEFT ->  new int[]{ (int)(dim.getScaledWidth() * 0.01),  (int)(dim.getScaledHeight() * 0.99) - scaledSize  };
                case TOP_RIGHT ->    new int[]{ (int)(dim.getScaledWidth() * 0.99 - scaledSize - textOffset) , (int)(dim.getScaledHeight() * 0.01)              };
                case CENTER_RIGHT -> new int[]{ (int)(dim.getScaledWidth() * 0.99 - scaledSize - textOffset) , (int)(dim.getScaledHeight() * 0.5)               };
                case BOTTOM_RIGHT -> new int[]{ (int)(dim.getScaledWidth() * 0.99 - scaledSize - textOffset) , (int)(dim.getScaledHeight() * 0.99) - scaledSize  };
            };
        }
    }

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            // Renders only if active
            MenuPrevention.renderIcon(context);

            ShowHUDOption showOffHud = PlayerAutomaOptionsScreen.showHudOption.getValue();
            if (showOffHud == ShowHUDOption.NOTHING) {
                return;
            }
            // Get/Calc guiScale
            int scale = ClientHelpers.getGuiScale();

            // Just looks better. Not sure how it looks on gui scale. But who is dumb enough to play like that?
            scale = scale > 1 ? scale - 1 : scale;

            // Texture are 13x13 and 14x14 therefore choose the larger one as default
            int size = 14;
            int scaledSize = scale * size;

            // Calculate position
            int[] pos = PlayerAutomaOptionsScreen.setHudPositionOption.getValue().getPosition(scaledSize);
            int x = pos[0]; int y = pos[1];



            if (showOffHud == ShowHUDOption.ICON || showOffHud == ShowHUDOption.TEXT_AND_ICON) {
                context.getMatrices().push();
                // Move x left to the text and create padding
                context.drawTexture(PlayerRecorder.state.getIcon(), x, y, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
                context.getMatrices().pop();
            }

            if (showOffHud == ShowHUDOption.TEXT || showOffHud == ShowHUDOption.TEXT_AND_ICON) {
                context.getMatrices().push();
                // Position given in 'scaled pixels'
                context.drawText(
                        client.textRenderer,
                        PlayerRecorder.state.getText(),
                        // Move x next to icon
                        x + 2 + scaledSize,
                        // Move y to align with center of icon
                        y - 2 + scaledSize / 2,
                        PlayerRecorder.state.getColor(),
                        true
                );
                context.getMatrices().pop();
            }
        });
    }
}
