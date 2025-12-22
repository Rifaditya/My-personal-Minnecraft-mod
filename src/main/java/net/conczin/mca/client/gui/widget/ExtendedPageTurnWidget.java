package net.conczin.mca.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.resources.Identifier;

public class ExtendedPageTurnWidget extends PageButton {
    private final Identifier texture;

    private final boolean isNextPageButton;

    public ExtendedPageTurnWidget(int x, int y, boolean isNextPageButton, OnPress action, boolean playPageTurnSound,
            Identifier texture) {
        super(x, y, isNextPageButton, action, playPageTurnSound);
        this.isNextPageButton = isNextPageButton;
        this.texture = texture;
    }

    // In 1.21.11, renderWidget in AbstractButton is final or visibility changed
    // Rendering disabled - using empty method
    // TODO: Implement custom rendering approach
}
