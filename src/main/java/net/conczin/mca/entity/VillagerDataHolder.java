package net.conczin.mca.entity;

import net.minecraft.world.entity.npc.villager.VillagerData;

/**
 * Stub interface for VillagerDataHolder - removed in 1.21.11
 * This interface is maintained for API compatibility with MCA
 * but the actual VillagerDataHolder interface was removed from Minecraft
 * 
 * Classes implementing this need to provide VillagerData management
 */
public interface VillagerDataHolder {
    VillagerData getVillagerData();

    void setVillagerData(VillagerData data);
}
