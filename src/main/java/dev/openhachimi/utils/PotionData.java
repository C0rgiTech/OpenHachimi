/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownPotion;

public class PotionData
extends TargetHelper {
    public PotionData() {
        super(new HashSet<Class<? extends Entity>>(Collections.singleton(ThrownPotion.class)), new Color(255, 66, 249));
    }

    @Override
    public float getGravity() {
        return 0.05f;
    }
}

