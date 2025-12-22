package net.conczin.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.conczin.mca.MCA;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.util.*;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class Supporters extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    protected static final Identifier ID = MCA.locate("api/supporters");
    static final RandomSource rng = RandomSource.create();
    private static Supporters INSTANCE;
    private final List<String> supporters = new ArrayList<>();
    private final Map<String, List<String>> supporterGroups = new HashMap<>();

    public Supporters() {
        INSTANCE = this;
    }

    public static String getRandomSupporter() {
        return INSTANCE.pickSupporter();
    }

    public static List<String> getSupporterGroup(String group) {
        return INSTANCE.supporterGroups.getOrDefault(group, new LinkedList<>());
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
        supporters.clear();
        supporterGroups.clear();
        for (Map.Entry<Identifier, JsonElement> pair : prepared.entrySet()) {
            List<String> strings = supporterGroups.computeIfAbsent(pair.getKey().toString(), x -> new LinkedList<>());
            for (JsonElement e : pair.getValue().getAsJsonArray()) {
                supporters.add(e.getAsString());
                strings.add(e.getAsString());
            }
        }
    }

    public String pickSupporter() {
        return PoolUtil.pickOne(supporters, "nobody", rng);
    }
}
