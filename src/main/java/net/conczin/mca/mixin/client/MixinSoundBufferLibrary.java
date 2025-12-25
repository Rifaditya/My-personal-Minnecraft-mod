package net.conczin.mca.mixin.client;

import net.conczin.mca.client.tts.AudioCache;

import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(SoundBufferLibrary.class)
public class MixinSoundBufferLibrary {
    // 1.21.11: Util.backgroundExecutor() may not exist - mixin disabled
    // @Inject(method =
    // "getStream(Lnet/minecraft/resources/Identifier;Z)Ljava/util/concurrent/CompletableFuture;",
    // at = @At("HEAD"), cancellable = true)
    // void mca$injectLoadStreamed(Identifier id, boolean repeatInstantly,
    // CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
    // // Disabled - API changes
    // }
}
