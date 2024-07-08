package net.jasper.mod.gui;


import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.automation.PlayerRecorder;
import net.jasper.mod.util.ClientHelpers;
import net.jasper.mod.util.IOHelpers;
import net.jasper.mod.util.data.Recording;
import net.jasper.mod.util.data.RecordingThumbnail;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.Util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static net.jasper.mod.PlayerAutomaClient.PLAYERAUTOMA_RECORDING_PATH;
import static net.jasper.mod.util.Textures.DEFAULT_BUTTON_TEXTURES;
import static net.jasper.mod.util.Textures.SelectorScreen.REFRESH_ICON;
// TODO: this could be a general class listing files of a directory and letting you select one given a callback function

/**
 * Screen copied from language-selection. Allows to select stored recordings.
 */
public class RecordingSelectorScreen extends Screen {

    private RecordingSelectionListWidget recordingSelectionList;
    private final String directoryPath;

    private final Screen parent;
    private final MinecraftClient client;

    // TODO: This should be cleaned up on update. However I can't be bothered right now as it is more a best practice than really necessary
    // Use map to store thumbnails and only load new ones in the future in "updateFiles"
    protected final static Map<String, RecordingThumbnail> thumbnails = new HashMap<>();

    /**
     * Initially load thumbnails on load to prevent lag when this screen is opened for the first time
     */
    public static void loadThumbnails() {
        File recordingFolder = new File(PLAYERAUTOMA_RECORDING_PATH);
        File[] fileList = recordingFolder.listFiles();
        if (fileList == null) {
            return;
        }
        for (File file : fileList) {
            if (file.getName().endsWith(".rec") || file.getName().endsWith(".json")) {
                // Only do this if the thumbnail is not yet registered
                if (!thumbnails.containsKey(file.getName())) {
                    Recording r = IOHelpers.loadRecordingFile(recordingFolder, file);
                    if (r != null) {
                        thumbnails.put(file.getName(), r.thumbnail);
                        try {
                            Identifier id = new Identifier(PlayerAutomaClient.MOD_ID, String.valueOf(file.getName().hashCode()));
                            MinecraftClient.getInstance().getTextureManager().registerTexture(id, new NativeImageBackedTexture(r.thumbnail.toNativeImage()));
                        } catch(InvalidIdentifierException e) {
                            PlayerAutomaClient.LOGGER.warn("Failed to load thumbnail for {}", file.getName());
                        }
                    }
                }
            }
        }
    }

    public RecordingSelectorScreen(Screen parent) {
        super(Text.translatable("playerautoma.screens.title.selector"));
        this.directoryPath = PLAYERAUTOMA_RECORDING_PATH;
        this.parent = parent;
        this.client = MinecraftClient.getInstance();
        this.init();
    }

    public static Screen open() {
        MinecraftClient client = MinecraftClient.getInstance();
        Screen result = new RecordingSelectorScreen(client.currentScreen);
        client.setScreen(result);
        return result;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private ButtonWidget refreshButton;

    protected void init() {
        this.recordingSelectionList = new RecordingSelectionListWidget(this.client, this.directoryPath);
        this.addSelectableChild(this.recordingSelectionList);

        // Button placement:
        //   [Refresh] [Delete] [Open Recording Folder] [Done]
        refreshButton = TexturedButtonWidget.builder(
                    Text.of(""),
                    (button) -> this.onRefresh()
            )
            .tooltip(Tooltip.of(Text.translatable("playerautoma.screens.fileSelector.tooltip.refresh")))
            .dimensions(this.width / 2 - 65 - 170, this.height - 38, 20, 20)
            .build();
        this.addDrawableChild(refreshButton);

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("playerautoma.screens.fileSelector.delete"),
                (button) -> this.onDelete()
        )
        .dimensions(this.width / 2 - 65 - 140, this.height - 38, 130, 20)
        .build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("playerautoma.screens.fileSelector.openFolder"),
                (button) -> Util.getOperatingSystem().open(new File(this.directoryPath).toURI())
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

    private void onRefresh() {
        this.recordingSelectionList.updateFiles();
    }

    private void onDone() {
        RecordingSelectionListWidget.RecordingEntry recEntry = this.recordingSelectionList.getSelectedOrNull();
        if (recEntry != null) {
            PlayerRecorder.loadRecord(recEntry.file);
        }
        this.client.setScreen(null);
    }

    private void onDelete() {
        RecordingSelectionListWidget.RecordingEntry recEntry = this.recordingSelectionList.getSelectedOrNull();
        if (recEntry != null) {
            client.execute(() -> {
                boolean deleteSuccess = recEntry.file.delete();
                if (!deleteSuccess) {
                    PlayerAutomaClient.LOGGER.warn("Could not delete recording file {}", recEntry.fileName);
                    this.close();
                    ClientHelpers.writeToActionBar(Text.translatable("playerautoma.messages.error.deleteFailedRecording"));
                }
                this.recordingSelectionList.updateFiles();
            });
        }
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
        // Render refresh texture over refresh button
        int scaledSize = 20;
        context.drawTexture(REFRESH_ICON, refreshButton.getX(), refreshButton.getY(), 0, 0, scaledSize, scaledSize, scaledSize, scaledSize);

        this.recordingSelectionList.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 16777215);
    }


