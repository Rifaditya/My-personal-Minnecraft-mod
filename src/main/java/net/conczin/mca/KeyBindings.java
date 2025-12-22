package net.conczin.mca;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();

    // TODO: In 1.21.11, KeyMapping constructor changed - disabled temporarily
    // public static final KeyMapping SKIN_LIBRARY = newKey("skin_library",
    // GLFW.GLFW_KEY_U);
    public static final KeyMapping SKIN_LIBRARY = null;

    /*
     * private static KeyMapping newKey(String name, int code) {
     * KeyMapping key = new KeyMapping(
     * "key.mca." + name,
     * InputConstants.Type.KEYSYM,
     * code,
     * "itemGroup.mca.mca_tab"
     * );
     * list.add(key);
     * return key;
     * }
     */
}
