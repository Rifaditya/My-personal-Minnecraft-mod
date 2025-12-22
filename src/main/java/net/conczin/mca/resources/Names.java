package net.conczin.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.conczin.mca.Config;
import net.conczin.mca.MCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.server.world.data.Nationality;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.*;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class Names extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    public static final Map<String, Map<Gender, WeightedPool<String>>> NAMES_MAP = new HashMap<>();
    public static final List<String> REGION_NAMES = new LinkedList<>();
    protected static final Identifier ID = MCA.locate("mca_names");
    static final RandomSource random = RandomSource.create();

    public Names() {
    }

    public static String getCitizenNation(Entity entity) {
        if (Config.getInstance().useModernUSANamesOnly) {
            return "modernusa";
        } else {
            int i = Nationality.get((ServerLevel) entity.level()).getRegionId(entity.blockPosition());
            return REGION_NAMES.get(Math.floorMod(i, REGION_NAMES.size()));
        }
    }

    public static String pickCitizenName(@NotNull Gender gender, Entity entity) {
        return NAMES_MAP.isEmpty() ? "Unnamed" : NAMES_MAP.get(getCitizenNation(entity)).get(gender.binary()).pickOne();
    }

    public static String pickCitizenName(@NotNull Gender gender) {
        return NAMES_MAP.isEmpty() ? "Unnamed"
                : NAMES_MAP.get(REGION_NAMES.get(random.nextInt(REGION_NAMES.size()))).get(gender.binary()).pickOne();
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
        NAMES_MAP.clear();
        for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
            String path = entry.getKey().getPath();
            // Remove directory and .json extension
            if (path.contains("/")) {
                path = path.substring(path.indexOf('/') + 1);
            }
            if (path.endsWith(".json")) {
                path = path.substring(0, path.length() - 5);
            }
            String[] split = path.split("/");
            if (split.length < 2)
                continue;

            Gender gender = Gender.byName(split[1]);

            Map<Gender, WeightedPool<String>> map = NAMES_MAP.computeIfAbsent(split[0], a -> new HashMap<>());

            WeightedPool.Mutable<String> names = new WeightedPool.Mutable<>("?");
            for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
                names.add(elementEntry.getKey(), (float) Math.pow(elementEntry.getValue().getAsInt(), 0.5));
            }

            map.put(gender, names);
        }

        REGION_NAMES.clear();
        Arrays.stream(NAMES_MAP.keySet().toArray()).sorted().forEach(n -> REGION_NAMES.add((String) n));
    }
}
