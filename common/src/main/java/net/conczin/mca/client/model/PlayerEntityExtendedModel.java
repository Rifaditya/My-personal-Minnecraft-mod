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
import net.conczin.mca.client.firstperson.FirstPersonLogic;
import net.conczin.mca.client.firstperson.FPMCompat;

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
        System.err.println("MCA DEBUG: PlayerEntityExtendedModel.renderToBuffer CALLED!");

        // Ensure static breasts are hidden so we don't double render or render static
        // ones
        this.breasts.visible = false;
        this.breastsWear.visible = false;

        // IMPORTANT: Render breasts FIRST, before super.renderToBuffer
        // This ensures hair and other layers render ON TOP of breasts
        renderBreasts(matrices, vertices, light, overlay, color);

        // Now render the base model (including hair) which will draw over breasts
        super.renderToBuffer(matrices, vertices, light, overlay, color);
    }

    @Override
    public BreastPhysics.PhysicsConfig makePhysicsConfig(T entity) {
        return new PlayerPhysicsConfig(entity);
    }

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
            return genetics.getArmorPhysicsOverride();
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

        @Override
        public float getElasticity() {
            return genetics.getGene(Genetics.ELASTICITY);
        }

        @Override
        public float getMass() {
            return genetics.getGene(Genetics.MASS);
        }

        @Override
        public float getShape() {
            return genetics.getGene(Genetics.SHAPE);
        }

        @Override
        public float getNippleSize() {
            return genetics.getGene(Genetics.NIPPLE_SIZE);
        }

        @Override
        public float getAreolaColor() {
            return genetics.getGene(Genetics.AREOLA_COLOR);
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

        try {
            if (CommonVillagerModel.getVillager(villager).getAgeState() == AgeState.BABY && !villager.isPassenger()) {
                limbDistance = (float) Math.sin(villager.tickCount / 12F);
                limbAngle = (float) Math.cos(villager.tickCount / 9F) * 3;
                headYaw += (float) Math.sin(villager.tickCount / 2F);
            }

            super.setupAnim(villager, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
            applyVillagerDimensions(CommonVillagerModel.getVillager(villager), villager.isCrouching());

            // Apply first-person visibility logic
            applyFirstPersonVisibility(villager);

            setPhysicsEntity(villager, animationProgress - villager.tickCount);
        } catch (Exception e) {
            System.err.println("MCA ERROR: Error in PlayerEntityExtendedModel.setupAnim");
            e.printStackTrace();
        }
    }

    public void setPhysicsEntity(T villager, float partialTicks) {
        updatePhysics(villager, partialTicks);
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

    /**
     * Applies first-person visibility logic to model parts.
     * Hides head, arms, and body based on camera state and config settings.
     * 
     * ====================================================================
     * FPM COMPATIBILITY
     * ====================================================================
     * If FirstPersonModel by tr7zw is installed, this method does NOTHING
     * and lets FPM handle all first-person rendering logic.
     * 
     * This allows users to:
     * - Use MCA alone (built-in FPM features)
     * - Install FPM for full GUI and advanced features
     * - Run mods that depend on FPM
     * ====================================================================
     * 
     * @param entity The entity being rendered
     */
    private void applyFirstPersonVisibility(T entity) {
        // CRITICAL: If FPM is installed, let it handle everything
        if (FPMCompat.isFPMInstalled()) {
            // FPM is present - defer all first-person logic to FPM
            // This ensures compatibility with FPM's GUI and other FPM-dependent mods
            return;
        }

        // FPM not installed - use MCA's built-in first-person logic

        // Only apply to the camera entity in first person
        if (!FirstPersonLogic.isCameraEntityFirstPerson(entity)) {
            return;
        }

        // Hide head in first person
        if (FirstPersonLogic.shouldHideHead()) {
            head.visible = false;
            hat.visible = false;
        }

        // Hide body when swimming/crawling
        if (FirstPersonLogic.shouldHideBody(entity)) {
            body.visible = false;
            jacket.visible = false;
            breasts.visible = false;
            breastsWear.visible = false;
        }

        // Handle arms based on dynamic hands setting
        if (FirstPersonLogic.shouldHideArms(entity)) {
            leftArm.visible = false;
            leftSleeve.visible = false;
            rightArm.visible = false;
            rightSleeve.visible = false;
        } else {
            // Apply dynamic arm offset when looking down
            float armOffset = FirstPersonLogic.getDynamicArmOffset(entity);
            if (armOffset > 0) {
                leftArm.xRot += armOffset;
                rightArm.xRot += armOffset;
            }
        }
    }
}
