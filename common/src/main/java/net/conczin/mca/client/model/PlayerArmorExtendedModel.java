package net.conczin.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

import static net.conczin.mca.client.model.VillagerEntityBaseModelMCA.BREASTS;

public class PlayerArmorExtendedModel<T extends LivingEntity> extends HumanoidModel<T>
        implements CommonVillagerModel<T> {
    public final ModelPart breasts;

    final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
    float breastSize;

    public PlayerArmorExtendedModel(ModelPart root, float dilation) {
        super(root);
        this.breasts = root.getChild(BREASTS);
        this.wildfireRenderer.setDilation(dilation);
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<T> target) {
        super.copyPropertiesTo(target);

        if (target instanceof PlayerEntityExtendedModel<T> playerTarget) {
            copyAttributes(playerTarget);
        }
    }

    private void copyAttributes(PlayerEntityExtendedModel<T> target) {
        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        target.breasts.copyFrom(breasts);
    }

    @Override
    public ModelPart getBreastPart() {
        return breasts;
    }

    @Override
    public ModelPart getBodyPart() {
        return body;
    }

    @Override
    public Iterable<ModelPart> getCommonHeadParts() {
        return headParts();
    }

    @Override
    public Iterable<ModelPart> getCommonBodyParts() {
        return bodyParts();
    }

    @Override
    public Iterable<ModelPart> getBreastParts() {
        return ImmutableList.of(breasts);
    }

    @Override
    public VillagerDimensions.Mutable getDimensions() {
        return dimensions;
    }

    @Override
    public float getBreastSize() {
        return breastSize;
    }

    @Override
    public void setBreastSize(float breastSize) {
        this.breastSize = breastSize;
    }

    // Wildfire Renderer Integration
    private final net.conczin.mca.client.render.wildfire.WildfireBreastRenderer wildfireRenderer = new net.conczin.mca.client.render.wildfire.WildfireBreastRenderer();
    private T currentEntity;
    private float currentPartialTicks;

    @Override
    public net.conczin.mca.client.render.wildfire.WildfireBreastRenderer getWildfireRenderer() {
        return wildfireRenderer;
    }

    @Override
    public void setCurrentEntity(T entity) {
        this.currentEntity = entity;
    }

    @Override
    public T getCurrentEntity() {
        return currentEntity;
    }

    @Override
    public void setCurrentPartialTicks(float partialTicks) {
        this.currentPartialTicks = partialTicks;
    }

    @Override
    public float getCurrentPartialTicks() {
        return currentPartialTicks;
    }

    @Override
    public void setupAnim(T villager, float limbAngle, float limbDistance, float animationProgress, float headYaw,
            float headPitch) {
        if (CommonVillagerModel.getVillager(villager).getAgeState() == AgeState.BABY && !villager.isPassenger()) {
            limbDistance = (float) Math.sin(villager.tickCount / 12F);
            limbAngle = (float) Math.cos(villager.tickCount / 9F) * 3;
            headYaw += (float) Math.sin(villager.tickCount / 2F);
        }

        super.setupAnim(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        applyVillagerDimensions(CommonVillagerModel.getVillager(villager), villager.isCrouching());

        this.currentEntity = villager;
        this.currentPartialTicks = animationProgress - villager.tickCount;

        // Disable static breasts to avoid double rendering, as we will render them with
        // physics
        if (CommonVillagerModel.getVillager(villager).getGenetics()
                .getGender() == net.conczin.mca.entity.ai.relationship.Gender.FEMALE) {
            breasts.visible = false;
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // Render head and body parts but skip static breast rendering
        // because we'll render dynamic Wildfire breasts instead
        float headSize = getDimensions().getHead();

        matrices.pushPose();
        matrices.scale(headSize, headSize, headSize);
        getCommonHeadParts().forEach(a -> a.render(matrices, vertices, light, overlay, color));
        matrices.popPose();

        // Render body parts (which includes the armor texture)
        getCommonBodyParts().forEach(a -> a.render(matrices, vertices, light, overlay, color));

        // Skip static breast rendering from renderCommon, instead render Wildfire
        // breasts
        if (currentEntity != null && CommonVillagerModel.getVillager(currentEntity).getGenetics()
                .getGender() == net.conczin.mca.entity.ai.relationship.Gender.FEMALE) {
            // wildfireRenderer.render(matrices, vertices, light, overlay, color,
            // currentEntity, this.body,
            // currentPartialTicks, createPhysicsConfig(currentEntity), true, 64);
        }
    }

    @Override
    public net.conczin.mca.client.physics.BreastPhysics.PhysicsConfig makePhysicsConfig(T entity) {
        return new net.conczin.mca.client.physics.BreastPhysics.PhysicsConfig() {
            private final net.conczin.mca.entity.VillagerLike<?> villager = CommonVillagerModel.getVillager(entity);

            @Override
            public boolean canHaveBreasts() {
                return villager.getGenetics().getGender() == net.conczin.mca.entity.ai.relationship.Gender.FEMALE;
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
                return false; // Or expose this in genetics if needed
            }
        };
    }
}
