package net.conczin.mca.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.MCAClient;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.Gender;

import net.conczin.mca.entity.ai.relationship.VillagerDimensions;
import net.conczin.mca.client.physics.BreastPhysics;
import net.conczin.mca.registry.EntitiesMCA;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.UUID;

public interface CommonVillagerModel<T extends LivingEntity> {
    static VillagerLike<?> getVillager(Level world, UUID uuid) {
        if (MCAClient.fallbackVillager == null) {
            MCAClient.fallbackVillager = EntitiesMCA.FEMALE_VILLAGER.create(world);
        }
        return MCAClient.getPlayerData(uuid).orElse(MCAClient.fallbackVillager);
    }

    static VillagerLike<?> getVillager(Entity villager) {
        if (villager instanceof VillagerLike<?> v) {
            return v;
        } else {
            return getVillager(villager.level(), villager.getUUID());
        }
    }

    ModelPart getBreastPart();

    ModelPart getBodyPart();

    Iterable<ModelPart> getCommonHeadParts();

    Iterable<ModelPart> getCommonBodyParts();

    Iterable<ModelPart> getBreastParts();

    VillagerDimensions.Mutable getDimensions();

    float getBreastSize();

    void setBreastSize(float getBreastSize);

    // Accessors for shared logic
    net.conczin.mca.client.render.wildfire.WildfireBreastRenderer getWildfireRenderer();

    void setCurrentEntity(T entity);

    T getCurrentEntity();

    void setCurrentPartialTicks(float partialTicks);

    float getCurrentPartialTicks();

    net.conczin.mca.client.physics.BreastPhysics.PhysicsConfig makePhysicsConfig(T entity);

    default void updatePhysics(T villager, float partialTicks) {
        setCurrentEntity(villager);
        setCurrentPartialTicks(partialTicks);

        for (ModelPart part : getBreastParts()) {
            part.visible = false;
        }

        // Disable physics in editor screens to prevent vibrating (it's a static
        // preview)
        if (net.minecraft.client.Minecraft
                .getInstance().screen instanceof net.conczin.mca.client.gui.VillagerEditorScreen) {
            return;
        }

        // Physics Tick
        if (villager.level().isClientSide) {
            net.conczin.mca.client.physics.PhysicsState state = net.conczin.mca.client.physics.PhysicsState
                    .get(villager);
            if (state.lastTick != villager.tickCount) {
                state.lastTick = villager.tickCount;
                var armor = net.conczin.mca.client.render.wildfire.IGenderArmor
                        .getArmorConfig(villager.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
                var config = makePhysicsConfig(villager);
                state.leftPhysics.update(villager, armor, config);
                state.rightPhysics.update(villager, armor, config);
            }
        }
    }

    default void renderBreasts(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            int color) {
        T entity = getCurrentEntity();
        if (entity != null) {
            try {
                // Don't render breasts if first-person mode would hide the body
                if (net.conczin.mca.client.firstperson.FirstPersonLogic.shouldHideBody(entity)) {
                    return;
                }

                BreastPhysics.PhysicsConfig config = makePhysicsConfig(entity);
                if (config == null) {
                    if (entity.tickCount % 100 == 0) {
                        System.err.println(
                                "MCA DEBUG: renderBreasts - config is NULL for " + entity.getClass().getSimpleName());
                    }
                    return;
                }

                boolean canHave = config.canHaveBreasts();
                if (!canHave) {
                    if (entity.tickCount % 100 == 0) {
                        System.err.println("MCA DEBUG: renderBreasts - canHaveBreasts returned FALSE for "
                                + entity.getClass().getSimpleName());
                    }
                    return;
                }

                getWildfireRenderer().render(poseStack, buffer, packedLight, packedOverlay, color, entity,
                        getBodyPart(), getCurrentPartialTicks(), config, 64);
            } catch (Exception e) {
                if (entity.tickCount % 100 == 0) {
                    System.err.println("MCA ERROR: renderBreasts exception for " + entity.getClass().getSimpleName()
                            + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    default void renderCommon(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // head
        float headSize = getDimensions().getHead();

        matrices.pushPose();
        matrices.scale(headSize, headSize, headSize);
        getCommonHeadParts().forEach(a -> a.render(matrices, vertices, light, overlay, color));
        matrices.popPose();

        // body
        getCommonBodyParts().forEach(a -> a.render(matrices, vertices, light, overlay, color));

        // Render breasts using the unified logic
        renderBreasts(matrices, vertices, light, overlay, color);
    }

    default void applyVillagerDimensions(VillagerLike<?> villager, boolean isSneaking) {
        getDimensions().set(villager.getVillagerDimensions());

        // Apply age-based breast size: genetics size * age multiplier
        // BABY/TODDLER/CHILD: 0.0 (flat), TEEN: 0.5 (half), ADULT: 1.0 (full)
        float geneticsBreastSize = villager.getGenetics().getBreastSize();
        float ageBreastMultiplier = villager.getAgeState().getBreasts();
        setBreastSize(geneticsBreastSize * ageBreastMultiplier);

        // Visibility handled by updatePhysics/renderBreasts now
        // getBreastPart().visible = villager.getGenetics().getGender() ==
        // Gender.FEMALE;

        for (ModelPart part : getBreastParts()) {
            part.xRot = (float) Math.PI * 0.3f + getBodyPart().xRot;

            float cy = 0.0f;
            float cz = 0.0f;
            if (isSneaking) {
                cy = 3.0f;
                cz = 1.5f;
            }

            part.setPos(0.25f, (float) (5.0f - Math.pow(getBreastSize(), 0.5) * 2.5f + cy),
                    -1.5f + getBreastSize() * 0.25f + cz);
        }
    }

    default void copyCommonAttributes(CommonVillagerModel<T> target) {
        target.getDimensions().set(getDimensions());
        target.setBreastSize(getBreastSize());
    }
}
