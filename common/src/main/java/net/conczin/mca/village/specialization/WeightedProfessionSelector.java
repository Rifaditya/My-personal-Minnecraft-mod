package net.conczin.mca.village.specialization;

import net.conczin.mca.server.world.data.Village;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects a random profession with weights based on village specialization.
 * 
 * ADDON-FRIENDLY: Standalone utility, no deep integration required.
 */
public class WeightedProfessionSelector {

    /**
     * Select a random profession, weighted by village specialization.
     * If no village, returns random profession.
     */
    public static VillagerProfession selectProfession(RandomSource random, Village village) {
        if (village == null || village.getSpecialization() == VillageSpecialization.NONE) {
            // No specialization - return truly random
            return getRandomVanillaProfession(random);
        }

        // Build weighted list
        List<VillagerProfession> options = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
            if (prof == VillagerProfession.NONE || prof == VillagerProfession.NITWIT) {
                continue; // Skip NONE and NITWIT
            }

            float weight = ProfessionWeighter.getWeight(village, prof);
            options.add(prof);
            weights.add(weight);
        }

        // Weighted random selection
        return weightedRandom(random, options, weights);
    }

    private static VillagerProfession getRandomVanillaProfession(RandomSource random) {
        List<VillagerProfession> professions = new ArrayList<>();
        for (VillagerProfession prof : BuiltInRegistries.VILLAGER_PROFESSION) {
            if (prof != VillagerProfession.NONE && prof != VillagerProfession.NITWIT) {
                professions.add(prof);
            }
        }
        return professions.get(random.nextInt(professions.size()));
    }

    private static <T> T weightedRandom(RandomSource random, List<T> options, List<Float> weights) {
        float totalWeight = 0;
        for (float weight : weights) {
            totalWeight += weight;
        }

        float value = random.nextFloat() * totalWeight;
        float currentWeight = 0;

        for (int i = 0; i < options.size(); i++) {
            currentWeight += weights.get(i);
            if (value < currentWeight) {
                return options.get(i);
            }
        }

        // Fallback (shouldn't happen)
        return options.get(options.size() - 1);
    }
}
