/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

public class MSTimer {
    private long lastMs = 0L;

    public boolean delay(float nextDelay, boolean reset) {
        if ((float)(System.currentTimeMillis() - this.lastMs) >= nextDelay) {
            if (reset) {
                this.reset();
            }
            return true;
        }
        return false;
    }

    public boolean delay(long nextDelay) {
        return System.currentTimeMillis() - this.lastMs >= nextDelay;
    }

    public void reset() {
        this.lastMs = System.currentTimeMillis();
    }
}

