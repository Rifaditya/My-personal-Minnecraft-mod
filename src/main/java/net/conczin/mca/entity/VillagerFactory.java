package net.conczin.mca.entity;

import net.conczin.mca.MCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.resources.Names;
import net.conczin.mca.util.WorldUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.OptionalInt;

public class VillagerFactory {
    private final Level world;

    private Optional<String> name = Optional.empty();
    private Optional<Gender> gender = Optional.empty();

    private Optional<VillagerProfession> profession = Optional.empty();
    private Optional<VillagerType> type = Optional.empty();
    private OptionalInt level = OptionalInt.empty();
    private Optional<MerchantOffers> offers = Optional.empty();

    private OptionalInt age = OptionalInt.empty();
    private Optional<Vec3> position = Optional.empty();

    private VillagerFactory(Level world) {
        this.world = world;
    }

    public static VillagerFactory newVillager(Level world) {
        return new VillagerFactory(world);
    }

    public VillagerFactory withGender(Gender gender) {
        this.gender = Optional.ofNullable(gender);
        return this;
    }

    public VillagerFactory withType(VillagerType type) {
        this.type = Optional.ofNullable(type);
        return this;
    }

    public VillagerFactory withProfession(VillagerProfession prof) {
        this.profession = Optional.ofNullable(prof);
        return this;
    }

    public VillagerFactory withProfession(VillagerProfession prof, int level) {
        withProfession(prof);
        this.level = OptionalInt.of(level);
        return this;
    }

    public VillagerFactory withProfession(VillagerProfession prof, int level, MerchantOffers offers) {
        withProfession(prof, level);
        this.offers = Optional.of(offers);
        return this;
    }

    public VillagerFactory withName(String name) {
        this.name = Optional.ofNullable(name);
        return this;
    }

    public VillagerFactory withPosition(double x, double y, double z) {
        return withPosition(new Vec3(x, y, z));
    }

    public VillagerFactory withPosition(Entity entity) {
        return withPosition(entity.getX(), entity.getY(), entity.getZ());
    }

    public VillagerFactory withPosition(Vec3 pos) {
        position = Optional.of(pos);
        return this;
    }

    public VillagerFactory withAge(int age) {
        this.age = OptionalInt.of(age);
        return this;
    }

    public VillagerEntityMCA spawn(EntitySpawnReason reason) {
        if (position.isEmpty()) {
            MCA.LOGGER.info("Attempted to spawn villager without a position being set!");
        }

        VillagerEntityMCA villager = build();

        WorldUtils.spawnEntity(world, villager, reason);

        return villager;
    }

    public VillagerEntityMCA build() {
        Gender gender = this.gender.orElseGet(Gender::getRandom);
        // EntityType.create now requires EntitySpawnReason in 1.21.11
        VillagerEntityMCA villager = gender.getVillagerType().create(world, EntitySpawnReason.LOAD);
        assert villager != null;
        villager.getGenetics().setGender(gender);
        villager.setAge(
                age.orElseGet(() -> villager.getRandom().nextInt(AgeState.getMaxAge() * 3) - AgeState.getMaxAge()));
        // absMoveTo replaced with moveTo in 1.21.11\n position.ifPresent(pos ->
        // villager.moveTo(pos.x(), pos.y(), pos.z()));
        villager.setCustomName(Component.literal(name.orElseGet(() -> Names.pickCitizenName(gender, villager))));
        VillagerData data = villager.getVillagerData();
        // VillagerData API changed in 1.21.11: use withType/withProfession/withLevel
        // builder methods
        VillagerData newData = data;
        if (type.isPresent()) {
            newData = newData.withType(net.minecraft.core.Holder.direct(type.get()));
        }
        if (profession.isPresent()) {
            newData = newData.withProfession(net.minecraft.core.Holder.direct(profession.get()));
        }
        if (level.isPresent()) {
            newData = newData.withLevel(level.getAsInt());
        }
        villager.setVillagerData(newData);
        offers.ifPresent(villager::setOffers);
        return villager;
    }
}
