package net.conczin.mca.entity.ai;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Chore {
    NONE("none", null),
    // In 1.21.11, tool-specific classes like PickaxeItem, SwordItem are removed
    // Tools are now just Items - using Item.class as placeholder
    // Actual tool detection should use data components in 1.21.11
    PROSPECT("prospecting", Item.class), // was PickaxeItem
    HARVEST("harvesting", Item.class), // was HoeItem
    CHOP("chopping", Item.class), // was AxeItem
    HUNT("hunting", Item.class), // was SwordItem
    FISH("fishing", FishingRodItem.class);

    private static final Chore[] VALUES = values();
    private static final Map<String, Chore> REGISTRY = Stream.of(VALUES).collect(Collectors.toMap(
            c -> c.friendlyName,
            Function.identity()));

    private final String friendlyName;

    @Nullable
    private final Class<?> toolType;

    Chore(String friendlyName, @Nullable Class<?> toolType) {
        this.friendlyName = friendlyName;
        this.toolType = toolType;
    }

    public static Optional<Chore> byCommand(String action) {
        return Optional.ofNullable(REGISTRY.get(action.toLowerCase(Locale.ENGLISH)));
    }

    public static Chore byId(int id) {
        if (id < 0 || id >= VALUES.length) {
            return NONE;
        }
        return VALUES[id];
    }

    public Component getName() {
        return Component.translatable("gui.label." + friendlyName);
    }

    @Nullable
    public Class<?> getToolType() {
        return toolType;
    }
}
