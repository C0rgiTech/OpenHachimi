/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;


public class NumberSetting
extends Setting<Float> {
    private static final byte[] S_1184DD = new byte[]{103, 108, 112, 36};  // XOR0x42 "%.2f"
    public float min;
    public float max;

    public void setByMouse(int mouseX, int x, int width) {
        float percent = (float)(mouseX - x) / (float)width;
        percent = Math.max(0.0f, Math.min(1.0f, percent));
        this.value = Float.valueOf(this.min + (this.max - this.min) * percent);
    }

    public NumberSetting(String name, float val, float min, float max) {
        super(name, Float.valueOf(val));
        this.min = min;
        this.max = max;
    }

    @Override
    public void set(Float val) {
        if (val == null) {
            return;
        }
        super.set(Float.valueOf(Math.max(this.min, Math.min(this.max, val.floatValue()))));
    }

    public float getPercent() {
        float percent = (((Float)this.value).floatValue() - this.min) / (this.max - this.min);
        return Math.max(0.0f, Math.min(1.0f, percent));
    }

    public void setPercent(float p) {
        p = Math.max(0.0f, Math.min(1.0f, p));
        this.value = Float.valueOf(this.min + (this.max - this.min) * p);
    }

    @Override
    public void click(int mouseX, int mouseY, int x, int width) {
        float percent = (float)(mouseX - x) / (float)width;
        this.setPercent(percent);
    }

    @Override
    public String getDisplay() {
        return String.format("%.2f", this.value);
    }
}

