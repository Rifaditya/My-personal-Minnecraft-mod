package net.conczin.mca.client.render.wildfire;

import net.minecraft.world.item.ItemStack;

/**
 * Simplified interface for determining how armor interacts with breasts.
 */
public interface IGenderArmor {
    IGenderArmor DEFAULT = new IGenderArmor() {
    };
    IGenderArmor EMPTY = new IGenderArmor() {
        @Override
        public boolean coversBreasts() {
            return false;
        }

        @Override
        public float physicsResistance() {
            return 0;
        }
    };

    default boolean coversBreasts() {
        return true;
    }

    default boolean alwaysHidesBreasts() {
        return false;
    }

    default float physicsResistance() {
        return 0.5f;
    }

    default float tightness() {
        return 0;
    }

    // Simplified helper to get config from stack
    static IGenderArmor getArmorConfig(ItemStack stack) {
        if (stack.isEmpty()) {
            return EMPTY;
        }
        // For vanilla armor, using DEFAULT config which provides standard coverage and
        // physics
        // Custom armor mods can implement IGenderArmor interface for specific behavior
        return DEFAULT;
    }
}
