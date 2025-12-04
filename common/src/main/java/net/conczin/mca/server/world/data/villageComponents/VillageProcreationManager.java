package net.conczin.mca.server.world.data.villageComponents;

import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.PoolUtil;
import net.conczin.mca.server.world.data.FamilyTree;
import net.conczin.mca.server.world.data.Village;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.ArrayList;
import java.util.List;

public class VillageProcreationManager {
    private final Village village;

    public VillageProcreationManager(Village village) {
        this.village = village;
    }

    // if the population is low, find a couple and let them have a child
    public void procreate(ServerLevel world) {
        if (world.random.nextFloat() >= Config.getInstance().villagerProcreationChancePerMinute) {
            return;
        }

        int population = village.getPopulation();
        int maxPopulation = village.getMaxPopulation();
        if (population >= maxPopulation * village.getPopulationThreshold()) {
            return;
        }

        // look for women who can procreate
        PoolUtil.pick(village.getResidents(world), world.random)
                .filter(villager -> villager.getGenetics().getGender() == Gender.FEMALE)
                .filter(villager -> !villager.getRelationships().getPregnancy().isPregnant())
                .filter(villager -> world.random.nextFloat() < 1.0
                        / (FamilyTree.get(world).getOrCreate(villager).getChildren().count() + 0.1))
                .ifPresent(villager -> {
                    // Find potential fathers
                    List<VillagerEntityMCA> candidates = new ArrayList<>();

                    // 1. Spouse
                    villager.getRelationships().getPartner()
                            .filter(p -> p instanceof VillagerEntityMCA)
                            .map(p -> (VillagerEntityMCA) p)
                            .ifPresent(candidates::add);

                    // 2. Lovers (> 200 hearts)
                    villager.getVillagerBrain().getMemories().forEach((uuid, memory) -> {
                        if (memory.getHearts() > 200) {
                            Entity e = world.getEntity(uuid);
                            if (e instanceof VillagerEntityMCA v && v.getGenetics().getGender() == Gender.MALE) {
                                if (!candidates.contains(v)) {
                                    candidates.add(v);
                                }
                            }
                        }
                    });

                    if (!candidates.isEmpty()) {
                        // 50% chance to proceed
                        if (world.random.nextFloat() < 0.5f) {
                            VillagerEntityMCA father = candidates.get(world.random.nextInt(candidates.size()));
                            if (villager.getRelationships().getPregnancy().tryStartGestation(father)) {
                                // tell everyone about it
                                if (Config.getInstance().villagerBirthNotification) {
                                    village.broadCastMessage(world, "events.baby", villager, father);
                                }
                            }
                        }
                    }
                });
    }
}
