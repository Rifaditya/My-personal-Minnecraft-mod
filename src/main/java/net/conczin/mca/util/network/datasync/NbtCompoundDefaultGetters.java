package net.conczin.mca.util.network.datasync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NbtCompoundDefaultGetters {

    public static int getInt(CompoundTag nbt, String key, int def) {
        // In 1.21.11, getInt returns Optional<Integer>
        return nbt.getInt(key).orElse(def);
    }

    public static float getFloat(CompoundTag nbt, String key, float def) {
        // In 1.21.11, getFloat returns Optional<Float>
        return nbt.getFloat(key).orElse(def);
    }

    public static String getString(CompoundTag nbt, String key, String def) {
        // In 1.21.11, getString returns Optional<String>
        return nbt.getString(key).orElse(def);
    }

    public static CompoundTag getCompound(CompoundTag nbt, String key, CompoundTag def) {
        // In 1.21.11, getCompound returns Optional<CompoundTag>, contains() only takes
        // key
        return nbt.getCompound(key).orElse(def.copy());
    }

    public static ItemStack getItemStack(CompoundTag nbt, String key, ItemStack def, HolderLookup.Provider provider) {
        // TODO: In 1.21.11, ItemStack.parse API may have changed
        // Disabled until API is researched - return default
        return def;
    }
}
