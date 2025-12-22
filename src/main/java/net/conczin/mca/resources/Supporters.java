package net.conczin.mca.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.conczin.mca.MCA;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

public class Supporters extends SimpleJsonResourceReloadListener {
    protected static final Identifier ID = MCA.locate("api/supporters");
    static final RandomSource rng = RandomSource.create();
    private static Supporters INSTANCE;
    private final List<String> supporters = new ArrayList<>();
    private final Map<String, List<String>> supporterGroups = new HashMap<>();

    public Supporters() {
        // TODO: In 1.21.11, SimpleJsonResourceReloadListener takes Codec not Gson
        // super(Resources.GSON, ID.getPath());
        super(ID.getPath());
        INSTANCE = this;
    }

    public Supporters(com.google.gson.Gson gson, String dataType) {
        // TODO: In 1.21.11, SimpleJsonResourceReloadListener takes Codec not Gson
        // super(gson, dataType);
        super(dataType);
    }

    public static String getRandomSupporter() {
        return INSTANCE.pickSupporter();
    }

    public static List<String> getSupporterGroup(String group) {
        return INSTANCE.supporterGroups.getOrDefault(group, new LinkedList<>());
    }

    // In 1.21.11, SimplePreparableReloadListener.apply() signature changed to
    // Object
    @Override
    @SuppressWarnings("unchecked")
    protected void apply(Object prepared, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> preparedMap = (Map<Identifier, JsonElement>) prepared;
        for (Map.Entry<Identifier, JsonElement> pair : preparedMap.entrySet()) {
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
