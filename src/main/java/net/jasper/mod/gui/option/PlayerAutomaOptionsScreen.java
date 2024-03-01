package net.jasper.mod.gui.option;

import net.jasper.mod.gui.HUDState;
import net.jasper.mod.util.data.LookingDirection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;


/**
 * Playerautoma option screen to configure settings
 */
public class    PlayerAutomaOptionsScreen extends GameOptionsScreen {

    public static OptionButton<HUDState.ShowHUDOption> showHudOption = new OptionButton<>(
        HUDState.ShowHUDOption.TEXT_AND_ICON,
        HUDState.ShowHUDOption.values(),
        "playerautoma.option.showHud",
        HUDState.ShowHUDOption::toString,
        HUDState.ShowHUDOption::fromString,
        HUDState.ShowHUDOption::toText
    );

    public static OptionButton<Boolean> useDefaultDirectionOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.useDefaultDirection",
        Object::toString,
        Boolean::parseBoolean,
        (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF)
    );

    public static OptionButton<LookingDirection.Name> setDefaultDirectionOption = new OptionButton<>(
        LookingDirection.Name.NORTH,
        LookingDirection.Name.values(),
        "playerautoma.option.setDefaultDirection",
        LookingDirection.Name::toString,
        LookingDirection.Name::fromString,
        LookingDirection.Name::toText
    );

    public static OptionButton<Boolean> useRelativeLookingDirectionOption = new OptionButton<>(
        false,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.useRelativeLookingDirection",
        Object::toString,
        Boolean::parseBoolean,
        (bool) -> (bool ? Text.translatable("playerautoma.option.relativeLookingDirection") : Text.translatable("playerautoma.option.absoluteLookingDirection"))
    );


    public static OptionButton<Boolean> restackBlocksOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.restackItems",
        Object::toString,
        Boolean::parseBoolean,
        (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF)
    );


    public static OptionButton<Boolean> recordInventoryActivitiesOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.recordInventoryActivities",
        Object::toString,
        Boolean::parseBoolean,
        (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF)
    );

    public static OptionButton<Boolean> writeStateToChatOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.writeStateToChat",
        Object::toString,
        Boolean::parseBoolean,
        (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF)
    );

    public static OptionButton<HUDState.Position> setHudPositionOption = new OptionButton<>(
        HUDState.Position.BOTTOM_LEFT,
        HUDState.Position.values(),
        "playerautoma.option.setHudPosition",
        HUDState.Position::toString,
        HUDState.Position::fromString,
        HUDState.Position::toText
    );


    public PlayerAutomaOptionsScreen(String title, Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of(title));
    }

    public void init() {
        super.init();
        assert this.client != null;

        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        ButtonWidget setHudPositionButton = setHudPositionOption.buttonOf();
        setHudPositionOption.setButton(setHudPositionButton);

        ButtonWidget showHudButton = ButtonWidget.builder(
                Text.translatable(showHudOption.key).append(": ").append(showHudOption.textProvider.provide(showHudOption.getValue())),
                (b) -> {
                    showHudOption.next();
                    setHudPositionButton.active = showHudOption.getValue() != HUDState.ShowHUDOption.NOTHING;
                }).build();
        setHudPositionButton.active = showHudOption.getValue() != HUDState.ShowHUDOption.NOTHING;
        showHudOption.setButton(showHudButton);

        ButtonWidget setDefaultDirectionButton = setDefaultDirectionOption.buttonOf();
        ButtonWidget useRelativeLookingDirectionButton = useRelativeLookingDirectionOption.buttonOf();

        useRelativeLookingDirectionButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.useRelativeLookingDirection")));
        useRelativeLookingDirectionOption.setButton(useRelativeLookingDirectionButton);
        ButtonWidget useDefaultDirectionButton = ButtonWidget.builder(
                Text.translatable(useDefaultDirectionOption.key).append(": ").append(useDefaultDirectionOption.textProvider.provide(useDefaultDirectionOption.getValue())),
                (b) -> {
                    useDefaultDirectionOption.next();
                    setDefaultDirectionButton.active = useDefaultDirectionOption.getValue();
                    useRelativeLookingDirectionButton.active = !useDefaultDirectionOption.getValue();
                }).build();

        // Set initial active state
        setDefaultDirectionButton.active = useDefaultDirectionOption.getValue();
        useRelativeLookingDirectionButton.active = !useDefaultDirectionOption.getValue();
        useDefaultDirectionOption.setButton(useDefaultDirectionButton);

        ButtonWidget restackBlocksButton = restackBlocksOption.buttonOf();
        ButtonWidget recordInventoryActivitiesButton = recordInventoryActivitiesOption.buttonOf();
        recordInventoryActivitiesButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.recordInventoryActivities")));

        ButtonWidget writeStateToChatButton = writeStateToChatOption.buttonOf();
        writeStateToChatOption.setButton(writeStateToChatButton);

        ButtonWidget openKeyBindOptionsButton = ButtonWidget.builder(
                Text.translatable("playerautoma.option.openKeyBindings"),
                (button) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new KeybindsScreen(this, client.options));
                }
        ).build();

        adder.add(showHudButton);
        adder.add(setHudPositionButton);
        adder.add(writeStateToChatButton);
        adder.add(useDefaultDirectionButton);
        adder.add(setDefaultDirectionButton);
        adder.add(useRelativeLookingDirectionButton);
        adder.add(restackBlocksButton);
        adder.add(recordInventoryActivitiesButton);
        adder.add(openKeyBindOptionsButton, 2);
        adder.add(EmptyWidget.ofHeight(16), 2);
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.parent)).width(200).build(), 2, adder.copyPositioner().marginTop(6));
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }
}