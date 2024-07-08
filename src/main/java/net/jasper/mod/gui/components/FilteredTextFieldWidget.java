package net.jasper.mod.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class FilteredTextFieldWidget extends TextFieldWidget {

    public interface Filter {
        boolean accept(String text);
    }



    public Filter filter;
    public String errorMessageTranslationKey;
    private long errorRemaining = 0;


    public FilteredTextFieldWidget(TextRenderer textRenderer, int width, int height, int x, int y, Text text, String errorMessageTranslationKey, Filter filter) {
        super(textRenderer, width, height, x, y, text);
        this.filter = filter;
        this.errorMessageTranslationKey = errorMessageTranslationKey;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        this.errorRemaining--;
        this.renderErrorMessage(context, RenderTickCounter.ONE, false);
    }

    private void renderErrorMessage(DrawContext context, RenderTickCounter tickCounter, boolean tinted) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (this.errorRemaining <= 0) {
            return;
        }

        float f = (float) this.errorRemaining - tickCounter.getTickDelta(false);
        int i = (int) (f * 255.0f / 20.0f);
        if (i > 255) {
            i = 255;
        }
        if (i > 8) {
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2.0f, context.getScaledWindowHeight() - 68, 0.0f);
            int j = tinted ? MathHelper.hsvToArgb(f / 50.0f, 0.7f, 0.6f, i) : ColorHelper.Argb.withAlpha(i, -1);
            int k = textRenderer.getWidth(Text.translatable(this.errorMessageTranslationKey));
            context.drawTextWithBackground(textRenderer, Text.translatable(this.errorMessageTranslationKey), -k / 2, -4, k, j);
            context.getMatrices().pop();
        }
    }



    @Override
    public void write(String text) {
        String loweredText = text.toLowerCase();
        if (this.filter.accept(loweredText)) {
            super.write(loweredText);
        } else {
            errorRemaining = 256;
        }
    }
}
