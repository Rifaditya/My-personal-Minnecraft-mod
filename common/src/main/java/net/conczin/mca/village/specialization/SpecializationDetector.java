package net.conczin.mca.village.specialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.conczin.mca.MCA;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.server.world.data.Village;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Detects and assigns village specializations based on villager professions.
 * 
 * ADDON-FRIENDLY: Uses data-driven configs, can be extracted to separate mod.
 */
public class SpecializationDetector {
    private static final Gson GSON = new Gson();
    private static final Map<VillageSpecialization, SpecializationConfig> CONFIGS = new HashMap<>();

    static {
        // Load configs from resources
        loadConfig(VillageSpecialization.MINING, "mining.json");
        loadConfig(VillageSpecialization.FARMING, "farming.json");
        loadConfig(VillageSpecialization.TRADING, "trading.json");
    }

    private static void loadConfig(VillageSpecialization spec, String filename) {
        try {
            InputStream stream = SpecializationDetector.class.getResourceAsStream(
                    "/data/mca/village_specializations/" + filename);
            if (stream != null) {
                JsonObject root = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                JsonObject config = root.getAsJsonObject(spec.getId());
                CONFIGS.put(spec, new SpecializationConfig(config));
            }
        } catch (Exception e) {
            MCA.LOGGER.error("Failed to load village specialization config: " + filename, e);
        }
    }

    /**
     * Detect and assign specialization to a village based on villager professions.
     */
    public static VillageSpecialization detectSpecialization(Village village, ServerLevel world) {
        if (!village.isVillage()) {
            return VillageSpecialization.NONE;
        }

        // Count professions by category
        Map<VillageSpecialization, Integer> professionCounts = countProfessions(village, world);

        // Calculate scores for each specialization
        Map<VillageSpecialization, Integer> scores = new HashMap<>();

        for (VillageSpecialization spec : VillageSpecialization.values()) {
            if (spec == VillageSpecialization.NONE)
                continue;

            SpecializationConfig config = CONFIGS.get(spec);
            if (config == null)
                continue;

            int count = professionCounts.getOrDefault(spec, 0);
            if (count >= config.threshold) {
                scores.put(spec, count);
            }
        }

        // Return highest scoring specialization
        return scores.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(VillageSpecialization.NONE);
    }

    /**
     * Count villagers by profession category.
     */
    private static Map<VillageSpecialization, Integer> countProfessions(Village village, ServerLevel world) {
        Map<VillageSpecialization, Integer> counts = new HashMap<>();
        counts.put(VillageSpecialization.FARMING, 0);
        counts.put(VillageSpecialization.MINING, 0);
        counts.put(VillageSpecialization.TRADING, 0);

        for (VillagerEntityMCA villager : village.getResidents(world)) {
            VillagerProfession prof = villager.getVillagerData().getProfession();

            // Categorize profession
            if (isFarmingProfession(prof)) {
                counts.merge(VillageSpecialization.FARMING, 1, Integer::sum);
            } else if (isMiningProfession(prof)) {
                counts.merge(VillageSpecialization.MINING, 1, Integer::sum);
            } else if (isTradingProfession(prof)) {
                counts.merge(VillageSpecialization.TRADING, 1, Integer::sum);
            }
        }

        return counts;
    }

    private static boolean isFarmingProfession(VillagerProfession prof) {
        return prof == VillagerProfession.FARMER ||
                prof == VillagerProfession.SHEPHERD ||
                prof == VillagerProfession.BUTCHER ||
                prof == VillagerProfession.FISHERMAN;
    }

    private static boolean isMiningProfession(VillagerProfession prof) {
        return prof == VillagerProfession.WEAPONSMITH ||
                prof == VillagerProfession.ARMORER ||
                prof == VillagerProfession.TOOLSMITH;
    }

    private static boolean isTradingProfession(VillagerProfession prof) {
        return prof == VillagerProfession.LIBRARIAN ||
                prof == VillagerProfession.CARTOGRAPHER ||
                prof == VillagerProfession.CLERIC;
    }

    /**
     * Configuration data for a specialization type.
     */
    private static class SpecializationConfig {
        final int threshold;

        SpecializationConfig(JsonObject json) {
            // Threshold is minimum number of villagers in this category
            threshold = json.has("threshold") ? json.get("threshold").getAsInt() : 3;
        }
    }
}
