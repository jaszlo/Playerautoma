package net.jasper.mod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jasper.mod.util.data.Recording;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class RecordingEditor extends Screen {

    public static final RecordingEditor SINGLETON = new RecordingEditor("Recording Editor");
    private static boolean isOpen = false;
    protected RecordingEditor(String title) {
        super(Text.of(title));
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 16777215);
    }

    public static void open() {
        if (!isOpen) {
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



    public static class DrawableRecord {

        private final Recording record;
        List<DrawableRecordEntry> entries = new ArrayList<>();
        public DrawableRecord(Recording record) {
            this.record = record;
            for (Recording.RecordEntry entry : record.entries) {
                this.entries.add(new DrawableRecordEntry(entry));
            }
        }
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            for (int i = 0; i < 5; i++) {
                DrawableRecordEntry entry = this.entries.get(i);
                context.drawHorizontalLine(RenderLayer.getLines(), 0, 500, i * 10 + 10, 0xFFFFFFFF);
                entry.render(context, i, 0, 0, 0, 0, mouseX, mouseY, false, delta);
            }
        }

    }

    public static class DrawableRecordEntry {

        private Recording.RecordEntry entry;
        public DrawableRecordEntry(Recording.RecordEntry entry) {
            this.entry = entry;
        }

        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, this.entry.toString(), 0, 0, 16777215);
        }

    }

}
