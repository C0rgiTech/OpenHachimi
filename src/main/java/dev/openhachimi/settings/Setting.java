/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;

import java.util.function.Supplier;

public abstract class Setting<T> {
    public String name;
    protected T value;
    public final T defaultValue;
    private Supplier<Boolean> visible = new Supplier<Boolean>(){

        @Override
        public Boolean get() {
            return true;
        }
    };

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
    }

    public T get() {
        return this.value;
    }

    public void set(T val) {
        this.value = val;
    }

    public void reset() {
        this.value = this.defaultValue;
    }

    public boolean isVisible() {
        return this.visible.get();
    }

    public abstract void click(int var1, int var2, int var3, int var4);

    public void setVisible(Supplier<Boolean> condition) {
        this.visible = condition;
    }

    public abstract String getDisplay();

    public void setUnchecked(T val) {
        if (val == null) {
            return;
        }
        this.value = val;
    }
}

