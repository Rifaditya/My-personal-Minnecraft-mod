package net.conczin.mca.client.physics;

import net.conczin.mca.client.render.wildfire.IGenderArmor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffects;

import java.util.concurrent.ThreadLocalRandom;

public class BreastPhysics {

    public static final float TIGHTNESS_REDUCTION_FACTOR = 0.15F;

    // X-Axis
    public float bounceVelX = 0, targetBounceX = 0, velocityX = 0, positionX, prePositionX;
    // Y-Axis
    public float bounceVel = 0, targetBounceY = 0, velocity = 0, positionY, prePositionY;
    // Rotation
    public float bounceRotVel = 0, targetRotVel = 0, rotVelocity = 0, wfg_bounceRotation, wfg_preBounceRotation;

    public float breastSize = 0, preBreastSize = 0;

    private Pose lastPose;
    private int lastSwingDuration = 6, lastSwingTick = 0;
    private Vec3 prePos;

    private int randomB = 1;
    private double lastVerticalMoveVelocity;

    public interface PhysicsConfig {
        float getBustSize();

        boolean canHaveBreasts();

        float getBounceMultiplier();

        float getFloppiness();

        boolean isUniboob();

        boolean getArmorPhysicsOverride();

        float getBreastXOffset();

        float getBreastYOffset();

        float getBreastZOffset();

        float getCleavage();
    }

    public BreastPhysics() {
    }

    private static boolean vehicleSuppressesRotation(Entity vehicle) {
        return (vehicle instanceof Chicken
                || vehicle instanceof AbstractHorse horseLike && !horseLike.isSaddled()
                || vehicle instanceof Camel camel && camel.getPose() == Pose.SITTING);
    }

    private static boolean shouldUseVehicleYaw(LivingEntity rider, Entity vehicle) {
        return (vehicle.getControllingPassenger() != null
                || vehicle instanceof Boat
                || vehicle.getYRot() == rider.getYRot());
    }

    private float calcRotation(LivingEntity entity, float bounceIntensity) {
        Entity vehicle = entity.getVehicle();
        if (vehicle != null) {
            if (vehicleSuppressesRotation(vehicle)) {
                return 0f;
            } else if (shouldUseVehicleYaw(entity, vehicle)) {
                float previous = vehicle instanceof LivingEntity living ? living.yBodyRotO : vehicle.yRotO;
                return -((vehicle.getYRot() - previous) / 15f) * bounceIntensity;
            }
        }

        float delta = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
        delta = Mth.clamp(delta, -20f, 20f); // Clamp max rotation speed per tick to prevent snapping
        return -(delta / 15f) * bounceIntensity;
    }

    public void update(LivingEntity entity, IGenderArmor armor, PhysicsConfig config) {
        if (entity instanceof ArmorStand) {
            simplifiedTick(armor, config);
            return;
        }

        this.prePositionY = this.positionY;
        this.prePositionX = this.positionX;
        this.wfg_preBounceRotation = this.wfg_bounceRotation;
        this.preBreastSize = this.breastSize;

        if (this.prePos == null) {
            this.prePos = entity.position();
            return;
        }

        float breastWeight = config.getBustSize() * 1.25f;
        float targetBreastSize = config.getBustSize();

        if (!config.canHaveBreasts()) {
            targetBreastSize = 0;
        } else {
            float tightness = Mth.clamp(armor.tightness(), 0, 1);
            if (config.getArmorPhysicsOverride())
                tightness = 0;
            targetBreastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
        }

        breastSize += (breastSize < targetBreastSize) ? Math.abs(breastSize - targetBreastSize) / 2f
                : -Math.abs(breastSize - targetBreastSize) / 2f;

        Vec3 motion = entity.position().subtract(this.prePos);
        // Clamp motion to prevent teleportation or massive speed spikes from breaking
        // physics
        if (motion.lengthSqr() > 1.0) {
            motion = motion.normalize();
        }
        this.prePos = entity.position();

        float bounceIntensity = (targetBreastSize * 2.5f) * Math.round((config.getBounceMultiplier() * 2.5f) * 100)
                / 100f;
        float resistance = Mth.clamp(armor.physicsResistance(), 0, 1);
        if (config.getArmorPhysicsOverride())
            resistance = 0;

        bounceIntensity *= 1 - resistance;

        if (!config.isUniboob()) {
            bounceIntensity = bounceIntensity * randFloat(0.5f, 1.5f);
        }

        tickMovement(entity, motion, bounceIntensity, breastWeight);
        tickPose(entity, bounceIntensity);
        tickVehicle(entity, bounceIntensity, breastWeight);
        tickArmSwing(entity, bounceIntensity);
        finishTick(config);
    }

    private void simplifiedTick(IGenderArmor armor, PhysicsConfig config) {
        if (config.canHaveBreasts()) {
            this.breastSize = config.getBustSize();
            if (!config.getArmorPhysicsOverride()) {
                float tightness = Mth.clamp(armor.tightness(), 0, 1);
                this.breastSize *= 1 - TIGHTNESS_REDUCTION_FACTOR * tightness;
            }
            this.preBreastSize = this.breastSize;
        } else {
            this.preBreastSize = this.breastSize = 0f;
        }
    }

