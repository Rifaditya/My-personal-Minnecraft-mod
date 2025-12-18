package net.conczin.mca.client.render;

import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for VillagerLikeEntityMCARenderer (1.21.11 API)
 * Extends HumanoidRenderState for humanoid mob rendering
 * Contains villager-specific state data
 */
public class VillagerLikeRenderState extends HumanoidRenderState {
    public float verticalScaleFactor = 1.0f;
    public float horizontalScaleFactor = 1.0f;
    public AgeState ageState = AgeState.ADULT;
    public boolean isPassenger = false;
    public float infectionProgress = 0f;
    public boolean hasCustomName = false;
    public boolean isInvisibleToPlayer = false;
    public double distanceToPlayer = 0.0;
}
