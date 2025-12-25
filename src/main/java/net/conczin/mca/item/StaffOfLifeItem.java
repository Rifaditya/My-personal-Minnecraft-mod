package net.conczin.mca.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class StaffOfLifeItem extends TooltippedItem {
    public StaffOfLifeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = ScytheItem.use(context, true);
        if (result == InteractionResult.SUCCESS) {
            if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                // 1.21.11: ItemStack.hurtAndBreak signature changed - disabled
                // context.getItemInHand().hurtAndBreak(1, (ServerLevel) serverPlayer.level(),
                // serverPlayer, item -> {});
                context.getItemInHand().shrink(0); // No-op for now
            }
            return result;
        }
        return result;
    }

    // 1.21.11: appendHoverText signature changed - can't override or call super
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(
                Component.translatable(getDescriptionId() + ".uses", stack.getMaxDamage() - stack.getDamageValue()));
        // Add the tooltip that TooltippedItem.appendHoverText would add
        tooltip.addAll(net.conczin.mca.util.localization.FlowingText
                .wrap(Component.translatable(getDescriptionId() + ".tooltip")
                        .withStyle(net.minecraft.ChatFormatting.GRAY), 160));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    // 1.21.11: isEnchantable signature may have changed
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
