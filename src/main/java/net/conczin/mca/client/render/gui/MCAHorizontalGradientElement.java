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
 * MCA render element for horizontal gradients.
 * Renders a rectangle with leftColor on left side and rightColor on right side.
 * 
 * Based on 1.21.11 GuiElementRenderState API.
 */
public record MCAHorizontalGradientElement(
        Matrix3x2f pose,
        float left, float top, float right, float bottom,
        int leftRed, int leftGreen, int leftBlue, int leftAlpha,
        int rightRed, int rightGreen, int rightBlue, int rightAlpha,
        ScreenRectangle bounds) implements GuiElementRenderState {

    /**
     * Convenience constructor with colors as ARGB ints.
     */
    public MCAHorizontalGradientElement(
            Matrix3x2f pose,
            float left, float top, float right, float bottom,
            int leftColorARGB, int rightColorARGB) {
        this(
                pose,
                left, top, right, bottom,
                (leftColorARGB >> 16) & 0xFF, (leftColorARGB >> 8) & 0xFF, leftColorARGB & 0xFF,
                (leftColorARGB >> 24) & 0xFF,
                (rightColorARGB >> 16) & 0xFF, (rightColorARGB >> 8) & 0xFF, rightColorARGB & 0xFF,
                (rightColorARGB >> 24) & 0xFF,
                new ScreenRectangle((int) left, (int) top, (int) (right - left), (int) (bottom - top))
                        .transformMaxBounds(pose));
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        // Left side uses leftColor, right side uses rightColor
        consumer.addVertexWith2DPose(pose, right, top).setColor(rightRed, rightGreen, rightBlue, rightAlpha);
        consumer.addVertexWith2DPose(pose, left, top).setColor(leftRed, leftGreen, leftBlue, leftAlpha);
        consumer.addVertexWith2DPose(pose, left, bottom).setColor(leftRed, leftGreen, leftBlue, leftAlpha);
        consumer.addVertexWith2DPose(pose, right, bottom).setColor(rightRed, rightGreen, rightBlue, rightAlpha);
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
