package net.jasper.mod.gui;

import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.gui.option.OptionButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 'Screen' (more of a pop-up) that allows to store a recording with a given name.
 */
public class RecordingStorer extends Screen {


    public static final RecordingStorer SINGLETON = new RecordingStorer();
    public static boolean isOpen;
    private TextFieldWidget input;

    protected RecordingStorer() {
        super(Text.of("RecordingStorer"));
        isOpen = false;

    }

    public static OptionButton<Boolean> useJSON = new OptionButton<>(
            false,
            OptionButton.BOOLEAN_VALUES,
            "playerautoma.option.exportAs",
            Object::toString,
            Boolean::parseBoolean,
            (bool) -> (bool ? Text.of(".json") : Text.of(".rec"))
    );


    protected void init() {
        TextWidget text = new TextWidget(
                this.width / 2 - 100,
                this.height / 2 - 30,
                200,
                20,
                Text.translatable("playerautoma.fileSelector.enterName"),
                MinecraftClient.getInstance().textRenderer
        );

        TextFieldWidget textField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                this.width / 2 - 100,
                this.height / 2 - 10,
                200,
                20,
                Text.of("")
        );

        // Set Text Field Properties
        textField.setTooltip(Tooltip.of(Text.translatable("playerautoma.fileSelector.tooltip.textField")));
        textField.setEditable(true);

        // Format the date and time as name of recording
        String datedName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm"));
        textField.setPlaceholder(Text.of(datedName));
        textField.setText(datedName);
        this.input = textField;

        ButtonWidget save = new ButtonWidget.Builder(
              Text.of("Save"),
              (button) -> {
                  String name = this.input.getText();
                  // Append correct file ending if necessary
                  String file_ending = useJSON.getValue() ? ".json" : ".rec";
                  name += file_ending;
                  PlayerRecorder.storeRecord(name);
                  this.close();
              }).dimensions(this.width / 2 - 100, this.height / 2 + 10, 150, 20)
                .tooltip(Tooltip.of(Text.translatable("playerautoma.fileSelector.tooltip.save")))
                .build();


        ButtonWidget useJSONButton = ButtonWidget.builder(
                Text.translatable(useJSON.key).append(": ").append(useJSON.textProvider.provide(useJSON.getValue())),
                (b) -> {
                    useJSON.next();
                }).tooltip(Tooltip.of(Text.translatable("playerautoma.options.tooltip.exportAs"))
                ).dimensions(this.width / 2 + 50, this.height / 2 + 10, 50, 20).build();
        useJSON.setButton(useJSONButton);


        this.addDrawableChild(textField);
        this.addDrawableChild(text);
        this.addDrawableChild(save);
        this.addDrawableChild(useJSONButton);
    }

    public static void open() {
        if (!isOpen) {
            MinecraftClient.getInstance().setScreen(SINGLETON);
            SINGLETON.input.active = true;
            isOpen = !isOpen;
        }

    }

    @Override
    public void close() {
        isOpen = false;
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(null);
        client.mouse.lockCursor();
    }
}
