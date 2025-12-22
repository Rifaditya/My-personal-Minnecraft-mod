package net.conczin.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.conczin.mca.MCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.data.skin.Hair;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class HairList extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    protected static final Identifier ID = MCA.locate("skins/hair");
    private static HairList INSTANCE;
    public final HashMap<String, Hair> hair = new HashMap<>();

    public HairList() {
        INSTANCE = this;
    }

    public static HairList getInstance() {
        return INSTANCE;
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> result = new HashMap<>();
        String directory = "skins/hair";
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
        hair.clear();

        prepared.forEach((id, file) -> {
            Gender gender = Gender.byName(id.getPath().split("\\.")[0]);

            if (gender == Gender.UNASSIGNED) {
                MCA.LOGGER.warn("Invalid gender for clothing pool: {}", id);
                return;
            }

            for (String key : file.getAsJsonObject().keySet()) {
                JsonObject object = file.getAsJsonObject().get(key).getAsJsonObject();

                for (int i = 0; i < GsonHelper.getAsInt(object, "count", 1); i++) {
                    String identifier = String.format(Locale.ROOT, key, i);

                    Hair c = new Hair(identifier, gender, GsonHelper.getAsFloat(object, "chance", 1.0f));

                    if (!hair.containsKey(identifier) || !object.has("count")) {
                        hair.put(identifier, c);
                    }
                }
            }
        });
    }

    public WeightedPool<String> getPool(Gender gender) {
        return hair.values().stream()
                .filter(c -> c.getGender() == Gender.NEUTRAL || gender == Gender.NEUTRAL || c.getGender() == gender)
                .collect(() -> new WeightedPool.Mutable<>("mca:missing"),
                        (list, entry) -> list.add(entry.getIdentifier(), entry.getChance()),
                        (a, b) -> {
                            a.entries.addAll(b.entries);
                        });
    }
}
