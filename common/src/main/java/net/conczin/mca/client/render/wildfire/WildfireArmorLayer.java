package net.conczin.mca.client.render.wildfire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.client.model.CommonVillagerModel;
import net.conczin.mca.client.physics.BreastPhysics;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class WildfireArmorLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final WildfireBreastRenderer wildfireRenderer = new WildfireBreastRenderer();

    public WildfireArmorLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        VillagerLike<?> villager = CommonVillagerModel.getVillager(entity);
        if (villager == null || villager.getGenetics().getGender() != Gender.FEMALE) {
            return;
        }

        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chestStack.getItem() instanceof ArmorItem armorItem)) {
            return;
        }

        if (armorItem.getEquipmentSlot() != EquipmentSlot.CHEST) {
            return;
        }
        // Get Armor Texture
        ResourceLocation texture = getArmorTexture(chestStack, armorItem);

        // Get Armor Color
        int color = 0xFFFFFFFF;
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(armorItem);
        if (key.getPath().contains("leather")) {
            net.minecraft.world.item.component.DyedItemColor dyedColor = chestStack
                    .get(net.minecraft.core.component.DataComponents.DYED_COLOR);
            if (dyedColor != null) {
                color = 0xFF000000 | dyedColor.rgb();
            } else {
                color = 0xFFA06540; // Default Leather Brown
            }
        }

        // Use armorCutoutNoCull to ensure visibility
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));

        // Render with forced height 64
        wildfireRenderer.render(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color, entity,
                this.getParentModel().body, partialTicks, createPhysicsConfig(villager), true, 32);
    }

    private ResourceLocation getArmorTexture(ItemStack stack, ArmorItem item) {
        // Heuristic to guess texture path from registry name
        // e.g. minecraft:diamond_chestplate ->
        // minecraft:textures/models/armor/diamond_layer_1.png
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String path = key.getPath();
        String namespace = key.getNamespace();

        String material = path.replace("_chestplate", "").replace("golden", "gold");

        // Handle special cases if needed, but this covers standard armor
        return ResourceLocation.fromNamespaceAndPath(namespace, "textures/models/armor/" + material + "_layer_1.png");
    }

    private BreastPhysics.PhysicsConfig createPhysicsConfig(VillagerLike<?> villager) {
        return new BreastPhysics.PhysicsConfig() {
            @Override
            public boolean canHaveBreasts() {
                return villager.getGenetics().getGender() == Gender.FEMALE;
            }

            @Override
            public float getBounceMultiplier() {
                return villager.getGenetics().getBounceMultiplier();
            }

            @Override
            public float getFloppiness() {
                return villager.getGenetics().getFloppiness();
            }

            @Override
            public float getBustSize() {
                return villager.getGenetics().getBreastSize();
            }

            @Override
            public boolean isUniboob() {
                return villager.getGenetics().isUniboob();
            }

            @Override
            public float getBreastXOffset() {
                return villager.getGenetics().getBreastXOffset();
            }

            @Override
            public float getBreastYOffset() {
                return villager.getGenetics().getBreastYOffset();
            }

            @Override
            public float getBreastZOffset() {
                return villager.getGenetics().getBreastZOffset();
            }

            @Override
            public float getCleavage() {
                return villager.getGenetics().getCleavage();
            }

            @Override
            public boolean getArmorPhysicsOverride() {
                return villager.getGenetics().getArmorPhysicsOverride();
            }

            @Override
            public float getElasticity() {
                return villager.getGenetics().getElasticity();
            }

            @Override
            public float getMass() {
                return villager.getGenetics().getMass();
            }

            @Override
            public float getShape() {
                return villager.getGenetics().getShape();
            }

            @Override
            public float getNippleSize() {
                return villager.getGenetics().getNippleSize();
            }

            @Override
            public float getAreolaColor() {
                return villager.getGenetics().getAreolaColor();
            }
        };
    }
}
