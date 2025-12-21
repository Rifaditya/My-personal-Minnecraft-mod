package net.conczin.mca.entity.interaction.gifts;

import net.conczin.mca.Config;
import net.conczin.mca.util.NbtHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class GiftSaturation {
    private List<Identifier> values = new LinkedList<>();

    public void add(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        // add to queue
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        values.add(id);

        // clear old values if limit is reached
        while (values.size() > Config.getInstance().giftDesaturationQueueLength) {
            pop();
        }
    }

    public int get(ItemStack stack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return (int) values.stream().filter(v -> v.equals(id)).count();
    }

    public void readFromNbt(ListTag nbt) {
        // In 1.21.11, Tag.getAsString() doesn't exist - cast to StringTag and use
        // asString()
        values = NbtHelper.toList(nbt, v -> Identifier.parse(((StringTag) v).asString().orElse("")));
    }

    public ListTag toNbt() {
        // In 1.21.11, use NbtOps for creating string tags
        ListTag list = new ListTag();
        for (Identifier v : values) {
            list.add(net.minecraft.nbt.NbtOps.INSTANCE.createString(v.toString()));
        }
        return list;
    }

    public void pop() {
        if (!values.isEmpty()) {
            values.remove(0);
        }
    }
}
