package net.conczin.mca;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class KeyBindings {
    public static final List<KeyMapping> list = new LinkedList<>();

    // 1.21.11: KeyMapping now uses Category instead of String for category
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("mca", "binding"));

    public static final KeyMapping SKIN_LIBRARY = newKey("skin_library", GLFW.GLFW_KEY_U);

    private static KeyMapping newKey(String name, int code) {
        // 1.21.11: Constructor is (String name, int code, Category category)
        KeyMapping key = new KeyMapping("key.mca." + name, code, CATEGORY);
        list.add(key);
        return key;
    }
}
