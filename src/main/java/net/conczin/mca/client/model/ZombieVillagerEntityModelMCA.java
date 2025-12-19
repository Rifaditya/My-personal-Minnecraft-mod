package net.conczin.mca.client.model;

import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;

/**
 * ZombieVillagerEntityModelMCA - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity type parameters
 */
public class ZombieVillagerEntityModelMCA<S extends VillagerLikeRenderState> extends VillagerEntityModelMCA<S> {
    public ZombieVillagerEntityModelMCA(ModelPart tree) {
        super(tree);
    }

    @Override
    public void setupAnim(S state) {
        super.setupAnim(state);
        // Zombie arm animation - in 1.21.11, animateZombieArms takes only 4 args:
        // leftArm, rightArm, isAggressive, ageInTicks
        AnimationUtils.animateZombieArms(leftArm, rightArm, false, state.ageInTicks);
        // ModelPart.copyFrom removed in 1.21.11, using direct property copy
        leftArmwear.setRotation(leftArm.xRot, leftArm.yRot, leftArm.zRot);
        rightArmwear.setRotation(rightArm.xRot, rightArm.yRot, rightArm.zRot);
    }
}
