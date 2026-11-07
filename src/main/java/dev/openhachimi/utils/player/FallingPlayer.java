/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class FallingPlayer {
    private static final Minecraft mc = Minecraft.getInstance();
    public double x;
    public double y;
    public double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private final float yaw;
    private final float strafe;
    private final float forward;
    private float jumpMovementFactor;

    public FallingPlayer(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float strafe, float forward) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.strafe = strafe;
        this.forward = forward;
    }

    public FallingPlayer(Player player) {
        this(player.getX(), player.getY(), player.getZ(), player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z, player.getYRot(), player.xxa, player.zza);
        float jumpingVelocity;
        float f = player.level().getBlockState(player.blockPosition()).getBlock().getJumpFactor();
        float f1 = player.level().getBlockState(player.getOnPos()).getBlock().getJumpFactor();
        this.jumpMovementFactor = jumpingVelocity = 0.42f * ((double)f == 1.0 ? f1 : f) + player.getJumpBoostPower();
    }

    private void step(boolean damping) {
        float fw;
        float sr = damping ? this.strafe * 0.98f : this.strafe;
        float v = sr * sr + (fw = damping ? this.forward * 0.98f : this.forward) * fw;
        if (v >= 1.0E-4f) {
            if ((v = Mth.sqrt((float)v)) < 1.0f) {
                v = 1.0f;
            }
            float factor = this.jumpMovementFactor;
            if (FallingPlayer.mc.player != null && FallingPlayer.mc.player.isSprinting()) {
                factor *= 1.3f;
            }
            v = factor / v;
            float sin = Mth.sin((float)(this.yaw * (float)Math.PI / 180.0f));
            float cos = Mth.cos((float)(this.yaw * (float)Math.PI / 180.0f));
            this.motionX += (double)((sr *= v) * cos - (fw *= v) * sin);
            this.motionZ += (double)(fw * cos + sr * sin);
        }
        this.motionY -= 0.08;
        this.motionY *= (double)0.98f;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
        if (damping) {
            this.motionX *= 0.91;
            this.motionZ *= 0.91;
        }
    }

    public void calculate(int ticks) {
        for (int i = 0; i < ticks; ++i) {
            this.step(true);
        }
    }

    public void calculateNoDrag(int ticks) {
        for (int i = 0; i < ticks; ++i) {
            this.step(false);
        }
    }
}

