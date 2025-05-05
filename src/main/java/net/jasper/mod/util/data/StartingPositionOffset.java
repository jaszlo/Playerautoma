package net.jasper.mod.util.data;

import net.minecraft.text.Text;

import java.io.Serializable;

/**
 * Small Data-Class to store a default position where a replay starts
 */
public record StartingPositionOffset(float x, float z) implements Serializable {

    public enum Name {
        TOP_LEFT,
        TOP_RIGHT,
        CENTER,
        BOTTOM_LEFT,
        BOTTOM_RIGHT;

        public StartingPositionOffset getOffset() {
            return switch (this) {
                case TOP_LEFT -> new StartingPositionOffset(0, 0);
                case TOP_RIGHT -> new StartingPositionOffset(1, 0);
                case CENTER -> new StartingPositionOffset(0.5f, 0.5f);
                case BOTTOM_LEFT -> new StartingPositionOffset(0, 1);
                case BOTTOM_RIGHT -> new StartingPositionOffset(1, 1);
            };
        }


        @Override
        public String toString() {
            return switch (this) {
                case TOP_LEFT -> "top_left";
                case TOP_RIGHT -> "top_right";
                case CENTER -> "center";
                case BOTTOM_LEFT -> "bottom_left";
                case BOTTOM_RIGHT -> "bottom_right";
            };
        }

        public static Name fromString(String s) {
            return Name.valueOf(s.toUpperCase());
        }

        public static Text toText(Name n) {
            return switch (n) {
                case TOP_LEFT -> Text.translatable("playerautoma.option.defaultPosition.topLeft");
                case TOP_RIGHT -> Text.translatable("playerautoma.option.defaultPosition.topRight");
                case CENTER -> Text.translatable("playerautoma.option.defaultPosition.center");
                case BOTTOM_LEFT -> Text.translatable("playerautoma.option.defaultPosition.bottomLeft");
                case BOTTOM_RIGHT -> Text.translatable("playerautoma.option.defaultPosition.bottomRight");
            };
        }
    }
}
