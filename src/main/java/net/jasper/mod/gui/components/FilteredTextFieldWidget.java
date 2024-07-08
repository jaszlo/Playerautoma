package net.jasper.mod.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
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
        this.renderErrorMessage(context, delta, false);
    }



    private void renderErrorMessage(DrawContext context, float tickDelta, boolean tinted) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (this.errorRemaining <= 0) {
            return;
        }

        float f = (float)this.errorRemaining - tickDelta;
        int i = (int)(f * 255.0f / 20.0f);
        if (i > 255) {
            i = 255;
        }

        if (i > 8) {
            context.getMatrices().push();
            context.getMatrices().translate(context.getScaledWindowWidth() / 2.0f, context.getScaledWindowHeight() - 68, 0.0f);
            int j = 0xFFFFFF;
            if (tinted) {
                j = MathHelper.hsvToRgb(f / 50.0f, 0.7f, 0.6f) & 0xFFFFFF;
            }
            int k = i << 24 & 0xFF000000;
            int l = textRenderer.getWidth(Text.of(this.errorMessageTranslationKey));
            //context.drawTextBackground(context, textRenderer, -4, l, 0xFFFFFF | k);
            context.drawTextWithShadow(textRenderer, Text.of(this.errorMessageTranslationKey), -l / 2, -4, j | k);
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
