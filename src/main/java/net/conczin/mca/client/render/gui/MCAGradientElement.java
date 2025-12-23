package net.conczin.mca.client.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

/**
 * MCA render element for vertical gradients.
 * Renders a rectangle with startColor at top and endColor at bottom.
 * 
 * Based on 1.21.11 GuiElementRenderState API.
 */
public record MCAGradientElement(
        Matrix3x2f pose,
        float left, float top, float right, float bottom,
        int startRed, int startGreen, int startBlue, int startAlpha,
        int endRed, int endGreen, int endBlue, int endAlpha,
        ScreenRectangle bounds) implements GuiElementRenderState {

    /**
     * Convenience constructor with colors as ARGB ints.
     */
    public MCAGradientElement(
            Matrix3x2f pose,
            float left, float top, float right, float bottom,
            int startColorARGB, int endColorARGB) {
        this(
                pose,
                left, top, right, bottom,
                (startColorARGB >> 16) & 0xFF, (startColorARGB >> 8) & 0xFF, startColorARGB & 0xFF,
                (startColorARGB >> 24) & 0xFF,
                (endColorARGB >> 16) & 0xFF, (endColorARGB >> 8) & 0xFF, endColorARGB & 0xFF,
                (endColorARGB >> 24) & 0xFF,
                new ScreenRectangle((int) left, (int) top, (int) (right - left), (int) (bottom - top))
                        .transformMaxBounds(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(pose, right, top).setColor(startRed, startGreen, startBlue, startAlpha);
        consumer.addVertexWith2DPose(pose, left, top).setColor(startRed, startGreen, startBlue, startAlpha);
        consumer.addVertexWith2DPose(pose, left, bottom).setColor(endRed, endGreen, endBlue, endAlpha);
        consumer.addVertexWith2DPose(pose, right, bottom).setColor(endRed, endGreen, endBlue, endAlpha);
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }
}
