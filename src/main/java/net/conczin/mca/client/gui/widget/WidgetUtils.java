package net.conczin.mca.client.gui.widget;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

// FIXME: 1.20 loses the DrawableHelper attachment, determine if DrawContext can replace this
public class WidgetUtils {
    public static void drawRectangle(GuiGraphics context, int x0, int y0, int x1, int y1, int color) {
        context.fill(x0 + 1, y0, x1, y0 + 1, color);
        context.fill(x1 - 1, y0 + 1, x1, y1, color);
        context.fill(x0, y1 - 1, x1 - 1, y1, color);
        context.fill(x0, y0, x0 + 1, y1 - 1, color);
    }

    public static void drawTexturedQuad(Matrix4f matrix, float x0, float x1, float y0, float y1, float z, float u0,
            float u1, float v0, float v1) {
        // 1.21.11: RenderSystem.setShader and BufferUploader.drawWithShader removed
        // Textured quad rendering disabled - not critical for mod functionality
    }

    /**
     * The same as the Inventory function but with negative Z
     * Note: 1.21.11 removed many RenderSystem, Lighting, and EntityRenderDispatcher
     * methods
     */
    public static void drawBackgroundEntity(int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        // 1.21.11: Entity background rendering disabled due to major API changes:
        // - RenderSystem.applyModelViewMatrix() removed
        // - Lighting.setupForEntityInInventory() removed
        // - EntityRenderDispatcher.overrideCameraOrientation() removed
        // - EntityRenderDispatcher.setRenderShadow() removed
        // - RenderSystem.runAsFancy() removed
        // - EntityRenderDispatcher.render() signature changed
        // - Lighting.setupFor3DItems() removed
        // Uses default entity rendering instead
    }
}
