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
import net.minecraft.util.Identifier;
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
    private final Text INFINITY = Text.of("∞");

    private long lastRightClick = 0;
    private long lastLeftClick = 0;
    private boolean lastLeftClickState = false; // Flag to prevent double button click sound from happening

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
        // -1 == Infinity
        if (loopCount < 0) {
            PlayerRecorder.startLoop();
        } else if (loopCount > 0) {
            PlayerRecorder.startReplay(loopCount);
        }
        loopCount = 0;

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
        boolean leftClicked = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        // Check cooldown. If not reached just return
        long CLICK_COOLDOWN = 100; // Milliseconds
        long now = System.currentTimeMillis();
        if (mouseOver && rightClicked) {
            if (now - this.lastRightClick >= CLICK_COOLDOWN) {
                // Toggle Infinity
                loopCount = loopCount < 0 ? 0 : -1; // Infinity
                client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));

                // Update successful right click
                this.lastRightClick = now;
            }
        } else if (mouseOver && leftClicked) {
            if (now - this.lastLeftClick >= CLICK_COOLDOWN) {
                // Update loopCount
                if (loopCount < 0) {
                    loopCount = 1;
                } else {
                    loopCount++;
                }

                // Play sound but not on first click to prevent double click
                if (this.lastLeftClickState) {
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                }

                // Update successful left click
                this.lastLeftClick = now;
            }
        }
        this.lastLeftClickState = leftClicked;
    }

    // Remove the blur by Overriding method
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderDarkening(context);
    }

    public static Screen open() {
        loopCount = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        Screen result = new QuickMenu(client.currentScreen);
        client.setScreen(result);
        return result;
    }

    public ButtonWidget buttonStartPauseRecord = ButtonWidget.builder(
            Text.of(""),
            b -> {
                if (PlayerRecorder.state.isRecording() || PlayerRecorder.state.isPausedRecording()) {
                    PlayerRecorder.togglePauseRecord();
                } else {
                    PlayerRecorder.startRecord();
                }
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonStopRecord = ButtonWidget.builder(
            Text.of(""),
            b -> PlayerRecorder.stopRecord()
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();


    public ButtonWidget buttonStartPauseReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {
                if (PlayerRecorder.state.isReplaying() || PlayerRecorder.state.isPausedReplaying()) {
                    PlayerRecorder.togglePauseReplay();
                } else {
                    PlayerRecorder.startReplay(false);
                }
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonStopReplay = ButtonWidget.builder(
            Text.of(""),
            b -> PlayerRecorder.stopReplay()
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();


    public ButtonWidget buttonLoopReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {/*Do Nothing. Is handled in 'tick' method to allow holding pressed */}
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public void init() {
        assert this.client != null;
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(3);

        // Start Record | Stop Record
        adder.add(this.buttonStartPauseRecord);
        adder.add(this.buttonStopRecord);
        adder.add(EmptyWidget.ofWidth(BUTTON_DIMENSIONS));

        // Start Replay/Stop Replay | Pause Replay | Loop Replay
        adder.add(this.buttonStartPauseReplay, 1);
        adder.add(this.buttonStopReplay, 1);
        adder.add(this.buttonLoopReplay, 1);

        adder.add(EmptyWidget.ofHeight(24), 3);

        adder.add(this.currentTooltip, 3);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);

        this.loopCountText.setX(buttonLoopReplay.getX() + BUTTON_DIMENSIONS + 1);
        this.loopCountText.setY(buttonLoopReplay.getY() + BUTTON_DIMENSIONS - 3);
        this.addDrawableChild(this.loopCountText);

        // Add tooltips
        tooltips.clear();
        tooltips.put(this.buttonStartPauseRecord, Text.translatable("playerautoma.screens.menu.tooltip.startRecording").append(":").append(Text.translatable("playerautoma.screens.menu.tooltip.pauseRecording")).append(":").append(Text.translatable("playerautoma.screens.menu.tooltip.resumeRecording")));
        tooltips.put(this.buttonStopRecord, Text.translatable("playerautoma.screens.menu.tooltip.stopRecording"));
        tooltips.put(this.buttonStartPauseReplay, Text.translatable("playerautoma.screens.menu.tooltip.startReplay").append(":").append(Text.translatable("playerautoma.screens.menu.tooltip.pauseReplay")).append(":").append(Text.translatable("playerautoma.screens.menu.tooltip.resumeReplay")));
        tooltips.put(this.buttonStopReplay, Text.translatable("playerautoma.screens.menu.tooltip.stopReplay"));
        tooltips.put(this.buttonLoopReplay, Text.translatable("playerautoma.screens.menu.tooltip.startLoop"));
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.mouseX = mouseX;
        this.mouseY = mouseY;

        // Draw loop count Text
        Text toSet;
        if (loopCount < 0)       toSet = INFINITY;
        else if (loopCount == 0) toSet = Text.of("");
        else /* loopCount > 0 */ toSet = Text.of("" + loopCount);

        loopCountText.setWidth(textRenderer.getWidth(toSet));
        loopCountText.setMessage(toSet);

        // Draw current Tooltip
        {
            ScreenAccessor screenAccessor = (ScreenAccessor) this;
            boolean hasTooltip = false;
            for (Drawable d : screenAccessor.getDrawables()) {
                if (!(d instanceof ButtonWidget button)) continue;
                if (button.isMouseOver(mouseX, mouseY)) {
                    // Adjust position to still be centered by first retrieving center via old width/xPos
                    Text t = tooltips.getOrDefault(button, Text.of(""));

                    // Special ToolTip case for button who switch function on context
                    {
                        boolean split = button == this.buttonStartPauseRecord || button == this.buttonStartPauseReplay;
                        int START_INDEX = 0; int PAUSE_INDEX = 1; int CONTINUE_INDEX = 2;

                        // Determine which tooltip to use. Tooltip are all for the same button as one big string seperated via ":"
                        int splitIndex = START_INDEX;
                        if (PlayerRecorder.state.isRecording() || PlayerRecorder.state.isReplaying()) splitIndex = PAUSE_INDEX;
                        if (PlayerRecorder.state.isPausedRecording() || PlayerRecorder.state.isPausedReplaying()) splitIndex = CONTINUE_INDEX;

                        if (split) {
                            String adjustedTooltip = t.getString().split(":")[splitIndex];
                            t = Text.of(adjustedTooltip);
                        }
                    }

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
            Identifier startPauseRecordTexture = PlayerRecorder.state.isRecording() ? Textures.QuickMenu.PAUSED_RECORDING : Textures.QuickMenu.START_RECORDING;
            Identifier startPauseReplayTexture = PlayerRecorder.state.isReplaying() ? Textures.QuickMenu.PAUSE_REPLAY : Textures.QuickMenu.START_REPLAY;

            context.drawTexture(startPauseRecordTexture, this.buttonStartPauseRecord.getX() + 2, this.buttonStartPauseRecord.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.STOP_RECORDING, this.buttonStopRecord.getX() + 1, this.buttonStopRecord.getY() + 1, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

            context.drawTexture(startPauseReplayTexture, this.buttonStartPauseReplay.getX() + 2, this.buttonStartPauseReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.STOP_REPLAY, this.buttonStopReplay.getX() + 2, this.buttonStopReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.START_LOOP, this.buttonLoopReplay.getX() + 2, this.buttonLoopReplay.getY() + 2, 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

            context.getMatrices().pop();
        }


    }

}
