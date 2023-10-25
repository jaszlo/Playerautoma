package net.jasper.mod.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jasper.mod.automation.InputRecorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class PlayerAutomaMenu extends Screen {

    public static PlayerAutomaMenu menu = new PlayerAutomaMenu("PlayerAutomaMenu");
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

        // Chest Finder/Autolooter
        public static ButtonWidget TOGGLE_FINDER_BEACONS;
        public static ButtonWidget TOGGLE_FINDER_XRAY;
        public static ButtonWidget TOGGLE_LOOTER;

    }

    public static void open() {
        if (!isOpen && !handled) {
            MinecraftClient.getInstance().setScreen(menu);
            isOpen = !isOpen;
        }

        handled = false;
    }

    private static boolean handled = false;
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputUtil.GLFW_KEY_O) {
            handled = true;
            menu.close();
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
        client.setScreen((Screen) null);
        client.mouse.lockCursor();
    }

    @Override
    protected void init() {
        Buttons.START_RECORDING = ButtonWidget.builder(Text.literal("Start Recording (g)"), button -> {
                    menu.close();
                    InputRecorder.startRecord();
                })
                .dimensions(width / 2 - 205, 20, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Recording your movement and close the Menu")))
                .build();

        Buttons.STOP_RECORDING = ButtonWidget.builder(Text.literal("Stop Recording (h)"), button -> {
                    menu.close();
                    InputRecorder.stopRecord();
                })
                .dimensions(width / 2 + 5, 20, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Stop Recording your movements and close the Menu")))
                .build();

        Buttons.START_REPLAY = ButtonWidget.builder(Text.literal("Start Replay (j)"), button -> {
                    menu.close();
                    InputRecorder.startReplay();
                })
                .dimensions(width / 2 - 205, 45, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Replaying your recorded movements and close the Menu")))
                .build();

        Buttons.STOP_REPLAY = ButtonWidget.builder(Text.literal("Stop Replay (k)"), button -> {
                    menu.close();
                    InputRecorder.stopReplay();

                })
                .dimensions(width / 2 + 5, 45, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Replaying your recorded movements and close the Menu")))
                .build();

        Buttons.START_LOOP = ButtonWidget.builder(Text.literal("Start Looping Replay (l)"), button -> {
                    menu.close();
                    InputRecorder.startLoop();
                })
                .dimensions(width / 2 - 205, 70, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Start Looping your recorded movements and close the Menu")))
                .build();

        Buttons.STORE_RECORDING = ButtonWidget.builder(Text.literal("Store Record To File (u)"), button -> {
                    menu.close();
                    InputRecorder.storeRecord();
                })
                .dimensions(width / 2 - 205, 95, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Stores your Recording to a .rec file on your Hard Drive")))
                .build();

        Buttons.LOAD_RECORDING = ButtonWidget.builder(Text.literal("Load Record From File (i)"), button -> {
                    menu.close();
                    InputRecorder.loadRecord();
                })
                .dimensions(width / 2 + 5, 95, BUTTON_WIDTH, BUTTON_HEIGHT)
                .tooltip(Tooltip.of(Text.literal("Loads a Record From a .rec file from your Hard Drive")))
                .build();

        addDrawableChild(Buttons.START_RECORDING);
        addDrawableChild(Buttons.STOP_RECORDING);
        addDrawableChild(Buttons.START_REPLAY);
        addDrawableChild(Buttons.STOP_REPLAY);
        addDrawableChild(Buttons.START_LOOP);
        addDrawableChild(Buttons.STORE_RECORDING);
        addDrawableChild(Buttons.LOAD_RECORDING);
    }
}
