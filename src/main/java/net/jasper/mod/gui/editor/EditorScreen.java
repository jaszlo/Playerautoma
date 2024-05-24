package net.jasper.mod.gui.editor;


import net.jasper.mod.PlayerAutomaClient;
import net.jasper.mod.util.data.Recording;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * Screen to edit Recordings
 */
public class EditorScreen extends Screen {

    private RecordingTickListWidget recordingSelectionList;
    private final Screen parent;
    public final Recording record;

    public EditorScreen(String title, Recording record, Screen parent) {
        super(Text.of(title));
        this.record = record;
        this.parent = parent;
        this.init();
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(this.parent);
    }

    protected void init() {
        this.recordingSelectionList = new RecordingTickListWidget(MinecraftClient.getInstance());
        this.addSelectableChild(this.recordingSelectionList);

        // Button placement
        this.addDrawableChild(ButtonWidget.builder(
                        ScreenTexts.DONE,
                        (button) -> this.onDone()
                )
                .dimensions(this.width / 2 - 65 + 140, this.height - 38, 130, 20)
                .build());

        super.init();
    }

    private void onDone() {
        RecordingTickListWidget.RecordingTickEntry recEntry = this.recordingSelectionList.getSelectedOrNull();
        if (recEntry != null) {
            PlayerAutomaClient.LOGGER.info("clicked rec-entry ({})", recEntry.tickNumber);
        }
        MinecraftClient.getInstance().setScreen(null);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyCodes.isToggle(keyCode)) {
            RecordingTickListWidget.RecordingTickEntry entry = this.recordingSelectionList.getSelectedOrNull();
            if (entry != null) {
                entry.onPressed();
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

    private class RecordingTickListWidget extends AlwaysSelectedEntryListWidget<RecordingTickListWidget.RecordingTickEntry> {
        public RecordingTickListWidget(MinecraftClient client) {
            super(client, EditorScreen.this.width, EditorScreen.this.height - 93, 32, 18);
            if (this.getSelectedOrNull() != null) {
                this.centerScrollOn(this.getSelectedOrNull());
            }

            this.updateEntries();
        }

        public void updateEntries() {
            this.clearEntries();
            int i = 0;
            for (Recording.RecordEntry entry : EditorScreen.this.record.entries) {
                this.addEntry(new RecordingTickEntry(i++, entry));
            }
        }



        public int getRowWidth() {
            return super.getRowWidth() + 50;
        }

        public class RecordingTickEntry extends AlwaysSelectedEntryListWidget.Entry<RecordingTickListWidget.RecordingTickEntry> {

            public int tickNumber;
            public Recording.RecordEntry entry;

            private long clickTime;

            public RecordingTickEntry(int tickNumber, Recording.RecordEntry entry) {
                this.tickNumber = tickNumber;
                this.entry = entry;
            }

            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.drawCenteredTextWithShadow(EditorScreen.this.textRenderer, this.tickNumber + "", RecordingTickListWidget.this.width / 2, y + 1, 16777215);
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                this.onPressed();
                if (Util.getMeasuringTimeMs() - this.clickTime < 250L) {
                    // Double screen
                    EditorScreen.this.onDone();
                }

                this.clickTime = Util.getMeasuringTimeMs();
                return true;
            }

            void onPressed() {
                RecordingTickListWidget.this.setSelected(this);
            }

            @Override
            public Text getNarration() {
                return Text.of(this.tickNumber + "");
            }
        }
    }
}

