package net.conczin.mca.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.MCAClient;
import net.conczin.mca.client.model.PlayerArmorExtendedModel;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 1.21.11: HumanoidArmorLayer type bounds changed - mixin disabled
@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer {
    // Mixin body disabled - type bounds incompatible in 1.21.11
    /*
     * @Unique
     * protected final Object mca$leggingsModel = null;
     * 
     * @Unique
     * protected final Object mca$bodyModel = null;
     * 
     * @Unique
     * protected boolean mca$injectionActive;
     * 
     * @Shadow
     * protected abstract boolean usesInnerModel(EquipmentSlot slot);
     * 
     * @Inject(method =
     * "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
     * at = @At("HEAD"))
     * public void mca$injectRender(PoseStack matrixStack, MultiBufferSource
     * vertexConsumerProvider, int i, Object livingEntity, float f, float g, float
     * h, float j, float k, float l, CallbackInfo ci) {
     * }
     * 
     * @Inject(method =
     * "getArmorModel(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/client/model/HumanoidModel;",
     * at = @At("HEAD"), cancellable = true)
     * private void mca$injectGetArmorModel(EquipmentSlot slot,
     * CallbackInfoReturnable<Object> cir) {
     * }
     */
}
