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

// TODO: In 1.21.11, ChunkAccess constructor signature changed significantly
// This mixin is disabled until proper constructor signature is determined
@Mixin(ProtoChunk.class)
abstract class MixinProtoChunk {
    // Mixin body disabled - ChunkAccess constructor incompatible
    // @Inject(method = "addEntity(Lnet/minecraft/world/entity/Entity;)V", at =
    // @At("HEAD"), cancellable = true)
    // private void onAddEntity(Entity entity, CallbackInfo info) {
    // if (SpawnQueue.getInstance().addVillager(entity)) {
    // info.cancel();
    // }
    // }
}
