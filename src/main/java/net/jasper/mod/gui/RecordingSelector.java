package net.jasper.mod.gui;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.automation.InputRecorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import java.io.File;

// TODO: this could be a general class listing files of a directory and letting you select one given a callback function
public class RecordingSelector extends Screen {

    private RecordingSelectionListWidget recordingSelectionList;
    private final String directoryPath;

    // Should no longer use a singleton when used in general
    public static final RecordingSelector SINGLETON = new RecordingSelector("Select a Recording", PlayerAutomaClient.RECORDING_PATH);
    public static boolean isOpen;

    public RecordingSelector(String title, String directoryPath) {
        super(Text.of(title));
        this.directoryPath = directoryPath;
        isOpen = false;
        this.init();
    }

    public static void open() {
        if (!isOpen) {
            SINGLETON.recordingSelectionList.updateFiles();
            MinecraftClient.getInstance().setScreen(SINGLETON);
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

    protected void init() {
        this.recordingSelectionList = new RecordingSelectionListWidget(MinecraftClient.getInstance(), this.directoryPath);
        this.addSelectableChild(this.recordingSelectionList);

        this.addDrawableChild(ButtonWidget.builder(
                ScreenTexts.DONE,
                (button) -> this.onDone()
        )
        .dimensions(this.width / 2 - 155 + 160, this.height - 38, 150, 20)
        .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.of("Open Recording Folder"),
                (button) -> Util.getOperatingSystem().open(new File(this.directoryPath).toURI())
        )
        .dimensions(this.width / 2 - 155, this.height - 38, 150, 20)
        .build());

        super.init();
    }

    private void onDone() {
        isOpen = false;
        RecordingSelectionListWidget.RecordingEntry recEntry = this.recordingSelectionList.getSelectedOrNull();
        if (recEntry != null) {
            InputRecorder.loadRecord(recEntry.file);
        }
        MinecraftClient.getInstance().setScreen(null);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            RecordingSelectionListWidget.RecordingEntry languageEntry = this.recordingSelectionList.getSelectedOrNull();
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
        this.recordingSelectionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 16777215);
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    @Environment(EnvType.CLIENT)
    private class RecordingSelectionListWidget extends AlwaysSelectedEntryListWidget<RecordingSelectionListWidget.RecordingEntry> {
        final String directoryPath;
        public RecordingSelectionListWidget(MinecraftClient client, String directoryPath) {
            super(client, RecordingSelector.this.width, RecordingSelector.this.height, 32, RecordingSelector.this.height - 65 + 4, 18);
            this.directoryPath = directoryPath;
            this.updateFiles();

            if (this.getSelectedOrNull() != null) {
                this.centerScrollOn(this.getSelectedOrNull());
            }
        }

        public void updateFiles() {
            this.clearEntries();
            File[] fileList = new File(this.directoryPath).listFiles();
            if (fileList == null) {
                return;
            }
            for (File file : fileList) {
                PlayerAutomaClient.LOGGER.info("Found file: " + file.getName());
                if (file.getName().endsWith(".rec")) {
                    RecordingEntry entry = new RecordingEntry(file.getName(), file);
                    this.addEntry(entry);
                }
            }
        }

        protected int getScrollbarPositionX() {
            return super.getScrollbarPositionX() + 20;
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        @Environment(EnvType.CLIENT)
        public class RecordingEntry extends AlwaysSelectedEntryListWidget.Entry<RecordingSelector.RecordingSelectionListWidget.RecordingEntry> {
            final String fileName;
            final File file;
            private long clickTime;

            public RecordingEntry(String fileName, File file) {
                this.fileName = fileName;
                this.file = file;
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(RecordingSelector.this.textRenderer, this.fileName, RecordingSelector.RecordingSelectionListWidget.this.width / 2, y + 1, 16777215);
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.onPressed();
                if (Util.getMeasuringTimeMs() - this.clickTime < 250L) {
                    RecordingSelector.this.onDone();
                }

                this.clickTime = Util.getMeasuringTimeMs();
                return true;
            }

            void onPressed() {
                RecordingSelector.RecordingSelectionListWidget.this.setSelected(this);
            }

            @Override
            public Text getNarration() {
                return Text.of(this.fileName);
            }
        }
    }
}

