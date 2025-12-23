package net.conczin.mca.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RegistryHelper {

    public static <T> Optional<TagKey<T>> tryGetTagKey(Registry<T> registry, Identifier id) {
        // TODO: In 1.21.11, getTagNames() API may have changed
        // Disabled until API is researched - return empty
        return Optional.empty();
    }

    public static <T> Iterable<Holder<T>> getEntries(TagKey<T> tagKey) {
        // 1.21.11: getTagOrEmpty() from Create-Fly FluidTagIngredient
        Registry<T> registry = getRegistryOf(tagKey);
        return registry != null ? registry.getTagOrEmpty(tagKey) : java.util.Collections.emptyList();
    }

    public static <T> Optional<Holder<T>> tryGetEntry(Registry<T> registry, T object) {
        // TODO: In 1.21.11, getHolderOrThrow API may have changed
        // Disabled until API is researched - return empty
        return Optional.empty();
    }

    public static <T> boolean isObjectInTag(Registry<T> registry, Identifier tagId, T object) {
        return tryGetTagKey(registry, tagId).map(tagKey -> isObjectInTag(registry, tagKey, object)).orElse(false);
    }

    public static <T> boolean isObjectInTag(Registry<T> registry, TagKey<T> tag, T object) {
        var entry = tryGetEntry(registry, object);
        return entry.map(tRegistryEntry -> tRegistryEntry.is(tag)).orElse(false);
    }

    public static <T> boolean isTagEmpty(TagKey<T> tag) {
        // 1.21.11: getEntries now returns Iterable, count manually
        int count = 0;
        for (Holder<T> ignored : getEntries(tag)) {
            count++;
            break; // Just need to know if any exist
        }
        return count == 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> Registry<T> getRegistryOf(@NotNull TagKey<T> key) {
        // 1.21.11: From Architectury - REGISTRY.getValue(registryKey.identifier())
        var registryKey = key.registry();
        return (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.identifier());
    }
}
