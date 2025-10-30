package net.jasper.mod.gui.option;

import net.jasper.mod.gui.PlayerautomaHUD;
import net.jasper.mod.util.data.LookingDirection;
import net.jasper.mod.util.data.StartingPositionOffset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Playerautoma option screen to configure settings
 */
public class PlayerautomaOptionsScreen extends GameOptionsScreen {

    private final GridWidget gridWidget;

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        AtomicBoolean returnValue = new AtomicBoolean(false);

        this.children().forEach(child -> {
            if (child.isMouseOver(click.x(), click.y())) {
                   returnValue.set(child.mouseClicked(click, false));
               }
        });
        return returnValue.get();
    }


    public static final OptionButton<PlayerautomaHUD.ShowHUDOption> showHudOption = new OptionButton<>(
        PlayerautomaHUD.ShowHUDOption.TEXT_AND_ICON,
        PlayerautomaHUD.ShowHUDOption.values(),
        "playerautoma.option.showHud",
        PlayerautomaHUD.ShowHUDOption::toString,
        PlayerautomaHUD.ShowHUDOption::fromString,
        PlayerautomaHUD.ShowHUDOption::toText
    );

    public static final OptionButton<Boolean> useDefaultDirectionOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.useDefaultDirection",
        Object::toString,
        Boolean::parseBoolean,
        OptionButton::booleanToOnOff
    );

    public static final OptionButton<LookingDirection.Name> setDefaultDirectionOption = new OptionButton<>(
        LookingDirection.Name.NORTH,
        LookingDirection.Name.values(),
        "playerautoma.option.setDefaultDirection",
        LookingDirection.Name::toString,
        LookingDirection.Name::fromString,
        LookingDirection.Name::toText
    );

    public static final OptionButton<StartingPositionOffset.Name> setDefaultStartingPositionOption = new OptionButton<>(
            StartingPositionOffset.Name.CENTER,
            StartingPositionOffset.Name.values(),
            "playerautoma.option.setDefaultStartingPosition",
            StartingPositionOffset.Name::toString,
            StartingPositionOffset.Name::fromString,
            StartingPositionOffset.Name::toText
    );

    public static final OptionButton<Boolean> useDefaultStartingPositionOption = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.useDefaultStartingPosition",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> restackBlocksOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.restackItems",
        Object::toString,
        Boolean::parseBoolean,
        OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> recordInventoryActivitiesOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.recordInventoryActivities",
        Object::toString,
        Boolean::parseBoolean,
        OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> writeStateToActionBarOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.writeStateToActionBar",
        Object::toString,
        Boolean::parseBoolean,
        OptionButton::booleanToOnOff
    );

    public static final OptionButton<PlayerautomaHUD.Position> setHudPositionOption = new OptionButton<>(
        PlayerautomaHUD.Position.BOTTOM_LEFT,
        PlayerautomaHUD.Position.values(),
        "playerautoma.option.setHudPosition",
        PlayerautomaHUD.Position::toString,
        PlayerautomaHUD.Position::fromString,
        PlayerautomaHUD.Position::toText
    );


    public static final OptionButton<Boolean> alwaysPreventMenuOption = new OptionButton<>(
            false,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.alwaysPreventMenu",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> resetKeyBindingsOnRecordingOption = new OptionButton<>(
        false,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.resetKeyBindingsOnRecording",
        Object::toString,
        Boolean::parseBoolean,
        OptionButton::booleanToOnOff
    );


    public static final OptionButton<Boolean> recordCommands = new OptionButton<>(
      false,
      OptionButton.BOOLEAN_VALUES,
      "playerautoma.option.recordCommands",
      Object::toString,
      Boolean::parseBoolean,
      OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> useCTRLForQuickSlots = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.useCTRLForQuickSlots",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> useALTForQuickSlots = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.useALTForQuickSlots",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> preventSlotChanges = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.preventSlotChanges",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> showQuickSlotsInQuickMenu = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.showQuickSlotsInQuickMenu",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> stopReplayOnManualInput = new OptionButton<>(
            false,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.stopReplayOnManualInput",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );

    public static final OptionButton<Boolean> saveThumbnailsWithRecording = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.saveThumbnailsWithRecording",
            Object::toString,
            Boolean::parseBoolean,
            OptionButton::booleanToOnOff
    );


    public static final ButtonWidget openCommandsToExclude = ButtonWidget.builder(
            Text.translatable("playerautoma.option.openCommandsToExclude"),
            b -> {
                MinecraftClient client= MinecraftClient.getInstance();
                client.setScreen(new CommandsToExcludeOption(client.currentScreen));
            }
    ).tooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.openCommandsToExclude"))).build();


    public PlayerautomaOptionsScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("playerautoma.screens.title.modOptions"));
        this.gridWidget = new GridWidget();
    }

    public static Screen open() {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen result = new PlayerautomaOptionsScreen(client.currentScreen);
        client.setScreen(result);
        return result;
    }

    @Override
    public void close() {
        assert this.client != null;
        this.client.setScreen(this.parent);
    }


    @Override
    public void init() {
        super.init();
        assert this.client != null;

        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(3);


        ButtonWidget setHudPositionButton = setHudPositionOption.buttonOf();
        setHudPositionOption.setButton(setHudPositionButton);

        ButtonWidget showHudButton = ButtonWidget.builder(
                Text.translatable(showHudOption.key).append(": ").append(showHudOption.textProvider.provide(showHudOption.getValue())),
                b -> {
                    showHudOption.next();
                    setHudPositionButton.active = showHudOption.getValue() != PlayerautomaHUD.ShowHUDOption.NOTHING;
                }).build();
        setHudPositionButton.active = showHudOption.getValue() != PlayerautomaHUD.ShowHUDOption.NOTHING;
        showHudOption.setButton(showHudButton);

        ButtonWidget setDefaultDirectionButton = setDefaultDirectionOption.buttonOf();
        setDefaultDirectionButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.setDefaultLookingDirection")));
        ButtonWidget useDefaultDirectionButton = ButtonWidget.builder(
                Text.translatable(useDefaultDirectionOption.key).append(": ").append(useDefaultDirectionOption.textProvider.provide(useDefaultDirectionOption.getValue())),
                b -> {
                    useDefaultDirectionOption.next();
                    setDefaultDirectionButton.active = useDefaultDirectionOption.getValue();
                })
                .tooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.useDefaultLookingDirection")))
                .build();


        ButtonWidget setDefaultStartingPositionButton = setDefaultStartingPositionOption.buttonOf();
        setDefaultStartingPositionButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.setDefaultStartingPosition")));
        ButtonWidget useDefaultStartingPositionButton = ButtonWidget.builder(
                Text.translatable(useDefaultStartingPositionOption.key).append(": ").append(useDefaultStartingPositionOption.textProvider.provide(useDefaultStartingPositionOption.getValue())),
                b -> {
                    useDefaultStartingPositionOption.next();
                    setDefaultStartingPositionButton.active = useDefaultStartingPositionOption.getValue();
                }).build();
        useDefaultStartingPositionButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.useDefaultStartingPosition")));

        // Set initial active state
        setDefaultStartingPositionButton.active = useDefaultStartingPositionOption.getValue();
        setDefaultDirectionButton.active = useDefaultDirectionOption.getValue();
        useDefaultStartingPositionOption.setButton(useDefaultStartingPositionButton);
        useDefaultDirectionOption.setButton(useDefaultDirectionButton);

        ButtonWidget restackBlocksButton = restackBlocksOption.buttonOf();
        ButtonWidget recordInventoryActivitiesButton = recordInventoryActivitiesOption.buttonOf();
        recordInventoryActivitiesButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.recordInventoryActivities")));

        ButtonWidget writeStateToActionBarButton = writeStateToActionBarOption.buttonOf();
        writeStateToActionBarOption.setButton(writeStateToActionBarButton);

        ButtonWidget alwaysPreventMenuButton = alwaysPreventMenuOption.buttonOf();
        alwaysPreventMenuButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.alwaysPreventMenu")));

        ButtonWidget resetKeyBindingsOnRecordingButton = resetKeyBindingsOnRecordingOption.buttonOf();
        resetKeyBindingsOnRecordingButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.resetKeyBindingsOnRecording")));

        ButtonWidget recordCommandosButton = ButtonWidget.builder(
                Text.translatable(recordCommands.key).append(": ").append(recordCommands.textProvider.provide(recordCommands.getValue())),
                b -> {
                    recordCommands.next();
                    openCommandsToExclude.active = recordCommands.getValue();
                }
        ).tooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.recordCommands"))).build();
        recordCommands.setButton(recordCommandosButton);
        openCommandsToExclude.active = recordCommands.getValue();

        ButtonWidget useCTRLForQuickSlotsButton = useCTRLForQuickSlots.buttonOf();
        useCTRLForQuickSlots.setButton(useCTRLForQuickSlotsButton);
        useCTRLForQuickSlotsButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.useCTRLForQuickSlots")));

        ButtonWidget useALTForQuickSlotsButton = useALTForQuickSlots.buttonOf();
        useALTForQuickSlots.setButton(useALTForQuickSlotsButton);
        useALTForQuickSlotsButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.useALTForQuickSlots")));

        ButtonWidget preventSlotChangesButton = preventSlotChanges.buttonOf();
        preventSlotChangesButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.preventSlotChanges")));
        preventSlotChanges.setButton(preventSlotChangesButton);

        ButtonWidget showQuickSlotsInQuickMenuButton = showQuickSlotsInQuickMenu.buttonOf();
        showQuickSlotsInQuickMenuButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.showQuickSlotsInQuickMenu")));
        showQuickSlotsInQuickMenu.setButton(showQuickSlotsInQuickMenuButton);

        ButtonWidget stopReplayOnManualInputButton = stopReplayOnManualInput.buttonOf();
        stopReplayOnManualInputButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.stopReplayOnManualInput")));
        stopReplayOnManualInput.setButton(stopReplayOnManualInputButton);

        ButtonWidget saveThumbnailsWithRecordingButton = saveThumbnailsWithRecording.buttonOf();
        saveThumbnailsWithRecordingButton.setTooltip(Tooltip.of(Text.translatable("playerautoma.option.tooltip.saveThumbnailsWithRecording")));
        saveThumbnailsWithRecording.setButton(saveThumbnailsWithRecordingButton);


        ButtonWidget openKeyBindOptionsButton = ButtonWidget.builder(
                Text.translatable("playerautoma.option.openKeyBindings"),
                b -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new KeybindsScreen(this, client.options));
                }
        ).build();

        adder.add(showHudButton);
        adder.add(setHudPositionButton);
        adder.add(writeStateToActionBarButton);
        adder.add(EmptyWidget.ofHeight(4), 3);

        adder.add(useDefaultDirectionButton);
        adder.add(setDefaultDirectionButton);
        adder.add(EmptyWidget.ofHeight(4));
        adder.add(useDefaultStartingPositionButton);
        adder.add(setDefaultStartingPositionButton);
        adder.add(EmptyWidget.ofHeight(4));
        adder.add(EmptyWidget.ofHeight(4), 3);

        adder.add(restackBlocksButton);
        adder.add(recordInventoryActivitiesButton);
        adder.add(alwaysPreventMenuButton);
        adder.add(resetKeyBindingsOnRecordingButton);
        adder.add(stopReplayOnManualInputButton);
        adder.add(saveThumbnailsWithRecordingButton);
        adder.add(EmptyWidget.ofHeight(4), 3);

        adder.add(useCTRLForQuickSlotsButton);
        adder.add(useALTForQuickSlotsButton);
        adder.add(EmptyWidget.ofHeight(4));
        adder.add(showQuickSlotsInQuickMenuButton);
        adder.add(preventSlotChangesButton);
        adder.add(EmptyWidget.ofHeight(4));
        adder.add(EmptyWidget.ofHeight(4), 3);

        adder.add(recordCommands.button);
        adder.add(openCommandsToExclude);
        adder.add(openKeyBindOptionsButton);

        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);

        this.layout.addBody(gridWidget);
    }

    @Override
    protected void addOptions() {
        // Added with 1.21 - Don't know what this does??
    }
}