    private void tickMovement(final LivingEntity entity, final Vec3 motion, final float bounceIntensity,
            final float breastWeight) {
        double vertVelocity = entity.getDeltaMovement().y;
        if ((lastVerticalMoveVelocity <= 0 && vertVelocity > 0)
                || (lastVerticalMoveVelocity < 0 && vertVelocity == 0)) {
            randomB = entity.level().random.nextBoolean() ? -1 : 1;
        }
        lastVerticalMoveVelocity = vertVelocity;

        this.targetBounceY = (float) motion.y * bounceIntensity;
        this.targetBounceY += breastWeight;

        this.targetRotVel = calcRotation(entity, bounceIntensity);
        this.targetRotVel += (float) motion.y * bounceIntensity * randomB;

        this.targetBounceX = -calcRotation(entity, bounceIntensity) / 10f;

        float f2 = (float) entity.getDeltaMovement().lengthSqr() / 0.2F;
        f2 = f2 * f2 * f2;
        if (f2 < 1.0F)
            f2 = 1.0F;
    }

    private void tickPose(final LivingEntity entity, final float bounceIntensity) {
        Pose pose = entity.getPose();
        if (pose != lastPose) {
            if (pose == Pose.CROUCHING || lastPose == Pose.CROUCHING) {
                this.targetBounceY += bounceIntensity;
            } else if (pose == Pose.SLEEPING || lastPose == Pose.SLEEPING) {
                this.targetBounceY = bounceIntensity;
            }
            lastPose = pose;
        }
    }

    private void tickVehicle(LivingEntity entity, final float bounceIntensity, final float breastWeight) {
        Entity vehicle = entity.getVehicle();
        if (vehicle instanceof Boat boat) {
            // Boat logic simplified or mapped
            // Assuming lerpPaddlePhase is not easily accessible or mapped differently,
            // skipping precise boat paddle sync for now
            this.targetBounceY = bounceIntensity / 3.25f;
        } else if (vehicle instanceof Minecart cart) {
            float speed = (float) cart.getDeltaMovement().lengthSqr();
            if (Math.random() * speed < 0.5f && speed > 0.2f) {
                this.targetBounceY = (Math.random() > 0.5 ? -bounceIntensity : bounceIntensity) / 6f;
                this.targetBounceY += breastWeight;
            }
        } else if (vehicle instanceof AbstractHorse horse) {
            float movement = (float) horse.getDeltaMovement().lengthSqr();
            if (horse.tickCount % clampMovement(movement) == 5 && movement > 0.05f) {
                this.targetBounceY = bounceIntensity / 4f;
                this.targetBounceY += breastWeight;
            }
        } else if (vehicle instanceof Pig pig) {
            float movement = (float) pig.getDeltaMovement().lengthSqr();
            if (pig.tickCount % clampMovement(movement) == 5 && movement > 0.002f) {
                this.targetBounceY = (bounceIntensity * Mth.clamp(movement * 75, 0.1f, 1f)) / 4f;
                this.targetBounceY += breastWeight;
            }
        } else if (vehicle instanceof Strider strider) {
            // Strider logic
            this.targetBounceY += bounceIntensity; // Simplified
        }
    }

    private void tickArmSwing(LivingEntity entity, final float bounceIntensity) {
        int swingDuration = getCurrentSwingDuration(entity);
        if ((swingDuration > 1 || lastSwingDuration > 1) && entity.getPose() != Pose.SLEEPING) {
            float rawAmplifier = 0f;
            if (swingDuration < 6) {
                rawAmplifier = 0.15f * (6 - swingDuration);
            } else if (swingDuration > 6) {
                rawAmplifier = -0.055f * (swingDuration - 6);
            }
            float amplifier = Mth.clamp(1 + rawAmplifier, 0.6f, 1.3f);

            HumanoidArm swingingArm = entity.getMainArm(); // Simplified, assuming main hand swing
            int swingTickDelta = entity.swingTime - lastSwingTick;
            float swingProgress = distanceFromMedian(0, lastSwingDuration,
                    Mth.clamp(lastSwingTick, 0, lastSwingDuration));
            HumanoidArm swingingToward = swingProgress > -0.2f ? swingingArm.getOpposite() : swingingArm;

            int everyNthTick = Mth.clamp(swingDuration - 1, 1, 5);
            if (entity.swinging && entity.tickCount % everyNthTick == 0) {
                this.targetBounceY += (Math.random() > 0.5 ? -0.25f : 0.25f) * amplifier * bounceIntensity;
                var xAmp = Mth.clamp(1 + (rawAmplifier * (rawAmplifier < 0 ? 1.625f : 0.8f)), 0.25f, 1.225f);
                this.targetBounceX = (0.325f * xAmp * bounceIntensity) * (swingingArm == HumanoidArm.RIGHT ? -1f : 1f);
            }

            if (swingTickDelta < 0 && lastSwingTick != lastSwingDuration - 1) {
                this.targetRotVel += (swingingArm == HumanoidArm.RIGHT ? -4f : 4f) * Math.abs(swingProgress)
                        * bounceIntensity;
            } else if (entity.swinging && swingDuration > 1) {
                this.targetRotVel += (swingingToward == HumanoidArm.RIGHT ? -0.2f : 0.2f) * amplifier * bounceIntensity;
            }
            lastSwingTick = entity.swingTime;
        }
        if (!entity.swinging) {
            lastSwingTick = 0;
        }
        lastSwingDuration = Math.max(swingDuration, 1);
    }

