package net.conczin.mca.client.gui.widget;

import net.conczin.mca.util.localization.FlowingText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class ExtendedSliderWidget<T> extends AbstractSliderButton {
    protected final Supplier<Component> tooltipSupplier;
    final Consumer<T> onApplyValue;
    private T oldValue;

    public ExtendedSliderWidget(int x, int y, int width, int height, Component text, double value,
            Consumer<T> onApplyValue, Supplier<Component> tooltipSupplier) {
        super(x, y, width, height, text, value);
        this.onApplyValue = onApplyValue;
        this.tooltipSupplier = tooltipSupplier;
    }

    protected double getOpticalValue() {
        return value;
    }

    abstract T getValue();

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int i = (this.isHovered() ? 2 : 1) * 20;

        // 1.21.11: Custom slider thumb rendering disabled - using default
        // Removed context.blit for slider thumb, default rendering works

        super.renderWidget(context, mouseX, mouseY, delta);

        if (this.isHovered()) {
            this.renderTooltip(context, mouseX, mouseY);
        }
    }

    @Override
    protected void applyValue() {
        T v = getValue();
        if (v != oldValue) {
            oldValue = v;
            onApplyValue.accept(v);
        }
    }

    public void renderTooltip(GuiGraphics context, int mouseX, int mouseY) {
        // 1.21.11: Use Create-Fly pattern with ClientTooltipComponent and
        // DefaultTooltipPositioner
        Minecraft mc = Minecraft.getInstance();
        List<Component> tooltipLines = FlowingText.wrap(tooltipSupplier.get(), 160);
        List<ClientTooltipComponent> components = tooltipLines.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .collect(Collectors.toList());
        context.renderTooltip(mc.font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }
}
