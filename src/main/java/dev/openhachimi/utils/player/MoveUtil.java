/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

import dev.openhachimi.utils.rotation.RotationManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MoveUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    private static float direction(float forward, float strafe) {
        float direction = RotationManager.getCameraYaw();
        boolean isMovingForward = forward > 0.0f;
        boolean isMovingBack = forward < 0.0f;
        boolean isMovingRight = strafe > 0.0f;
        boolean isMovingLeft = strafe < 0.0f;
        boolean isMovingSideways = isMovingRight || isMovingLeft;
        boolean isMovingStraight = isMovingForward || isMovingBack;
        boolean bl = isMovingStraight;
        if (forward != 0.0f || strafe != 0.0f) {
            if (isMovingBack && !isMovingSideways) {
                return direction + 180.0f;
            }
            if (isMovingForward && isMovingLeft) {
                return direction + 45.0f;
            }
            if (isMovingForward && isMovingRight) {
                return direction - 45.0f;
            }
            if (!isMovingStraight && isMovingLeft) {
                return direction + 90.0f;
            }
            if (!isMovingStraight && isMovingRight) {
                return direction - 90.0f;
            }
            if (isMovingBack && isMovingLeft) {
                return direction + 135.0f;
            }
            if (isMovingBack) {
                return direction - 135.0f;
            }
        }
        return direction;
    }

    public static boolean isMoving() {
        if (MoveUtil.mc.player == null) {
            return false;
        }
        return MoveUtil.mc.player.input.forwardImpulse != 0.0f || MoveUtil.mc.player.input.leftImpulse != 0.0f;
    }

    public static void fixMovement(Input input, float yaw) {
        float forward = input.forwardImpulse;
        float strafe = input.leftImpulse;
        int angleUnit = 45;
        float angleTolerance = 22.5f;
        float directionFactor = Math.max(Math.abs(forward), Math.abs(strafe));
        double angleDifference = Mth.wrapDegrees((float)(MoveUtil.direction(forward, strafe) - yaw));
        double angleDistance = Math.abs(angleDifference);
        forward = 0.0f;
        strafe = 0.0f;
        if (angleDistance <= (double)((float)angleUnit + angleTolerance)) {
            forward += 1.0f;
        } else if (angleDistance >= (double)(180.0f - (float)angleUnit - angleTolerance)) {
            forward -= 1.0f;
        }
        if (angleDifference >= (double)((float)angleUnit - angleTolerance) && angleDifference <= (double)(180.0f - (float)angleUnit + angleTolerance)) {
            strafe -= 1.0f;
        } else if (angleDifference <= (double)((float)(-angleUnit) + angleTolerance) && angleDifference >= (double)(-180.0f + (float)angleUnit - angleTolerance)) {
            strafe += 1.0f;
        }
        input.forwardImpulse = forward *= directionFactor;
        input.leftImpulse = strafe *= directionFactor;
    }

    public static double getDirection() {
        if (MoveUtil.mc.player == null) {
            return 0.0;
        }
        float yaw = MoveUtil.mc.player.getYRot();
        float forward = MoveUtil.mc.player.input.forwardImpulse;
        float strafe = MoveUtil.mc.player.input.leftImpulse;
        if (forward < 0.0f) {
            yaw += 180.0f;
        }
        float strafeAngle = 0.0f;
        if (strafe < 0.0f) {
            strafeAngle = -90.0f;
        } else if (strafe > 0.0f) {
            strafeAngle = 90.0f;
        }
        if (forward != 0.0f) {
            yaw = strafe >= 0.0f ? (yaw = yaw + strafeAngle * (forward > 0.0f ? 0.5f : -0.5f)) : (yaw = yaw - strafeAngle * (forward > 0.0f ? 0.5f : -0.5f));
        }
        return Math.toRadians(yaw);
    }

    public static double getSpeed() {
        if (MoveUtil.mc.player == null) {
            return 0.0;
        }
        return Math.sqrt(MoveUtil.mc.player.getDeltaMovement().x * MoveUtil.mc.player.getDeltaMovement().x + MoveUtil.mc.player.getDeltaMovement().z * MoveUtil.mc.player.getDeltaMovement().z);
    }

    public static void setSpeed(double speed) {
        if (MoveUtil.mc.player == null) {
            return;
        }
        Vec3 motion = MoveUtil.mc.player.getDeltaMovement();
        float yaw = MoveUtil.mc.player.getYRot();
        double forward = MoveUtil.mc.player.input.forwardImpulse;
        double strafe = MoveUtil.mc.player.input.leftImpulse;
        if (forward != 0.0 || strafe != 0.0) {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            double cos = Math.cos(Math.toRadians(yaw + 90.0f));
            double sin = Math.sin(Math.toRadians(yaw + 90.0f));
            MoveUtil.mc.player.setDeltaMovement(forward * speed * cos + strafe * speed * sin, motion.y, forward * speed * sin - strafe * speed * cos);
        } else {
            MoveUtil.mc.player.setDeltaMovement(0.0, motion.y, 0.0);
        }
    }
}

