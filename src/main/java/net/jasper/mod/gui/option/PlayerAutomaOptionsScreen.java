package net.jasper.mod.gui.option;

import net.jasper.mod.util.data.LookingDirection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;


public class PlayerAutomaOptionsScreen extends GameOptionsScreen {

    public static PlayerAutomaOptionsScreen create() {
        return new PlayerAutomaOptionsScreen("PlayerAutoma Options", new OptionsScreen(new TitleScreen(true), MinecraftClient.getInstance().options));
    }

    public static OptionButton<Boolean> showHudOption = new OptionButton<>(
            true,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.showHud",
            (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF).getString(),
            Boolean::parseBoolean
    );

    public static OptionButton<Boolean> useDefaultDirectionOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.option.useDefaultDirection",
        (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF).getString(),
        Boolean::parseBoolean
    );

    public static OptionButton<LookingDirection.Names> setDefaultDirectionOption = new OptionButton<>(
        LookingDirection.Names.NORTH,
        LookingDirection.Names.values(),
        "playerautoma.option.setDefaultDirection",
        Enum::name,
        LookingDirection.Names::valueOf
    );


    public static OptionButton<Boolean> restackBlocksOption = new OptionButton<>(
        true,
        OptionButton.BOOLEAN_VALUES,
        "playerautoma.options.restackItems",
        (bool) -> (bool ? ScreenTexts.ON : ScreenTexts.OFF).getString(),
        Boolean::parseBoolean
    );

    protected PlayerAutomaOptionsScreen(String title, Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.of(title));
    }

    public void init() {
        super.init();
        assert this.client != null;

        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(5).marginBottom(4).alignHorizontalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        ButtonWidget showHudButton = ButtonWidget.builder(
            Text.translatable(showHudOption.key).append(": ").append(showHudOption.encoder.encode(showHudOption.getValue())),
            (b) -> showHudOption.next()).build();
        showHudOption.setButton(showHudButton);
        adder.add(showHudButton);

        ButtonWidget setDefaultDirectionButton = ButtonWidget.builder(
            Text.translatable(setDefaultDirectionOption.key).append(": ").append(setDefaultDirectionOption.encoder.encode(setDefaultDirectionOption.getValue())),
            (b) -> setDefaultDirectionOption.next()).build();
        setDefaultDirectionOption.setButton(setDefaultDirectionButton);
        setDefaultDirectionButton.active = useDefaultDirectionOption.getValue();
        adder.add(setDefaultDirectionButton);

        ButtonWidget useDefaultDirectionButton = ButtonWidget.builder(
                Text.translatable(useDefaultDirectionOption.key).append(": ").append(useDefaultDirectionOption.encoder.encode(useDefaultDirectionOption.getValue())),
                (b) -> {
                    useDefaultDirectionOption.next();
                    setDefaultDirectionButton.active = useDefaultDirectionOption.getValue();
                }).build();
        useDefaultDirectionOption.setButton(useDefaultDirectionButton);
        adder.add(useDefaultDirectionButton);


        ButtonWidget restackBlocksButton = ButtonWidget.builder(
                Text.translatable(restackBlocksOption.key).append(": ").append(restackBlocksOption.encoder.encode(restackBlocksOption.getValue())),
                (b) -> restackBlocksOption.next()).build();
        restackBlocksOption.setButton(restackBlocksButton);
        adder.add(restackBlocksButton);

        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> this.client.setScreen(this.parent)).width(200).build(), 2, adder.copyPositioner().marginTop(6));
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, this.height / 6 - 12, this.width, this.height, 0.5f, 0.0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }
}