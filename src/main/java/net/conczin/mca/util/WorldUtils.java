package net.conczin.mca.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface WorldUtils {
    static List<Entity> getCloseEntities(Level world, Entity e, double range) {
        Vec3 pos = e.position();
        return world.getEntities(e, new AABB(pos, pos).inflate(range));
    }

    static <T extends Entity> List<T> getCloseEntities(Level world, Entity e, double range, Class<T> c) {
        return getCloseEntities(world, e.position(), range, c);
    }

    static <T extends Entity> List<T> getCloseEntities(Level world, Vec3 pos, double range, Class<T> c) {
        return world.getEntitiesOfClass(c, new AABB(pos, pos).inflate(range));
    }

    @SuppressWarnings("DataFlowIssue")
    static <T extends SavedData> T loadData(ServerLevel world, BiFunction<CompoundTag, HolderLookup.Provider, T> loader,
            Function<ServerLevel, T> factory, String dataId) {
        // 1.21.11: SavedData API changed to SavedDataType with Codec - disabled
        // Returns new instance from factory as workaround
        return factory.apply(world);
    }

    static void spawnEntity(Level world, Mob entity, EntitySpawnReason reason) {
        // 1.21.11: finalizeSpawn API changed - disabled, adding entity directly
        world.addFreshEntity(entity);
    }

    // a wrapper for the unnecessary complex query provided by minecraft
    static Optional<BlockPos> getClosestStructurePosition(ServerLevel world, BlockPos center, Identifier structure,
            int radius) {
        // 1.21.11: Registry.get returns Optional - disabled, returning empty
        return Optional.empty();
    }

    static Optional<BlockPos> getClosestStructurePosition(ServerLevel world, BlockPos center, TagKey<Structure> tag,
            int radius) {
        // 1.21.11: Registry.getTag API changed - disabled, returning empty
        return Optional.empty();
    }

    static boolean isChunkLoaded(ServerLevel world, Vec3i pos) {
        return isChunkLoaded(world, new BlockPos(pos));
    }

    static boolean isChunkLoaded(ServerLevel world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        LevelChunk worldChunk = world.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
        if (worldChunk != null) {
            return worldChunk.getFullStatus() == FullChunkStatus.ENTITY_TICKING
                    && world.areEntitiesLoaded(chunkPos.toLong());
        }
        return false;
    }

    static boolean isAreaLoaded(ServerLevel world, ChunkPos pos, int radius) {
        ServerChunkCache chunkManager = world.getChunkSource();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (!chunkManager.hasChunk(pos.x + x, pos.z + z)) {
                    return false;
                }
            }
        }
        return true;
    }
}
