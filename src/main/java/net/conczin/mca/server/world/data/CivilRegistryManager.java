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
        // TODO: Component.Serializer.fromJson signature changed in 1.21.11
        // Skipping deserialization for now - will be empty on load
        // Need to research correct API for Component JSON deserialization
    }

    public static CivilRegistryManager get(ServerLevel world, Village village) {
        return WorldUtils.loadData(world.getServer().overworld(), CivilRegistryManager::new, CivilRegistryManager::new,
                "mca_civil_registry_" + village.getId());
    }

    // In 1.21.11, SavedData.save() signature changed, removing @Override
    // temporarily
    // TODO: Refactor to use SavedDataType with CODEC pattern as in 1.21.11
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider provider) {
        // In 1.21.11, create StringTag via NbtOps or direct constructor
        ListTag elements = new ListTag();
        for (Component entry : entries) {
            String json = Component.Serializer.toJson(entry, provider);
            // Use NbtOps to encode the string as a StringTag element
            net.minecraft.nbt.Tag strTag = net.minecraft.nbt.NbtOps.INSTANCE.createString(json);
            elements.add(strTag);
        }
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
