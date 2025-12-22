package net.conczin.mca.mixin;

import net.conczin.mca.server.SpawnQueue;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProtoChunk.class)
abstract class MixinProtoChunk extends ChunkAccess {
    // TODO: In 1.21.11, ChunkAccess constructor signature changed
    // public MixinProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData,
    // LevelHeightAccessor levelHeightAccessor, Registry<Biome> registry, long l,
    // @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData
    // blendingData) {
    // super(chunkPos, upgradeData, levelHeightAccessor, registry, l,
    // levelChunkSections, blendingData);
    // }
    public MixinProtoChunk(ChunkPos chunkPos, UpgradeData upgradeData, LevelHeightAccessor levelHeightAccessor, long l,
            @Nullable LevelChunkSection[] levelChunkSections, @Nullable BlendingData blendingData) {
        super(chunkPos, upgradeData, levelHeightAccessor, l, levelChunkSections, blendingData);
    }

    // TODO: addEntity method may have changed
    // @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at =
    // @At("HEAD"), cancellable = true)
    // private void onAddEntity(Entity entity, CallbackInfo info) {
    // if (SpawnQueue.getInstance().addVillager(entity)) {
    // info.cancel();
    // }
    // }
}
