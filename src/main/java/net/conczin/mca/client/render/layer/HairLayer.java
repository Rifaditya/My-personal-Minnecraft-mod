package net.conczin.mca.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.client.gui.immersive_library.SkinCache;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.conczin.mca.client.resources.ColorPalette;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;

/**
 * HairLayer - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity access
 */
public class HairLayer<S extends VillagerLikeRenderState, M extends HumanoidModel<S>> extends VillagerLayer<S, M> {
    public HairLayer(RenderLayerParent<S, M> renderer, M model) {
        super(renderer, model);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float f, float g) {
        model.setAllVisible(true);
        this.model.leftLeg.visible = false;
        this.model.rightLeg.visible = false;

        super.submit(poseStack, collector, light, state, f, g);
    }

    @Override
    public Identifier getSkin(S state) {
        String identifier = state.hair;
        if (identifier.startsWith("immersive_library:")) {
            return SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring(18)));
        }
        return cached(identifier, Identifier::parse);
    }

    @Override
    protected Identifier getOverlay(S state) {
        return cached(state.hair.replace(".png", "_overlay.png"), Identifier::parse);
    }

    @Override
    public int getColor(S state) {
        // Rainbow trait not easily implemented without tick data
        // Hair dye color could be stored in state if needed

        float albinism = state.hasAlbinism ? 0.1f : 1.0f;

        return ColorPalette.HAIR.getColor(
                state.eumelaninGene * albinism,
                state.pheomelaninGene * albinism,
                0);
    }
}

