package net.conczin.mca.entity.ai;

import net.conczin.mca.Config;
import net.minecraft.world.entity.LivingEntity;

/**
 * MCA Schedules - Now using vanilla Schedule constants since ScheduleBuilder
 * was removed in 1.21.11
 * 
 * Original custom schedules have been replaced with vanilla equivalents:
 * - DEFAULT -> Schedule.VILLAGER_DEFAULT
 * - NIGHT_OWL_DEFAULT -> Schedule.VILLAGER_DEFAULT (night owl behavior via
 * config)
 * - GUARD -> Schedule.VILLAGER_DEFAULT (guards use same schedule)
 * - GUARD_NIGHT -> Schedule.VILLAGER_DEFAULT
 * - GUESTS -> Schedule.SIMPLE (simplified schedule for inn guests)
 */
public interface SchedulesMCA {
    // Use vanilla schedules as replacements
    Schedule DEFAULT = Schedule.VILLAGER_DEFAULT;
    Schedule NIGHT_OWL_DEFAULT = Schedule.VILLAGER_DEFAULT;
    Schedule GUARD = Schedule.VILLAGER_DEFAULT;
    Schedule GUARD_NIGHT = Schedule.VILLAGER_DEFAULT;
    Schedule GUESTS = Schedule.SIMPLE;

    static void bootstrap() {
        // No custom schedules to register anymore
    }

    static Schedule getTypeSchedule(LivingEntity entity, boolean allowNightOwl, Schedule normalSchedule,
            Schedule nightSchedule) {
        return (allowNightOwl && entity.getRandom().nextFloat() < Config.getInstance().nightOwlChance) ? nightSchedule
                : normalSchedule;
    }

    static Schedule getTypeSchedule(LivingEntity entity, Schedule normalSchedule, Schedule nightSchedule) {
        return getTypeSchedule(entity, Config.getInstance().allowAnyNightOwl, normalSchedule, nightSchedule);
    }

    static Schedule getTypeSchedule(LivingEntity entity, boolean allowNightOwl) {
        return getTypeSchedule(entity, allowNightOwl, SchedulesMCA.DEFAULT, SchedulesMCA.NIGHT_OWL_DEFAULT);
    }

    static Schedule getTypeSchedule(LivingEntity entity) {
        return getTypeSchedule(entity, Config.getInstance().allowAnyNightOwl);
    }
}

