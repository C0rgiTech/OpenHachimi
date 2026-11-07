/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class RotationUtil2 {
    private static final Minecraft mc = Minecraft.getInstance();

    private RotationUtil2() {
    }

    public static float wrap(float degrees) {
        return Mth.wrapDegrees((float)degrees);
    }

    public static float rotateToYaw(float speed, float currentYaw, float targetYaw) {
        return RotationUtil2.updateRotation(currentYaw, targetYaw, speed);
    }

    public static float updateRotation(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees((float)(target - current));
        if (delta > maxDelta) {
            delta = maxDelta;
        }
        if (delta < -maxDelta) {
            delta = -maxDelta;
        }
        return current + delta;
    }

    public static float angleDiff(float a, float b) {
        return Mth.wrapDegrees((float)(b - a));
    }

    public static float[] toDelta(double dx, double dy, double dz) {
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Mth.atan2((double)dz, (double)dx) * 57.29577951308232) - 90.0f;
        float pitch = (float)(-(Mth.atan2((double)dy, (double)dist) * 57.29577951308232));
        return new float[]{Mth.wrapDegrees((float)yaw), pitch};
    }

    public static boolean inFov(Entity entity, float fov) {
        if (RotationUtil2.mc.player == null) {
            return false;
        }
        float[] rot = RotationUtil2.toEntity(entity);
        float diff = Math.abs(RotationUtil2.mc.player.getYRot() % 360.0f - rot[0]);
        float minDiff = Math.abs(Math.min(diff, 360.0f - diff));
        return minDiff <= fov;
    }

    public static double angleTo(Entity entity) {
        if (RotationUtil2.mc.player == null) {
            return 180.0;
        }
        float[] rot = RotationUtil2.toEntity(entity);
        float yawDiff = RotationUtil2.angleDiff(RotationUtil2.mc.player.getYRot(), rot[0]);
        float pitchDiff = RotationUtil2.angleDiff(RotationUtil2.mc.player.getXRot(), rot[1]);
        return Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
    }

    public static float[] fixGcd(float yaw, float pitch, float lastYaw, float lastPitch) {
        double sensitivity = (Double) RotationUtil2.mc.options.sensitivity().get();
        float f = (float)(sensitivity * 0.6 + 0.2);
        float gcd = f * f * f * 1.2f;
        if (gcd < 1.0E-4f) {
            return new float[]{yaw, pitch};
        }
        float deltaYaw = yaw - lastYaw;
        float deltaPitch = pitch - lastPitch;
        float fixedYaw = lastYaw + (deltaYaw - deltaYaw % gcd);
        float fixedPitch = lastPitch + (deltaPitch - deltaPitch % gcd);
        return new float[]{fixedYaw, fixedPitch};
    }

    public static float[] toTarget(Vec3 eye, Vec3 target) {
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Mth.atan2((double)dz, (double)dx) * 57.29577951308232) - 90.0f;
        float pitch = (float)(-(Mth.atan2((double)dy, (double)dist) * 57.29577951308232));
        return new float[]{Mth.wrapDegrees((float)yaw), pitch};
    }

    public static float[] toEntity(Entity entity) {
        if (RotationUtil2.mc.player == null || entity == null) {
            return new float[]{0.0f, 0.0f};
        }
        Vec3 eye = RotationUtil2.mc.player.getEyePosition(1.0f);
        Vec3 target = new Vec3(entity.getX(), entity.getY() + (double)entity.getEyeHeight(), entity.getZ());
        return RotationUtil2.toTarget(eye, target);
    }

    public static float[] toBlock(BlockPos pos, float partialTicks) {
        if (RotationUtil2.mc.player == null) {
            return new float[]{0.0f, 0.0f};
        }
        Vec3 move = RotationUtil2.mc.player.getDeltaMovement();
        double px = RotationUtil2.mc.player.getX() + move.x * (double)partialTicks;
        double py = RotationUtil2.mc.player.getY() + (double) RotationUtil2.mc.player.getEyeHeight() + move.y * (double)partialTicks;
        double pz = RotationUtil2.mc.player.getZ() + move.z * (double)partialTicks;
        double dx = (double)pos.getX() - px + 0.5;
        double dy = (double)pos.getY() - py + 0.5;
        double dz = (double)pos.getZ() - pz + 0.5;
        return RotationUtil2.toDelta(dx, dy, dz);
    }

    public static Vec3 look(float yaw, float pitch) {
        float yawRad = -yaw * ((float)Math.PI / 180) - (float)Math.PI;
        float pitchRad = -pitch * ((float)Math.PI / 180);
        float cosPitch = -Mth.cos((float)pitchRad);
        return new Vec3((double)(Mth.sin((float)yawRad) * cosPitch), (double)Mth.sin((float)pitchRad), (double)(Mth.cos((float)yawRad) * cosPitch));
    }

    public static Vec3 look() {
        if (RotationUtil2.mc.player == null) {
            return Vec3.ZERO;
        }
        return RotationUtil2.look(RotationUtil2.mc.player.getYRot(), RotationUtil2.mc.player.getXRot());
    }
}

