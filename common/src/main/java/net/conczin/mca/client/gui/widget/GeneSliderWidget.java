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
                float ageMultiplier = getAgeMultiplierForGene(villager, geneType);
                int geneticPercent = (int) (value * 100);

                if (ageMultiplier != 1.0f) {
                    int currentPercent = (int) (value * ageMultiplier * 100);
                    return Component.literal(
                            Component.translatable(geneType.getTranslationKey()).getString() + ": " +
                                    geneticPercent + "% (current " + currentPercent + "%)");
                } else {
                    return Component.translatable("gene.tooltip",
                            Component.translatable(geneType.getTranslationKey()),
                            geneticPercent);
                }
            }
            return text;
        });
        this.villager = villager;
        this.geneType = geneType;
    }

    private static float getAgeMultiplierForGene(VillagerEntityMCA villager, Genetics.GeneType gene) {
        if (gene == Genetics.BREAST) {
            // Breast development based on age state
            return villager.getAgeState().getBreasts();
        } else if (gene == Genetics.SIZE || gene == Genetics.WIDTH) {
            // Adult aging curve for size and width
            int age = villager.getAge();
            if (age < 0) {
                // Child stages - use height multiplier
                return villager.getAgeState().getHeight();
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
                    return YOUNG_SIZE + (growthProgress * (PEAK_SIZE - YOUNG_SIZE));
                } else if (ageProgress <= PLATEAU_END) {
                    return PEAK_SIZE; // Peak phase
                } else {
                    // Elder shrinking phase
                    float elderProgress = (ageProgress - PLATEAU_END) / (1.0f - PLATEAU_END);
                    return PEAK_SIZE - (elderProgress * (PEAK_SIZE - YOUNG_SIZE));
                }
            }
        }
        // Other genes are not affected by age
        return 1.0f;
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
