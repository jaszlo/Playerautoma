package net.jasper.mod.gui.components;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

// TODO: Use this for QuickMenu in the future, also finish implementation?
public class DoubleButtonWidget extends ButtonWidget {
    private static final NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableText)textSupplier.get();
    private PressAction onLeftClick;
    private PressAction onRightClick;

    protected DoubleButtonWidget(int x, int y, int width, int height, Text message, PressAction onLeftClick, PressAction onRightClick) {
        super(x, y, width, height, message, (b) -> {}, DEFAULT_NARRATION_SUPPLIER);
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) {
            return false;
        }


        if (this.isValidClickButton(button)) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(mouseX, mouseY);
            return true;
        }
        return false;
    }


    public static class Builder {
        private final Text message;
        private PressAction onLeftClick;
        private PressAction onRightClick;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;

        public Builder(Text message, PressAction onLeftPress, PressAction onRightClick) {
            this.message = message;
            this.onLeftClick = onLeftPress;
            this.onRightClick = onRightClick;

        }

        public DoubleButtonWidget.Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public DoubleButtonWidget.Builder width(int width) {
            this.width = width;
            return this;
        }

        public DoubleButtonWidget.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public DoubleButtonWidget.Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public DoubleButtonWidget.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public DoubleButtonWidget.Builder narrationSupplier(NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public DoubleButtonWidget build() {
            DoubleButtonWidget buttonWidget = new DoubleButtonWidget(this.x, this.y, this.width, this.height, this.message, this.onLeftClick, this.onRightClick);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }
}
