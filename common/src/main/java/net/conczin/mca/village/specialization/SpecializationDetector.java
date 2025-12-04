package net.conczin.mca.village.specialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.conczin.mca.MCA;
import net.conczin.mca.server.world.data.Building;
import net.conczin.mca.server.world.data.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Detects and assigns village specializations based on buildings and biomes.
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
     * Detect and assign specialization to a village based on buildings and biome.
     */
    public static VillageSpecialization detectSpecialization(Village village, ServerLevel world) {
        if (!village.isVillage()) {
            return VillageSpecialization.NONE;
        }

        // Calculate scores for each specialization
        Map<VillageSpecialization, Integer> scores = new HashMap<>();

        for (VillageSpecialization spec : VillageSpecialization.values()) {
            if (spec == VillageSpecialization.NONE)
                continue;

            SpecializationConfig config = CONFIGS.get(spec);
            if (config == null)
                continue;

            int score = calculateScore(village, world, config);
            if (score >= config.threshold) {
                scores.put(spec, score);
            }
        }

        // Return highest scoring specialization
        return scores.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(VillageSpecialization.NONE);
    }

    private static int calculateScore(Village village, ServerLevel world, SpecializationConfig config) {
        int score = 0;

        // Score based on buildings
        for (Building building : village) {
            if (config.buildings.contains(building.getType())) {
                score += config.scorePerBuilding;
            }
        }

        // Score based on biome
        BlockPos center = new BlockPos(village.getCenter());
        Holder<Biome> biomeHolder = world.getBiome(center);

        for (ResourceLocation biomeId : config.biomes) {
            if (biomeHolder.is(biomeId)) {
                score += config.scorePerBiome;
                break; // Only count once
            }
        }

        return score;
    }

    /**
     * Configuration data for a specialization type.
     */
    private static class SpecializationConfig {
        final Set<String> buildings;
        final Set<ResourceLocation> biomes;
        final int scorePerBuilding;
        final int scorePerBiome;
        final int threshold;

        SpecializationConfig(JsonObject json) {
            buildings = new HashSet<>();
            if (json.has("buildings")) {
                json.getAsJsonArray("buildings").forEach(e -> buildings.add(e.getAsString()));
            }

            biomes = new HashSet<>();
            if (json.has("biomes")) {
                json.getAsJsonArray("biomes").forEach(e -> biomes.add(ResourceLocation.parse(e.getAsString())));
            }

            scorePerBuilding = json.has("score_per_building") ? json.get("score_per_building").getAsInt() : 3;
            scorePerBiome = json.has("score_per_biome_match") ? json.get("score_per_biome_match").getAsInt() : 5;
            threshold = json.has("threshold") ? json.get("threshold").getAsInt() : 10;
        }
    }
}