    private void finishTick(PhysicsConfig config) {
        float percent = config.getFloppiness();
        float bounceAmount = 0.45f * (1f - percent) + 0.15f;
        bounceAmount = Mth.clamp(bounceAmount, 0.15f, 0.6f);
        float delta = 2.25f - bounceAmount;

        float distanceFromMin = Math.abs(bounceVel + 1.5f) * 0.5f;
        float distanceFromMax = Math.abs(bounceVel - 2.65f) * 0.5f;

        if (bounceVel < -0.5f) {
            targetBounceY += distanceFromMin;
        }
        if (bounceVel > 2.5f) {
            targetBounceY -= distanceFromMax;
        }

        targetBounceY = Mth.clamp(targetBounceY, -1.5f, 2.5f);
        targetRotVel = Mth.clamp(targetRotVel, -25f, 25f);

        this.velocity = Mth.lerp(bounceAmount, this.velocity, (this.targetBounceY - this.bounceVel) * delta);
        this.bounceVel += this.velocity * percent * 1.1625f;

        // Clamp Velocity
        this.bounceVel = Mth.clamp(this.bounceVel, -3.0f, 3.0f);

        // X
        this.velocityX = Mth.lerp(bounceAmount, this.velocityX, (this.targetBounceX - this.bounceVelX) * delta);
        this.bounceVelX += this.velocityX * percent;

        // Clamp X Velocity
        this.bounceVelX = Mth.clamp(this.bounceVelX, -2.0f, 2.0f);

        this.rotVelocity = Mth.lerp(bounceAmount, this.rotVelocity, (this.targetRotVel - this.bounceRotVel) * delta);
        this.bounceRotVel += this.rotVelocity * percent;

        // Clamp Rotation Velocity
        this.bounceRotVel = Mth.clamp(this.bounceRotVel, -45f, 45f);

        this.wfg_bounceRotation = this.bounceRotVel;
        this.positionX = this.bounceVelX;
        this.positionY = this.bounceVel;

        // Safety checks for NaN
        if (Float.isNaN(this.positionX) || Float.isInfinite(this.positionX)) {
            this.positionX = 0;
            this.bounceVelX = 0;
            this.velocityX = 0;
        }
        if (Float.isNaN(this.positionY) || Float.isInfinite(this.positionY)) {
            this.positionY = 0;
            this.bounceVel = 0;
            this.velocity = 0;
        }
        if (Float.isNaN(this.wfg_bounceRotation) || Float.isInfinite(this.wfg_bounceRotation)) {
            this.wfg_bounceRotation = 0;
            this.bounceRotVel = 0;
            this.rotVelocity = 0;
        }

        if (this.positionY < -0.5f)
            this.positionY = -0.5f;
        if (this.positionY > 1.5f) {
            this.positionY = 1.5f;
            this.velocity = 0;
        }

        // Clamp X to prevent flying off sideways
        if (this.positionX < -1.0f) {
            this.positionX = -1.0f;
            this.velocityX = 0;
        }
        if (this.positionX > 1.0f) {
            this.positionX = 1.0f;
            this.velocityX = 0;
        }
    }

    public float getPrePositionY() {
        return this.prePositionY;
    }

    public float getPositionY() {
        return this.positionY;
    }

    public float getPrePositionX() {
        return this.prePositionX;
    }

    public float getPositionX() {
        return this.positionX;
    }

    public float getBounceRotation() {
        return this.wfg_bounceRotation;
    }

    public float getPreBounceRotation() {
        return this.wfg_preBounceRotation;
    }

    public float getBreastSize() {
        return this.breastSize;
    }

    public float getPreBreastSize() {
        return this.preBreastSize;
    }

    private int clampMovement(float movement) {
        return Math.max((int) (10 - movement * 2f), 1);
    }

    private static float distanceFromMedian(final int p1, final int p2, float point) {
        if (p1 >= p2)
            throw new IllegalArgumentException("p2 must be greater than p1");
        if (point < p1 || point > p2)
            throw new IllegalArgumentException(point + " is not within bounds");

        if (point == p1 || point == p2)
            return 0;
        float median = (p2 - p1) / 2f;
        point -= p1;
        if (point > median) {
            point = -(median - (point - median));
        }
        return point / median;
    }

    private static float randFloat(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    private int getCurrentSwingDuration(LivingEntity entity) {
        if (entity.hasEffect(MobEffects.DIG_SPEED)) {
            return 6 - (1 + entity.getEffect(MobEffects.DIG_SPEED).getAmplifier());
        } else {
            return entity.hasEffect(MobEffects.DIG_SLOWDOWN)
                    ? 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2
                    : 6;
        }
    }

}
