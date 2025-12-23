package net.conczin.mca.client.render.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

/**
 * MCA GUI rendering utilities for 1.21.11.
 * Provides helper methods for common rendering operations that changed in
 * 1.21.11.
 * 
 * Key API changes in 1.21.11:
 * - context.blit() replaced with blitSprite() or submitGuiElement()
 * - RenderSystem methods (enableBlend, setShader) removed
 * - context.pose() returns Matrix3x2fStack instead of PoseStack
 */
public final class MCAGuiRenderer {

    private MCAGuiRenderer() {
    } // Utility class

    /**
     * Gets a TextureSetup for the given texture identifier.
     * Uses the TextureManager to properly bind the texture.
     */
    public static TextureSetup getTextureSetup(Identifier texture) {
        TextureManager manager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = manager.getTexture(texture);
        return TextureSetup.singleTexture(abstractTexture.getTextureView(), abstractTexture.getSampler());
    }

    /**
     * Draws a textured rectangle using UV coordinates.
     * Replacement for context.blit(texture, x, y, u, v, width, height, texWidth,
     * texHeight)
     * 
     * @param graphics  The GuiGraphics context
     * @param texture   The texture identifier
     * @param x         Left position
     * @param y         Top position
     * @param u         Texture U start (0-texWidth)
     * @param v         Texture V start (0-texHeight)
     * @param width     Width to draw
     * @param height    Height to draw
     * @param texWidth  Total texture width (usually 256)
     * @param texHeight Total texture height (usually 256)
     */
    public static void drawTexture(
            GuiGraphics graphics,
            Identifier texture,
            int x, int y,
            int u, int v,
            int width, int height,
            int texWidth, int texHeight) {
        drawTexture(graphics, texture, x, y, u, v, width, height, texWidth, texHeight, 0xFFFFFFFF);
    }

    /**
     * Draws a textured rectangle with color tint.
     */
    public static void drawTexture(
            GuiGraphics graphics,
            Identifier texture,
            int x, int y,
            int u, int v,
            int width, int height,
            int texWidth, int texHeight,
            int colorARGB) {
        float u1 = (float) u / texWidth;
        float u2 = (float) (u + width) / texWidth;
        float v1 = (float) v / texHeight;
        float v2 = (float) (v + height) / texHeight;

        TextureSetup textureSetup = getTextureSetup(texture);

        graphics.guiRenderState.submitGuiElement(new MCATexturedQuadElement(
                new Matrix3x2f(graphics.pose()),
                textureSetup,
                x, x + width, y, y + height,
                colorARGB,
                u1, u2, v1, v2,
                graphics.scissorStack.peek()));
    }

    /**
     * Draws a simple textured quad covering the entire texture.
     * Use for textures that don't need UV sub-regions.
     */
    public static void drawFullTexture(
            GuiGraphics graphics,
            Identifier texture,
            int x, int y,
            int width, int height) {
        drawTexture(graphics, texture, x, y, 0, 0, width, height, width, height);
    }

    /**
     * Draws a sprite using the simple blitSprite API.
     * For textures registered as sprites, not raw textures with UV.
     */
    public static void drawSprite(
            GuiGraphics graphics,
            Identifier sprite,
            int x, int y,
            int width, int height) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
    }

    /**
     * Draws a sprite with color tint.
     */
    public static void drawSprite(
            GuiGraphics graphics,
            Identifier sprite,
            int x, int y,
            int width, int height,
            int colorARGB) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height, colorARGB);
    }

    /**
     * Draws a vertical gradient rectangle.
     * Replacement for RenderSystem-based gradient rendering.
     */
    public static void drawGradient(
            GuiGraphics graphics,
            int x, int y,
            int width, int height,
            int topColorARGB, int bottomColorARGB) {
        graphics.guiRenderState.submitGuiElement(new MCAGradientElement(
                new Matrix3x2f(graphics.pose()),
                x, y, x + width, y + height,
                topColorARGB, bottomColorARGB));
    }

    /**
     * Draws a horizontal gradient rectangle.
     */
    public static void drawHorizontalGradient(
            GuiGraphics graphics,
            int x, int y,
            int width, int height,
            int leftColorARGB, int rightColorARGB) {
        // For horizontal gradient, we need to flip the corners
        Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        graphics.guiRenderState.submitGuiElement(new MCAHorizontalGradientElement(
                pose,
                x, y, x + width, y + height,
                leftColorARGB, rightColorARGB));
    }

    /**
     * Draws a solid color rectangle.
     */
    public static void drawRect(
            GuiGraphics graphics,
            int x, int y,
            int width, int height,
            int colorARGB) {
        graphics.fill(x, y, x + width, y + height, colorARGB);
    }

    /**
     * Draws a rectangle outline.
     */
    public static void drawRectOutline(
            GuiGraphics graphics,
            int x, int y,
            int width, int height,
            int colorARGB) {
        graphics.fill(x, y, x + width, y + 1, colorARGB); // Top
        graphics.fill(x, y + height - 1, x + width, y + height, colorARGB); // Bottom
        graphics.fill(x, y, x + 1, y + height, colorARGB); // Left
        graphics.fill(x + width - 1, y, x + width, y + height, colorARGB); // Right
    }
}
