package net.conczin.mca.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Render state for TombstoneBlockEntityRenderer (1.21.11 API)
 * Holds all data needed for rendering, extracted from the block entity
 * Uses basic types to avoid complex import issues
 */
public class TombstoneBlockEntityRenderState extends BlockEntityRenderState {
    public boolean hasEntity = false;
    public Direction facing = Direction.NORTH;
    public float rotation = 0f;
    public Vec3 nameplateOffset = Vec3.ZERO;
    public int lineWidth = 90;
    public int maxNameHeight = 40;
    public String genderDataName = "male";
    // FlowingText data stored as primitives
    public float nameScale = 1.0f;
    public java.util.List<net.minecraft.util.FormattedCharSequence> nameLines = java.util.Collections.emptyList();
}

