package net.jasper.mod.gui;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.automation.QuickSlots;
import net.jasper.mod.mixins.accessors.ScreenAccessor;
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

    private final ButtonWidget[] buttonsQuickSlots = new ButtonWidget[QuickSlots.QUICKSLOTS_N];
    private final float EMPTY_QUICKSLOT_BUTTON_ALPHA = 0.25f;
    private final float FULL_QUICKSLOT_BUTTON_ALPHA = 1f;

    private final Text INFINITY = Text.of("∞");

    private long lastWheelClick = 0;
    private long lastRightClick = 0;
    private long lastLeftClick = 0;
    private boolean lastLeftClickState = false;  // Flag to prevent double button click sound from happening for loop button
    private boolean lastRightClickState = false; // Flag to prevent holding clicked from acting more than once for quickslots
    private boolean lastWheelClickState = false;

    public static int loopCount = 0;

    private final Map<ButtonWidget, Text> tooltips = new HashMap<>();


    public ButtonWidget buttonStartPauseRecord = ButtonWidget.builder(
            Text.of(""),
            b -> {
                loopCount = 0;
                if (PlayerRecorder.state.isRecording() || PlayerRecorder.state.isPausedRecording()) {
                    PlayerRecorder.togglePauseRecord();
                } else {
                    PlayerRecorder.startRecord();
                }
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonStopRecord = ButtonWidget.builder(
            Text.of(""),
            b -> {
                loopCount = 0;
                PlayerRecorder.stopRecord();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();


    public ButtonWidget buttonStartPauseReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {
                loopCount = 0;
                if (PlayerRecorder.state.isReplaying() || PlayerRecorder.state.isPausedReplaying()) {
                    PlayerRecorder.togglePauseReplay();
                } else {
                    PlayerRecorder.startReplay(false);
                }
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    public ButtonWidget buttonStopReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {
                loopCount = 0;
                PlayerRecorder.stopReplay();
            }
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();


    public ButtonWidget buttonLoopReplay = ButtonWidget.builder(
            Text.of(""),
            b -> {/*Do Nothing. Is handled in 'tick' method to allow holding pressed */}
    ).size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS).build();

    protected QuickMenu(Screen parent) {
        super(Text.translatable("playerautoma.screens.title.quickMenu"));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        currentTooltip = new TextWidget(Text.of(""), this.client.textRenderer);
        loopCountText = new TextWidget(Text.of(""), this.client.textRenderer);

        // Create quickslot buttons
        for (int i = 0; i < QuickSlots.QUICKSLOTS_N; i++) {
            int finalI = i;
            buttonsQuickSlots[i] = ButtonWidget.builder(
                    Text.of(""),
                    b -> QuickSlots.storeRecording(finalI))
                .size(BUTTON_DIMENSIONS, BUTTON_DIMENSIONS)
                .build();
            float alpha = QuickSlots.quickSlots[i].isEmpty() ? EMPTY_QUICKSLOT_BUTTON_ALPHA : FULL_QUICKSLOT_BUTTON_ALPHA;
            buttonsQuickSlots[i].setAlpha(alpha);
        }

        this.init();
        this.updateButtonActive();
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
        // Update if button should be active or not depending on state
        this.updateButtonActive();

        boolean rightClicked = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean leftClicked = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean wheelClicked = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        long CLICK_COOLDOWN = 150; // Milliseconds
        long now = System.currentTimeMillis();

        // If loop button is not active do not process its clicks any further
        if (buttonLoopReplay.active) {
            boolean mouseOver = buttonLoopReplay.isMouseOver(this.mouseX, this.mouseY);
            // Check cooldown. If not reached just return
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
        }

        for (int i = 0; i < QuickSlots.QUICKSLOTS_N; i++) {
            ButtonWidget button = buttonsQuickSlots[i];
            boolean mouseOver = button.isMouseOver(this.mouseX, this.mouseY);
            if (mouseOver && rightClicked && !lastRightClickState) {
                // Check cooldown. If not reached just return
                if (now - this.lastRightClick >= CLICK_COOLDOWN) {
                    // Update successful right click
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    this.lastRightClick = now;
                    QuickSlots.loadRecording(i);
                }
            }
            if (mouseOver && wheelClicked && !lastWheelClickState) {
                if (now - this.lastWheelClick >= CLICK_COOLDOWN) {
                    // Update successful wheel click
                    client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    this.lastWheelClick = now;
                    QuickSlots.clearQuickSlot(i);
                }
            }
        }

        // Update for next tick
        this.lastLeftClickState = leftClicked;
        this.lastRightClickState = rightClicked;
        this.lastWheelClickState = wheelClicked;
    }



    private void updateButtonActive() {
        buttonStartPauseRecord.active = PlayerRecorder.state.isAny(PlayerRecorder.State.IDLE, PlayerRecorder.State.RECORDING, PlayerRecorder.State.PAUSED_RECORDING);
        buttonStopRecord.active = PlayerRecorder.state.isAny(PlayerRecorder.State.RECORDING, PlayerRecorder.State.PAUSED_RECORDING);

        buttonStartPauseReplay.active = PlayerRecorder.state.isAny(PlayerRecorder.State.IDLE, PlayerRecorder.State.REPLAYING, PlayerRecorder.State.PAUSED_REPLAY);
        buttonLoopReplay.active = PlayerRecorder.state.isAny(PlayerRecorder.State.IDLE);
        buttonStopReplay.active = PlayerRecorder.state.isAny(PlayerRecorder.State.REPLAYING, PlayerRecorder.State.PAUSED_REPLAY);
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

    public void init() {
        assert this.client != null;
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(3).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(3);

        // Start Record | Stop Record
        adder.add(this.buttonStartPauseRecord);
        adder.add(this.buttonStopRecord);
        adder.add(EmptyWidget.ofWidth(BUTTON_DIMENSIONS));

        // Start Replay/Stop Replay | Pause Replay | Loop Replay
        adder.add(this.buttonStartPauseReplay, 1);
        adder.add(this.buttonStopReplay, 1);
        adder.add(this.buttonLoopReplay, 1);
        adder.add(EmptyWidget.ofHeight(4), 3);

        for (int i = 0; i < QuickSlots.QUICKSLOTS_N; i++) {
            adder.add(this.buttonsQuickSlots[i]);
        }

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
        for (int i = 0; i < QuickSlots.QUICKSLOTS_N; i++) {
            tooltips.put(this.buttonsQuickSlots[i], Text.translatable("playerautoma.screens.menu.tooltip.quickslotbutton"));
        }
    }

    private void updateTooltip(Text toSet) {
        int currentX = currentTooltip.getX();
        int oldWidth = currentTooltip.getWidth();
        int center = currentX + oldWidth / 2;

        // Now shift from center given newWidth
        int newWidth = textRenderer.getWidth(toSet);
        int newX = center - newWidth / 2;
        currentTooltip.setWidth(newWidth);
        currentTooltip.setX(newX);
        currentTooltip.setMessage(toSet);
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
        // Special case for quickslots. Tooltip should be shown over entire area. ignore default tooltip handling in this case
        boolean xHit = buttonsQuickSlots[0].getX() <= mouseX && mouseX <= buttonsQuickSlots[buttonsQuickSlots.length - 1].getX() + BUTTON_DIMENSIONS;
        boolean yHit = buttonsQuickSlots[0].getY() <= mouseY && mouseY <= buttonsQuickSlots[buttonsQuickSlots.length - 1].getY() + BUTTON_DIMENSIONS;
        if (xHit && yHit) {
            Text t = tooltips.getOrDefault(buttonsQuickSlots[0], Text.of(""));
            updateTooltip(t);
        } else {
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
                    updateTooltip(t);
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
            // Use any button to get size for texture. all buttons have same size
            int scaledSize = buttonStartPauseRecord.getWidth();

            context.getMatrices().push();
            Identifier startPauseRecordTexture = PlayerRecorder.state.isRecording() ? Textures.QuickMenu.PAUSED_RECORDING : Textures.QuickMenu.START_RECORDING;
            Identifier startPauseReplayTexture = PlayerRecorder.state.isReplaying() ? Textures.QuickMenu.PAUSE_REPLAY : Textures.QuickMenu.START_REPLAY;

            context.drawTexture(startPauseRecordTexture, this.buttonStartPauseRecord.getX(), this.buttonStartPauseRecord.getY(), 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.STOP_RECORDING, this.buttonStopRecord.getX(), this.buttonStopRecord.getY(), 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

            context.drawTexture(startPauseReplayTexture, this.buttonStartPauseReplay.getX(), this.buttonStartPauseReplay.getY(), 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.STOP_REPLAY, this.buttonStopReplay.getX(), this.buttonStopReplay.getY(), 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);
            context.drawTexture(Textures.QuickMenu.START_LOOP, this.buttonLoopReplay.getX(), this.buttonLoopReplay.getY(), 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

            context.getMatrices().pop();
        }

        // Draw quick slot thumbnails
        {
            // Update button alpha and texture
            for (int i = 0; i < QuickSlots.QUICKSLOTS_N; i++) {
                boolean isEmpty = QuickSlots.quickSlots[i].isEmpty();
                float alpha = isEmpty ? EMPTY_QUICKSLOT_BUTTON_ALPHA : FULL_QUICKSLOT_BUTTON_ALPHA;
                buttonsQuickSlots[i].setAlpha(alpha);
                ButtonWidget b = buttonsQuickSlots[i];

                // Thumbnail only available if not empty
                if (!isEmpty) {
                    context.drawTexture(QuickSlots.THUMBNAIL_IDENTIFIER[i], b.getX() + 1, b.getY() + 1, 0, 0, BUTTON_DIMENSIONS - 2, BUTTON_DIMENSIONS - 2, BUTTON_DIMENSIONS - 2, BUTTON_DIMENSIONS - 2);
                }
            }
        }

    }

}
