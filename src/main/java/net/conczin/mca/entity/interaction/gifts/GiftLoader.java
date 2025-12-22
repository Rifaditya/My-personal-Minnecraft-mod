package net.conczin.mca.entity.interaction.gifts;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.conczin.mca.MCA;
import net.conczin.mca.resources.Resources;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class GiftLoader extends SimpleJsonResourceReloadListener {
    protected static final Identifier ID = MCA.locate("gifts");

    public GiftLoader() {
        // TODO: In 1.21.11, SimpleJsonResourceReloadListener takes Codec not Gson
        // super(Resources.GSON, "gifts");
        super("gifts");
    }

    // In 1.21.11, SimplePreparableReloadListener.apply() signature changed to
    // Object
    @Override
    @SuppressWarnings("unchecked")
    protected void apply(Object prepared, ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> data = (Map<Identifier, JsonElement>) prepared;
        GiftType.REGISTRY.clear();
        data.forEach((id, json) -> {
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
