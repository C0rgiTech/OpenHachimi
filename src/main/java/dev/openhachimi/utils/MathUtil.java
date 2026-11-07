/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.util.concurrent.ThreadLocalRandom;

public class MathUtil {
    public static long getRandomInRange(long min, long max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1L);
    }

    public static int getRandomInRange(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double getRandomInRange(double min, double max) {
        return min >= max ? min : ThreadLocalRandom.current().nextDouble() * (max - min) + min;
    }

    public static float getRandomInRange(float min, float max) {
        return min >= max ? min : ThreadLocalRandom.current().nextFloat() * (max - min) + min;
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return MathUtil.interpolate(oldValue, newValue, (float)interpolationValue).floatValue();
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return MathUtil.interpolate(oldValue, newValue, (float)interpolationValue).intValue();
    }
}

