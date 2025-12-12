package net.conczin.mca.client.render.wildfire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.conczin.mca.client.physics.BreastPhysics;
import net.conczin.mca.client.physics.PhysicsState;
import net.conczin.mca.client.render.wildfire.uv.UVLayout;
import net.conczin.mca.client.render.wildfire.WildfireModelRenderer.BreastModelBox;
import net.conczin.mca.client.render.wildfire.WildfireModelRenderer.OverlayModelBox;
import net.conczin.mca.client.firstperson.FPMCompat;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Objects;
import java.util.function.Consumer;

import net.conczin.mca.client.render.wildfire.uv.UVQuad;

/**
 * ====================================================================
 * WILDFIRE GENDER MODS INTEGRATION
 * ====================================================================
 * This code is adapted from Wildfire's Female Gender Mod
 * Original mod: https://github.com/WildfireRomeo/WildfireFemaleGenderMod
 * Author: WildfireRomeo
 * License: LGPL-3.0
 * 
 * Integrated into MCA to provide breast physics and rendering for
 * female villager models.
 * ====================================================================
 * 
 * @author WildfireRomeo (original Wildfire Gender Mod)
 * @author MCA Team (integration and adaptation)
 */
public class WildfireBreastRenderer {

    // Default UV Layouts from Wildfire Configuration
    private static final UVLayout LEFT_BREAST_UV_LAYOUT = new UVLayout(
            new UVQuad(24, 21, 27, 26), // EAST
            new UVQuad(16, 21, 20, 26), // WEST
            new UVQuad(20, 17, 24, 21), // DOWN
            new UVQuad(20, 25, 24, 27), // UP
            new UVQuad(20, 21, 24, 26), // NORTH
            new UVQuad(20, 21, 24, 26) // SOUTH (Reusing NORTH)
    );

    private static final UVLayout RIGHT_BREAST_UV_LAYOUT = new UVLayout(
            new UVQuad(28, 21, 32, 26), // EAST
            new UVQuad(21, 21, 24, 26), // WEST
            new UVQuad(24, 17, 28, 21), // DOWN
            new UVQuad(24, 25, 28, 27), // UP
            new UVQuad(24, 21, 28, 26), // NORTH
            new UVQuad(24, 21, 28, 26) // SOUTH (Reusing NORTH)
    );

    private static final UVLayout LEFT_BREAST_OVERLAY_UV_LAYOUT = new UVLayout(
            new UVQuad(0, 0, 0, 0), // EAST (not used)
            new UVQuad(17, 37, 20, 42), // WEST
            new UVQuad(20, 34, 24, 37), // DOWN
            new UVQuad(20, 42, 24, 45), // UP
            new UVQuad(20, 37, 24, 42), // NORTH
            new UVQuad(20, 37, 24, 42) // SOUTH
    );

    private static final UVLayout RIGHT_BREAST_OVERLAY_UV_LAYOUT = new UVLayout(
            new UVQuad(28, 37, 31, 42), // EAST
            new UVQuad(0, 0, 0, 0), // WEST (not used)
            new UVQuad(24, 34, 28, 37), // DOWN
            new UVQuad(24, 42, 28, 45), // UP
            new UVQuad(24, 37, 28, 42), // NORTH
            new UVQuad(24, 37, 28, 42) // SOUTH
    );

    private static final float DEG_TO_RAD = (float) (Math.PI / 180);

    private BreastModelBox lBreast, rBreast;
    private OverlayModelBox lBreastWear, rBreastWear;

    private UVLayout prevLeftBreastUVLayout;
    private UVLayout prevRightBreastUVLayout;
    private UVLayout prevLeftBreastOverlayUVLayout;
    private UVLayout prevRightBreastOverlayUVLayout;

    protected float breastOffsetX, breastOffsetY, breastOffsetZ, lPhysPositionY, lPhysPositionX, rPhysPositionY,
            rPhysPositionX,
            lPhysBounceRotation, rPhysBounceRotation, breastSize, zOffset, outwardAngle;

    private boolean isUniboob;
    protected ItemStack armorStack;
    protected IGenderArmor genderArmor;
    protected boolean isChestplateOccupied, bounceEnabled, breathingAnimation;

