package net.conczin.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static net.conczin.mca.client.gui.InteractScreen.ICON_TEXTURES;

public class ToggleableTooltipIconButtonWidget extends ToggleableTooltipButtonWidget {
    private final int u;
    private final int v;

    public ToggleableTooltipIconButtonWidget(int x, int y, int u, int v, boolean toggle, MutableComponent tooltip,
            OnPress onPress) {
        super(x, y, 16, 16, toggle, Component.literal(""), tooltip, onPress);

        this.u = u;
        this.v = v;
    }

    // In 1.21.11, renderWidget in AbstractButton is final or visibility changed
    // Rendering disabled - base class renders
}
