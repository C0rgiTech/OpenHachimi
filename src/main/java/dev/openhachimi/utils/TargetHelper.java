/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.awt.Color;
import java.util.Set;
import net.minecraft.world.entity.Entity;

public class TargetHelper
implements ThrowableData {
    private final Color color;
    private final Set<Class<? extends Entity>> entityClass;

    public TargetHelper(Set<Class<? extends Entity>> classes) {
        this(classes, new Color(255, 255, 255));
    }

    public TargetHelper(Set<Class<? extends Entity>> classes, Color color) {
        this.entityClass = classes;
        this.color = color;
    }

    @Override
    public Color getColor(Entity entity) {
        return this.color;
    }

    @Override
    public boolean isTargetEntity(Entity entity) {
        for (Class<? extends Entity> clazz : this.entityClass) {
            if (!clazz.isInstance(entity)) continue;
            return true;
        }
        return false;
    }
}

