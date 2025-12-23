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

    // 1.21.11: Use renderContents to render the icon
    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Render icon texture using MCAGuiRenderer
        net.conczin.mca.client.render.gui.MCAGuiRenderer.drawTexture(
                guiGraphics, ICON_TEXTURES, getX(), getY(), u, v, 16, 16, 256, 256);
    }
}
