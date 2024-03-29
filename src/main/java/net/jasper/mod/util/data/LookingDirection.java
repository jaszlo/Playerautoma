package net.jasper.mod.util.data;

import net.minecraft.text.Text;

import java.io.Serializable;

/**
 * Small Data-Class to store yaw and pitch of the player
 */
public record LookingDirection(float yaw, float pitch) implements Serializable {
    @Override
    public String toString() {
        return "LookingDirection{" + " yaw =" + yaw +  ", pitch = " + pitch + " }";
    }

    public enum Name {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        FLOOR,
        SKY;

        public LookingDirection getYawPitch() {
            return switch (this) {
                case NORTH -> new LookingDirection(180, 0);
                case SOUTH -> new LookingDirection(0, 0);
                case EAST -> new LookingDirection(270, 0);
                case WEST -> new LookingDirection(90, 0);
                case FLOOR -> new LookingDirection(0, 90);
                case SKY -> new LookingDirection(0, -90);
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case NORTH -> "north";
                case SOUTH -> "south";
                case EAST -> "east";
                case WEST -> "west";
                case FLOOR -> "floor";
                case SKY -> "sky";
            };
        }

        public static Name fromString(String s) {
            return Name.valueOf(s.toUpperCase());
        }

        public static Text toText(Name n) {
            return switch (n) {
                case NORTH -> Text.translatable("playerautoma.option.defaultDirection.north");
                case SOUTH -> Text.translatable("playerautoma.option.defaultDirection.south");
                case EAST -> Text.translatable("playerautoma.option.defaultDirection.east");
                case WEST -> Text.translatable("playerautoma.option.defaultDirection.west");
                case FLOOR -> Text.translatable("playerautoma.option.defaultDirection.floor");
                case SKY -> Text.translatable("playerautoma.option.defaultDirection.sky");
            };
        }
    }
}
