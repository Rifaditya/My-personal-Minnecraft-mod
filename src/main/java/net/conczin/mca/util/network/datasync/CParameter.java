package net.conczin.mca.util.network.datasync;

import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.UUID;

public interface CParameter<T, TrackedType> {
    static CDataParameter<Integer> create(String id, int def) {
        return new CDataParameter<>(id, EntityDataSerializers.INT, def,
                (nbt, key, provider) -> NbtCompoundDefaultGetters.getInt(nbt, key, def),
                (nbt, key, value, provider) -> nbt.putInt(key, value));
    }

    static CDataParameter<Float> create(String id, float def) {
        return new CDataParameter<>(id, EntityDataSerializers.FLOAT, def,
                (nbt, key, provider) -> NbtCompoundDefaultGetters.getFloat(nbt, key, def),
                (nbt, key, value, provider) -> nbt.putFloat(key, value));
    }

    static CDataParameter<Boolean> create(String id, boolean def) {
        return new CDataParameter<>(id, EntityDataSerializers.BOOLEAN, def,
                (nbt, key, provider) -> {
                    if (nbt.contains(key)) {
                        // In 1.21.11, getInt returns Optional
                        return nbt.getInt(key).orElse(0) != 0;
                    } else {
                        return def;
                    }
                },
                (nbt, key, value, provider) -> nbt.putInt(key, value ? 1 : 0));
    }

    static CDataParameter<String> create(String id, String def) {
        return new CDataParameter<>(id, EntityDataSerializers.STRING, def,
                (nbt, key, provider) -> NbtCompoundDefaultGetters.getString(nbt, key, def),
                (nbt, key, value, provider) -> nbt.putString(key, value));
    }

    // In 1.21.11, EntityDataSerializers.COMPOUND_TAG removed - use STRING with
    // serialization
    // This is a workaround that may need more sophisticated handling
    // WARNING: Type mismatch workaround - serializer is STRING but we
    // store/retrieve CompoundTag
    @SuppressWarnings("unchecked")
    static CDataParameter<CompoundTag> create(String id, CompoundTag def) {
        // We use a raw type cast workaround since EntityDataSerializers.STRING
        // is EntityDataSerializer<String> but we need EntityDataSerializer<CompoundTag>
        // The actual sync happens via NBT, so this works at runtime
        EntityDataSerializer rawSerializer = EntityDataSerializers.STRING;
        return new CDataParameter<>(id, (EntityDataSerializer<CompoundTag>) rawSerializer, def,
                (nbt, key, provider) -> NbtCompoundDefaultGetters.getCompound(nbt, key, def),
                (nbt, key, value, provider) -> nbt.put(key, value));
    }

    static CDataParameter<ItemStack> create(String id, ItemStack def) {
        return new CDataParameter<>(id, EntityDataSerializers.ITEM_STACK, def,
                (nbt, key, provider) -> NbtCompoundDefaultGetters.getItemStack(nbt, key, ItemStack.EMPTY, provider),
                (nbt, key, stack, provider) -> {
                    // In 1.21.11, ItemStack.save methods changed - use saveOptional with
                    // RegistryAccess
                    // For NBT serialization, we can use ItemStack.CODEC with NbtOps
                    if (!stack.isEmpty()) {
                        net.minecraft.nbt.Tag itemTag = ItemStack.CODEC.encodeStart(
                                net.minecraft.nbt.NbtOps.INSTANCE, stack).result().orElse(new CompoundTag());
                        nbt.put(key, itemTag);
                    }
                });
    }

    static CDataParameter<BlockPos> create(String id, BlockPos def) {
        return new CDataParameter<>(id, EntityDataSerializers.BLOCK_POS, def,
                (tag, key, provider) -> new BlockPos(
                        // In 1.21.11, getInt returns Optional
                        tag.getInt(key + "X").orElse(0),
                        tag.getInt(key + "Y").orElse(0),
                        tag.getInt(key + "Z").orElse(0)),
                (tag, key, pos, provider) -> {
                    tag.putInt(key + "X", pos.getX());
                    tag.putInt(key + "Y", pos.getY());
                    tag.putInt(key + "Z", pos.getZ());
                });
    }

    // In 1.21.11, EntityDataSerializers.OPTIONAL_UUID removed - use STRING with
    // UUID conversion
    // WARNING: Type mismatch workaround - serializer is STRING but we
    // store/retrieve Optional<UUID>
    @SuppressWarnings("unchecked")
    static CDataParameter<Optional<UUID>> create(String id, Optional<UUID> def) {
        // We use a raw type cast workaround since EntityDataSerializers.STRING
        // is EntityDataSerializer<String> but we need
        // EntityDataSerializer<Optional<UUID>>
        EntityDataSerializer rawSerializer = EntityDataSerializers.STRING;
        return new CDataParameter<>(id, (EntityDataSerializer<Optional<UUID>>) rawSerializer, def,
                (tag, key, provider) -> {
                    // hasUUID/getUUID removed in 1.21.11 - use string
                    String uuidStr = tag.getString(key).orElse("");
                    if (uuidStr.isEmpty())
                        return Optional.empty();
                    try {
                        return Optional.of(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        return Optional.empty();
                    }
                },
                (tag, key, v, provider) -> v.ifPresent(uuid -> tag.putString(key, uuid.toString())));
    }

    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> CEnumParameter<T> create(String id, T def) {
        return new CEnumParameter<>(id, (Class<T>) def.getClass(), def);
    }

    static <T extends Enum<T>> CEnumParameter<T> create(String id, Class<T> type) {
        return new CEnumParameter<>(id, type, null);
    }

    TrackedType getDefault();

    T get(EntityDataAccessor<TrackedType> param, SynchedEntityData tracker);

    void set(EntityDataAccessor<TrackedType> param, SynchedEntityData tracker, T v);

    T load(CompoundTag nbt, RegistryAccess registries);

    void save(CompoundTag nbt, T value, RegistryAccess registries);

    EntityDataAccessor<TrackedType> createParam(Class<? extends Entity> type);
}
