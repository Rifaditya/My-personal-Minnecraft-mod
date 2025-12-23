package net.conczin.mca.client.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;

/**
 * MCA render element for textured quads with UV coordinates.
 * Used for rendering texture regions in GUI (color pickers, buttons, etc.)
 * 
 * Based on 1.21.11 GuiElementRenderState API.
 */
public record MCATexturedQuadElement(
        Matrix3x2f pose,
        float left, float right, float top, float bottom,
        int red, int green, int blue, int alpha,
        float u1, float u2, float v1, float v2,
        TextureSetup texture,
        ScreenRectangle bounds,
        ScreenRectangle scissorArea) implements GuiElementRenderState {

    /**
     * Convenience constructor with color as single ARGB int.
     */
    public MCATexturedQuadElement(
            Matrix3x2f pose,
            TextureSetup texture,
            int left, int right, int top, int bottom,
            int colorARGB,
            float u1, float u2, float v1, float v2,
            ScreenRectangle scissorArea) {
        this(
                pose,
                left, right, top, bottom,
                (colorARGB >> 16) & 0xFF, // red
                (colorARGB >> 8) & 0xFF, // green
                colorARGB & 0xFF, // blue
                (colorARGB >> 24) & 0xFF, // alpha
                u1, u2, v1, v2,
                texture,
                new ScreenRectangle(left, top, right - left, bottom - top).transformMaxBounds(pose),
                scissorArea);
    }

    /**
     * Simple constructor with white color and full opacity.
     */
    public MCATexturedQuadElement(
            Matrix3x2f pose,
            TextureSetup texture,
            int left, int right, int top, int bottom,
            float u1, float u2, float v1, float v2,
            ScreenRectangle scissorArea) {
        this(pose, texture, left, right, top, bottom, 0xFFFFFFFF, u1, u2, v1, v2, scissorArea);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(pose, left, bottom).setColor(red, green, blue, alpha).setUv(u1, v2);
        consumer.addVertexWith2DPose(pose, right, bottom).setColor(red, green, blue, alpha).setUv(u2, v2);
        consumer.addVertexWith2DPose(pose, right, top).setColor(red, green, blue, alpha).setUv(u2, v1);
        consumer.addVertexWith2DPose(pose, left, top).setColor(red, green, blue, alpha).setUv(u1, v1);
    }

    @Override
    public TextureSetup textureSetup() {
        return texture;
    }

    @Override
    public ScreenRectangle scissorArea() {
        return scissorArea;
    }
}
