/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;


public class BooleanSetting
extends Setting<Boolean> {
    private static final byte[] S_9DF = new byte[]{13, 12};  // XOR0x42 "ON"
    private static final byte[] S_1314F = new byte[]{13, 4, 4};  // XOR0x42 "OFF"
    public float animOffset = 0.0f;
    public float animAlpha = 0.0f;

    public BooleanSetting(String name, boolean val) {
        super(name, val);
    }

    @Override
    public void click(int mX, int mY, int w, int x) {
        this.value = (Boolean)this.value == false;
    }

    @Override
    public String getDisplay() {
        return (Boolean)this.value != false ? "ON" : "OFF";
    }
}

