package net.jasper.mod.gui.option;

import lombok.Setter;
import net.jasper.mod.PlayerautomaClient;
import net.jasper.mod.util.PlayerautomaExceptionHandler;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that holds and takes care of playerautoma options
 * @param <V> The Type of the value that the option has
 */
public class OptionButton<V> {

    public static Text booleanToOnOff(boolean bool) {
        return bool ? ScreenTexts.ON : ScreenTexts.OFF;
    }

    private static final String OPTION_FILE_NAME = "playerautoma_options.txt";
    private static final Logger LOGGER = LoggerFactory.getLogger("playerautoma::options");

    public static final Boolean[] BOOLEAN_VALUES = { true, false };
    public static final File OPTION_FILE = new File(String.valueOf(Path.of(PlayerautomaClient.PLAYERAUTOMA_FOLDER_PATH, OPTION_FILE_NAME)));

    public final String key;
    @Setter
    public ButtonWidget button;

    private V currentValue;
    private final V[] values;
    private final V defaultValue;
    public final ValueDecoder<V> decoder;
    public final ValueEncoder<V> encoder;
    public final TextProvider<V> textProvider;

    private int valueIndex = -1;

    public interface ValueEncoder<V> {
        String encode(V v);
    }

    public interface ValueDecoder<V> {
        V decode(String s);
    }

    public interface TextProvider<V> {
        Text provide(V v);
    }


    public V getValue() {
        return this.currentValue;
    }

    public OptionButton(V defaultValue, V[] values, String key, ValueEncoder<V> encoder, ValueDecoder<V> decoder, TextProvider<V> textProvider) {
        this.button = null;
        this.defaultValue = defaultValue;
        this.currentValue = defaultValue;
        this.values = values;
        this.key = key;
        this.encoder = encoder;
        this.decoder = decoder;
        this.textProvider = textProvider;

        load();

        for (int i = 0; i < values.length; i++) {
            if (values[i] == this.currentValue) {
                valueIndex = i;
                break;
            }
        }

        if (valueIndex < 0) {
            LOGGER.error("currentValue is not a defined value for Option {}", key);
            valueIndex = 0;
            this.currentValue = values[0];
        }
    }

    public void next() {
        this.valueIndex = (this.valueIndex + 1) % this.values.length;
        this.currentValue = this.values[valueIndex];
        this.button.setMessage(Text.translatable(this.key).append(": ").append(this.textProvider.provide(this.currentValue)));
        this.store();
    }


    public void store() {
        List<String> lines = new ArrayList<>();

        // Read existing content from the file
        try (BufferedReader br = new BufferedReader(new FileReader(OPTION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            PlayerautomaExceptionHandler.handleException(e);
        }

        boolean keyFound = false;

        String valueToWrite = this.encoder.encode(currentValue);
        // Check if the key already exists in the file
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith(this.key + ":")) {
                lines.set(i, this.key + ":" + valueToWrite);
                keyFound = true;
                break;
            }
        }

        // If the key is not found, append it to the end of the file
        if (!keyFound) {
            lines.add(this.key + ":" + valueToWrite);
        }

        // Write the updated content back to the file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OPTION_FILE))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            PlayerautomaExceptionHandler.handleException(e);
        }
    }

    public void load() {
        String readValue = null;
        try (BufferedReader br = new BufferedReader(new FileReader(OPTION_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split each line into key and readValue
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].trim().equals(key)) {
                    readValue = parts[1].trim();
                    break;
                }
            }
        } catch (IOException e) {
            PlayerautomaExceptionHandler.handleException(e);
        }

        // Value was not in option file therefore store default value
        if (readValue == null) {
            store();
            return;
        }
        // Catch invalid values in config (if edited manually) and set/write defaultValue
        try {
            this.currentValue = decoder.decode(readValue);
        } catch (Exception e) {
            LOGGER.warn("Forbidden value '{}' found in playerautoma_options.txt for '{}'", readValue, this.key);
            this.currentValue = defaultValue;
            store();
        }
    }

    /**
     * Creates the default button widget for this option button and returns it. Also registers the button for this option.
     * Will always call this.next for button onClick.
     * @return ButtonWidget of this option
     */
    public ButtonWidget buttonOf() {
        ButtonWidget newButton =  ButtonWidget.builder(
            Text.translatable(this.key).append(": ").append(this.textProvider.provide(this.getValue())),
            b -> this.next()).build();
        this.setButton(newButton);
        return button;
    }
}
