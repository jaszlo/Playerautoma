package net.jasper.mod.gui;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.PlayerAutomaOptionsScreen;
import net.jasper.mod.util.PlayerAutomaExceptionHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.Objects;

/**
 * Main-Menu if you like that allows for control of the InputRecorder via buttons.
 */
public class PlayerAutomaMenuScreen extends Screen {
    private final Screen parent;

    public PlayerAutomaMenuScreen(Screen parent) {
        super(Text.translatable("playerautoma.screens.title.modMenu"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
    }

    // Player Recorder
    private static final ButtonWidget START_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.screens.menu.startRecording"), button -> {
            Objects.requireNonNull(MinecraftClient.getInstance().currentScreen).close();
            PlayerAutomaExceptionHandler.callSafe(PlayerRecorder::startRecord);
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.screens.menu.tooltip.startRecording"))).build();

    private static final ButtonWidget STOP_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.screens.menu.stopRecording"), button -> {
            Objects.requireNonNull(MinecraftClient.getInstance().currentScreen).close();
            PlayerAutomaExceptionHandler.callSafe(PlayerRecorder::stopRecord);
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.screens.menu.tooltip.stopRecording"))).build();

    private static final ButtonWidget START_REPLAY = ButtonWidget.builder(Text.translatable("playerautoma.screens.menu.startReplay"), button -> {
            Objects.requireNonNull(MinecraftClient.getInstance().currentScreen).close();
            PlayerAutomaExceptionHandler.callSafe(() -> PlayerRecorder.startReplay(false));
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.screens.menu.tooltip.startReplay"))).build();

    private static final ButtonWidget START_LOOP = ButtonWidget.builder(Text.translatable("playerautoma.screens.menu.startLoop"), button -> {
            Objects.requireNonNull(MinecraftClient.getInstance().currentScreen).close();
            PlayerAutomaExceptionHandler.callSafe(PlayerRecorder::startLoop);
        }).tooltip(Tooltip.of(Text.translatable("playerautoma.screens.menu.tooltip.startLoop"))).build();

    private static final ButtonWidget STORE_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.screens.menu.storeRecording"),
            button -> RecordingStorerScreen.open()).tooltip(Tooltip.of(Text.translatable("playerautoma.screens.menu.tooltip.storeRecording"))).build();

    private static final ButtonWidget LOAD_RECORDING = ButtonWidget.builder(Text.translatable("playerautoma.screens.menu.loadRecording"),
            button -> RecordingSelectorScreen.open()).tooltip(Tooltip.of(Text.translatable("playerautoma.screens.menu.tooltip.loadRecording"))).build();

    private static final ButtonWidget OPTION_MENU = ButtonWidget.builder(Text.translatable("playerautoma.options"),
            button -> PlayerAutomaOptionsScreen.open()).width(200).build();

    public static Screen open() {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen result = new PlayerAutomaMenuScreen(client.currentScreen);
        client.setScreen(result);
        return result;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        assert this.client != null;
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(START_RECORDING);
        adder.add(STOP_RECORDING);
        adder.add(EmptyWidget.ofHeight(16), 2);
        adder.add(START_REPLAY);
        adder.add(START_LOOP);
        adder.add(EmptyWidget.ofHeight(16), 2);
        adder.add(STORE_RECORDING);
        adder.add(LOAD_RECORDING);
        adder.add(EmptyWidget.ofHeight(16), 2);
        adder.add(OPTION_MENU, 2);
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).width(200).build(), 2);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);

    }
}
