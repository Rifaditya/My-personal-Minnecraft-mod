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
        // Register trades for ADVENTURER profession using Fabric API
        // In 1.21.11, registerVillagerOffers takes ResourceKey<VillagerProfession>
        // Use BuiltInRegistries to get the ResourceKey from the profession
        net.minecraft.resources.ResourceKey<net.minecraft.world.entity.npc.villager.VillagerProfession> adventurerKey = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION
                .getResourceKey(ProfessionsMCA.ADVENTURER).orElseThrow();
        TradeOfferHelper.registerVillagerOffers(adventurerKey, 1, trades -> {
            trades.add(new SellItemFactory(Items.SLIME_BALL, 1, 1, 16, 1));
            trades.add(new SellItemFactory(Items.LEATHER_HORSE_ARMOR, 3, 1, 4, 10));
            trades.add(new SellItemFactory(Items.SADDLE, 4, 1, 3, 5));
            trades.add(new SellItemFactory(Items.IRON_HORSE_ARMOR, 5, 1, 2, 20));
            trades.add(new SellItemFactory(Items.DIAMOND, 10, 1, 8, 20));
            trades.add(new SellItemFactory(Items.GOLDEN_HORSE_ARMOR, 10, 1, 3, 30));
            trades.add(new SellItemFactory(Items.GOLDEN_APPLE, 5, 1, 8, 30));
            trades.add(new SellItemFactory(Items.DIAMOND_HORSE_ARMOR, 15, 1, 1, 30));
            trades.add(new SellItemFactory(Items.ENCHANTED_GOLDEN_APPLE, 32, 1, 3, 50));
            trades.add(new BuyForOneEmeraldFactory(Items.BREAD, 10, 10, 30));
        });

        // Register trades for CULTIST profession using Fabric API
        net.minecraft.resources.ResourceKey<net.minecraft.world.entity.npc.villager.VillagerProfession> cultistKey = net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION
                .getResourceKey(ProfessionsMCA.CULTIST).orElseThrow();
        TradeOfferHelper.registerVillagerOffers(cultistKey, 1, trades -> {
            trades.add(new SellItemFactory(ItemsMCA.SIRBEN_BABY_BOY, 5, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.SIRBEN_BABY_GIRL, 5, 1, 1, 1));
            trades.add(new BuyForOneEmeraldFactory(ItemsMCA.BABY_BOY, 1, 1, 1));
            trades.add(new BuyForOneEmeraldFactory(ItemsMCA.BABY_GIRL, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.BOOK_CULT_0, 1, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.BOOK_CULT_1, 1, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.BOOK_CULT_2, 1, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.BOOK_DEATH, 1, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.BOOK_INFECTION, 1, 1, 1, 1));
            trades.add(new SellItemFactory(ItemsMCA.BOOK_SUPPORTERS, 1, 1, 1, 1));
        });
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
