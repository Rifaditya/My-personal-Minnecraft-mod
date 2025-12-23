package net.conczin.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class HorizontalGradientWidget extends HorizontalColorPickerWidget {
    private final Supplier<float[]> startColorSupplier;
    private final Supplier<float[]> endColorSupplier;

    public HorizontalGradientWidget(int x, int y, int width, int height, double valueX,
            Supplier<float[]> startColorSupplier, Supplier<float[]> endColorSupplier,
            DualConsumer<Double, Double> consumer) {
        super(x, y, width, height, valueX, null, consumer);

        this.startColorSupplier = startColorSupplier;
        this.endColorSupplier = endColorSupplier;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 1.21.11: Use MCAGuiRenderer for gradient rendering
        float[] startColor = startColorSupplier.get();
        float[] endColor = endColorSupplier.get();

        // Convert RGBA floats to ARGB int
        int startARGB = ((int) (startColor[3] * 255) << 24) |
                ((int) (startColor[0] * 255) << 16) |
                ((int) (startColor[1] * 255) << 8) |
                (int) (startColor[2] * 255);
        int endARGB = ((int) (endColor[3] * 255) << 24) |
                ((int) (endColor[0] * 255) << 16) |
                ((int) (endColor[1] * 255) << 8) |
                (int) (endColor[2] * 255);

        net.conczin.mca.client.render.gui.MCAGuiRenderer.drawHorizontalGradient(
                context, getX(), getY(), width, height, startARGB, endARGB);

        // Draw outline
        WidgetUtils.drawRectangle(context, getX(), getY(), getX() + width, getY() + height, 0xaaffffff);

        // Draw cursor indicator
        net.conczin.mca.client.render.gui.MCAGuiRenderer.drawTexture(
                context, ColorPickerWidget.MCA_GUI_ICONS_TEXTURE,
                (int) (getX() + valueX * width) - 8,
                (int) (getY() + valueY * height) - 8,
                240, 0, 16, 16, 256, 256);
    }
}
