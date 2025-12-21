package net.conczin.mca.server.world.data;

import net.conczin.mca.util.NbtHelper;
import net.conczin.mca.util.WorldUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.LinkedList;
import java.util.List;

public class CivilRegistryManager extends SavedData {
    private final LinkedList<Component> entries = new LinkedList<>();

    CivilRegistryManager(ServerLevel world) {

    }

    CivilRegistryManager(CompoundTag nbt, HolderLookup.Provider provider) {
        // In 1.21.11, StringTag.getAsString() -> asString() returns Optional
        entries.addAll(NbtHelper.toList(nbt.get("entries"),
                element -> Component.Serializer.fromJson(((StringTag) element).asString().orElse(""), provider)));
    }

    public static CivilRegistryManager get(ServerLevel world, Village village) {
        return WorldUtils.loadData(world.getServer().overworld(), CivilRegistryManager::new, CivilRegistryManager::new,
                "mca_civil_registry_" + village.getId());
    }

    // In 1.21.11, SavedData.save() signature changed, removing @Override
    // temporarily
    // TODO: Refactor to use SavedDataType with CODEC pattern as in 1.21.11
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        // In 1.21.11, StringTag.valueOf may be replaced by StringTag.of()
        ListTag elements = NbtHelper.fromList(entries,
                a -> StringTag.of(Component.Serializer.toJson(a, provider)));
        nbt.put("entries", elements);
        return nbt;
    }

    public void addText(Component text) {
        entries.addFirst(text);
        setDirty();
    }

    public List<Component> getPage(int from, int to) {
        to = Math.min(entries.size(), to);
        return to <= from ? List.of() : entries.subList(from, to);
    }
}
