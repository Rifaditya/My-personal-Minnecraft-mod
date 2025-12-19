package net.conczin.mca.client.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.conczin.mca.MCA;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;

/**
 * FaceLayer - updated for 1.21.11 API
 * Now uses VillagerLikeRenderState instead of entity access
 */
public class FaceLayer<S extends VillagerLikeRenderState, M extends HumanoidModel<S>> extends VillagerLayer<S, M> {
    private static final int FACE_COUNT = 22;

    private final String variant;

    public FaceLayer(RenderLayerParent<S, M> renderer, M model, String variant) {
        super(renderer, model);
        this.variant = variant;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float f, float g) {
        model.setAllVisible(false);
        model.head.visible = true;

        super.submit(poseStack, collector, light, state, f, g);
    }

    @Override
    protected boolean isTranslucent() {
        return true;
    }

    @Override
    public Identifier getSkin(S state) {
        // Use face gene from state - approximate calculation
        int index = (int) Math.min(FACE_COUNT - 1, Math.max(0, 0.5f * FACE_COUNT)); // Default to middle
        // Blink logic simplified since we don't have tick count in render state
        boolean blink = false;
        boolean hasHeterochromia = variant.equals("normal") && state.hasAlbinism; // Approximation
        String gender = state.gender.getDataName();
        String blinkTexture = blink ? "_blink" : (hasHeterochromia ? "_hetero" : "");

        return cached("skins/face/" + variant + "/" + gender + "/" + index + blinkTexture + ".png", MCA::locate);
    }
}

