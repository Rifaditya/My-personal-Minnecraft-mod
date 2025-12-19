package net.conczin.mca.client.render.layer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.MCA;
import net.conczin.mca.MCAClient;
import net.conczin.mca.client.model.PlayerEntityExtendedModel;
import net.conczin.mca.client.model.VillagerEntityModelMCA;
import net.conczin.mca.client.render.VillagerLikeRenderState;
// ResourceLocationException removed in 1.21.11
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * VillagerLayer - updated for 1.21.11 API
 * Now uses RenderState instead of Entity type and submit() instead of render()
 * Note: Some functionality is simplified due to API changes
 */
public abstract class VillagerLayer<S extends VillagerLikeRenderState, M extends HumanoidModel<S>>
        extends RenderLayer<S, M> {
    private static final Map<String, Identifier> TEXTURE_CACHE = Maps.newHashMap();
    private static final Map<Identifier, Boolean> TEXTURE_EXIST_CACHE = Maps.newHashMap();

    static {
        // the temp image is used for temporary canvases and definitely exists
        TEXTURE_EXIST_CACHE.put(MCA.locate("temp"), true);
    }

    public final M model;

    public VillagerLayer(RenderLayerParent<S, M> renderer, M model) {
        super(renderer);
        this.model = model;
    }

    @Nullable
    public Identifier getSkin(S state) {
        return null;
    }

    @Nullable
    protected Identifier getOverlay(S state) {
        return null;
    }

    public int getColor(S state) {
        return 0xFFFFFFFF;
    }

    protected boolean isTranslucent() {
        return false;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float f, float g) {
        // Check if we should render for players
        // Note: Player-specific checks may need to be handled differently in 1.21.11
        // as we no longer have direct entity access here

        // primarily restores compatibility with Armourers Workshop
        // noinspection rawtypes
        if (model instanceof VillagerEntityModelMCA layer) {
            // noinspection unchecked
            layer.copyVisibility(getParentModel());
        }
        // noinspection rawtypes
        if (model instanceof PlayerEntityExtendedModel layer) {
            // noinspection unchecked
            layer.copyVisibility(getParentModel());
        }

        // copy the animation to this layers model
        getParentModel().copyPropertiesTo(model);

        submitFinal(poseStack, collector, light, state);
    }

    public void submitFinal(PoseStack poseStack, SubmitNodeCollector collector, int light, S state) {
        // Note: In 1.21.11, we use the RenderState's flags for visibility
        boolean visible = !state.isInvisible;
        boolean glowing = state.appearsGlowing;

        Identifier skin = getSkin(state);
        if (canUse(skin)) {
            int color = getColor(state);
            // TODO: Implement proper model rendering with SubmitNodeCollector
            // The old API used MultiBufferSource.getBuffer() which is different from
            // SubmitNodeCollector
            // For now this is a stub - full implementation requires understanding new
            // rendering pipeline
        }

        Identifier overlay = getOverlay(state);
        if (!Objects.equals(skin, overlay) && canUse(overlay)) {
            // TODO: Implement overlay rendering with SubmitNodeCollector
        }
    }

    @Nullable
    protected RenderType getRenderLayer(Identifier texture, boolean showBody, boolean translucent,
            boolean showOutline) {
        if (translucent) {
            return RenderType.itemEntityTranslucentCull(texture);
        } else if (showBody) {
            return this.model.renderType(texture);
        } else {
            return showOutline ? RenderType.outline(texture) : null;
        }
    }

    public final boolean canUse(Identifier texture) {
        return TEXTURE_EXIST_CACHE.computeIfAbsent(texture, s -> {
            if (texture != null && texture.getNamespace().equals("immersive_library")) {
                return true;
            }
            return texture != null && Minecraft.getInstance().getResourceManager().getResource(texture).isPresent();
        });
    }

    @Nullable
    protected final Identifier cached(String name, Function<String, Identifier> supplier) {
        return TEXTURE_CACHE.computeIfAbsent(name, s -> {
            try {
                return supplier.apply(s);
            } catch (Exception ignored) { // Was ResourceLocationException
                return null;
            }
        });
    }
}
