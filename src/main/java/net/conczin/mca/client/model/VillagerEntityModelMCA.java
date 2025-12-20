package net.conczin.mca.client.model;

import com.google.common.collect.ImmutableList;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * VillagerEntityModelMCA - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity type parameters
 */
public class VillagerEntityModelMCA<S extends VillagerLikeRenderState> extends VillagerEntityBaseModelMCA<S> {
    protected static final String BREASTPLATE = "breastplate";

    public final ModelPart breastsWear;
    public final ModelPart leftArmwear;
    public final ModelPart rightArmwear;
    public final ModelPart leftLegwear;
    public final ModelPart rightLegwear;
    public final ModelPart bodyWear;

    private boolean wearsHidden;

    public VillagerEntityModelMCA(ModelPart tree) {
        super(tree);
        bodyWear = tree.getChild(PartNames.JACKET);
        leftArmwear = tree.getChild("left_sleeve");
        rightArmwear = tree.getChild("right_sleeve");
        leftLegwear = tree.getChild("left_pants");
        rightLegwear = tree.getChild("right_pants");

        breastsWear = tree.getChild(BREASTPLATE);
    }

    //
    // body - 0 (body.body 0.0)
    // face - 0 (body.head 0.01)
    // clothing - 1 (clothing.body 0.075)
    // hair - 2 (hair.body 0.1) + (hair.hat 0.1 + 0.3 = 0.4)
    // hood - 3 (clothing.hat 0.075 + 0.5 = 0.575)

    public static MeshDefinition hairData(CubeDeformation dilation) {
        MeshDefinition modelData = bodyData(dilation);
        PartDefinition root = modelData.getRoot();
        root.addOrReplaceChild(PartNames.HAT,
                CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, dilation.extend(0.3F)),
                PartPose.ZERO);
        return modelData;
    }

    public static MeshDefinition bodyData(CubeDeformation dilation) {
        return bodyData(dilation, false);
    }

    public static MeshDefinition bodyData(CubeDeformation dilation, boolean slim) {
        MeshDefinition modelData = PlayerModel.createMesh(dilation, slim);
        PartDefinition root = modelData.getRoot();
        root.addOrReplaceChild(BREASTS, newBreasts(dilation, 0), PartPose.ZERO);
        root.addOrReplaceChild(BREASTPLATE, newBreasts(dilation.extend(0.1F), 16), PartPose.ZERO);
        return modelData;
    }

    public static MeshDefinition armorData(CubeDeformation dilation) {
        MeshDefinition modelData = HumanoidModel.createMesh(dilation, 0.0f);
        PartDefinition root = modelData.getRoot();
        root.addOrReplaceChild(BREASTS, newBreasts(dilation, 0), PartPose.ZERO);
        return modelData;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(body, rightArm, leftArm, rightLeg, leftLeg, bodyWear, leftLegwear, rightLegwear,
                leftArmwear, rightArmwear);
    }

    @Override
    public Iterable<ModelPart> getBreastParts() {
        return ImmutableList.of(breasts, breastsWear);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        ModelPartHelper.copyTransform(leftLegwear, leftLeg);
        ModelPartHelper.copyTransform(rightLegwear, rightLeg);
        ModelPartHelper.copyTransform(leftArmwear, leftArm);
        ModelPartHelper.copyTransform(rightArmwear, rightArm);
        ModelPartHelper.copyTransform(bodyWear, body);
        ModelPartHelper.copyTransform(breastsWear, breasts);
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);

        leftArmwear.visible = !wearsHidden && visible;
        rightArmwear.visible = !wearsHidden && visible;
        leftLegwear.visible = !wearsHidden && visible;
        rightLegwear.visible = !wearsHidden && visible;
        bodyWear.visible = !wearsHidden && visible;
    }

    public VillagerEntityModelMCA<S> hideWears() {
        wearsHidden = true;
        breastsWear.visible = false;
        leftArmwear.visible = false;
        rightArmwear.visible = false;
        leftLegwear.visible = false;
        rightLegwear.visible = false;
        bodyWear.visible = false;
        return this;
    }

    // copyPropertiesTo may not exist in parent in 1.21.11
    public void copyPropertiesToModel(HumanoidModel<S> target) {
        // super.copyPropertiesTo(target);
        if (target instanceof VillagerEntityModelMCA) {
            copyAttributes((VillagerEntityModelMCA<S>) target);
        }
    }

    private void copyAttributes(VillagerEntityModelMCA<S> target) {
        ModelPartHelper.copyTransform(target.leftLegwear, leftLegwear);
        ModelPartHelper.copyTransform(target.rightLegwear, rightLegwear);
        ModelPartHelper.copyTransform(target.leftArmwear, leftArmwear);
        ModelPartHelper.copyTransform(target.rightArmwear, rightArmwear);
        ModelPartHelper.copyTransform(target.bodyWear, bodyWear);
        ModelPartHelper.copyTransform(target.breastsWear, breastsWear);
    }

    public <M extends HumanoidModel<S>> void copyVisibility(M model) {
        head.visible = model.head.visible;
        hat.visible = model.head.visible;
        body.visible = model.body.visible;
        bodyWear.visible = model.body.visible;
        breasts.visible = model.body.visible;
        breastsWear.visible = model.body.visible;
        leftArm.visible = model.leftArm.visible;
        leftArmwear.visible = model.leftArm.visible;
        rightArm.visible = model.rightArm.visible;
        rightArmwear.visible = model.rightArm.visible;
        leftLeg.visible = model.leftLeg.visible;
        leftLegwear.visible = model.leftLeg.visible;
        rightLeg.visible = model.rightLeg.visible;
        rightLegwear.visible = model.rightLeg.visible;
    }
}
