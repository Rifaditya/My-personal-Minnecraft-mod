package net.conczin.mca.util.compat;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class ButtonWidget extends net.minecraft.client.gui.components.Button {
    /**
     * Creates a 1.19.2 and lower button implementation.
     *
     * @since MC 1.19.3
     */
    public ButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public ButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress, Component tooltip) {
        this(x, y, width, height, message, onPress);
        setTooltip(Tooltip.create(tooltip));
    }

    // In 1.21.11, AbstractButton requires this method to be implemented
    @Override
    protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // TODO: renderString doesn't exist on Button in 1.21.11
        // Empty for now - buttons won't render text until fixed
    }
}
