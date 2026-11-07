/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;


public class ListSetting
extends Setting<Integer> {
    public int min;
    public int max;

    public void setByMouse(int mouseX, int x, int width) {
        float percent = (float)(mouseX - x) / (float)width;
        percent = Math.max(0.0f, Math.min(1.0f, percent));
        this.value = (int)((float)this.min + (float)(this.max - this.min) * percent);
    }

    public ListSetting(String name, int val, int min, int max) {
        super(name, val);
        this.min = min;
        this.max = max;
    }

    @Override
    public void set(Integer val) {
        if (val == null) {
            return;
        }
        super.set(Math.max(this.min, Math.min(this.max, val)));
    }

    public float getPercent() {
        float percent = (float)((Integer)this.value - this.min) / (float)(this.max - this.min);
        return Math.max(0.0f, Math.min(1.0f, percent));
    }

    public void setPercent(float p) {
        p = Math.max(0.0f, Math.min(1.0f, p));
        this.value = this.min + Math.round((float)(this.max - this.min) * p);
    }

    @Override
    public void click(int mouseX, int mouseY, int x, int width) {
        float percent = (float)(mouseX - x) / (float)width;
        this.setPercent(percent);
    }

    @Override
    public String getDisplay() {
        return String.valueOf(this.value);
    }
}

