package net.jasper.mod.gui.components;

import lombok.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

@Getter
@Setter
// TO-DO: Use this for QuickMenu in the future, also finish implementation
@Builder
public class DoubleButtonWidget extends ButtonWidget {
    private static final NarrationSupplier DEFAULT_NARRATION_SUPPLIER = Supplier::get;
    private final PressAction onLeftClick;
    private final PressAction onRightClick;

    public DoubleButtonWidget(int x, int y, int width, int height, Text message, PressAction onLeftClick, PressAction onRightClick) {
        super(x, y, width, height, message, b -> {}, DEFAULT_NARRATION_SUPPLIER);
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
    }

    public DoubleButtonWidget(PressAction onLeftClick, PressAction onRightClick) {
        super(0, 0, 0, 0, Text.of(""), b -> {}, DEFAULT_NARRATION_SUPPLIER);
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (!this.active || !this.visible) {
            return false;
        }


        if (this.isValidClickButton(click.buttonInfo())) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            this.onClick(click, false);
            return true;
        }
        return false;
    }
}
