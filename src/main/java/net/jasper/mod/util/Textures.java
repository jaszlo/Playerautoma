package net.jasper.mod.util;

import net.jasper.mod.PlayerAutomaClient;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.util.Identifier;

/**
 * Namespace to hold all identifiers used by Playerautoma
 */
public class Textures {
    public static class HUD {
        public static final Identifier BLOCK_MENU_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/block_menu.png");
        public static final Identifier REPLAYING_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/replaying.png");
        public static final Identifier REPLAYING_PAUSED_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/replaying_paused.png");
        public static final Identifier RECORDING_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/recording.png");
        public static final Identifier RECORDING_PAUSED_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/recording_paused.png");
        public static final Identifier IDLE_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recorder_icons/idle.png");
    }

    public static class SelectorScreen {
        public static final Identifier REFRESH_ICON = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/recording_selector_icons/refresh.png");
    }


    public static class QuickMenu {
        public static final Identifier START_RECORDING = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/quickmenu_icons/start_recording.png");
        public static final Identifier STOP_RECORDING = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/quickmenu_icons/stop_recording.png");
        public static final Identifier PAUSED_RECORDING = HUD.RECORDING_PAUSED_ICON;
        public static final Identifier START_REPLAY = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/quickmenu_icons/start_replay.png");
        public static final Identifier STOP_REPLAY = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/quickmenu_icons/stop_replay.png");
        public static final Identifier PAUSE_REPLAY = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/quickmenu_icons/pause_replay.png");
        public static final Identifier START_LOOP = Identifier.of(PlayerAutomaClient.MOD_ID, "textures/gui/quickmenu_icons/start_loop.png");
    }

    // Other

    public static final ButtonTextures DEFAULT_BUTTON_TEXTURES = new ButtonTextures(Identifier.of("widget/button"), Identifier.of("widget/button_disabled"), Identifier.of("widget/button_highlighted"));
}
