package net.jasper.mod.gui;

import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

/**
 * Main-Menu if you like that allows for control of the InputRecorder via buttons.
 */
public class PlayerAutomaMenu extends Screen {

    public static PlayerAutomaMenu SINGLETON = new PlayerAutomaMenu("PlayerAutomaMenu");
    private static boolean isOpen = false;

    public PlayerAutomaMenu(String title) {
        super(Text.literal(title));
    }
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    // Player Recorder
    public static class Buttons {
        // Player Recorder
        public static ButtonWidget START_RECORDING;
        public static ButtonWidget STOP_RECORDING;
        public static ButtonWidget START_REPLAY;
        public static ButtonWidget STOP_REPLAY;
        public static ButtonWidget START_LOOP;
        public static ButtonWidget STORE_RECORDING;
        public static ButtonWidget LOAD_RECORDING;
        public static ButtonWidget CANCEL_REPLAY;

    }

    public static void open() {
        if (!isOpen && !handled) {
            MinecraftClient.getInstance().setScreen(SINGLETON);

            isOpen = !isOpen;
        }

        handled = false;
    }

    private static boolean handled = false;
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputUtil.GLFW_KEY_O) {
            handled = true;
            SINGLETON.close();
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        isOpen = false;
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(null);
        client.mouse.lockCursor();
    }

    @Override
    protected void init() {
        Buttons.START_RECORDING = ButtonWidget.builder(Text.literal("Start Recording"), button -> {
                    SINGLETON.close();
                    PlayerRecorder.startRecord();
                })
                .dimensions(width / 2 - 205, 20, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Recording your movement and close the Menu")))
                .build();

        Buttons.STOP_RECORDING = ButtonWidget.builder(Text.literal("Stop Recording"), button -> {
                    SINGLETON.close();
                    PlayerRecorder.stopRecord();
                })
                .dimensions(width / 2 + 5, 20, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Stop Recording your movements and close the Menu")))
                .build();

        Buttons.START_REPLAY = ButtonWidget.builder(Text.literal("Start Replay"), button -> {
                    SINGLETON.close();
                    PlayerRecorder.startReplay();
                })
                .dimensions(width / 2 - 205, 45, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Replaying your recorded movements and close the Menu")))
                .build();

        Buttons.STOP_REPLAY = ButtonWidget.builder(Text.literal("Stop Replay"), button -> {
                    SINGLETON.close();
                    PlayerRecorder.stopReplay();

                })
                .dimensions(width / 2 + 5, 45, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Replaying your recorded movements and close the Menu")))
                .build();

        Buttons.START_LOOP = ButtonWidget.builder(Text.literal("Start Looping Replay"), button -> {
                    SINGLETON.close();
                    PlayerRecorder.startLoop();
                })
                .dimensions(width / 2 - 205, 70, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Looping your recorded movements and close the Menu")))
                .build();

        Buttons.CANCEL_REPLAY = ButtonWidget.builder(Text.literal("Cancel Replay"), button -> {
                    SINGLETON.close();
                    PlayerRecorder.stopReplay();
                })
                .dimensions(width / 2 + 5, 70, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Cancel Replaying your recorded movements and close the Menu")))
                .build();

        Buttons.STORE_RECORDING = ButtonWidget.builder(Text.literal("Store Record To File"), button -> {
                    SINGLETON.close();
                    RecordingStorer.open();
                })
                .dimensions(width / 2 - 205, 95, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Stores your Recording to a .rec file on your Hard Drive")))
                .build();

        Buttons.LOAD_RECORDING = ButtonWidget.builder(Text.literal("Load Record From File"), button -> {
                    SINGLETON.close();
                    RecordingSelector.open();
                })
                .dimensions(width / 2 + 5, 95, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Loads a Record From a .rec file from your Hard Drive")))
                .build();

        this.addDrawableChild(Buttons.START_RECORDING);
        this.addDrawableChild(Buttons.STOP_RECORDING);
        this.addDrawableChild(Buttons.START_REPLAY);
        this.addDrawableChild(Buttons.STOP_REPLAY);
        this.addDrawableChild(Buttons.START_LOOP);
        this.addDrawableChild(Buttons.CANCEL_REPLAY);
        this.addDrawableChild(Buttons.STORE_RECORDING);
        this.addDrawableChild(Buttons.LOAD_RECORDING);
    }
}
