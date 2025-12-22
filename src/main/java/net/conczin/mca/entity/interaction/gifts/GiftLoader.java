package net.conczin.mca.entity.interaction.gifts;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.conczin.mca.MCA;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class GiftLoader extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    protected static final Identifier ID = MCA.locate("gifts");

    public GiftLoader() {
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> result = new HashMap<>();
        String directory = "gifts";
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
        GiftType.REGISTRY.clear();
        prepared.forEach((id, json) -> {
            try {
                GiftType.REGISTRY.add(GiftType.fromJson(id, GsonHelper.convertToJsonObject(json, "root")));
            } catch (JsonParseException e) {
                MCA.LOGGER.error("Could not load gift type for id {}", id, e);
            }
        });

        // extend from mca entries to avoid copy pasta commonly used stuff
        for (GiftType type : GiftType.REGISTRY) {
            if (!type.getId().getNamespace().equals(MCA.MOD_ID) && type.getConditions().isEmpty()) {
                for (GiftType extendingType : GiftType.REGISTRY) {
                    if (extendingType.getId().getNamespace().equals(MCA.MOD_ID)
                            && extendingType.getId().getPath().equals(type.getId().getPath())) {
                        type.extendFrom(extendingType);
                        break;
                    }
                }
            }
        }
    }
}
