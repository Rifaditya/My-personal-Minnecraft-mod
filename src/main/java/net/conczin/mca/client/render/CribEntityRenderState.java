package net.conczin.mca.client.render;

import net.conczin.mca.entity.CribWoodType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

/**
 * Render state for CribEntityRenderer (1.21.11 API)
 * Holds all data needed for rendering, extracted from the crib entity
 */
public class CribEntityRenderState extends EntityRenderState {
    public CribWoodType woodType = CribWoodType.OAK;
    public DyeColor color = DyeColor.WHITE;
    public ItemStack babyItem = ItemStack.EMPTY;
    public Identifier texture = null;
    public int entityId = 0;
}
