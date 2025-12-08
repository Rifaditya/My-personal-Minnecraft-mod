package net.conczin.mca.client.firstperson;

import net.conczin.mca.Config;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

/**
 * Logic handler for first-person model visibility features.
 * Integrated from FirstPersonModel mod to prevent conflicts with MCA rendering.
 * 
 * @author MCA Team (integrated from tr7zw's FirstPersonModel)
 */
public class FirstPersonLogic {

    /**
     * Checks if first-person mode features are currently active.
     * 
     * @return true if in first person and FPM is enabled
     */
    public static boolean isFirstPersonModeActive() {
        Config.FirstPersonMode config = Config.getInstance().firstPersonMode;
        if (!config.enabled) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        return mc.options.getCameraType() == CameraType.FIRST_PERSON;
    }

    /**
     * Checks if the player head should be hidden.
     * 
     * @return true if head should be hidden in first person
     */
    public static boolean shouldHideHead() {
        if (!isFirstPersonModeActive()) {
            return false;
        }

        return Config.getInstance().firstPersonMode.hideHeadInFirstPerson;
    }

    /**
     * Checks if the body should be hidden (swimming/crawling).
     * 
     * @param entity The living entity to check
     * @return true if body should be hidden
     */
    public static boolean shouldHideBody(LivingEntity entity) {
        if (!isFirstPersonModeActive()) {
            return false;
        }

        Config.FirstPersonMode config = Config.getInstance().firstPersonMode;
        if (!config.hideBodyWhenSwimming) {
            return false;
        }

        // Hide body when swimming or crawling (1.14+)
        return entity.isSwimming() || entity.isFallFlying();
    }

    /**
     * Checks if arms should be hidden based on held items and dynamic hands
     * setting.
     * 
     * @param entity The living entity to check
     * @return true if arms should be hidden
     */
    public static boolean shouldHideArms(LivingEntity entity) {
        if (!isFirstPersonModeActive()) {
            return false;
        }

        Config.FirstPersonMode config = Config.getInstance().firstPersonMode;

        // If dynamic hands is enabled, check if looking down
        if (config.dynamicHands) {
            float pitch = entity.getXRot();
            // Only show arms when looking down past threshold
            return pitch < config.dynamicHandsPitchThreshold;
        }

        // Default: hide arms in first person
        return true;
    }

    /**
     * Calculates the dynamic arm offset for the "looking down" effect.
     * 
     * @param entity The living entity
     * @return arm rotation offset in radians, or 0 if not applicable
     */
    public static float getDynamicArmOffset(LivingEntity entity) {
        if (!isFirstPersonModeActive()) {
            return 0.0f;
        }

        Config.FirstPersonMode config = Config.getInstance().firstPersonMode;
        if (!config.dynamicHands) {
            return 0.0f;
        }

        float pitch = entity.getXRot();
        if (pitch < config.dynamicHandsPitchThreshold) {
            return 0.0f;
        }

        // Calculate offset based on how far past threshold we are
        float excessPitch = pitch - config.dynamicHandsPitchThreshold;
        float normalizedOffset = Math.min(excessPitch / 50.0f, 1.0f); // 50 degrees range

        return normalizedOffset * config.dynamicHandsMaxOffset;
    }

    /**
     * Quick check: is the entity in first person view?
     * 
     * @param entity The entity to check
     * @return true if this is the camera entity in first person
     */
    public static boolean isCameraEntityFirstPerson(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        return mc.options.getCameraType() == CameraType.FIRST_PERSON
                && mc.getCameraEntity() == entity;
    }
}
