package net.conczin.mca.village.specialization;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.conczin.mca.MCA;
import net.conczin.mca.server.world.data.Village;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages profession spawn weights based on village specialization.
 * Mining villages spawn more miners/smiths, farming villages spawn more
 * farmers, etc.
 * 
 * ADDON-FRIENDLY: Data-driven via JSON configs.
 */
public class ProfessionWeighter {
    private static final Gson GSON = new Gson();
    private static final Map<VillageSpecialization, Map<VillagerProfession, Float>> WEIGHTS = new HashMap<>();
    private static final float DEFAULT_FARMER_MINIMUM = 0.20f; // 20% minimum farmers

    static {
        loadWeights(VillageSpecialization.MINING, "mining.json");
        loadWeights(VillageSpecialization.FARMING, "farming.json");
        loadWeights(VillageSpecialization.TRADING, "trading.json");
    }

    private static void loadWeights(VillageSpecialization spec, String filename) {
        try {
            InputStream stream = ProfessionWeighter.class.getResourceAsStream(
                    "/data/mca/village_specializations/" + filename);
            if (stream != null) {
                JsonObject root = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                JsonObject config = root.getAsJsonObject(spec.getId());

                if (config != null && config.has("profession_weights")) {
                    Map<VillagerProfession, Float> weights = new HashMap<>();
                    JsonObject profWeights = config.getAsJsonObject("profession_weights");

                    for (Map.Entry<String, JsonElement> entry : profWeights.entrySet()) {
                        ResourceLocation profId = ResourceLocation.parse(entry.getKey());
                        VillagerProfession profession = BuiltInRegistries.VILLAGER_PROFESSION.get(profId);
                        if (profession != null) {
                            weights.put(profession, entry.getValue().getAsFloat());
                        }
                    }

                    WEIGHTS.put(spec, weights);
                }
            }
        } catch (Exception e) {
            MCA.LOGGER.error("Failed to load profession weights: " + filename, e);
        }
    }

    /**
     * Get the spawn weight for a profession in this village.
     * Higher weight = more likely to spawn.
     */
    public static float getWeight(Village village, VillagerProfession profession, ServerLevel world) {
        VillageSpecialization spec = village.getSpecialization();

        // Ensure minimum farmers
        if (profession == VillagerProfession.FARMER) {
            float farmerPercent = getFarmerPercentage(village, world);
            if (farmerPercent < DEFAULT_FARMER_MINIMUM) {
                return 5.0f; // High priority to reach minimum
            }
        }

        // Check specialization weights
        if (spec != VillageSpecialization.NONE && WEIGHTS.containsKey(spec)) {
            return WEIGHTS.get(spec).getOrDefault(profession, 1.0f);
        }

        return 1.0f; // Default weight
    }

    private static float getFarmerPercentage(Village village, ServerLevel world) {
        if (village == null || world == null) {
            return 0.15f; // Fallback when no context available
        }

        int totalVillagers = village.getPopulation();
        if (totalVillagers == 0) {
            return 0.0f;
        }

        // Count farmers among loaded villagers
        long farmerCount = village.getResidents(world).stream()
                .filter(v -> v.getVillagerData().getProfession() == VillagerProfession.FARMER)
                .count();

        return (float) farmerCount / totalVillagers;
    }
}
