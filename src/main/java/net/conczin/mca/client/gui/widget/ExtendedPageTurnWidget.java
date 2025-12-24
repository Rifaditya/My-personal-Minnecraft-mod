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

    // 1.21.11: renderWidget in PageButton is final - using parent's rendering
    // Custom texture rendering disabled for now - uses default page turn appearance
}
