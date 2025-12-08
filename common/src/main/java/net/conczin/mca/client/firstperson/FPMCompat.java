package net.conczin.mca.client.firstperson;

/**
 * ====================================================================
 * FIRSTPERSON MODEL COMPATIBILITY
 * ====================================================================
 * This utility detects if FirstPersonModel by tr7zw is installed and
 * provides compatibility checks.
 * 
 * When FPM is installed, MCA defers first-person rendering control to
 * FPM, allowing users to use FPM's GUI and advanced features.
 * 
 * When FPM is NOT installed, MCA uses built-in FirstPersonLogic for
 * basic first-person features.
 * ====================================================================
 * 
 * @author MCA Team
 */
public class FPMCompat {

    private static Boolean fpmInstalled = null;
    private static Boolean fpmEnabled = null;
    private static Object fpmApiInstance = null;

    /**
     * Detects if FirstPersonModel mod is installed.
     * Uses class loading to check for FPM's presence.
     * 
     * @return true if FPM is installed
     */
    public static boolean isFPMInstalled() {
        if (fpmInstalled == null) {
            try {
                Class.forName("dev.tr7zw.firstperson.FirstPersonModelCore");
                fpmInstalled = true;
                System.out.println("MCA: FirstPersonModel detected! Using FPM for first-person rendering.");
            } catch (ClassNotFoundException e) {
                fpmInstalled = false;
                System.out.println("MCA: FirstPersonModel not found. Using built-in first-person features.");
            }
        }
        return fpmInstalled;
    }

    /**
     * Check if FPM is currently enabled (if installed).
     * 
     * @return true if FPM is installed AND enabled
     */
    public static boolean isFPMActive() {
        if (!isFPMInstalled()) {
            return false;
        }

        try {
            // Use reflection to call FirstPersonAPI.isEnabled()
            Class<?> apiClass = Class.forName("dev.tr7zw.firstperson.api.FirstPersonAPI");
            java.lang.reflect.Method isEnabledMethod = apiClass.getMethod("isEnabled");
            Boolean enabled = (Boolean) isEnabledMethod.invoke(null);
            return enabled != null && enabled;
        } catch (Exception e) {
            // If we can't check, assume it's active if installed
            return true;
        }
    }

    /**
     * Check if FPM is currently rendering the player.
     * This is the key check - when true, MCA should NOT apply its own first-person
     * logic.
     * 
     * @return true if FPM is actively rendering first-person player
     */
    public static boolean isFPMRenderingPlayer() {
        if (!isFPMInstalled()) {
            return false;
        }

        try {
            // Use reflection to call FirstPersonAPI.isRenderingPlayer()
            Class<?> apiClass = Class.forName("dev.tr7zw.firstperson.api.FirstPersonAPI");
            java.lang.reflect.Method isRenderingMethod = apiClass.getMethod("isRenderingPlayer");
            Boolean rendering = (Boolean) isRenderingMethod.invoke(null);
            return rendering != null && rendering;
        } catch (Exception e) {
            // If we can't check, assume false (let MCA handle it)
            return false;
        }
    }

    /**
     * Should MCA use its built-in first-person logic?
     * Returns true only if FPM is NOT installed or NOT rendering.
     * 
     * @return true if MCA should apply built-in first-person features
     */
    public static boolean shouldUseBuiltInFPM() {
        return !isFPMInstalled() || !isFPMActive();
    }
}
