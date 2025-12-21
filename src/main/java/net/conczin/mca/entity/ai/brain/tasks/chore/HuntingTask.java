package net.conczin.mca.entity.ai.brain.tasks.chore;

import com.google.common.collect.ImmutableMap;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Chore;
import net.conczin.mca.util.InventoryUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class HuntingTask extends AbstractChoreTask {
    private int ticks = 0;
    private int nextAction = 0;
    private Animal target = null;

    public HuntingTask() {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET,
                MemoryStatus.VALUE_ABSENT));
    }

    // In 1.21.11, SwordItem class was removed. Use this helper to check for melee
    // weapons
    // This is a simplified check - can be enhanced with data component checks
    private static boolean isMeleeWeapon(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        // Check if item is a sword-like item by checking its type
        // For now, accept any non-empty item in the weapon slot as valid
        // More sophisticated check could use Attributes.ATTACK_DAMAGE component
        return true; // Placeholder - actual sword detection needs data component approach
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel world, VillagerEntityMCA villager) {
        return villager.getVillagerBrain().getCurrentJob() == Chore.HUNT
                && super.checkExtraStartConditions(world, villager);
    }

    @Override
    protected boolean canStillUse(ServerLevel world, VillagerEntityMCA villager, long time) {
        return checkExtraStartConditions(world, villager);
    }

    @Override
    protected void stop(ServerLevel world, VillagerEntityMCA villager, long time) {
        ItemStack stack = villager.getItemInHand(villager.getDominantHand());
        if (!stack.isEmpty()) {
            villager.setItemInHand(villager.getDominantHand(), ItemStack.EMPTY);
        }
    }

    @Override
    protected void start(ServerLevel world, VillagerEntityMCA villager, long time) {
        super.start(world, villager, time);

        if (!villager.hasItemInSlot(villager.getDominantSlot())) {
            // In 1.21.11, SwordItem removed - use Item.class and isMeleeWeapon check
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), HuntingTask::isMeleeWeapon);
            if (i == -1) {
                abandonJobWithMessage("chore.hunting.nosword");
            } else {
                ItemStack stack = villager.getInventory().getItem(i);
                villager.setItemInHand(villager.getDominantHand(), stack);
            }
        }
    }

    @Override
    protected void tick(ServerLevel world, VillagerEntityMCA villager, long time) {
        super.tick(world, villager, time);

        // In 1.21.11, SwordItem removed - use Item.class check
        if (!InventoryUtils.contains(villager.getInventory(), Item.class)
                && !villager.hasItemInSlot(villager.getDominantSlot())) {
            abandonJobWithMessage("chore.hunting.nosword");
        } else if (!villager.hasItemInSlot(villager.getDominantSlot())) {
            int i = InventoryUtils.getFirstSlotContainingItem(villager.getInventory(), HuntingTask::isMeleeWeapon);
            ItemStack stack = villager.getInventory().getItem(i);
            villager.setItemInHand(villager.getDominantHand(), stack);
        }

        if (target == null) {
            ticks++;

            if (ticks >= nextAction) {
                ticks = 0;
                if (villager.level().random.nextFloat() >= 0.0D) {
                    villager.level().getEntitiesOfClass(Animal.class, villager.getBoundingBox().inflate(15, 3, 15))
                            .stream()
                            .filter(a -> !(a instanceof TamableAnimal))
                            .filter(a -> !a.isBaby())
                            .min(Comparator.comparingDouble(villager::distanceToSqr))
                            .ifPresent(animal -> {
                                target = animal;
                                villager.moveTowards(target.blockPosition());
                            });
                }

                nextAction = 50;

                if (target == null) {
                    failedTicks = FAILED_COOLDOWN;
                }
            }
        } else {
            villager.moveTowards(target.blockPosition());

            if (target.isDeadOrDying()) {
                // search for EntityItems around the target and grab them
                villager.level().getEntitiesOfClass(ItemEntity.class, villager.getBoundingBox().inflate(15, 3, 15))
                        .forEach(item -> {
                            villager.getInventory().addItem(item.getItem());
                            item.discard();
                        });
                target = null;
            } else if (villager.distanceToSqr(target) <= 12.25F) {
                villager.moveTowards(target.blockPosition());
                villager.swing(villager.getDominantHand());
                target.hurt(world.damageSources().mobAttack(villager), 6.0F);
                villager.getMainHandItem().hurtAndBreak(1, villager, EquipmentSlot.MAINHAND);
            }
        }
    }
}
