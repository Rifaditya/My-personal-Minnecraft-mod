package net.conczin.mca.village.specialization;

import net.minecraft.network.chat.Component;

/**
 * Village specialization types that affect profession distribution,
 * building composition, and economic bonuses.
 * 
 * ADDON-FRIENDLY: Can be extracted to separate mod.
 */
public enum VillageSpecialization {
    /**
     * No specialization - balanced profession distribution
     */
    NONE("none", 1.0f),

    /**
     * Mining village - focuses on ore extraction and metal working.
     * Common in mountain biomes with multiple blacksmiths.
     * Bonuses: +50% ore yield from mining chores
     */
    MINING("mining", 1.5f),

    /**
     * Farming village - focuses on agriculture and animal husbandry.
     * Common in plains with multiple farms.
     * Bonuses: +50% crop yield from harvesting
     */
    FARMING("farming", 1.5f),

    /**
     * Trading village - focuses on commerce and knowledge.
     * Common near crossroads with libraries/markets.
     * Bonuses: Better trade prices
     */
    TRADING("trading", 1.25f);

    private final String id;
    private final float productivityMultiplier;

    VillageSpecialization(String id, float productivityMultiplier) {
        this.id = id;
        this.productivityMultiplier = productivityMultiplier;
    }

    public String getId() {
        return id;
    }

    public float getProductivityMultiplier() {
        return productivityMultiplier;
    }

    public Component getDisplayName() {
        return Component.translatable("village.specialization." + id);
    }

    public static VillageSpecialization fromId(String id) {
        for (VillageSpecialization spec : values()) {
            if (spec.id.equals(id)) {
                return spec;
            }
        }
        return NONE;
    }
}
