package net.conczin.mca.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.MCAClient;
import net.conczin.mca.client.render.VillagerLikeRenderState;
import net.conczin.mca.entity.VillagerLike;
import net.conczin.mca.entity.ai.relationship.Gender;
import net.conczin.mca.entity.ai.relationship.VillagerDimensions;
import net.conczin.mca.registry.EntitiesMCA;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Utility method to copy model part transformations - ModelPart.copyFrom() was
 * removed in 1.21.11
 */
class ModelPartHelper {
    public static void copyTransform(ModelPart target, ModelPart source) {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
        target.xScale = source.xScale;
        target.yScale = source.yScale;
        target.zScale = source.zScale;
        target.visible = source.visible;
    }
}

/**
 * CommonVillagerModel - updated for 1.21.11 API
 * Now uses RenderState-extending types instead of entity types
 */
public interface CommonVillagerModel<S extends VillagerLikeRenderState> {
    static VillagerLike<?> getVillager(Level world, UUID uuid) {
        if (MCAClient.fallbackVillager == null) {
            MCAClient.fallbackVillager = EntitiesMCA.MALE_VILLAGER.create(world);
        }
        return MCAClient.playerData.getOrDefault(uuid, MCAClient.fallbackVillager);
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

    default void renderCommon(PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        // head
        float headSize = getDimensions().getHead();

        matrices.pushPose();
        matrices.scale(headSize, headSize, headSize);
        getCommonHeadParts().forEach(a -> a.render(matrices, vertices, light, overlay, color));
        matrices.popPose();

        // body
        getCommonBodyParts().forEach(a -> a.render(matrices, vertices, light, overlay, color));

        if (getBreastPart().visible && getBodyPart().visible) {
            float breastSize = getBreastSize() * getDimensions().getBreasts();

            if (breastSize > 0) {
                matrices.pushPose();
                matrices.scale(breastSize * 0.2f + 1.05f, breastSize * 0.75f + 0.75f, breastSize * 0.75f + 0.75f);
                for (ModelPart part : getBreastParts()) {
                    part.render(matrices, vertices, light, overlay, color);
                }
                matrices.popPose();
            }
        }
    }

    /**
     * Apply villager dimensions from RenderState
     * Updated for 1.21.11 API - uses RenderState instead of entity
     */
    default void applyVillagerDimensions(S state, boolean isSneaking) {
        // Get dimensions and breast size from state
        setBreastSize(state.breastSize);
        getBreastPart().visible = state.gender == Gender.FEMALE;

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

    default void copyCommonAttributes(CommonVillagerModel<S> target) {
        target.getDimensions().set(getDimensions());
        target.setBreastSize(getBreastSize());
    }
}
