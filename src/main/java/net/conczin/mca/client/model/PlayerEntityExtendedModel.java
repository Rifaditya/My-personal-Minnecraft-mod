package net.conczin.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import static net.conczin.mca.client.model.VillagerEntityBaseModelMCA.BREASTS;
import static net.conczin.mca.client.model.VillagerEntityModelMCA.BREASTPLATE;

/**
 * PlayerEntityExtendedModel - updated for 1.21.11 API
 * Now extends HumanoidModel with VillagerLikeRenderState instead of PlayerModel
 * PlayerModel no longer takes type parameters in 1.21.11
 */
public class PlayerEntityExtendedModel<S extends VillagerLikeRenderState> extends HumanoidModel<S>
        implements CommonVillagerModel<S> {
    public final ModelPart breasts;
    public final ModelPart breastsWear;

    // Player model parts (mirroring what PlayerModel had)
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart jacket;

    final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
    float breastSize;

    public PlayerEntityExtendedModel(ModelPart root) {
        super(root);
        this.breasts = root.getChild(BREASTS);
        this.breastsWear = root.getChild(BREASTPLATE);

        // Initialize player-specific parts
        this.leftPants = root.getChild("left_pants");
        this.rightPants = root.getChild("right_pants");
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightSleeve = root.getChild("right_sleeve");
        this.jacket = root.getChild("jacket");
    }

    @Override
    public void copyPropertiesTo(HumanoidModel<S> target) {
        super.copyPropertiesTo(target);

        if (target instanceof PlayerEntityExtendedModel<S> playerTarget) {
            copyAttributes(playerTarget);
        }
        if (target instanceof PlayerArmorExtendedModel<S> armorTarget) {
            copyAttributes(armorTarget);
        }
    }

    private void copyAttributes(PlayerEntityExtendedModel<S> target) {
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

    private void copyAttributes(PlayerArmorExtendedModel<S> target) {
        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        target.breasts.copyFrom(breasts);
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // Idk anymore
        breastsWear.visible = jacket.visible;

        renderCommon(matrices, vertices, light, overlay, color);
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
    public void setupAnim(S state) {
        super.setupAnim(state);

        // Use state data instead of entity access
        if (state.ageState == AgeState.BABY && !state.isPassenger) {
            // Animation adjustments already handled in state
        }

        applyVillagerDimensions(state, state.isCrouching);

        // Copy wear parts from main parts
        leftPants.copyFrom(leftLeg);
        rightPants.copyFrom(rightLeg);
        leftSleeve.copyFrom(leftArm);
        rightSleeve.copyFrom(rightArm);
        jacket.copyFrom(body);
        breastsWear.copyFrom(breasts);
    }

    public <M extends HumanoidModel<S>> void copyVisibility(M model) {
        head.visible = model.head.visible;
        hat.visible = model.head.visible;
        body.visible = model.body.visible;
        jacket.visible = model.body.visible;
        breasts.visible = model.body.visible;
        breastsWear.visible = model.body.visible;
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
