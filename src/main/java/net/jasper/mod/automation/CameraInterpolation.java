package net.jasper.mod.automation;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.jasper.mod.PlayerAutomaClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class CameraInterpolation {

    private static float goalPitch;
    private static float goalYaw;

    private static float startPitch;
    private static float startYaw;

    private static int interpolationSteps;
    private static int currentInterpolationStep;

    private static boolean active = false;

    private static final int GAME_TICKS_PER_SECOND = 20;

    public static void enable() {
        active = true;
    }

    public static void disable() {
        active = false;
    }

    public static void update(float pitch, float yaw, int frameRate) {
        frameRate = Math.max(20, frameRate); // At least match game-ticks
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        goalPitch = pitch;
        goalYaw = yaw;

        startPitch = player.getPitch();
        startYaw = player.getYaw();

        interpolationSteps = frameRate / GAME_TICKS_PER_SECOND;
        currentInterpolationStep = 0;
    }

    /**
     *
     * @param start Start value
     * @param goal Goal value
     * @param step is between 0 and the static variable interpolationSteps
     * @return lerped value for given params
     */
    private static float lerpPitch(float start, float goal, int step) {
        // Ensure step is within the bounds
        if (step < 0) step = 0;
        if (step > interpolationSteps) step = interpolationSteps;

        // Calculate the interpolation factor (percentage)
        float t = (float) step / interpolationSteps;

        // Linear interpolation formula
        return start + t * (goal - start);
    }

    private static float lerpYaw(float start, float goal, int step) {
        // Ensure step is within the bounds
        if (step < 0) step = 0;
        if (step > interpolationSteps) step = interpolationSteps;

        // Calculate the interpolation factor (percentage)
        float t = (float) step / interpolationSteps;

        // Calculate the difference considering the wrap-around at 180/-180
        float delta = goal - start;

        // If delta is greater than 180, it means wrapping around the -180/180 boundary
        if (delta > 180) {
            delta -= 360;
        } else if (delta < -180) {
            delta += 360;
        }

        // Linear interpolation considering the circular yaw
        float result = start + t * delta;

        // Wrap result back into the range of -180 to 180
        if (result > 180) {
            result -= 360;
        } else if (result < -180) {
            result += 360;
        }

        return result;
    }


    public static void register() {
        WorldRenderEvents.START.register(context -> {
            if (!active) return;

            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null && PlayerRecorder.state.isReplaying()) {
                float newPitch = lerpPitch(startPitch, goalPitch, currentInterpolationStep);
                float newYaw = lerpYaw(startYaw, goalYaw, currentInterpolationStep);
                currentInterpolationStep++;
                client.player.setAngles(newYaw, newPitch);
                PlayerAutomaClient.LOGGER.info("(" + newPitch + ", " + newYaw + ")");
            }
        });
    }
}
