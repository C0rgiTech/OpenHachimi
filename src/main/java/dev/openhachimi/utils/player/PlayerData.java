/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class PlayerData {
    private static final Minecraft mc = Minecraft.getInstance();
    public double x;
    public double y;
    public double z;
    public double motionX;
    public double motionY;
    public double motionZ;
    public float yaw;

    public Vec3 getPosition() {
        return new Vec3(this.x, this.y, this.z);
    }

    public PlayerData(Player player) {
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.motionX = player.getDeltaMovement().x;
        this.motionY = player.getDeltaMovement().y;
        this.motionZ = player.getDeltaMovement().z;
        this.yaw = player.getYRot();
    }

    public PlayerData(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
    }

    public void update() {
        this.motionY -= 0.08;
        this.motionY *= 0.98;
        this.motionX *= 0.91;
        this.motionZ *= 0.91;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
    }

    public void calculate(int ticks) {
        for (int i = 0; i < ticks; ++i) {
            this.update();
        }
    }

    public double getDistanceTo(double x, double y, double z) {
        double dx = this.x - x;
        double dy = this.y - y;
        double dz = this.z - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public boolean isCollidedHorizontally() {
        if (PlayerData.mc.level == null) {
            return false;
        }
        return PlayerData.mc.level.getBlockState(BlockPos.containing((double)this.x, (double)this.y, (double)this.z)).blocksMotion();
    }

    public boolean isCollidedVertically() {
        if (PlayerData.mc.level == null) {
            return false;
        }
        return PlayerData.mc.level.getBlockState(BlockPos.containing((double)this.x, (double)(this.y - 0.1), (double)this.z)).blocksMotion();
    }
}

