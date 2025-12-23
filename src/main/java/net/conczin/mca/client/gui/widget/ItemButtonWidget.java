package net.conczin.mca.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ItemButtonWidget extends TooltipButtonWidget {
    final ItemStack item;

    public ItemButtonWidget(int x, int y, int size, MutableComponent message, ItemStack item, OnPress onPress) {
        super(x, y, size, size, Component.literal(""), message, onPress);

        this.item = item;
    }

    // 1.21.11: Use renderContents to render the item icon
    @Override
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Render the item icon centered in the button
        int itemX = getX() + (width - 16) / 2;
        int itemY = getY() + (height - 16) / 2;
        guiGraphics.renderItem(item, itemX, itemY);
    }
}
