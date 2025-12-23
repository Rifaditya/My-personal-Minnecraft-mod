package net.conczin.mca.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface InventoryUtils {
    static Stream<ItemStack> stream(Container inventory) {
        return IntStream.range(0, inventory.getContainerSize()).mapToObj(inventory::getItem);
    }

    static int getFirstSlotContainingItem(Container inv, Predicate<ItemStack> predicate) {
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!predicate.test(stack))
                continue;
            return i;
        }
        return -1;
    }

    static boolean contains(Container inv, Class<?> clazz) {
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            final ItemStack stack = inv.getItem(i);
            final Item item = stack.getItem();

            if (item.getClass() == clazz)
                return true;
        }
        return false;
    }

    /**
     * Gets the best quality (max damage) item of the specified type that is in the
     * inventory.
     *
     * @param type The class of item that will be returned.
     * @return The item stack containing the item of the specified type with the
     *         highest max damage.
     */
    static ItemStack getBestItemOfType(Container inv, @Nullable Class<?> type) {
        return type == null ? ItemStack.EMPTY : inv.getItem(getBestItemOfTypeSlot(inv, type));
    }

    static int getBestItemOfTypeSlot(Container inv, Class<?> type) {
        int highestMaxDamage = 0;
        int best = -1;

        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stackInInventory = inv.getItem(i);

            final String itemClassName = stackInInventory.getItem().getClass().getName();

            if (itemClassName.equals(type.getName()) && highestMaxDamage < stackInInventory.getMaxDamage()) {
                highestMaxDamage = stackInInventory.getMaxDamage();
                best = i;
            }
        }

        return best;
    }

    static Optional<ItemStack> getBestArmor(Container inv, EquipmentSlot slot) {
        // 1.21.11: Use DataComponents.EQUIPPABLE to check if item fits slot
        // and DataComponents.ATTRIBUTE_MODIFIERS to get armor value
        ItemStack best = null;
        double bestDefense = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;

            // Check if equippable in target slot
            var equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable == null || equippable.slot() != slot)
                continue;

            // Get armor defense from attribute modifiers
            var attrMods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (attrMods != null) {
                double defense = attrMods.modifiers().stream()
                        .filter(entry -> entry.attribute().value().equals(Attributes.ARMOR.value()))
                        .mapToDouble(entry -> entry.modifier().amount())
                        .sum();
                if (defense > bestDefense) {
                    bestDefense = defense;
                    best = stack;
                }
            } else if (best == null) {
                best = stack; // Any equippable is better than nothing
            }
        }
        return Optional.ofNullable(best);
    }

    static Optional<ItemStack> getBestSword(Container inv) {
        // 1.21.11: Check for items with attack damage attribute
        ItemStack best = null;
        double bestDamage = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;

            // Check for attack damage attribute modifier
            var attrMods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (attrMods == null)
                continue;

            double damage = attrMods.modifiers().stream()
                    .filter(entry -> entry.attribute().value().equals(Attributes.ATTACK_DAMAGE.value()))
                    .mapToDouble(entry -> entry.modifier().amount())
                    .sum();
            if (damage > bestDamage) {
                bestDamage = damage;
                best = stack;
            }
        }
        return Optional.ofNullable(best);
    }

    static Optional<ItemStack> getBestRanged(Container inv) {
        // 1.21.11: Check for ProjectileWeaponItem subclass or use data components
        ItemStack best = null;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
                continue;

            // Check if it's a projectile weapon (bow, crossbow, etc.)
            if (stack.getItem() instanceof net.minecraft.world.item.ProjectileWeaponItem) {
                best = stack;
                break; // First ranged weapon found
            }
        }
        return Optional.ofNullable(best);
    }

    static void dropAllItems(Entity entity, Container inv) {
        // 1.21.11: spawnAtLocation(ServerLevel, stack) from Create-Fly
        // PotatoProjectileEntity
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    entity.spawnAtLocation(serverLevel, stack);
                }
            }
        }
        inv.clearContent();
    }

    static void saveToNBT(RegistryAccess registryAccess, SimpleContainer inv, CompoundTag nbt) {
        // 1.21.11: Use ItemStack.CODEC for serialization
        net.minecraft.nbt.ListTag listTag = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                ItemStack.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, stack)
                        .result()
                        .ifPresent(tag -> itemTag.put("Item", tag));
                listTag.add(itemTag);
            }
        }
        nbt.put("Items", listTag);
    }

    static void readFromNBT(RegistryAccess registryAccess, SimpleContainer inv, CompoundTag nbt) {
        // 1.21.11: Use ItemStack.CODEC for deserialization
        inv.clearContent();
        nbt.getList("Items").ifPresent(listTag -> {
            for (int i = 0; i < listTag.size(); i++) {
                listTag.getCompound(i).ifPresent(itemTag -> {
                    int slot = itemTag.getInt("Slot").orElse(0);
                    if (slot >= 0 && slot < inv.getContainerSize()) {
                        itemTag.getCompound("Item").ifPresent(itemNbt -> {
                            ItemStack stack = ItemStack.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE, itemNbt)
                                    .result()
                                    .orElse(ItemStack.EMPTY);
                            inv.setItem(slot, stack);
                        });
                    }
                });
            }
        });
    }

    static double approximateDamage(ItemStack stack, LivingEntity entity) {
        // 1.21.11: Use DataComponents.ATTRIBUTE_MODIFIERS to get damage
        double baseDamage = entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        var attrMods = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (attrMods != null) {
            double weaponDamage = attrMods.modifiers().stream()
                    .filter(entry -> entry.attribute().value().equals(Attributes.ATTACK_DAMAGE.value()))
                    .mapToDouble(entry -> entry.modifier().amount())
                    .sum();
            return baseDamage + weaponDamage;
        }
        return baseDamage;
    }
}
