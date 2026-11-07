/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraftforge.eventbus.api.EventPriority;

@Retention(value=RetentionPolicy.SOURCE)
@Target(value={ElementType.METHOD})
public @interface InterpolationHelper {
    public EventPriority priority() default EventPriority.NORMAL;

    public boolean receiveCancelled() default false;
}

