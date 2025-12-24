package net.conczin.mca.entity.ai.chatAI.modules;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class EnvironmentModule {
    public static void apply(List<String> input, VillagerEntityMCA villager, ServerPlayer player) {
        if (player.level().isRaining()) {
            input.add("It is raining. ");
        }
        if (player.level().isThundering()) {
            input.add("It is thundering. ");
        }
        // 1.21.11: isNight() removed - using time-based check (13000-23000 = night)
        long dayTime = player.level().getDayTime() % 24000L;
        if (dayTime >= 13000 && dayTime <= 23000) {
            input.add("It is night. ");
        }
    }
}
