package net.jasper.mod.gui;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.mixins.accessors.ScreenAccessor;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.Textures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class QuickMenu extends Screen {

    private final Screen parent;
    private final MinecraftClient client;

    private int mouseX;
    private int mouseY;

    public static boolean wasClosed = false;
    public static final int BUTTON_DIMENSIONS = 32;

    private final TextWidget currentTooltip;
    private final TextWidget loopCountText;

    public static int loopCount = 0;

    private final Map<ButtonWidget, Text> tooltips = new HashMap<>();

    protected QuickMenu(Screen parent) {
        super(Text.translatable("playerautoma.screens.title.quickMenu"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        currentTooltip = new TextWidget(Text.of(""), this.client.textRenderer);
        loopCountText = new TextWidget(Text.of(""), this.client.textRenderer);
        this.init();

    }

    @Override
    public void close() {
        // This flag prevents the quickMenu from being reopened the next frame because the key was still pressed
        // Only releasing the quick menu key once will set 'wasClosed' to false again and make it possible to open the quick menu again.
        wasClosed = true;

        if (loopCount > 0) {
            PlayerRecorder.startReplay(loopCount);
            loopCount = 0;
        }

        this.client.setScreen(this.parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void tick() {
        boolean mouseOver = buttonLoopReplay.isMouseOver(this.mouseX, this.mouseY);
        boolean rightClicked = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (mouseOver && rightClicked) {
            PlayerRecorder.startLoop();
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            this.close();
        }
    }

    public static Screen open() {
        loopCount = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        Screen result = new QuickMenu(client.currentScreen);
        client.setScreen(result);
        return result;
    }

    public ButtonWidget buttonStartRecord = ButtonWidget.builder(
            Text.of(""),
            b -> {
                PlayerRecorder.startRecord();
                this.close();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonStopRecord = ButtonWidget.builder(
            Text.of(""),
            b -> {
                PlayerRecorder.stopRecord();
                this.close();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();


    public ButtonWidget buttonStartReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {
                PlayerRecorder.startReplay(false);
                this.close();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonStopReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {
                PlayerRecorder.stopReplay();
                this.close();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonPauseReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {
                PlayerRecorder.togglePauseReplay();
                this.close();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonLoopReplay = ButtonWidget.builder(
            Text.of(""),
            b -> loopCount++
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public void init() {
        assert this.client != null;
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(4);

        // Start Record | Stop Record
        adder.add(this.buttonStartRecord, 2);
        adder.add(this.buttonStopRecord, 2);

        // Start Replay | Stop Replay | Pause Replay | Loop Replay
        adder.add(this.buttonStartReplay);
        adder.add(this.buttonPauseReplay);
        adder.add(this.buttonStopReplay);
        adder.add(this.buttonLoopReplay);

        adder.add(EmptyWidget.ofHeight(16), 4);

        adder.add(this.currentTooltip, 4);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);

        this.loopCountText.setX(buttonLoopReplay.getX() + BUTTON_DIMENSIONS + 1);
        this.loopCountText.setY(buttonLoopReplay.getY() + BUTTON_DIMENSIONS - 3);
        this.addDrawableChild(this.loopCountText);

        // Add tooltips
        tooltips.clear();
        tooltips.put(this.buttonStartRecord, Text.translatable("playerautoma.screens.menu.tooltip.startRecording"));
        tooltips.put(this.buttonStopRecord, Text.translatable("playerautoma.screens.menu.tooltip.stopRecording"));
        tooltips.put(this.buttonStartReplay, Text.translatable("playerautoma.screens.menu.tooltip.startReplay"));
        tooltips.put(this.buttonPauseReplay, Text.translatable("playerautoma.screens.menu.tooltip.pauseReplay"));
        tooltips.put(this.buttonStopReplay, Text.translatable("playerautoma.screens.menu.tooltip.stopReplay"));
        tooltips.put(this.buttonLoopReplay, Text.translatable("playerautoma.screens.menu.tooltip.startLoop"));
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Draw loop count Text
        if (loopCount > 0) {
            Text t = Text.of(loopCount + "");
            loopCountText.setWidth(textRenderer.getWidth(t));
            loopCountText.setMessage(t);
        }

        // Draw current Tooltip
        {
            ScreenAccessor screenAccessor = (ScreenAccessor) this;
            boolean hasTooltip = false;
            for (Drawable d : screenAccessor.getDrawables()) {
                if (!(d instanceof ButtonWidget button)) continue;
                if (button.isMouseOver(mouseX, mouseY)) {
                    // Adjust position to still be centered by first retrieving center via old width/xPos
                    Text t = tooltips.getOrDefault(button, Text.of(""));
                    int currentX = currentTooltip.getX();
                    int oldWidth = currentTooltip.getWidth();
                    int center = currentX + oldWidth / 2;

                    // Now shift from center given newWidth
                    int newWidth = textRenderer.getWidth(t);
                    int newX = center - newWidth / 2;
                    currentTooltip.setWidth(newWidth);
                    currentTooltip.setX(newX);
                    currentTooltip.setMessage(t);
                    hasTooltip = true;
                }
            }
            // Clear Tooltip if mouse not over any buttons
            if (!hasTooltip) {
                currentTooltip.setMessage(Text.of(""));
            }
        }


        // Draw Icons on buttons
        {
            int scale = ClientHelpers.getGuiScale();
            // Just looks better. Not sure how it looks on gui scale. But who is dumb enough to play like that?
            scale = scale > 1 ? scale - 1 : scale;
            // Texture are 12x12 and 14x14 therefore choose the larger one as default
            int size = 14;
            int scaledSize = scale * size;

            context.getMatrices().push();
            // Start replay
            context.drawTexture(Textures.QuickMenu.START_RECORDING, this.buttonStartRecord.getX() + 2, this.buttonStartRecord.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.STOP_RECORDING, this.buttonStopRecord.getX() + 1, this.buttonStopRecord.getY() + 1, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

            context.drawTexture(Textures.QuickMenu.START_REPLAY, this.buttonStartReplay.getX() + 2, this.buttonStartReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.PAUSE_REPLAY, this.buttonPauseReplay.getX() + 2, this.buttonPauseReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.STOP_REPLAY, this.buttonStopReplay.getX() + 2, this.buttonStopReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.START_LOOP, this.buttonLoopReplay.getX() + 2, this.buttonLoopReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

            context.getMatrices().pop();
        }


    }

}
