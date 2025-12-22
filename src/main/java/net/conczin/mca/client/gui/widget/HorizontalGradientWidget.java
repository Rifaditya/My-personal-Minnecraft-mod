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
        // TODO: In 1.21.11, RenderSystem.enableBlend/defaultBlendFunc/setShader removed
        // TODO: In 1.21.11, context.pose() returns Matrix3x2fStack not PoseStack
        // Gradient rendering disabled for now

        // Draw simple fallback
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF808080);

        WidgetUtils.drawRectangle(context, getX(), getY(), getX() + width, getY() + height, 0xaaffffff);

        // TODO: In 1.21.11, blit requires RenderType
        // context.blit(MCA_GUI_ICONS_TEXTURE, (int) (getX() + valueX * width) - 8,
        // (int) (getY() + valueY * height) - 8, 240, 0, 16, 16, 256, 256);
    }
}
