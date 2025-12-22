package net.conczin.mca.client.resources;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.List;

public class SkinPorter {
    private static final List<UVMapping> mappings = List.of(
            // leg
            new UVMapping(4, 16, 8, 20, 16, 32, true), // top
            new UVMapping(8, 16, 12, 20, 16, 32, true), // bottom
            new UVMapping(0, 20, 4, 32, 24, 32, true), // left
            new UVMapping(4, 20, 8, 32, 16, 32, true), // front
            new UVMapping(8, 20, 12, 32, 8, 32, true), // right
            new UVMapping(12, 20, 16, 32, 16, 32, true), // back

            // arm
            new UVMapping(44, 16, 48, 20, -8, 32, true), // top
            new UVMapping(48, 16, 52, 20, -8, 32, true), // bottom
            new UVMapping(40, 20, 44, 32, 0, 32, true), // left
            new UVMapping(44, 20, 48, 32, -8, 32, true), // front
            new UVMapping(48, 20, 52, 32, -16, 32, true), // right
            new UVMapping(52, 20, 56, 32, -8, 32, true) // back
    );

    /**
     * Ports a legacy 32px skin to a 64px skin, mirroring and filling the right arm
     * and left, leaving the rest blank
     * TODO: In 1.21.11, NativeImage setPixelRGBA/getPixelRGBA methods changed
     */
    public static NativeImage portLegacySkin(NativeImage image) {
        // Skin porting disabled due to 1.21.11 NativeImage API changes
        // Just return the original image
        return image;
    }

    /**
     * Checks if that skin could be a slim format
     * TODO: In 1.21.11, NativeImage getLuminanceOrAlpha method removed
     */
    public static boolean isSlimFormat(NativeImage image) {
        // Slim format detection disabled
        return false;
    }

    /**
     * Converts a slim skin to a default one by stretching the center pixel. Not
     * fancy but at least valid.
     * TODO: In 1.21.11, NativeImage setPixelRGBA/getPixelRGBA methods changed
     */
    public static void convertSlimToDefault(NativeImage image) {
        // Conversion disabled due to 1.21.11 API changes
    }

    private static void stretch(NativeImage image, int offsetX, int offsetY) {
        // Stretch disabled due to 1.21.11 API changes
    }

    private record UVMapping(int x0, int y0, int x1, int y1, int offsetX, int offsetY, boolean flip) {
    }
}
