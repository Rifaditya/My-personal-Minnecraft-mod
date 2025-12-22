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

    // In 1.21.11, renderWidget in AbstractButton is final or visibility changed
    // Rendering disabled - using empty method
    // TODO: Implement custom rendering approach
}
