package net.conczin.mca.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.Config;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.conczin.mca.entity.ai.relationship.VillagerDimensions;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * VillagerEntityBaseModelMCA - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity type parameters
 */
public class VillagerEntityBaseModelMCA<S extends VillagerLikeRenderState> extends HumanoidModel<S>
        implements CommonVillagerModel<S> {
    protected static final String BREASTS = "breasts";

    public final ModelPart breasts;

    final VillagerDimensions.Mutable dimensions = new VillagerDimensions.Mutable(AgeState.ADULT);
    float breastSize;

    public VillagerEntityBaseModelMCA(ModelPart root) {
        super(root);
        this.breasts = root.getChild(BREASTS);
    }

    public static MeshDefinition getModelData(CubeDeformation dilation) {
        MeshDefinition modelData = HumanoidModel.createMesh(dilation, 0.0f);
        PartDefinition data = modelData.getRoot();

        data.addOrReplaceChild(BREASTS, newBreasts(dilation, 0), PartPose.ZERO);

        return modelData;
    }

    protected static CubeListBuilder newBreasts(CubeDeformation dilation, int oy) {
        CubeListBuilder builder = CubeListBuilder.create();
        if (Config.getInstance().enableBoobs) {
            builder.texOffs(18, 21 + oy).addBox(-3.25F, -1.25F, -1.5F, 6, 3, 3, dilation);
        }
        return builder;
    }

    // headParts() and bodyParts() no longer overridable in 1.21.11 HumanoidModel
    // Keeping as public methods for internal use
    public Iterable<ModelPart> headParts() {
        return ImmutableList.of(head, hat);
    }

    public Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg);
    }

    @Override
    public void setupAnim(S state) {
        // In 1.21.11, setupAnim only takes the RenderState
        // Animation calculations now use the state data
        super.setupAnim(state);

        // Get animation values from state
        float limbAngle = state.walkAnimationPos;
        float limbDistance = state.walkAnimationSpeed;
        float animationProgress = state.ageInTicks;

        if (state.ageState == AgeState.BABY && !state.isPassenger) {
            limbDistance = (float) Math.sin(state.tickCount / 12F);
            limbAngle = (float) Math.cos(state.tickCount / 9F) * 3;
        }

        // Baby boost adjustment
        if (state.isBabyVillager) {
            limbAngle /= 3.0f;
        }

        // Scale adjustment
        limbAngle /= (0.2f + state.verticalScaleFactor);

        // Panicking animation
        if (state.isPanicking) {
            float toRadians = (float) Math.PI / 180;

            float armRaise = (((float) Math.sin(animationProgress / 5) * 30 - 180)
                    + ((float) Math.sin(animationProgress / 3) * 3))
                    * toRadians;
            float waveSideways = ((float) Math.sin(animationProgress / 2) * 12 - 17) * toRadians;

            this.leftArm.xRot = armRaise;
            this.leftArm.zRot = -waveSideways;
            this.rightArm.xRot = -armRaise;
            this.rightArm.zRot = waveSideways;
        }

        applyVillagerDimensions(state, state.isCrouching);
    }

    // copyPropertiesTo signature may have changed in 1.21.11
    public void copyPropertiesToModel(HumanoidModel<S> target) {
        // super.copyPropertiesTo(target); // May not exist in parent

        if (target instanceof VillagerEntityBaseModelMCA<S> m) {
            copyCommonAttributes(m);

            m.breasts.visible = breasts.visible;
            ModelPartHelper.copyTransform(m.breasts, breasts);
        }
    }

    // renderToBuffer is now final in Model - use renderCommon directly
    // Original: renderCommon(matrices, vertices, light, overlay, color);

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
}
