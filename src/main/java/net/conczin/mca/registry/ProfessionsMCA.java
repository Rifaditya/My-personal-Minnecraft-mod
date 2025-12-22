package net.conczin.mca.registry;

import com.google.common.collect.ImmutableSet;
import net.conczin.mca.MCA;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface ProfessionsMCA {
    Map<Identifier, VillagerProfession> PROFESSIONS = new HashMap<>();
    Set<VillagerProfession> CAN_NOT_TRADE = new HashSet<>();
    Set<VillagerProfession> SUPPORTS_GUARD_EQUIPMENT = new HashSet<>();
    Set<VillagerProfession> NEEDS_NO_HOME = new HashSet<>();

    VillagerProfession OUTLAW = register("outlaw",
            PoiTypes.UNEMPLOYED, () -> SoundEvents.VILLAGER_AMBIENT, false, false, false);
    VillagerProfession GUARD = register("guard",
            PoiTypes.UNEMPLOYED, () -> SoundEvents.VILLAGER_AMBIENT, true, true, true);
    VillagerProfession ARCHER = register("archer",
            PoiTypes.UNEMPLOYED, () -> SoundEvents.VILLAGER_AMBIENT, true, true, true);
    VillagerProfession ADVENTURER = register("adventurer",
            PoiTypes.UNEMPLOYED, () -> SoundEvents.VILLAGER_AMBIENT, true, true, true);
    VillagerProfession MERCENARY = register("mercenary",
            PoiTypes.UNEMPLOYED, () -> SoundEvents.VILLAGER_AMBIENT, true, true, true);
    VillagerProfession CULTISTS = register("cultist",
            PoiTypes.UNEMPLOYED, () -> SoundEvents.VILLAGER_AMBIENT, false, false, false);

    static VillagerProfession register(String name, Object heldJobSite,
            java.util.function.Supplier<net.minecraft.sounds.SoundEvent> workSound,
            boolean canNotTrade, boolean supportsGuardEquipment, boolean needsNoHome) {
        Identifier id = MCA.locate(name);
        VillagerProfession result = new VillagerProfession(
                id.toString(),
                // TODO: In 1.21.11, PoiTypes handling may have changed
                (holder) -> false,
                (holder) -> false,
                ImmutableSet.of(),
                ImmutableSet.of(),
                workSound.get());
        if (canNotTrade) {
            ProfessionsMCA.CAN_NOT_TRADE.add(result);
        }
        if (supportsGuardEquipment) {
            ProfessionsMCA.SUPPORTS_GUARD_EQUIPMENT.add(result);
        }
        if (needsNoHome) {
            ProfessionsMCA.NEEDS_NO_HOME.add(result);
        }
        PROFESSIONS.put(id, result);
        return result;
    }

    static String getFavoredBuilding(VillagerProfession profession) {
        // TODO: In 1.21.11, VillagerProfession comparison may need adjustment
        // Professions are now accessed via ResourceKey, direct comparison disabled
        if (GUARD == profession || ARCHER == profession) {
            return "inn";
        }
        return null;
    }

    static void registerProfessions(MCA.RegisterHelper<VillagerProfession> helper) {
        PROFESSIONS.forEach(helper::register);

        // TODO: In 1.21.11, VillagerProfession.NONE/NITWIT may need Holder lookup
        // CAN_NOT_TRADE.add(VillagerProfession.NONE);
        // CAN_NOT_TRADE.add(VillagerProfession.NITWIT);
    }
}
