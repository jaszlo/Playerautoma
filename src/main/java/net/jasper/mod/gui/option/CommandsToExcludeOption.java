package net.jasper.mod.gui.option;


import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.ClientHelpers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Screen copied from language-selection. Allows to select stored recordings.
 */
public class CommandsToExcludeOption extends Screen {

    private CommandsToExcludeListWidget commandSelectionList;
    private final Screen parent;
    private final MinecraftClient client;

    // Should no longer use a singleton when used in general
    public static String FILE_NAME = "ignored_commands.txt";
    public static String FILE_PATH = Path.of(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), FILE_NAME).toString();

    public static final List<String> userCommandsToIgnore = new ArrayList<>();

    public CommandsToExcludeOption(String title, Screen parent) {
        super(Text.of(title));
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        this.init();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }


    private TextFieldWidget tfw;

    protected void init() {
        this.commandSelectionList = new CommandsToExcludeListWidget(MinecraftClient.getInstance(), FILE_PATH);
        this.addSelectableChild(this.commandSelectionList);

        // Button placement:
        //   [Delete] [TextField] [Add] [Done]
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("playerautoma.screens.commandsToExclude.delete"),
                (button) -> this.onDelete()
        )
        .dimensions(this.width / 2 - 65 - 280, this.height - 38, 130, 20)
        .build());

        this.tfw = new TextFieldWidget(
            this.client.textRenderer,
            this.width / 2 - 65 - 140,
            this.height - 38,
            130,
            20,
            Text.of("")
        );
        this.tfw.setEditable(true);
        this.tfw.active = true;

        this.addDrawableChild(this.tfw);

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("playerautoma.screens.commandsToExclude.add"),
                (button) -> this.commandSelectionList.add(this.tfw.getText())
        )
        .dimensions(this.width / 2 - 65, this.height - 38, 130, 20)
        .build());

        this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                (button) -> this.onDone()
        )
        .dimensions(this.width / 2 - 65 + 140, this.height - 38, 130, 20)
        .build());

        super.init();
    }

    private void onDone() {
        MinecraftClient.getInstance().setScreen(this.parent);
        this.commandSelectionList.writeCommands();
    }

    private void onDelete() {
        CommandsToExcludeListWidget.CommandEntry recEntry = this.commandSelectionList.getSelectedOrNull();
        if (recEntry != null) {
            boolean deleteSuccess = this.commandSelectionList.remove(recEntry);
            if (!deleteSuccess) {
                PlayerAutomaClient.LOGGER.warn("Could not delete command to ignore {}", recEntry.command);
                this.close();
                ClientHelpers.writeToChat(Text.translatable("playerautoma.messages.deleteFailedCommandToExclude"));
            }
            this.commandSelectionList.writeCommands();
            this.commandSelectionList.updateCommands();
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            CommandsToExcludeListWidget.CommandEntry languageEntry = this.commandSelectionList.getSelectedOrNull();
            if (languageEntry != null) {
                languageEntry.onPressed();
                this.onDone();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.commandSelectionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 16777215);
    }

    private class CommandsToExcludeListWidget extends AlwaysSelectedEntryListWidget<CommandsToExcludeListWidget.CommandEntry> {
        final String filePath;
        public CommandsToExcludeListWidget(MinecraftClient client, String filePath) {
            // EntryListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight)
            super(client, CommandsToExcludeOption.this.width, CommandsToExcludeOption.this.height - 93, 32, 18);
            this.filePath = filePath;
            this.updateCommands();

            if (this.getSelectedOrNull() != null) {
                this.centerScrollOn(this.getSelectedOrNull());
            }
        }

        public void updateCommands() {
            this.clearEntries();
            userCommandsToIgnore.clear();

            // Iterate over each line in file and add it as an entry
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Process the line
                    addEntry(new CommandEntry(line));
                    userCommandsToIgnore.add(line);
                }
            } catch (IOException e) {
                // Don't know when this should happen
                PlayerAutomaClient.LOGGER.info(e.toString());
            }
        }

        public void writeCommands() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                for (CommandEntry e : this.children()) {
                    bw.write(e.command);
                    bw.newLine(); // Add a new line after each line
                }
            } catch (IOException e) {
                // Don't know when this should happen
                PlayerAutomaClient.LOGGER.info(e.toString());
            }
        }

        public boolean remove(CommandEntry toRemove) {
            return this.children().remove(toRemove);
        }

        public void add(String command) {
            CommandEntry toAdd = new CommandEntry(command);
            if (this.children().contains(toAdd)) {
                return;
            }
            this.children().add(toAdd);
            this.writeCommands();
            this.updateCommands();
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class CommandEntry extends Entry<CommandEntry> {
            final String command;
            private long clickTime;

            public CommandEntry(String command) {
                this.command = command;
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(CommandsToExcludeOption.this.textRenderer, this.command, CommandsToExcludeListWidget.this.width / 2, y + 1, 16777215);
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.onPressed();
                if (Util.getMeasuringTimeMs() - this.clickTime < 250L) {
                    CommandsToExcludeOption.this.onDone();
                }

                this.clickTime = Util.getMeasuringTimeMs();
                return true;
            }

            void onPressed() {
                CommandsToExcludeListWidget.this.setSelected(this);
            }

            @Override
            public Text getNarration() {
                return Text.of(this.command);
            }
        }
    }
}

