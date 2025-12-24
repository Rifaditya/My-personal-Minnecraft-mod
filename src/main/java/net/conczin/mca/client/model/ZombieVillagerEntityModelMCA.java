package net.conczin.mca.client.model;

import net.conczin.mca.client.render.VillagerLikeRenderState;
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
        // 1.21.11: AnimationUtils API changed - using hardcoded raised arm position
        leftArm.xRot = -1.5f; // Raised arm
        rightArm.xRot = -1.5f;
        leftArmwear.setRotation(leftArm.xRot, leftArm.yRot, leftArm.zRot);
        rightArmwear.setRotation(rightArm.xRot, rightArm.yRot, rightArm.zRot);
    }
}
