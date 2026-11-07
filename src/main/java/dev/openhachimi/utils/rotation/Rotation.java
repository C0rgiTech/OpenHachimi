/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.rotation;

import net.minecraft.world.phys.Vec2;

public class Rotation {
    private float yaw;
    private float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(Vec2 vector2f) {
        this.yaw = vector2f.x;
        this.pitch = vector2f.y;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}

