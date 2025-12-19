package net.conczin.mca.client.render.layer;

import net.conczin.mca.MCA;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.conczin.mca.client.resources.ColorPalette;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;

/**
 * SkinLayer - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity access
 */
public class SkinLayer<S extends VillagerLikeRenderState, M extends HumanoidModel<S>> extends VillagerLayer<S, M> {
    public SkinLayer(RenderLayerParent<S, M> renderer, M model) {
        super(renderer, model);
    }

    @Override
    public Identifier getSkin(S state) {
        int skin = (int) Math.min(4, Math.max(0, state.skinGene * 5));
        return cached("skins/skin/" + state.gender.getDataName() + "/" + skin + ".png", MCA::locate);
    }

    @Override
    public int getColor(S state) {
        float albinism = state.hasAlbinism ? 0.1f : 1.0f;

        return ColorPalette.SKIN.getColor(
                state.melaninGene * albinism,
                state.hemoglobinGene * albinism,
                state.infectionProgress);
    }
}

