package net.conczin.mca.client.render;

import net.conczin.mca.entity.ReaperAttackState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Render state for GrimReaperRenderer (1.21.11 API)
 * Extends HumanoidRenderState for humanoid mob rendering
 */
public class GrimReaperRenderState extends HumanoidRenderState {
    // Attack state for animation
    public ReaperAttackState attackState = ReaperAttackState.IDLE;
}
