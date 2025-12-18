package net.conczin.mca.client.render.layer;

import net.conczin.mca.client.gui.immersive_library.SkinCache;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;

/**
 * ClothingLayer - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity access
 */
public class ClothingLayer<S extends VillagerLikeRenderState, M extends HumanoidModel<S>> extends VillagerLayer<S, M> {
    private final String variant;

    public ClothingLayer(RenderLayerParent<S, M> renderer, M model, String variant) {
        super(renderer, model);
        this.variant = variant;
    }

    @Override
    public Identifier getSkin(S state) {
        String v = state.isBurned ? "burnt" : variant;
        String identifier = state.clothes;
        if (identifier.startsWith("immersive_library:")) {
            return SkinCache.getTextureIdentifier(Integer.parseInt(identifier.substring(18)));
        }
        return cached(identifier + v, clothes -> {
            Identifier id = Identifier.parse(state.clothes);

            Identifier idNew = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("normal", v));
            if (canUse(idNew)) {
                return idNew;
            }

            return id;
        });
    }
}
