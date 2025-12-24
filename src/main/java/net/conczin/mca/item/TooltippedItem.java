package net.conczin.mca.item;

import net.conczin.mca.util.localization.FlowingText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class TooltippedItem extends Item {
    public TooltippedItem(Item.Properties properties) {
        super(properties);
    }

    // 1.21.11: appendHoverText signature changed - can't override or call super
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag tooltipFlag) {
        // super.appendHoverText not available with this signature
        tooltip.addAll(FlowingText
                .wrap(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY), 160));
    }
}
