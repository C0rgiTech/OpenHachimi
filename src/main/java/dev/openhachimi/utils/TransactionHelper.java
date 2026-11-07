/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.awt.Color;
import net.minecraft.world.entity.Entity;

public interface TransactionHelper {
    public Color getColor(Entity var1);

    default public float getData2() {
        return 0.25f;
    }

    default public float getGravity() {
        return 0.03f;
    }

    default public float getData1() {
        return 0.125f;
    }

    public boolean isTargetEntity(Entity var1);
}

