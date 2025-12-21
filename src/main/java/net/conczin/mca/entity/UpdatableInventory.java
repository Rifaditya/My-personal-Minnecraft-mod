package net.conczin.mca.entity;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;

public class UpdatableInventory extends SimpleContainer {
    public UpdatableInventory(int size) {
        super(size);
    }

    public void update(Entity entity) {
        // In 1.21.11, ItemStack.inventoryTick signature changed significantly
        // (requires ServerLevel, Entity, EquipmentSlot instead of Level, Entity, int,
        // boolean)
        // Disabled for now pending proper implementation
        /*
         * for (int slot = 0; slot < getContainerSize(); slot++) {
         * if (!getItem(slot).isEmpty()) {
         * getItem(slot).inventoryTick(entity.level(), entity, slot, false);
         * }
         * }
         */
    }
}
