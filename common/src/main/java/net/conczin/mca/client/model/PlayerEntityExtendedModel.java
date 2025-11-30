package net.conczin.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.conczin.mca.client.render.wildfire.WildfireBreastRenderer;
import net.conczin.mca.client.physics.BreastPhysics;
import net.conczin.mca.entity.ai.Genetics;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.VillagerLike;

import static net.conczin.mca.client.model.VillagerEntityBaseModelMCA.BREASTS;
import static net.conczin.mca.client.model.VillagerEntityModelMCA.BREASTPLATE;

public class PlayerEntityExtendedModel<T extends LivingEntity> extends PlayerModel<T>
        implements CommonVillagerModel<T> {
    public final ModelPart breasts;
    public final ModelPart breastsWear;

    final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
    float breastSize;

    private final WildfireBreastRenderer wildfireRenderer = new WildfireBreastRenderer();
    private T currentEntity;
    private float currentPartialTicks;

    public PlayerEntityExtendedModel(ModelPart root) {
        super(root, false);
        this.breasts = root.getChild(BREASTS);
        this.breastsWear = root.getChild(BREASTPLATE);
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<T> target) {
        super.copyPropertiesTo(target);

        if (target instanceof PlayerEntityExtendedModel<T> playerTarget) {
            copyAttributes(playerTarget);
        }
        if (target instanceof PlayerArmorExtendedModel<T> armorTarget) {
            copyAttributes(armorTarget);
        }
    }

    private void copyAttributes(PlayerEntityExtendedModel<T> target) {
        target.leftPants.copyFrom(leftPants);
        target.rightPants.copyFrom(rightPants);
        target.leftSleeve.copyFrom(leftSleeve);
        target.rightSleeve.copyFrom(rightSleeve);
        target.jacket.copyFrom(jacket);
        target.breastsWear.copyFrom(breastsWear);

        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        target.breasts.copyFrom(breasts);
    }

    private void copyAttributes(PlayerArmorExtendedModel<T> target) {
        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        target.breasts.copyFrom(breasts);
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // Idk anymore
        breastsWear.visible = jacket.visible;

        super.renderToBuffer(matrices, vertices, light, overlay, color);

        if (currentEntity != null) {
            wildfireRenderer.render(matrices, vertices, light, overlay, color, currentEntity, this.body,
                    currentPartialTicks,
                    createPhysicsConfig(currentEntity));
        }
    }

    private BreastPhysics.PhysicsConfig createPhysicsConfig(T entity) {
        return new PlayerPhysicsConfig(entity);
    }

    private class PlayerPhysicsConfig implements BreastPhysics.PhysicsConfig {
        private final T entity;
        private final Genetics genetics;

        public PlayerPhysicsConfig(T entity) {
            this.entity = entity;
            this.genetics = CommonVillagerModel.getVillager(entity).getGenetics();
        }

        @Override
        public float getBustSize() {
            return genetics.getGene(Genetics.BREAST);
        }

        @Override
        public boolean canHaveBreasts() {
            return CommonVillagerModel.getVillager(entity).getGenetics().getGender() == Gender.FEMALE;
        }

        @Override
        public float getBounceMultiplier() {
            return genetics.getGene(Genetics.BOUNCE_MULTIPLIER);
        }

        @Override
        public float getFloppiness() {
            return genetics.getGene(Genetics.FLOPPINESS);
        }

        @Override
        public boolean isUniboob() {
            return genetics.getGene(Genetics.UNIBOOB) > 0.5f;
        }

        @Override
        public boolean getArmorPhysicsOverride() {
            return false;
        }

        @Override
        public float getBreastXOffset() {
            return genetics.getGene(Genetics.BREAST_X_OFFSET);
        }

        @Override
        public float getBreastYOffset() {
            return genetics.getGene(Genetics.BREAST_Y_OFFSET);
        }

        @Override
        public float getBreastZOffset() {
            return genetics.getGene(Genetics.BREAST_Z_OFFSET);
        }

        @Override
        public float getCleavage() {
            return genetics.getGene(Genetics.CLEAVAGE);
        }
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
        return ImmutableList.of(breasts, breastsWear);
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

        setPhysicsEntity(villager, animationProgress - villager.tickCount);
    }

    public void setPhysicsEntity(T villager, float partialTicks) {
        this.currentEntity = villager;
        this.currentPartialTicks = partialTicks;

        // Hide default breasts
        this.breasts.visible = false;
        this.breastsWear.visible = false;

        // Physics Tick
        net.conczin.mca.client.physics.PhysicsState state = net.conczin.mca.client.physics.PhysicsState.get(villager);
        if (villager.level().isClientSide && state.lastTick != villager.tickCount) {
            state.lastTick = villager.tickCount;
            state.leftPhysics.update(villager,
                    net.conczin.mca.client.render.wildfire.IGenderArmor
                            .getArmorConfig(villager.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)),
                    createPhysicsConfig(villager));
            state.rightPhysics.update(villager,
                    net.conczin.mca.client.render.wildfire.IGenderArmor
                            .getArmorConfig(villager.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST)),
                    createPhysicsConfig(villager));
        }
    }

    public <M extends HumanoidModel<T>> void copyVisibility(M model) {
        head.visible = model.head.visible;
        hat.visible = model.head.visible;
        body.visible = model.body.visible;
        jacket.visible = model.body.visible;
        breasts.visible = false; // model.body.visible;
        breastsWear.visible = false; // model.body.visible;
        leftArm.visible = model.leftArm.visible;
        leftSleeve.visible = model.leftArm.visible;
        rightArm.visible = model.rightArm.visible;
        rightSleeve.visible = model.rightArm.visible;
        leftLeg.visible = model.leftLeg.visible;
        leftPants.visible = model.leftLeg.visible;
        rightLeg.visible = model.rightLeg.visible;
        rightPants.visible = model.rightLeg.visible;
    }
}
