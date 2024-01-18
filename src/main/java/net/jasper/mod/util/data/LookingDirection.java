package net.jasper.mod.util.data;

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
                case NORTH -> "North";
                case SOUTH -> "South";
                case EAST -> "East";
                case WEST -> "West";
                case FLOOR -> "Floor";
                case SKY -> "Sky";
            };
        }

        public static Name fromString(String s) {
            return Name.valueOf(s.toUpperCase());
        }
    }
}
