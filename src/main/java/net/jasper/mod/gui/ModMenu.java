package net.jasper.mod.gui;

import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Main-Menu if you like that allows for control of the InputRecorder via buttons.
 */
public class ModMenu extends Screen {

    public static ModMenu SINGLETON = new ModMenu("PlayerAutomaMenu");
    private static boolean isOpen = false;

    public ModMenu(String title) {
        super(Text.literal(title));
    }

    // Player Recorder
    public static ButtonWidget START_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.menu.startRecording"), button -> {
            SINGLETON.close();
            PlayerRecorder.startRecord();
        }).tooltip(Tooltip.of(Text.literal("playerautoma.menu.tooltip.startRecording"))).build();

    public static ButtonWidget STOP_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.menu.stopRecording"), button -> {
            SINGLETON.close();
            PlayerRecorder.stopRecord();
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.menu.tooltip.stopRecording"))).build();

    public static ButtonWidget START_REPLAY = ButtonWidget.builder(Text.translatable("playerautoma.menu.startReplay"), button -> {
            SINGLETON.close();
            PlayerRecorder.startReplay(false);
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.menu.tooltip.startReplay"))).build();

    public static ButtonWidget STOP_REPLAY = ButtonWidget.builder(Text.translatable("playerautoma.menu.stopReplay"), button -> {
            SINGLETON.close();
            PlayerRecorder.stopReplay();
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.menu.tooltip.stopReplay"))).build();

    public static ButtonWidget START_LOOP = ButtonWidget.builder(Text.translatable("playerautoma.menu.startLoop"), button -> {
            SINGLETON.close();
            PlayerRecorder.startLoop();
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.menu.tooltip.startLoop"))).build();

    public static ButtonWidget STORE_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.menu.storeRecording"), button -> {
            SINGLETON.close();
            RecordingStorer.open();
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.menu.tooltip.storeRecording"))).build();

    public static ButtonWidget LOAD_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.keys.loadRecording"), button -> {
        SINGLETON.close();
        RecordingSelector.open();
    }).tooltip(Tooltip.of(Text.translatable("playerautoma.menu.tooltip.loadRecording"))).build();


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
        assert this.client != null;
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(START_RECORDING);
        adder.add(STOP_RECORDING);
        adder.add(START_REPLAY);
        adder.add(STOP_REPLAY);
        adder.add(START_LOOP);
        adder.add(STORE_RECORDING);
        adder.add(LOAD_RECORDING);
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build(), 2, adder.copyPositioner().marginTop(6));

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);

    }
}
