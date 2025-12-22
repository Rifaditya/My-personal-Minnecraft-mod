package net.conczin.mca.registry;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

/**
 * MCA custom trade offers for custom professions.
 * Updated for 1.21.11 to use Fabric TradeOfferHelper API instead of direct
 * VillagerTrades.TRADES manipulation.
 */
public class TradeOffersMCA {
    public static void bootstrap() {
        // TODO: In 1.21.11, custom professions are disabled (null)
        // Cannot register trades until professions are fixed
        // Disabled trade registration for ADVENTURER and CULTIST
        // since ProfessionsMCA.ADVENTURER and ProfessionsMCA.CULTIST are currently null
    }

    // Custom factory that buys items for emeralds
    // Updated for 1.21.11: getOffer now takes (ServerLevel, Entity, RandomSource)
    static class BuyForOneEmeraldFactory implements VillagerTrades.ItemListing {
        private final Item buy;
        private final int price;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public BuyForOneEmeraldFactory(ItemLike item, int price, int maxUses, int experience) {
            this.buy = item.asItem();
            this.price = price;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = 0.05f;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel level, Entity entity, RandomSource random) {
            ItemCost cost = new ItemCost(this.buy, this.price);
            return new MerchantOffer(cost, new ItemStack(Items.EMERALD), this.maxUses, this.experience,
                    this.multiplier);
        }
    }

    // Custom factory that sells items for emeralds
    // Updated for 1.21.11: getOffer now takes (ServerLevel, Entity, RandomSource)
    static class SellItemFactory implements VillagerTrades.ItemListing {
        private final ItemStack sell;
        private final int price;
        private final int count;
        private final int maxUses;
        private final int experience;
        private final float multiplier;

        public SellItemFactory(Item item, int price, int count, int maxUses, int experience) {
            this(new ItemStack(item), price, count, maxUses, experience);
        }

        public SellItemFactory(ItemStack stack, int price, int count, int maxUses, int experience) {
            this(stack, price, count, maxUses, experience, 0.05f);
        }

        public SellItemFactory(ItemStack stack, int price, int count, int maxUses, int experience, float multiplier) {
            this.sell = stack;
            this.price = price;
            this.count = count;
            this.maxUses = maxUses;
            this.experience = experience;
            this.multiplier = multiplier;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel level, Entity entity, RandomSource random) {
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.price),
                    new ItemStack(this.sell.getItem(), this.count), this.maxUses, this.experience, this.multiplier);
        }
    }
}
