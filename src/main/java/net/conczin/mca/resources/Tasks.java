package net.conczin.mca.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.conczin.mca.MCA;
import net.conczin.mca.resources.data.tasks.Task;
import net.conczin.mca.resources.data.tasks.TaskRegistry;
import net.conczin.mca.server.world.data.Village;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

// Changed from SimpleJsonResourceReloadListener to SimplePreparableReloadListener for 1.21.11 compatibility
public class Tasks extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {
    protected static final Identifier ID = MCA.locate("tasks");

    private static Tasks INSTANCE;
    public final Map<Rank, List<Task>> tasks = new HashMap<>();

    public Tasks() {
        INSTANCE = this;
    }

    public static Tasks getInstance() {
        return INSTANCE;
    }

    public static Set<String> getCompletedIds(Village village, ServerPlayer player) {
        return getInstance().tasks.values().stream().flatMap(Collection::stream)
                .filter(t -> t.isCompleted(village, player)).map(Task::getId).collect(Collectors.toSet());
    }

    public static Rank getRank(Village village, ServerPlayer player) {
        Rank[] ranks = Rank.values();
        for (int i = ranks.length - 1; i >= 0; i--) {
            if (getInstance().tasks.get(ranks[i]).stream()
                    .allMatch(t -> !t.isRequired() || t.isCompleted(village, player))) {
                return ranks[i];
            }
        }
        return Rank.OUTLAW;
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
        tasks.clear();
        for (Rank r : Rank.values()) {
            tasks.put(r, new LinkedList<>());
        }

        prepared.forEach((id, file) -> {
            String path = id.getPath();
            if (path.contains("/")) {
                path = path.substring(path.lastIndexOf('/') + 1);
            }
            if (path.endsWith(".json")) {
                path = path.substring(0, path.length() - 5);
            }
            Rank rank = Rank.fromName(path.split("\\.")[0]);
            file.getAsJsonArray().forEach(entry -> {
                Task task = TaskRegistry.fromJson(entry.getAsJsonObject());
                tasks.get(rank).add(task);
            });
        });
    }
}
