package net.conczin.mca.entity.ai;

/**
 * Stub Schedule class to replace the removed
 * net.minecraft.world.entity.schedule.Schedule in 1.21.11
 * 
 * The Schedule class and related ScheduleBuilder were removed in Minecraft
 * 1.21.11.
 * This stub provides a minimal implementation for the MCA mod to compile.
 * 
 * NOTE: Villager schedule functionality is disabled until a proper replacement
 * is implemented.
 */
public class Schedule {
    public static final Schedule VILLAGER_DEFAULT = new Schedule("villager_default");
    public static final Schedule VILLAGER_BABY = new Schedule("villager_baby");
    public static final Schedule SIMPLE = new Schedule("simple");
    public static final Schedule EMPTY = new Schedule("empty");

    private final String name;

    public Schedule(String name) {
        this.name = name;
    }

    public Schedule() {
        this("custom");
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Schedule{" + name + "}";
    }
}