    private boolean isArmor;
    private float dilation = 0.0f;
    private float currentPartialTicks;
    private LivingEntity currentEntity;

    private int texHeight = 64;
    private int lastTexHeight = -1;

    public void setDilation(float dilation) {
        this.dilation = dilation;
    }

    // Overloaded render to accept VertexConsumer directly (better for integration)
    public void render(PoseStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color,
            LivingEntity entity, ModelPart body, float partialTicks, BreastPhysics.PhysicsConfig config,
            int texHeight) {
        render(matrixStack, vertexConsumer, light, overlay, color, entity, body, partialTicks, config, false,
                texHeight);
    }

    public void render(PoseStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color,
            LivingEntity entity, ModelPart body, float partialTicks, BreastPhysics.PhysicsConfig config,
            boolean isArmor, int texHeight) {

        this.texHeight = texHeight;
        this.isArmor = isArmor;

        if (!setupRender(entity, config, partialTicks)) {
            // If setupRender failed, but it's armor, we might want to force it?
            // But setupRender handles physics updates too.
            return;
        }

        renderSides(matrixStack, body, side -> {
            renderBreast(matrixStack, vertexConsumer, light, overlay, color, side);
        });
    }

    protected boolean setupRender(LivingEntity entity, BreastPhysics.PhysicsConfig config, float partialTicks) {
        this.currentPartialTicks = partialTicks;
        this.currentEntity = entity;
        if (config == null)
            return false;

        armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        genderArmor = IGenderArmor.getArmorConfig(armorStack);
        isChestplateOccupied = genderArmor.coversBreasts() && !config.getArmorPhysicsOverride();

        if (!isArmor && armorStack.isEmpty()) {
            isChestplateOccupied = false;
        }

        // Get Physics State
        PhysicsState physicsState = PhysicsState.get(entity);
        BreastPhysics leftPhysics = physicsState.leftPhysics;
        BreastPhysics rightPhysics = physicsState.rightPhysics;

        // Populate physics positions and rotations with interpolation
        lPhysPositionX = Mth.lerp(partialTicks, leftPhysics.prePositionX, leftPhysics.positionX);
        lPhysPositionY = Mth.lerp(partialTicks, leftPhysics.prePositionY, leftPhysics.positionY);
        lPhysBounceRotation = Mth.lerp(partialTicks, leftPhysics.wfg_preBounceRotation, leftPhysics.wfg_bounceRotation);

        rPhysPositionX = Mth.lerp(partialTicks, rightPhysics.prePositionX, rightPhysics.positionX);
        rPhysPositionY = Mth.lerp(partialTicks, rightPhysics.prePositionY, rightPhysics.positionY);
        rPhysBounceRotation = Mth.lerp(partialTicks, rightPhysics.wfg_preBounceRotation,
                rightPhysics.wfg_bounceRotation);

        if (config.isUniboob()) {
            rPhysPositionX = lPhysPositionX;
            rPhysPositionY = lPhysPositionY;
            rPhysBounceRotation = lPhysBounceRotation;
        }

        breastOffsetX = config.getBreastXOffset();
        breastOffsetY = config.getBreastYOffset();
        breastOffsetZ = config.getBreastZOffset();

        isUniboob = config.isUniboob();

        float bSize = config.getBustSize();
        outwardAngle = config.getCleavage() * 100f;
        outwardAngle = Math.min(outwardAngle, 10);

        // Resize box if needed (UV layouts)
        resizeBox(config);

        // Simplified scaling: Linear growth based on gene value.
        // Multiplier 2.5f allows for very large sizes at max gene value.
        breastSize = bSize * 2.5f;

        // Adjust Z offset to push breasts outward as they grow
        // 0.0625f is base offset. Larger breasts need to move forward (negative Z)
        // more.
        zOffset = 0.0625f - (bSize * 0.25f);

        float resistance = Mth.clamp(genderArmor.physicsResistance(), 0, 1);
        // Assuming breathing is always enabled for now, or we could add it to config
        boolean isBreathing = true;
        breathingAnimation = ((config.getArmorPhysicsOverride() || resistance <= 0.5F) && isBreathing);

        // Enable bounce if resistance is not total, even if chestplate is occupied
        // RELAXED: Trust the physics engine to handle damping.
        // Only disable if the config explicitly says to override physics (which usually
        // means "static" or "vanilla" behavior).
        bounceEnabled = true;

        // Force armor to follow physics state ("glued" to body)
        if (isArmor) {
            bounceEnabled = true;
        }

        return true;
    }

