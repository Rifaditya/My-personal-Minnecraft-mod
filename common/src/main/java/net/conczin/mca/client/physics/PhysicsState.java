package net.conczin.mca.client.physics;

import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsState {
    private static final Map<UUID, PhysicsState> STATE_MAP = new ConcurrentHashMap<>();

    public final BreastPhysics leftPhysics = new BreastPhysics();
    public final BreastPhysics rightPhysics = new BreastPhysics();
    public int lastTick = -1;

    public static PhysicsState get(LivingEntity entity) {
        return STATE_MAP.computeIfAbsent(entity.getUUID(), k -> new PhysicsState());
    }

    public static void remove(LivingEntity entity) {
        STATE_MAP.remove(entity.getUUID());
    }
}
