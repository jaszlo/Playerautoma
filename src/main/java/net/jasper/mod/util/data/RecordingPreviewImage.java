package net.jasper.mod.util.data;

import java.io.Serializable;

/**
 * Class to store the bytes of an image and information about the dimensions of the picture
 * @param width
 * @param height
 * @param bytes
 */
public record RecordingPreviewImage(int width, int height, byte[] bytes) implements Serializable {}
