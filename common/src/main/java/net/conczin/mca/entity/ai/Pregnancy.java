package net.conczin.mca.entity.ai;

import net.conczin.mca.Config;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.item.BabyItem;
import net.conczin.mca.registry.CriterionMCA;
import net.conczin.mca.server.world.data.Village;
import net.conczin.mca.util.WorldUtils;
import net.conczin.mca.util.network.datasync.CDataManager;
import net.conczin.mca.util.network.datasync.CDataParameter;
import net.conczin.mca.util.network.datasync.CParameter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * The progenator. Preg-genator? Preg-genator.
 */
public class Pregnancy {
    private static final CDataParameter<Boolean> HAS_BABY = CParameter.create("HasBaby", false);
    private static final CDataParameter<Boolean> IS_BABY_MALE = CParameter.create("IsBabyMale", false);
    private static final CDataParameter<Integer> BABY_AGE = CParameter.create("BabyAge", 0);
    private static final CDataParameter<Optional<java.util.UUID>> FATHER_ID = CParameter.create("FatherId",
            Optional.empty());
    private final VillagerEntityMCA mother;

    Pregnancy(VillagerEntityMCA entity) {
        this.mother = entity;
    }

    public static <E extends Entity> CDataManager.Builder<E> createTrackedData(CDataManager.Builder<E> builder) {
        return builder.addAll(HAS_BABY, IS_BABY_MALE, BABY_AGE, FATHER_ID);
    }

    public boolean isPregnant() {
        return mother.getTrackedValue(HAS_BABY);
    }

    public void setPregnant(boolean pregnant) {
        mother.setTrackedValue(HAS_BABY, pregnant);
    }

    public int getBabyAge() {
        return mother.getTrackedValue(BABY_AGE);
    }

    public void setBabyAge(int age) {
        mother.setTrackedValue(BABY_AGE, age);
    }

    public Gender getGender() {
        return mother.getTrackedValue(IS_BABY_MALE) ? Gender.MALE : Gender.FEMALE;
    }

    public void tick() {
        if (!isPregnant()) {
            return;
        }

        setBabyAge(getBabyAge() + 60);

        if (getBabyAge() < Config.getInstance().babyItemGrowUpTime) {
            return;
        }

        setBabyAge(0);
        setBabyAge(0);

        // Use stored father ID if available, otherwise try to find current partner
        Optional<VillagerEntityMCA> fatherOpt = mother.getTrackedValue(FATHER_ID)
                .map(uuid -> {
                    if (mother.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        Entity e = serverLevel.getEntity(uuid);
                        if (e instanceof VillagerEntityMCA v)
                            return v;
                    }
                    return null;
                })
                .or(this::getFather);

        fatherOpt.ifPresent(father -> {
            setPregnant(false);
            mother.setTrackedValue(FATHER_ID, Optional.empty());

            VillagerEntityMCA child = createChild(getGender(), father);

            child.setPos(mother.getX(), mother.getY(), mother.getZ());
            WorldUtils.spawnEntity(mother.level(), child, MobSpawnType.BREEDING);
        });
    }

    public boolean tryStartGestation() {
        return getFather().map(this::tryStartGestation).orElse(false);
    }

    public boolean tryStartGestation(Entity father) {
        // You can't get double-pregnant
        if (isPregnant()) {
            return false;
        }

        // In case we're the father, impregnate the other
        if (mother.getGenetics().getGender() == Gender.MALE) {
            if (father instanceof VillagerEntityMCA vFather && vFather.getGenetics().getGender() != Gender.MALE) {
                return vFather.getRelationships().getPregnancy().tryStartGestation(mother);
            }
            // If father is player (male), and mother is male... well, no baby.
            // But if mother is female, we proceed.
        }

        setPregnant(true);
        mother.setTrackedValue(IS_BABY_MALE, mother.level().random.nextBoolean());
        mother.setTrackedValue(FATHER_ID, Optional.of(father.getUUID()));
        return true;
    }

    public VillagerEntityMCA createChild(Gender gender, VillagerEntityMCA partner) {
        VillagerEntityMCA child = Objects.requireNonNull(gender.getVillagerType().create(mother.level()));

        child.getGenetics().combine(partner.getGenetics(), mother.getGenetics());
        child.getTraits().inherit(partner.getTraits());
        child.getTraits().inherit(mother.getTraits());
        child.setBaby(true);
        child.setAgeState(AgeState.TODDLER);
        child.getRelationships().getFamilyEntry().assignParents(mother.getRelationships(), partner.getRelationships());

        // advancement
        child.getRelationships().getFamily(2, 0)
                .filter(ServerPlayer.class::isInstance)
                .map(ServerPlayer.class::cast)
                .forEach(CriterionMCA.FAMILY::trigger);

        // civil entry
        mother.getResidency().getHomeVillage().flatMap(Village::getCivilRegistry)
                .ifPresent(r -> r.addText(Component.translatable("events.baby", mother.getName(), partner.getName())));

        return child;
    }

    public VillagerEntityMCA createChild(Gender gender) {
        return createChild(gender, mother);
    }

    private Optional<VillagerEntityMCA> getFather() {
        return mother.getRelationships().getPartner()
                .filter(VillagerEntityMCA.class::isInstance)
                .map(VillagerEntityMCA.class::cast);
    }

    public void procreate(Entity spouse) {
        RandomSource random = mother.getRandom();

        // make sure this villager is registered in the family tree
        int count = 1;
        while (random.nextFloat() < Config.getInstance().twinBabyChance && count < 8) {
            count++;
        }

        // advancement
        if (spouse instanceof ServerPlayer player) {
            CriterionMCA.BABY.trigger(player, count);
        }

        long seed = random.nextLong();
        for (int i = 0; i < count; i++) {
            boolean flip = mother.getGenetics().getGender() == Gender.MALE;
            ItemStack stack = BabyItem.createItem(flip ? spouse : mother, flip ? mother : spouse, seed);
            if (!(spouse instanceof Player player && player.addItem(stack))) {
                mother.getInventory().addItem(stack);
            }
        }
    }
}
