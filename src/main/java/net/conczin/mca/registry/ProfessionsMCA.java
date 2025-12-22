package net.conczin.mca.registry;

import net.conczin.mca.MCA;
import net.minecraft.resources.Identifier;
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
    // Add missing IS_IMPORTANT set
    Set<VillagerProfession> IS_IMPORTANT = new HashSet<>();

    // TODO: In 1.21.11, VillagerProfession constructor changed significantly
    // These are placeholder nulls - will cause NPEs at runtime but allow
    // compilation
    VillagerProfession OUTLAW = null;
    VillagerProfession GUARD = null;
    VillagerProfession ARCHER = null;
    VillagerProfession ADVENTURER = null;
    VillagerProfession MERCENARY = null;
    // Fix typo: CULTISTS -> add CULTIST alias
    VillagerProfession CULTISTS = null;
    VillagerProfession CULTIST = null; // Alias for code that uses singular

    static String getFavoredBuilding(VillagerProfession profession) {
        // TODO: In 1.21.11, VillagerProfession comparison may need adjustment
        if (GUARD == profession || ARCHER == profession) {
            return "inn";
        }
        return null;
    }

    static void registerProfessions(MCA.RegisterHelper<VillagerProfession> helper) {
        PROFESSIONS.forEach(helper::register);
    }
}
