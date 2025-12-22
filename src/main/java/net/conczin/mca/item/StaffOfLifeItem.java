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
                // TODO: In 1.21.11, ItemStack.hurtAndBreak signature changed
                // context.getItemInHand().hurtAndBreak(1, (ServerLevel) serverPlayer.level(),
                // serverPlayer, item -> {});
                context.getItemInHand().shrink(0); // No-op for now
            }
            return result;
        }
        return result;
    }

    // TODO: In 1.21.11, appendHoverText signature changed
    // @Override
    // public void appendHoverText(ItemStack stack, TooltipContext context,
    // List<Component> tooltip, TooltipFlag flag) {
    // tooltip.add(Component.translatable(getDescriptionId() + ".uses",
    // stack.getMaxDamage() - stack.getDamageValue()));
    // super.appendHoverText(stack, context, tooltip, flag);
    // }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    // TODO: In 1.21.11 signature may have changed
    // @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
