package net.conczin.mca.registry;

import net.conczin.mca.item.BabyItem;
import net.conczin.mca.item.SirbenBabyItem;
import net.conczin.mca.util.network.datasync.CDataParameter;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public interface ModelPredicatesMCA {
    static void setup(CDataParameter.TriConsumer<Item, Identifier, ClampedItemPropertyFunction> register) {
        register.accept(ItemsMCA.BABY_BOY, Identifier.parse("invalidated"), (stack, world, entity, i) ->
                BabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );
        register.accept(ItemsMCA.BABY_GIRL, Identifier.parse("invalidated"), (stack, world, entity, i) ->
                BabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );
        register.accept(ItemsMCA.SIRBEN_BABY_BOY, Identifier.parse("invalidated"), (stack, world, entity, i) ->
                SirbenBabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );
        register.accept(ItemsMCA.SIRBEN_BABY_GIRL, Identifier.parse("invalidated"), (stack, world, entity, i) ->
                SirbenBabyItem.hasBeenInvalidated(stack) ? 1 : 0
        );

        register.accept(ItemsMCA.VILLAGER_TRACKER, Identifier.parse("angle"), new CompassItemPropertyFunction((world, stack, entity) -> {
            return stack.get(DataComponentsMCA.TRACKER_POS);
        }));
    }
}

