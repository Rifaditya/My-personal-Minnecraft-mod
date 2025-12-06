package net.conczin.mca.client.gui.widget;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class GeneSliderWidget extends ExtendedSliderWidget<Double> {
    private final VillagerEntityMCA villager;
    private final Genetics.GeneType geneType;

    public GeneSliderWidget(int x, int y, int width, int height, Component text, double value,
            Consumer<Double> callback, VillagerEntityMCA villager, Genetics.GeneType geneType) {
        super(x, y, width, height, text, value, callback, () -> {
            if (villager != null && geneType != null) {
                GeneInfo info = getGeneInfo(villager, geneType, value);

                if (info.ageMultiplier != 1.0f) {
                    String status = info.growthPhase != null ? ", " + info.growthPhase : "";
                    return Component.literal(
                            Component.translatable(geneType.getTranslationKey()).getString() + ": " +
                                    info.geneticPercent + "% (current " + info.currentPercent + "%" + status + ")");
                } else {
                    return Component.translatable("gene.tooltip",
                            Component.translatable(geneType.getTranslationKey()),
                            info.geneticPercent);
                }
            }
            return text;
        });
        this.villager = villager;
        this.geneType = geneType;
    }

    private static class GeneInfo {
        final float ageMultiplier;
        final int geneticPercent;
        final int currentPercent;
        final String growthPhase; // "growing", "peak", or "shrinking"

        GeneInfo(float multiplier, int genetic, int current, String phase) {
            this.ageMultiplier = multiplier;
            this.geneticPercent = genetic;
            this.currentPercent = current;
            this.growthPhase = phase;
        }
    }

    private static GeneInfo getGeneInfo(VillagerEntityMCA villager, Genetics.GeneType gene, double value) {
        int geneticPercent = (int) (value * 100);
        float ageMultiplier = 1.0f;
        String growthPhase = null;

        if (gene == Genetics.BREAST) {
            // Breast development based on age state
            ageMultiplier = villager.getAgeState().getBreasts();
            if (ageMultiplier < 1.0f) {
                growthPhase = "growing";
            } else {
                growthPhase = "peak";
            }
        } else if (gene == Genetics.SIZE || gene == Genetics.WIDTH) {
            // Adult aging curve for size and width
            int age = villager.getAge();
            if (age < 0) {
                // Child stages - use height multiplier
                ageMultiplier = villager.getAgeState().getHeight();
                growthPhase = "growing";
            } else {
                // Adult aging curve: 70% at age 18, 100% at ages 60-70, back to 70% at age 100
                float maxAge = AgeState.getMaxAge();
                float ageProgress = Math.min(age / maxAge, 1.0f);
                final float GROWTH_END = 0.51f; // Age 60
                final float PLATEAU_END = 0.63f; // Age 70
                final float YOUNG_SIZE = 0.7f;
                final float PEAK_SIZE = 1.0f;

                if (ageProgress <= GROWTH_END) {
                    // Growing phase
                    float growthProgress = ageProgress / GROWTH_END;
                    ageMultiplier = YOUNG_SIZE + (growthProgress * (PEAK_SIZE - YOUNG_SIZE));
                    growthPhase = "growing";
                } else if (ageProgress <= PLATEAU_END) {
                    ageMultiplier = PEAK_SIZE; // Peak phase
                    growthPhase = "peak";
                } else {
                    // Elder shrinking phase
                    float elderProgress = (ageProgress - PLATEAU_END) / (1.0f - PLATEAU_END);
                    ageMultiplier = PEAK_SIZE - (elderProgress * (PEAK_SIZE - YOUNG_SIZE));
                    growthPhase = "shrinking";
                }
            }
        }

        int currentPercent = (int) (value * ageMultiplier * 100);
        return new GeneInfo(ageMultiplier, geneticPercent, currentPercent, growthPhase);
    }

    @Override
    Double getValue() {
        return value;
    }

    @Override
    protected void updateMessage() {
        // No message update needed
    }
}
