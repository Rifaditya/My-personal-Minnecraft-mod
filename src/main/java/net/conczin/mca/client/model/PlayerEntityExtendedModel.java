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

    // In 1.21.11, copyPropertiesTo may have been removed or renamed
    public void copyPropertiesTo(HumanoidModel<S> target) {
        // 1.21.11: super.copyPropertiesTo removed - using direct copy

        if (target instanceof PlayerEntityExtendedModel<S> playerTarget) {
            copyAttributes(playerTarget);
        }
        if (target instanceof PlayerArmorExtendedModel<S> armorTarget) {
            copyAttributes(armorTarget);
        }
    }

    private void copyAttributes(PlayerEntityExtendedModel<S> target) {
        ModelPartHelper.copyTransform(target.leftPants, leftPants);
        ModelPartHelper.copyTransform(target.rightPants, rightPants);
        ModelPartHelper.copyTransform(target.leftSleeve, leftSleeve);
        ModelPartHelper.copyTransform(target.rightSleeve, rightSleeve);
        ModelPartHelper.copyTransform(target.jacket, jacket);
        ModelPartHelper.copyTransform(target.breastsWear, breastsWear);

        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        ModelPartHelper.copyTransform(target.breasts, breasts);
    }

    private void copyAttributes(PlayerArmorExtendedModel<S> target) {
        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        ModelPartHelper.copyTransform(target.breasts, breasts);
    }

    // renderToBuffer is now final in Model - commented out
    // Use renderCommon directly from render layer
    // breastsWear.visible = jacket.visible;
    // renderCommon(matrices, vertices, light, overlay, color);

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
        // 1.21.11: headParts() removed - returning custom list
        return ImmutableList.of(head, hat);
    }

    @Override
    public Iterable<ModelPart> getCommonBodyParts() {
        // 1.21.11: bodyParts() removed - returning custom list
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg);
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
        ModelPartHelper.copyTransform(leftPants, leftLeg);
        ModelPartHelper.copyTransform(rightPants, rightLeg);
        ModelPartHelper.copyTransform(leftSleeve, leftArm);
        ModelPartHelper.copyTransform(rightSleeve, rightArm);
        ModelPartHelper.copyTransform(jacket, body);
        ModelPartHelper.copyTransform(breastsWear, breasts);
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
