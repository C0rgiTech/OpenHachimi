/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.awt.Color;
import java.util.Collections;
import java.util.HashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Arrow;

public class ExecutorHelper
extends TargetHelper {
    public ExecutorHelper() {
        super(new HashSet<Class<? extends Entity>>(Collections.singletonList(Arrow.class)), new Color(255, 0, 0));
    }

    @Override
    public float getData2() {
        return 0.5f;
    }

    @Override
    public float getGravity() {
        return 0.05f;
    }

    @Override
    public float getData1() {
        return 0.25f;
    }
}