    private class RecordingSelectionListWidget extends AlwaysSelectedEntryListWidget<RecordingSelectionListWidget.RecordingEntry> {
        final String directoryPath;
        public RecordingSelectionListWidget(MinecraftClient client, String directoryPath) {
            // EntryListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight)
            super(client, RecordingSelectorScreen.this.width, RecordingSelectorScreen.this.height - 93, 32, 18);
            this.directoryPath = directoryPath;
            this.updateFiles();

            if (this.getSelectedOrNull() != null) {
                this.centerScrollOn(this.getSelectedOrNull());
            }
        }

        public void updateFiles() {
            this.clearEntries();
            File recordingDirectory = new File(this.directoryPath);
            File[] fileList = recordingDirectory.listFiles();
            if (fileList == null) {
                return;
            }
            for (File file : fileList) {
                if (file.getName().endsWith(".rec") || file.getName().endsWith(".json")) {
                    // Only do this if the thumbnail is not yet registered
                    if (!thumbnails.containsKey(file.getName())) {
                        // Do not load async or image will not be available for screen when first opened
                        Recording r = IOHelpers.loadRecordingFile(recordingDirectory, file);
                        if (r != null) thumbnails.put(file.getName(), r.thumbnail);
                    }
                    RecordingEntry entry = new RecordingEntry(file.getName(), file, thumbnails.get(file.getName()));
                    this.addEntry(entry);
                }
            }
        }

        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class RecordingEntry extends AlwaysSelectedEntryListWidget.Entry<RecordingSelectorScreen.RecordingSelectionListWidget.RecordingEntry> {
            final String fileName;
            final File file;
            NativeImageBackedTexture texture = null;
            Identifier textureIdentifier = null;
            private long clickTime;


            public RecordingEntry(String fileName, File file, RecordingThumbnail thumbnail) {
                this.fileName = fileName;
                this.file = file;
                if (thumbnail != null) {
                    this.texture = new NativeImageBackedTexture(thumbnail.toNativeImage());
                    try {
                        this.textureIdentifier = new Identifier(PlayerAutomaClient.MOD_ID, String.valueOf(fileName.hashCode()));
                        MinecraftClient.getInstance().getTextureManager().registerTexture(textureIdentifier, texture);
                    } catch (InvalidIdentifierException e) {
                        PlayerAutomaClient.LOGGER.warn("Could not load thumbnail for file {}", fileName, e);
                    }
                }
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(RecordingSelectorScreen.this.textRenderer, this.fileName, RecordingSelectorScreen.RecordingSelectionListWidget.this.width / 2, y + 1, 16777215);

                // Only render if present to prevent exception or 'unknown texture' to be rendered
                if (texture != null && textureIdentifier != null) {
                    int thumbnailX = x - 25;
                    int thumbnailSize = 20;
                    context.drawGuiTexture(DEFAULT_BUTTON_TEXTURES.get(false, false), thumbnailX, y - 2, thumbnailSize, thumbnailSize);
                    context.drawTexture(this.textureIdentifier, thumbnailX + 1 , y - 1, 0,0, thumbnailSize - 2, thumbnailSize - 2, thumbnailSize - 2, thumbnailSize - 2);
                }
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.onPressed();
                if (Util.getMeasuringTimeMs() - this.clickTime < 250L) {
                    RecordingSelectorScreen.this.onDone();
                }

                this.clickTime = Util.getMeasuringTimeMs();
                return true;
            }

            void onPressed() {
                RecordingSelectorScreen.RecordingSelectionListWidget.this.setSelected(this);
            }

            @Override
            public Text getNarration() {
                return Text.of(this.fileName);
            }
        }
    }
}

