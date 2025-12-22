package net.conczin.mca.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class LegacyImageButton extends ImageButton {
    private static final Component EMPTY = Component.literal("");

    private final Identifier Identifier;
    private final int xTexStart;
    private final int yTexStart;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

    public LegacyImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex,
            Identifier Identifier, int textureWidth, int textureHeight, OnPress onPress) {
        this(x, y, width, height, xTexStart, yTexStart, yDiffTex, Identifier, textureWidth, textureHeight, onPress,
                EMPTY);
    }

    public LegacyImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffTex,
            Identifier Identifier, int textureWidth, int textureHeight, OnPress onPress, Component message) {
        super(x, y, width, height, new WidgetSprites(Identifier, Identifier), onPress, message);

        this.Identifier = Identifier;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffTex = yDiffTex;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public void renderTexture(GuiGraphics guiGraphics, Identifier texture, int x, int y, int uOffset, int vOffset,
            int textureDifference, int width, int height, int textureWidth, int textureHeight) {
        int i = vOffset;
        if (isHoveredOrFocused()) {
            i += textureDifference;
        }
        // TODO: In 1.21.11, RenderSystem.enableDepthTest removed
        // RenderSystem.enableDepthTest();
        // TODO: In 1.21.11, blit requires RenderType
        // guiGraphics.blit(texture, x, y, uOffset, i, width, height, textureWidth,
        // textureHeight);
    }

    // In 1.21.11, renderWidget in AbstractButton is final or visibility changed
    // Rendering disabled - base class renders
}
