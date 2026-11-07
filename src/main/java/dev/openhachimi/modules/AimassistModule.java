/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.NumberSetting;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

public class AimassistModule
extends Module {
    private static final byte[] S_3C383FEE = new byte[]{3, 43, 47, 3, 49, 49, 43, 49, 54};  // XOR0x42 "AimAssist"
    private static final byte[] S_20F2E62 = new byte[]{3, 43, 47, 98, 16, 35, 44, 37, 39};  // XOR0x42 "Aim Range"
    private static final byte[] S_114AD = new byte[]{4, 45, 52};  // XOR0x42 "Fov"
    private static final byte[] S_18E2494B = new byte[]{10, 45, 48, 43, 56, 45, 44, 54, 35, 46, 98, 17, 50, 39, 39, 38};  // XOR0x42 "Horizontal Speed"
    private static final byte[] S_505A671D = new byte[]{20, 39, 48, 54, 43, 33, 35, 46, 98, 17, 50, 39, 39, 38};  // XOR0x42 "Vertical Speed"
    private static final byte[] S_1359E97E = new byte[]{3, 43, 47, 98, 20, 39, 48, 54, 43, 33, 35, 46, 46, 59};  // XOR0x42 "Aim Vertically"
    private static final byte[] S_1BBCF938 = new byte[]{16, 39, 51, 55, 43, 48, 39, 98, 15, 45, 55, 49, 39, 98, 6, 45, 53, 44};  // XOR0x42 "Require Mouse Down"
    private static final byte[] S_617AFA33 = new byte[]{17, 54, 48, 35, 36, 39, 98, 11, 44, 33, 48, 39, 35, 49, 39};  // XOR0x42 "Strafe Increase"
    private static final byte[] S_6A883F32 = new byte[]{20, 43, 49, 43, 32, 43, 46, 43, 54, 59};  // XOR0x42 "Visibility"
    private static final byte[] S_45CD5992 = new byte[]{18, 46, 35, 59, 39, 48, 49};  // XOR0x42 "Players"
    private static final byte[] S_24AD93 = new byte[]{15, 45, 32, 49};  // XOR0x42 "Mobs"
    private static final byte[] S_61A74A34 = new byte[]{1, 46, 45, 49, 39, 49, 54, 98, 3, 48, 39, 35};  // XOR0x42 "Closest Area"
    private final NumberSetting distance = new NumberSetting("Aim Range", 5.0f, 1.0f, 8.0f);
    private final NumberSetting maxAngle = new NumberSetting("Fov", 180.0f, 5.0f, 360.0f);
    private final NumberSetting horizontalSpeed = new NumberSetting("Horizontal Speed", 5.0f, 1.0f, 30.0f);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", 5.0f, 1.0f, 30.0f);
    private final BooleanSetting aimVertically = new BooleanSetting("Aim Vertically", false);
    private final BooleanSetting requireMouseDown = new BooleanSetting("Require Mouse Down", true);
    private final BooleanSetting strafeIncrease = new BooleanSetting("Strafe Increase", false);
    private final BooleanSetting visibility = new BooleanSetting("Visibility", true);
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting closestArea = new BooleanSetting("Closest Area", false);
    private final Random random = new Random();
    private LivingEntity target;
    private double prevTargetX;
    private double prevTargetZ;
    private double targetX;
    private double targetY;
    private double targetZ;
    private float horizontalMouseAccumulator;
    private float verticalMouseAccumulator;
    private float horizontalVelocity;
    private float horizontalVelocityBuffer;
    private float verticalVelocity;
    private float verticalVelocityBuffer;
    private int sampleCounter;
    private int retargetCounter;
    private boolean prevOnLeft;
    private boolean prevAbove;
    private float pitchBoost;
    private float yawBoost;
    private double lastAngleDiff;
    private double driftTimer;
    private int driftX;
    private int driftY;
    private int randomOffsetX;
    private int randomOffsetY;

    public AimassistModule() {
        super("AimAssist", Category.COMBAT);
        this.values.add(this.distance);
        this.values.add(this.maxAngle);
        this.values.add(this.horizontalSpeed);
        this.values.add(this.verticalSpeed);
        this.values.add(this.aimVertically);
        this.values.add(this.requireMouseDown);
        this.values.add(this.strafeIncrease);
        this.values.add(this.visibility);
        this.values.add(this.players);
        this.values.add(this.mobs);
        this.values.add(this.closestArea);
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    private double range(double low, double high) {
        return low + this.random.nextDouble() * (high - low);
    }

    private boolean isValid(LocalPlayer player, LivingEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        if (AntibotModule.isBot((Entity)entity)) {
            return false;
        }
        if (entity instanceof Player && entity == player) {
            return false;
        }
        if (TeamsModule.isSameTeam((Entity)entity)) {
            return false;
        }
        if (FriendModule.isFriend((Entity)entity)) {
            return false;
        }
        if (entity.isInvisible() && !((Boolean)this.visibility.get()).booleanValue()) {
            return false;
        }
        if ((double)player.distanceTo((Entity)entity) > (double)((Float)this.distance.get()).floatValue()) {
            return false;
        }
        return player.hasLineOfSight((Entity)entity);
    }

    private float angleDiff(float target, float current) {
        float diff = (target - current) % 360.0f;
        if (diff < -180.0f) {
            diff += 360.0f;
        }
        if (diff > 180.0f) {
            diff -= 360.0f;
        }
        return diff;
    }

    private void applyRotation(LocalPlayer player) {
        boolean strafingAway = false; // initialised: only set inside a ternary branch
        this.updateDrift();
        this.targetX = this.target.getX();
        this.targetY = this.target.getY() + (double)this.target.getBbHeight() * 0.5;
        this.targetZ = this.target.getZ();
        if (((Boolean)this.closestArea.get()).booleanValue()) {
            Vec3 closest = this.aimPoint(this.target);
            this.targetX = closest.x;
            this.targetY = closest.y;
            this.targetZ = closest.z;
        }
        double targetMotionX = this.targetX - this.prevTargetX;
        double targetMotionZ = this.targetZ - this.prevTargetZ;
        this.prevTargetX = this.targetX;
        this.prevTargetZ = this.targetZ;
        double predictionFactor = 1.7;
        double predictedTargetX = this.targetX + targetMotionX * predictionFactor;
        double predictedTargetZ = this.targetZ + targetMotionZ * predictionFactor;
        Vec3 eye = player.getEyePosition(1.0f);
        float viewYaw = player.getYRot();
        float viewPitch = player.getXRot();
        float desiredYaw = this.yawTo(eye, new Vec3(predictedTargetX, this.targetY, predictedTargetZ));
        float explicitYawDiff = this.angleDiff(desiredYaw, viewYaw);
        double horizontalAngleDifference = Math.abs(explicitYawDiff);
        boolean targetOnLeft = explicitYawDiff < 0.0f;
        float desiredPitch = this.pitchTo(eye, new Vec3(this.targetX, this.targetY, this.targetZ));
        float explicitPitchDiff = desiredPitch - viewPitch;
        int verticalAngleDifference = (int)explicitPitchDiff;
        boolean targetAbove = verticalAngleDifference < 0;
        int verticalDeadZoneDistance = Math.abs(verticalAngleDifference) - 10;
        double horizontalForce = 1.0;
        double verticalForce = 1.0;
        horizontalForce += this.range(0.0, 2.0);
        horizontalForce += horizontalAngleDifference / 50.0;
        verticalForce += this.range(0.0, 2.0);
        verticalForce += (double)((float)Math.abs(verticalDeadZoneDistance) / 50.0f);
        if (Math.abs(horizontalAngleDifference - this.lastAngleDiff) > 6.0) {
            horizontalForce += horizontalAngleDifference / 35.0;
        }
        double dx = player.getX() - this.target.getX();
        double dz = player.getZ() - this.target.getZ();
        double realDistance = Math.sqrt(dx * dx + dz * dz);
        double proximityBoost = Math.max(0.0, (9.0 - realDistance) / 2.5 - 2.0);
        horizontalForce += proximityBoost;
        float strafeInput = player.input.leftImpulse;
        boolean bl = targetOnLeft ? strafeInput < 0.0f : (strafingAway = strafeInput > 0.0f);
        if (((Boolean)this.strafeIncrease.get()).booleanValue() && strafingAway) {
            horizontalForce *= 1.6;
        }
        if (realDistance < 0.5) {
            horizontalForce /= 5.0;
        }
        float horizontalAcceleration = (float)horizontalForce / 90.0f * (targetOnLeft ? -1.0f : 1.0f);
        float verticalAcceleration = (float)verticalForce / 90.0f * (targetAbove ? 1.0f : -1.0f);
        if (horizontalAngleDifference < 5.0) {
            boolean strafingToward = false; // initialised: only set inside a ternary branch
            horizontalAcceleration = 0.0f;
            this.horizontalVelocity *= 0.7f;
            boolean bl2 = targetOnLeft ? strafeInput > 0.0f : (strafingToward = strafeInput < 0.0f);
            if (strafingToward) {
                this.horizontalVelocity *= 0.5f;
            }
        }
        if (targetOnLeft != this.prevOnLeft) {
            this.horizontalVelocity = -this.horizontalVelocity;
            this.horizontalVelocityBuffer = -this.horizontalVelocityBuffer;
            this.horizontalMouseAccumulator = 0.0f;
        }
        if (targetAbove != this.prevAbove) {
            this.verticalVelocityBuffer = -this.verticalVelocityBuffer;
            this.verticalVelocity = -this.verticalVelocity;
            this.verticalMouseAccumulator = 0.0f;
        }
        if (verticalDeadZoneDistance < 5) {
            verticalAcceleration = 0.0f;
            this.verticalVelocityBuffer *= 0.7f;
        }
        this.horizontalVelocityBuffer += horizontalAcceleration;
        this.verticalVelocity += verticalAcceleration;
        float smoothedHorizontalVelocity = this.horizontalVelocity;
        float smoothedVerticalVelocity = this.verticalVelocityBuffer;
        if (Math.abs(smoothedHorizontalVelocity) > 10.0f) {
            this.horizontalVelocityBuffer = 0.0f;
            this.horizontalVelocity = 0.0f;
            return;
        }
        float horizontalAdjustment = smoothedHorizontalVelocity * 0.15f;
        if (horizontalAngleDifference <= 9.0) {
            horizontalAdjustment = (float)((double)horizontalAdjustment / (10.0 - horizontalAngleDifference));
        }
        if (Float.isNaN(horizontalAdjustment)) {
            this.horizontalVelocityBuffer = 0.0f;
            this.horizontalVelocity = 0.0f;
            return;
        }
        this.queueHorizontalAdjustment(horizontalAdjustment, (float)horizontalAngleDifference);
        if (((Boolean)this.aimVertically.get()).booleanValue()) {
            float verticalAdjustment = smoothedVerticalVelocity * 0.15f;
            if (Float.isNaN(verticalAdjustment)) {
                this.verticalVelocity = 0.0f;
                this.verticalVelocityBuffer = 0.0f;
                return;
            }
            this.queueVerticalAdjustment(verticalAdjustment, verticalAngleDifference);
        }
        this.prevAbove = targetAbove;
        this.prevOnLeft = targetOnLeft;
        ++this.sampleCounter;
        if (this.sampleCounter > 10) {
            this.lastAngleDiff = horizontalAngleDifference;
            this.sampleCounter = 0;
        }
    }

    private void resetRotationState() {
        this.horizontalMouseAccumulator = 0.0f;
        this.verticalMouseAccumulator = 0.0f;
        this.randomOffsetX = 0;
        this.randomOffsetY = 0;
        this.driftX = 0;
        this.driftY = 0;
    }

    private Vec3 aimPoint(LivingEntity entity) {
        if (((Boolean)this.closestArea.get()).booleanValue()) {
            Vec3 eye = Minecraft.getInstance().player.getEyePosition(1.0f);
            double cx = Math.max(entity.getBoundingBox().minX, Math.min(entity.getBoundingBox().maxX, eye.x));
            double cy = Math.max(entity.getBoundingBox().minY, Math.min(entity.getBoundingBox().maxY, eye.y));
            double cz = Math.max(entity.getBoundingBox().minZ, Math.min(entity.getBoundingBox().maxZ, eye.z));
            return new Vec3(cx, cy, cz);
        }
        return new Vec3(entity.getX(), entity.getY() + (double)entity.getBbHeight() * 0.5, entity.getZ());
    }

    private void applyMouseDelta(LocalPlayer player) {
        this.horizontalMouseAccumulator += (float)this.driftX;
        this.verticalMouseAccumulator += (float)this.driftY;
        double sensitivity = (Double)Minecraft.getInstance().options.sensitivity().get();
        float sensitivityBase = (float)sensitivity * 0.6f + 0.2f;
        float sensitivityScale = sensitivityBase * sensitivityBase * sensitivityBase * 8.0f;
        float horizontalDelta = this.horizontalMouseAccumulator * sensitivityScale * 0.15f;
        float verticalDelta = this.verticalMouseAccumulator * sensitivityScale * 0.15f;
        if (horizontalDelta == 0.0f && verticalDelta == 0.0f) {
            this.driftX = 0;
            this.driftY = 0;
            return;
        }
        float yaw = player.getYRot() + horizontalDelta;
        float pitch = Math.max(-90.0f, Math.min(90.0f, player.getXRot() - verticalDelta));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
        player.yRotO += horizontalDelta;
        player.xRotO -= verticalDelta;
        this.horizontalMouseAccumulator = 0.0f;
        this.verticalMouseAccumulator = 0.0f;
        this.driftX = 0;
        this.driftY = 0;
    }

    private float yawTo(Vec3 eye, Vec3 point) {
        double dx = point.x - eye.x;
        double dz = point.z - eye.z;
        return (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    private LivingEntity findBestTarget(LocalPlayer player) {
        boolean allowPlayers = (Boolean)this.players.get();
        boolean allowMobs = (Boolean)this.mobs.get();
        double maxDist = ((Float)this.distance.get()).floatValue();
        double maxAngle = ((Float)this.maxAngle.get()).floatValue();
        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
            double score;
            Vec3 eye;
            float yaw;
            double angle;
            double dist;
            boolean isPlayer;
            LivingEntity living;
            if (!(entity instanceof LivingEntity) || !(living = (LivingEntity)entity).isAlive() || living == player || (isPlayer = living instanceof Player) && !allowPlayers || isPlayer && TeamsModule.isSameTeam((Entity)living) || !isPlayer && !allowMobs || living.isInvisible() && !((Boolean)this.visibility.get()).booleanValue() || AntibotModule.isBot((Entity)living) || FriendModule.isFriend((Entity)living) || (dist = (double)player.distanceTo((Entity)living)) > maxDist || !player.hasLineOfSight((Entity)living) || (angle = (double)Math.abs(this.angleDiff(yaw = this.yawTo(eye = player.getEyePosition(1.0f), this.aimPoint(living)), player.getYRot()))) > maxAngle * 0.5) continue;
            double d = score = (Boolean)this.closestArea.get() != false ? dist : angle;
            if (score >= bestScore) continue;
            bestScore = score;
            best = living;
        }
        return best;
    }

    private float pitchTo(Vec3 eye, Vec3 point) {
        double dx = point.x - eye.x;
        double dy = point.y - eye.y;
        double dz = point.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        return (float)(-Math.toDegrees(Math.atan2(dy, horizontal)));
    }

    private void updateDrift() {
        this.driftTimer += 1.0;
        if (this.driftTimer >= (double)(250 + this.random.nextInt(50))) {
            this.driftTimer = this.range(-100.0, -50.0);
            this.randomOffsetX = this.random.nextInt(3) - 1;
            this.randomOffsetY = this.random.nextInt(3) - 1;
        }
        int horizontalStep = this.randomOffsetX;
        int verticalStep = this.randomOffsetY;
        if (this.random.nextInt(10) < 2) {
            horizontalStep = 0;
        }
        if (this.random.nextInt(10) < 2) {
            verticalStep = 0;
        }
        if (this.driftTimer < 0.0) {
            horizontalStep = 0;
            verticalStep = 0;
        }
        if (this.random.nextInt(20) == 1) {
            this.driftX += horizontalStep;
            this.driftY += verticalStep;
        }
        if (this.horizontalMouseAccumulator > 0.0f && this.driftX < 0 || this.horizontalMouseAccumulator < 0.0f && this.driftX > 0) {
            this.driftX = 0;
        }
    }

    @Override
    public void onDisable() {
        this.target = null;
        this.resetRotationState();
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.resetRotationState();
        this.sampleCounter = 0;
        this.retargetCounter = 0;
        this.prevTargetX = 0.0;
        this.prevTargetZ = 0.0;
    }

    private void queueHorizontalAdjustment(float adjustment, float angleDifference) {
        if (adjustment != 0.0f) {
            adjustment *= 5.0f;
            float speed = ((Float)this.horizontalSpeed.get()).floatValue();
            if (angleDifference <= 10.0f) {
                this.pitchBoost = speed;
            }
            if (this.pitchBoost > 0.0f) {
                speed -= this.pitchBoost / 3.0f;
                this.pitchBoost -= angleDifference / 200.0f;
            }
            this.horizontalMouseAccumulator += speed * adjustment;
        } else {
            this.horizontalMouseAccumulator = 0.0f;
        }
    }

    private void updateVelocityBuffers() {
        this.verticalVelocityBuffer = this.verticalVelocity;
        this.horizontalVelocity = this.horizontalVelocityBuffer;
        this.horizontalVelocityBuffer = 0.0f;
        this.verticalVelocity = 0.0f;
    }

    private void queueVerticalAdjustment(float adjustment, float angleDifference) {
        if (adjustment != 0.0f) {
            adjustment *= 5.0f;
            float speed = ((Float)this.verticalSpeed.get()).floatValue();
            float absoluteAngleDifference = Math.abs(angleDifference);
            if (absoluteAngleDifference <= 10.0f) {
                this.yawBoost = speed;
            }
            if (this.yawBoost > 0.0f) {
                speed -= this.yawBoost / 3.0f;
                this.yawBoost -= absoluteAngleDifference / 200.0f;
            }
            this.verticalMouseAccumulator += speed * adjustment;
        } else {
            this.verticalMouseAccumulator = 0.0f;
        }
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            this.target = null;
            this.resetRotationState();
            return;
        }
        boolean mouseDown = mc.options.keyAttack.isDown();
        boolean require = (Boolean)this.requireMouseDown.get();
        if (!(this.target == null || this.target.isAlive() && this.isValid(player, this.target))) {
            this.resetRotationState();
            this.target = null;
        }
        if (require && !mouseDown) {
            this.target = null;
            this.resetRotationState();
            return;
        }
        if (require && mouseDown && this.target == null || !require) {
            LivingEntity candidate = this.findBestTarget(player);
            if (!require) {
                ++this.retargetCounter;
                if (this.retargetCounter > 700 || this.target == null || !this.isValid(player, this.target)) {
                    this.target = candidate;
                    this.retargetCounter = 0;
                }
            } else {
                this.target = candidate;
            }
        }
        if (this.target == null || mc.screen != null) {
            this.resetRotationState();
        }
    }

    @Override
    public void onRender3D() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || this.target == null || mc.screen != null) {
            return;
        }
        for (int i = 0; i < 6; ++i) {
            this.updateVelocityBuffers();
            this.applyRotation(player);
        }
        this.applyMouseDelta(player);
    }
}

