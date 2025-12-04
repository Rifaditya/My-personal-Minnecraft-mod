package net.conczin.mca.mixin;

import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.Traits;
import net.conczin.mca.client.model.CommonVillagerModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    private static final TagKey<Item> GLUTEN_TAG = TagKey.create(
            BuiltInRegistries.ITEM.key(),
            ResourceLocation.fromNamespaceAndPath("mca", "gluten"));

    private static final TagKey<Item> SUGAR_TAG = TagKey.create(
            BuiltInRegistries.ITEM.key(),
            ResourceLocation.fromNamespaceAndPath("mca", "sugar"));

    private static final TagKey<Item> MEAT_TAG = TagKey.create(
            BuiltInRegistries.ITEM.key(),
            ResourceLocation.fromNamespaceAndPath("mca", "meat"));

    @Inject(method = "eat", at = @At("RETURN"))
    public void mca$injectEat(Level world, ItemStack food, CallbackInfoReturnable<ItemStack> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        VillagerLike<?> villagerLike = world.isClientSide
                ? CommonVillagerModel.getVillager(entity)
                : VillagerLike.toVillager(entity);

        if (villagerLike != null) {
            // Coeliac disease - gluten causes nausea and weakness
            if (food.is(GLUTEN_TAG) && villagerLike.getTraits().hasTrait(Traits.COELIAC_DISEASE)) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
            }

            // Diabetes - sugar causes temporary slowness and weakness
            if (food.is(SUGAR_TAG) && villagerLike.getTraits().hasTrait(Traits.DIABETES)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 1));
            }

            // Vegetarian - meat causes nausea
            if (food.is(MEAT_TAG) && villagerLike.getTraits().hasTrait(Traits.VEGETARIAN)) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            }
        }
    }
}
