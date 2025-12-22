package net.conczin.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.conczin.mca.MCA;
import net.conczin.mca.resources.data.BuildingType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class BuildingTypes extends SimplePreparableReloadListener<Map<Identifier, JsonElement>>
        implements Iterable<BuildingType> {
    protected static final Identifier ID = MCA.locate("building_types");
    private static BuildingTypes INSTANCE = new BuildingTypes();
    private final Map<String, BuildingType> buildingTypes = new HashMap<>();
    private final Map<String, BuildingType> buildingTypesClient = new HashMap<>();

    public BuildingTypes() {
        INSTANCE = this;
    }

    public static BuildingTypes getInstance() {
        return INSTANCE;
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> result = new HashMap<>();
        String directory = ID.getPath();
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
        buildingTypes.clear();
        for (Map.Entry<Identifier, JsonElement> pair : prepared.entrySet()) {
            String name = pair.getKey().getPath();
            // Remove directory prefix and .json extension
            if (name.contains("/")) {
                name = name.substring(name.lastIndexOf('/') + 1);
            }
            if (name.endsWith(".json")) {
                name = name.substring(0, name.length() - 5);
            }
            buildingTypes.put(name, new BuildingType(name, pair.getValue().getAsJsonObject()));
        }
        setBuildingTypes(buildingTypes);
    }

    public Map<String, BuildingType> getServerBuildingTypes() {
        return buildingTypes;
    }

    public Map<String, BuildingType> getBuildingTypes() {
        return buildingTypesClient;
    }

    // Provide the client with building types
    public void setBuildingTypes(Map<String, BuildingType> buildingTypes) {
        buildingTypesClient.clear();
        buildingTypesClient.putAll(buildingTypes);
    }

    public BuildingType getBuildingType(String type) {
        return buildingTypesClient.containsKey(type) ? buildingTypesClient.get(type) : new BuildingType();
    }

    @Override
    public Iterator<BuildingType> iterator() {
        return buildingTypesClient.values().iterator();
    }
}