    protected void setupTransformations(ModelPart body, PoseStack matrixStack, BreastSide side) {
        if (body.zRot != 0.0F || body.yRot != 0.0F || body.xRot != 0.0F) {
            matrixStack.mulPose(new Quaternionf().rotationZYX(body.zRot, body.yRot, body.xRot));
        }

        if (isArmor) {
            // Wildfire Armor Scaling Logic
            // Apply extra scaling for outer armor (jacket layer) if dilation is high
            // (standard outer armor is 1.0)
            if (this.dilation > 0.5f) {
                matrixStack.translate(0, 0, -0.05f);
                matrixStack.scale(1.07f, 1.07f, 1.07f);
            }
            matrixStack.translate(side.isLeft ? 0.002f : -0.002f, 0.015f, -0.1f);
            matrixStack.scale(1.07f, 1, 1);
        }

        if (bounceEnabled) {
            matrixStack.translate((side.isLeft ? lPhysPositionX : rPhysPositionX) / 32f, 0, 0);
            matrixStack.translate(0, (side.isLeft ? lPhysPositionY : rPhysPositionY) / 32f, 0);
        }

        matrixStack.translate((side.isLeft ? (breastOffsetX - 0.5f) : -(breastOffsetX - 0.5f)) * 0.25f,
                0.05625f + ((breastOffsetY - 0.5f) * 0.25f), zOffset - 0.0625f * 2f + ((breastOffsetZ - 0.5f) * 0.2f));

        if (!isUniboob) {
            matrixStack.translate(-0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }
        if (bounceEnabled) {
            matrixStack.mulPose(new Quaternionf().rotationXYZ(0,
                    (float) ((side.isLeft ? lPhysBounceRotation : rPhysBounceRotation) * (Math.PI / 180f)), 0));
        }
        if (!isUniboob) {
            matrixStack.translate(0.0625f * 2 * (side.isLeft ? 1 : -1), 0, 0);
        }

        float rotation = breastSize;
        if (bounceEnabled) {

            rotation -= (side.isLeft ? lPhysPositionY : rPhysPositionY) / 12f;
        }

        rotation = Math.min(rotation, breastSize + 0.2f);
        rotation = Math.min(rotation, 1);

        if (isChestplateOccupied) {
            matrixStack.translate(0, 0, 0.01f);
        }

        Quaternionf rotationTransform = new Quaternionf()
                .rotationY((side.isLeft ? outwardAngle : -outwardAngle) * DEG_TO_RAD)
                .rotateX(-37f * rotation * DEG_TO_RAD);

        if (breathingAnimation) {
            float age = currentEntity.tickCount + currentPartialTicks;
            float f5 = -Mth.cos(age * 0.09F) * 0.45F + 0.45F;
            rotationTransform.rotateX(f5 * DEG_TO_RAD);
        }

        matrixStack.mulPose(rotationTransform);

        // Apply physical scaling based on breast size
        // Quadratic curve for "slow start, fast finish" growth
        // TUNED: Reduced from 0.4f to 0.2f to further reduce max size
        float physicalScale = 1.0f + (breastSize * breastSize * 0.2f);
        matrixStack.scale(physicalScale, physicalScale, physicalScale);

        matrixStack.scale(0.9995f, 1f, 1f);
    }

    private void resizeBox(BreastPhysics.PhysicsConfig config) {
        // Check if texture height changed, if so force recreation
        if (this.texHeight != this.lastTexHeight) {
            this.lBreast = null;
            this.rBreast = null;
            this.lBreastWear = null;
            this.rBreastWear = null;
            this.lastTexHeight = this.texHeight;
        }

        if (lBreast == null || rBreast == null || lBreastWear == null || rBreastWear == null) {
            this.lBreast = new BreastModelBox(64, this.texHeight, -4F, 0.0F, 0F, 4, 5, 3, 0.0F, LEFT_BREAST_UV_LAYOUT);
            this.rBreast = new BreastModelBox(64, this.texHeight, 0F, 0.0F, 0F, 4, 5, 3, 0.0F, RIGHT_BREAST_UV_LAYOUT);
            // Increased delta from 0.25F to 0.4F to prevent culling interference in
            // first-person view
            this.lBreastWear = new OverlayModelBox(64, this.texHeight, -4F, 0.0F, 0F, 4, 5, 3, 0.4F,
                    LEFT_BREAST_OVERLAY_UV_LAYOUT);
            this.rBreastWear = new OverlayModelBox(64, this.texHeight, 0, 0.0F, 0F, 4, 5, 3, 0.4F,
                    RIGHT_BREAST_OVERLAY_UV_LAYOUT);
        }
    }

    private void renderBreast(PoseStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color,
            BreastSide side) {
        // Always use the standard breast model (reused for armor)
        var model = side.isLeft ? lBreast : rBreast;
        renderBox(model, matrixStack, vertexConsumer, light, overlay, color);

        // Render wear layer (clothing/jacket overlay) when appropriate
        // The wear layer should ALWAYS render when:
        // 1. We're not rendering the armor layer itself (!isArmor)
        // 2. No chest armor is equipped (armorStack.isEmpty()) OR physics override is
        // enabled
        if (!isArmor) {
            // Simplified logic: Always render wear layer unless actual armor is covering
            // breasts
            boolean shouldRenderWear = armorStack.isEmpty() || !isChestplateOccupied;
            if (shouldRenderWear) {
                var wearModel = side.isLeft ? lBreastWear : rBreastWear;

                // FIRST-PERSON CULLING PROTECTION:
                // When FPM is rendering the player, add extra z-offset to prevent culling
                // interference from other mods (like Sodium culling optimizations)
                boolean isFirstPersonRendering = FPMCompat.isFPMRenderingPlayer();
                if (isFirstPersonRendering) {
                    // Push wear layer slightly forward to ensure it renders on top of base breast
                    matrixStack.pushPose();
                    matrixStack.translate(0, 0, -0.015f); // Small z-offset to prevent z-fighting
                    renderBox(wearModel, matrixStack, vertexConsumer, light, overlay, color);
                    matrixStack.popPose();
                } else {
                    renderBox(wearModel, matrixStack, vertexConsumer, light, overlay, color);
                }
            }
        }
    }

    protected void renderSides(PoseStack matrixStack, ModelPart body, Consumer<BreastSide> renderer) {
        matrixStack.pushPose();
        try {
            setupTransformations(body, matrixStack, BreastSide.LEFT);
            renderer.accept(BreastSide.LEFT);
        } finally {
            matrixStack.popPose();
        }

        matrixStack.pushPose();
        try {
            setupTransformations(body, matrixStack, BreastSide.RIGHT);
            renderer.accept(BreastSide.RIGHT);
        } finally {
            matrixStack.popPose();
        }
    }

    public static void renderBox(net.conczin.mca.client.render.wildfire.WildfireModelRenderer.ModelBox model,
            PoseStack matrixStack, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        PoseStack.Pose entry = matrixStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        for (var quad : model.quads) {
            if (quad == null)
                continue;
            if (quad.uvs[0] == 0.0F && quad.uvs[1] == 0.0F && quad.uvs[2] == 0.0F && quad.uvs[3] == 0.0F)
                continue;

            Vector3f vector3f = new Vector3f(quad.normal.x(), quad.normal.y(), quad.normal.z()).mul(matrix3f);
            float normalX = vector3f.x;
            float normalY = vector3f.y;
            float normalZ = vector3f.z;

            for (var vertex : quad.vertexPositions) {
                float j = vertex.x() / 16.0F;
                float k = vertex.y() / 16.0F;
                float l = vertex.z() / 16.0F;
                Vector4f vector4f = new Vector4f(j, k, l, 1.0F).mul(matrix4f);
                vertexConsumer.addVertex(vector4f.x(), vector4f.y(), vector4f.z())
                        .setColor(color)
                        .setUv(vertex.u(), vertex.v())
                        .setOverlay(overlay)
                        .setLight(light)
                        .setNormal(normalX, normalY, normalZ);
            }
        }
    }
}
