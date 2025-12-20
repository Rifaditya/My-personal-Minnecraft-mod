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

/**
 * PlayerArmorExtendedModel - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity type parameters
 */
public class PlayerArmorExtendedModel<S extends VillagerLikeRenderState> extends HumanoidModel<S>
        implements CommonVillagerModel<S> {
    public final ModelPart breasts;

    final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
    float breastSize;

    public PlayerArmorExtendedModel(ModelPart root) {
        super(root);
        this.breasts = root.getChild(BREASTS);
    }

    // copyPropertiesTo signature changed in 1.21.11 - no longer overrides
    public void copyPropertiesToModel(HumanoidModel<S> target) {
        // super.copyPropertiesTo(target);

        if (target instanceof PlayerEntityExtendedModel<S> playerTarget) {
            copyAttributes(playerTarget);
        }
    }

    private void copyAttributes(PlayerEntityExtendedModel<S> target) {
        copyCommonAttributes(target);

        target.breasts.visible = breasts.visible;
        ModelPartHelper.copyTransform(target.breasts, breasts);
    }

    // renderToBuffer is now final in Model - commented out
    // Use renderCommon directly from render layer

    @Override
    public ModelPart getBreastPart() {
        return breasts;
    }

    @Override
    public ModelPart getBodyPart() {
        return body;
    }

    // headParts() and bodyParts() no longer accessible in parent
    public Iterable<ModelPart> getCommonHeadParts() {
        return ImmutableList.of(head, hat);
    }

    @Override
    public Iterable<ModelPart> getCommonBodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg);
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

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);

        // Use state data instead of entity access
        applyVillagerDimensions(state, state.isCrouching);
    }
}
