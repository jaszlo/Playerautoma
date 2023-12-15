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
}
