/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.rotation;

import dev.openhachimi.render.StencilBuffer;

import dev.openhachimi.utils.player.RaytraceUtil2;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    public static Rotation move(Rotation lastRotation, Rotation targetRotation, double speed) {
        if (targetRotation == null) {
            return lastRotation != null ? lastRotation : new Rotation(0.0f, 0.0f);
        }
        if (lastRotation == null) {
            return targetRotation;
        }
        if (speed != 0.0) {
            double deltaPitch;
            double deltaYaw = Mth.wrapDegrees((float)(targetRotation.getYaw() - lastRotation.getYaw()));
            double distance = Math.sqrt(deltaYaw * deltaYaw + (deltaPitch = (double)(targetRotation.getPitch() - lastRotation.getPitch())) * deltaPitch);
            if (distance < 1.0E-4) {
                return new Rotation(0.0f, 0.0f);
            }
            double distributionYaw = Math.abs(deltaYaw / distance);
            double distributionPitch = Math.abs(deltaPitch / distance);
            double maxYaw = speed * distributionYaw;
            double maxPitch = speed * distributionPitch;
            float moveYaw = (float)Math.max(Math.min(deltaYaw, maxYaw), -maxYaw);
            float movePitch = (float)Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);
            return new Rotation(moveYaw, movePitch);
        }
        return new Rotation(0.0f, 0.0f);
    }

    public static Rotation move(Rotation targetRotation, double speed) {
        return RotationUtil.move(StencilBuffer.getLastRotation(), targetRotation, speed);
    }

    public static Rotation calculate(BlockPos position, Direction enumFacing) {
        double x = (double)position.getX() + 0.5;
        double y = (double)position.getY() + 0.5;
        double z = (double)position.getZ() + 0.5;
        return RotationUtil.calculate(new Vec3(x += (double)enumFacing.getNormal().getX() * 0.5, y += (double)enumFacing.getNormal().getY() * 0.5, z += (double)enumFacing.getNormal().getZ() * 0.5));
    }

    public static Rotation calculate(Vec3 position, Direction enumFacing) {
        double x = position.x + 0.5;
        double y = position.y + 0.5;
        double z = position.z + 0.5;
        return RotationUtil.calculate(new Vec3(x += (double)enumFacing.getNormal().getX() * 0.5, y += (double)enumFacing.getNormal().getY() * 0.5, z += (double)enumFacing.getNormal().getZ() * 0.5));
    }

    public static Rotation calculate(Vec3 from, Vec3 to) {
        Vec3 diff = to.subtract(from);
        double distance = Math.hypot(diff.x, diff.z);
        float yaw = (float)(Mth.atan2((double)diff.z, (double)diff.x) * 57.2957763671875) - 90.0f;
        float pitch = (float)(-(Mth.atan2((double)diff.y, (double)distance) * 57.2957763671875));
        return new Rotation(yaw, pitch);
    }

    public static Rotation calculate(Entity entity) {
        return RotationUtil.calculate(entity.position().add(0.0, (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.85, 0.0));
    }

    public static Rotation calculate(Entity entity, boolean adaptive, double range) {
        Rotation normalRotations = RotationUtil.calculate(entity);
        HitResult result = RaytraceUtil2.rayCast(normalRotations, range, 0.0f);
        if (!adaptive || result != null && result.getType() == HitResult.Type.ENTITY) {
            return normalRotations;
        }
        AABB bb = entity.getBoundingBox();
        double minX = bb.minX;
        double maxX = bb.maxX;
        double minY = bb.minY;
        double maxY = bb.maxY;
        double minZ = bb.minZ;
        double maxZ = bb.maxZ;
        Vec3 basePos = entity.position();
        for (double yPercent = 1.0; yPercent >= 0.0; yPercent -= 0.25 + Math.random() * 0.1) {
            for (double xPercent = 1.0; xPercent >= -0.5; xPercent -= 0.5) {
                for (double zPercent = 1.0; zPercent >= -0.5; zPercent -= 0.5) {
                    double offsetX = (maxX - minX) * xPercent;
                    double offsetY = (maxY - minY) * yPercent;
                    double offsetZ = (maxZ - minZ) * zPercent;
                    Vec3 targetPoint = basePos.add(offsetX, offsetY, offsetZ);
                    Rotation adaptiveRotations = RotationUtil.calculate(targetPoint);
                    HitResult rayCastResult = RaytraceUtil2.rayCast(adaptiveRotations, range, 0.0f);
                    if (rayCastResult == null || rayCastResult.getType() != HitResult.Type.ENTITY) continue;
                    return adaptiveRotations;
                }
            }
        }
        return normalRotations;
    }

    public static Rotation calculate(BlockPos to) {
        return RotationUtil.calculate(RotationUtil.mc.player.getEyePosition(), new Vec3((double)to.getX(), (double)to.getY(), (double)to.getZ()).add(0.5, 0.5, 0.5));
    }

    public static Rotation calculate(Vec3 to) {
        return RotationUtil.calculate(RotationUtil.mc.player.getEyePosition(), to);
    }

    public static double getDistance(Player player, Entity entity) {
        Vec3 eyes = player.getEyePosition();
        AABB aabb = entity.getBoundingBox();
        double x = Mth.clamp((double)eyes.x, (double)aabb.minX, (double)aabb.maxX);
        double y = Mth.clamp((double)eyes.y, (double)aabb.minY, (double)aabb.maxY);
        double z = Mth.clamp((double)eyes.z, (double)aabb.minZ, (double)aabb.maxZ);
        return Math.sqrt(eyes.distanceToSqr(x, y, z));
    }

    public static double getDistance(Entity entity) {
        Vec3 eyes = RotationUtil.mc.player.getEyePosition();
        AABB aabb = entity.getBoundingBox();
        double x = Mth.clamp((double)eyes.x, (double)aabb.minX, (double)aabb.maxX);
        double y = Mth.clamp((double)eyes.y, (double)aabb.minY, (double)aabb.maxY);
        double z = Mth.clamp((double)eyes.z, (double)aabb.minZ, (double)aabb.maxZ);
        return Math.sqrt(eyes.distanceToSqr(x, y, z));
    }

    public static Rotation smooth(Rotation targetRotation, double speed) {
        return RotationUtil.smooth(StencilBuffer.getLastRotation(), targetRotation, speed);
    }

    public static Rotation smooth(Rotation lastRotation, Rotation targetRotation, double speed) {
        if (targetRotation == null) {
            return lastRotation != null ? lastRotation : new Rotation(0.0f, 0.0f);
        }
        if (lastRotation == null) {
            return targetRotation;
        }
        float yaw = targetRotation.getYaw();
        float pitch = targetRotation.getPitch();
        float lastYaw = lastRotation.getYaw();
        float lastPitch = lastRotation.getPitch();
        if (speed != 0.0) {
            Rotation move = RotationUtil.move(targetRotation, speed);
            yaw = lastYaw + move.getYaw();
            pitch = lastPitch + move.getPitch();
            if ((double)(Math.abs(move.getYaw()) + Math.abs(move.getPitch())) > 1.0E-4) {
                yaw = (float)((double)yaw + (Math.random() - 0.5) / 1000.0);
                pitch = (float)((double)pitch - Math.random() / 200.0);
            }
            Rotation rotations = new Rotation(yaw, pitch);
            Rotation fixedRotations = RotationUtil.applySensitivityPatch(rotations);
            yaw = fixedRotations.getYaw();
            pitch = Math.max(-90.0f, Math.min(90.0f, fixedRotations.getPitch()));
        }
        return new Rotation(yaw, pitch);
    }

    public static Rotation relateToPlayerRotation(Rotation rotation) {
        float prevYaw = RotationUtil.mc.player.yRotO;
        float prevPitch = RotationUtil.mc.player.xRotO;
        Rotation previousRotation = new Rotation(prevYaw, prevPitch);
        float yaw = previousRotation.getYaw() + Mth.wrapDegrees((float)(rotation.getYaw() - previousRotation.getYaw()));
        float pitch = Mth.clamp((float)rotation.getPitch(), (float)-90.0f, (float)90.0f);
        return new Rotation(yaw, pitch);
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle3 = Math.abs(angle1 - angle2) % 360.0f;
        if (angle3 > 180.0f) {
            angle3 = 0.0f;
        }
        return angle3;
    }

    public static Rotation applySensitivityPatch(Rotation rotation) {
        if (RotationUtil.mc.player == null) {
            return rotation;
        }
        Rotation lastRot = StencilBuffer.getLastRotation();
        float prevYaw = lastRot.getYaw();
        float prevPitch = lastRot.getPitch();
        Rotation previousRotation = new Rotation(prevYaw, prevPitch);
        float mouseSensitivity = (float)((Double) RotationUtil.mc.options.sensitivity().get() * (double)0.6f + (double)0.2f);
        double multiplier = (double)(mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0f) * 0.15;
        float yaw = previousRotation.getYaw() + (float)((double)Math.round((double)Mth.wrapDegrees((float)(rotation.getYaw() - previousRotation.getYaw())) / multiplier) * multiplier);
        float pitch = previousRotation.getPitch() + (float)((double)Math.round((double)(rotation.getPitch() - previousRotation.getPitch()) / multiplier) * multiplier);
        return new Rotation(yaw, Mth.clamp((float)pitch, (float)-90.0f, (float)90.0f));
    }

    public static Rotation applySensitivityPatch(Rotation rotation, Rotation previousRotation) {
        float mouseSensitivity = (float)((Double) RotationUtil.mc.options.sensitivity().get() * (double)0.6f + (double)0.2f);
        double multiplier = (double)(mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0f) * 0.15;
        float yaw = previousRotation.getYaw() + (float)((double)Math.round((double)Mth.wrapDegrees((float)(rotation.getYaw() - previousRotation.getYaw())) / multiplier) * multiplier);
        float pitch = previousRotation.getPitch() + (float)((double)Math.round((double)(rotation.getPitch() - previousRotation.getPitch()) / multiplier) * multiplier);
        return new Rotation(yaw, Mth.clamp((float)pitch, (float)-90.0f, (float)90.0f));
    }

    public static Rotation resetRotation(Rotation rotation) {
        if (rotation == null) {
            return null;
        }
        float yaw = rotation.getYaw() + Mth.wrapDegrees((float)(RotationUtil.mc.player.getYRot() - rotation.getYaw()));
        float pitch = RotationUtil.mc.player.getXRot();
        return new Rotation(yaw, pitch);
    }
}

