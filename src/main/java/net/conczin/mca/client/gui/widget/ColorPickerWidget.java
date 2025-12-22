package net.conczin.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.conczin.mca.MCA;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class ColorPickerWidget extends AbstractWidget {
    public static final Identifier MCA_GUI_ICONS_TEXTURE = MCA.locate("textures/gui.png");
    private final DualConsumer<Double, Double> consumer;
    private final Identifier texture;
    double valueX;
    double valueY;

    public ColorPickerWidget(int x, int y, int width, int height, double valueX, double valueY, Identifier texture,
            DualConsumer<Double, Double> consumer) {
        super(x, y, width, height, Component.literal(""));
        this.consumer = consumer;
        this.texture = texture;
        this.valueX = valueX;
        this.valueY = valueY;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // TODO: In 1.21.11, RenderSystem methods removed
        // RenderSystem.setShaderColor(1, 1, 1, alpha);
        // RenderSystem.enableBlend();
        // RenderSystem.defaultBlendFunc();
        // RenderSystem.enableDepthTest();
        // context.blit(texture, getX(), getY(), 0, 0, width, height, width, height);

        WidgetUtils.drawRectangle(context, getX(), getY(), getX() + width, getY() + height, 0xaaffffff);

        // TODO: Fix blit for 1.21.11
        // context.blit(MCA_GUI_ICONS_TEXTURE, (int) (getX() + valueX * width) - 8,
        // (int) (getY() + valueY * height) - 8, 240, 0, 16, 16, 256, 256);

        WidgetUtils.drawRectangle(context, getX(), getY(), getX() + width, getY() + height, 0xaaffffff);
    }

    // In 1.21.11, onDrag signature changed, removing @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        update(mouseX, mouseY);
    }

    // In 1.21.11, mouseClicked signature changed, removing @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInArea(mouseX, mouseY)) {
            update(mouseX, mouseY);
            return true;
        }
        return false;
    }

    private boolean isInArea(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() && mouseY <= getY() + height;
    }

    void update(double mouseX, double mouseY) {
        valueX = Mth.clamp((mouseX - getX()) / width, 0.0, 1.0);
        valueY = Mth.clamp((mouseY - getY()) / height, 0.0, 1.0);
        consumer.apply(valueX, valueY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        defaultButtonNarrationText(builder);
    }

    public double getValueX() {
        return valueX;
    }

    public void setValueX(double valueX) {
        this.valueX = valueX;
    }

    public double getValueY() {
        return valueY;
    }

    public void setValueY(double valueY) {
        this.valueY = valueY;
    }

    @FunctionalInterface
    public interface DualConsumer<A, B> {
        void apply(A a, B b);
    }
}
