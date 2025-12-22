package net.conczin.mca.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.conczin.mca.MCA;
import net.conczin.mca.client.resources.Icon;
import net.conczin.mca.resources.Resources;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class MCAScreens extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    protected static final Identifier ID = MCA.locate("screens");
    private static final Type ICONS_TYPE = new TypeToken<Map<String, Icon>>() {
    }.getType();

    private static MCAScreens INSTANCE;
    private final Map<String, MCAButton[]> buttons = new HashMap<>();
    private final Map<String, Icon> icons = new HashMap<>();

    public MCAScreens() {
        INSTANCE = this;
    }

    public static MCAScreens getInstance() {
        return INSTANCE;
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> result = new HashMap<>();
        String directory = "api/gui";
        for (Identifier id : manager.listResources(directory, path -> path.getPath().endsWith(".json")).keySet()) {
            try (var reader = new InputStreamReader(manager.getResource(id).orElseThrow().open())) {
                result.put(id, JsonParser.parseReader(reader));
            } catch (Exception e) {
                MCA.LOGGER.error("Failed to load JSON resource {}", id, e);
            }
        }
        return result;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        buttons.clear();
        icons.clear();
        prepared.forEach(this::loadScreen);
    }

    private void loadScreen(Identifier id, JsonElement element) {
        if (element.isJsonObject()) {
            icons.putAll(Resources.GSON.fromJson(element, ICONS_TYPE));
        } else {
            buttons.put(id.getPath(), Resources.GSON.fromJson(element, MCAButton[].class));
        }
    }

    /**
     * Returns an API icon based on its key
     *
     * @param key String key of icon
     * @return Instance of APIIcon matching the ID provided
     */
    public Icon getIcon(String key) {
        return icons.getOrDefault(key, Icon.EMPTY);
    }

    /**
     * Gets all of the buttons for a particular screen.
     *
     * @param guiKey String key for the GUI's buttons
     */
    public Optional<MCAButton[]> getScreen(String guiKey) {
        return Optional.ofNullable(buttons.get(guiKey));
    }

    /**
     * Returns an API button based on its ID
     *
     * @param id String id matching the targeted button
     * @return Instance of APIButton matching the ID provided
     */
    public Optional<MCAButton> getButton(String key, String id) {
        return Arrays.stream(buttons.get(key)).filter(b -> b.identifier().equals(id)).findFirst();
    }
}
