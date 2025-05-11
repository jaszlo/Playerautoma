package net.jasper.mod.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.automation.PlayerRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerAutomaExceptionHandler {

    private static final DateTimeFormatter errorDateTimeFormat = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");

    private static final String[] ERROR_MESSAGE_HEADER = {
        "I’m sorry that I let you down :/",
        "Well, this is awkward...",
        "Error? I barely know her!",
        "Congratulations! You’ve found a bug. Do you feel special now?",
        "This wasn’t supposed to happen. Let’s pretend it didn’t.",
        "I was told there would be no math...",
        "I tried my best, but my best is apparently not good enough.",
        "If you’re reading this, something went horribly wrong. Good luck!",
        "This error is brought to you by: Sleep Deprivation and Coffee Overdose.",
        "I’m not saying it’s your fault, but... it’s definitely not mine."
    };

    private static final Random random = new Random();

    private static final String ERROR_DUMP_TEMPLATE =
            """
            %s
            
            The following exception occurred due to the usage of the playerautoma mod.
            Please create an Issue via Github at https://github.com/jaszlo/Playerautoma/issues
            or sent a Discord message on the server at https://discord.com/invite/yBT2dbCPCv
            with this file attached
            
            Version-Info: %s
            
            StackTrace:
            =========================================================================================
            %s
            """;

    /**
     * Handle the given exception by creating an error dumb and resetting the PlayerRecorder
     * @param exception to handle
     */
    public static void handleException(Exception exception)  {
        String fileName = "%s_error_dumb.txt".formatted(LocalDateTime.now().format(errorDateTimeFormat));
        File errorFile = Path.of(PlayerAutomaClient.PLAYERAUTOMA_FOLDER_PATH, fileName).toFile();
        Text fileNameText = Text.literal(fileName)
                .formatted(Formatting.UNDERLINE)
                .styled(style -> style.withClickEvent(new ClickEvent.OpenFile(errorFile.getAbsolutePath())));

        Text message = Text.translatable("playerautoma.messages.error.unknownException", fileNameText);

        // Create a file to dump the error message into
        try (FileWriter writer = new FileWriter(errorFile)) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            exception.printStackTrace(printWriter);

            writer.write(ERROR_DUMP_TEMPLATE.formatted(
                    ERROR_MESSAGE_HEADER[random.nextInt(ERROR_MESSAGE_HEADER.length)],
                    PlayerAutomaClient.getVersion(),
                    stringWriter.toString()
            ));

        } catch (Exception e) {
            // Ignore I guess??
        }

        ClientHelpers.writeToChat(message);

        // Reset into functional state
        PlayerRecorder.reset();
    }

    public static void callSafe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public static Runnable produceSafeCall(Runnable run) {
        return () -> {
            try {
                run.run();
            } catch (Exception exception) {
                handleException(exception);
            }
        };
    }

}
