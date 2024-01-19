package net.jasper.mod.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.mixins.InGameHudDimensions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;

/**
 * Little HUD for Playerautoma to display current state of player recorder
 */
public class HUDState {
    public static void register() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!PlayerAutomaOptionsScreen.showHudOption.getValue() || MinecraftClient.getInstance().currentScreen != null) {
                return;
            }

            InGameHud hud = MinecraftClient.getInstance().inGameHud;
            InGameHudDimensions dim = (InGameHudDimensions) hud;
            TextRenderer r = hud.getTextRenderer();
            context.getMatrices().push();
            r.draw(
                    /* text            */ PlayerRecorder.state.getText(),
                    /* x               */ 2,
                    /* y               */ dim.getScaledHeight() - 10,
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
