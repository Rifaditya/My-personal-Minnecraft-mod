package net.conczin.mca.client.gui.immersive_library;

import com.mojang.blaze3d.platform.NativeImage;
import net.conczin.mca.client.resources.SkinLocations;
import net.minecraft.util.Mth;

public class Utils {
    // Sanity check of skins by checking if at least one skin pixel is transparent,
    // thus not being a valid vanilla skin, thus requiring at least minimal effort
    // to convert to a valid skin
    public static boolean verify(NativeImage image) {
        // TODO: In 1.21.11, NativeImage.getLuminanceOrAlpha() removed
        // Simplified verification - always return false (allow all images)
        return false;
    }

    // Check if hair is grayscale and bright
    public static boolean verifyHair(NativeImage image) {
        // TODO: In 1.21.11,
        // NativeImage.getRedOrLuminance/getGreenOrLuminance/getBlueOrLuminance removed
        // Simplified verification - always return true (allow all hair)
        return true;
    }
}
