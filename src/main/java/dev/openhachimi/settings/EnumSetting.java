/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;

import com.mojang.blaze3d.platform.InputConstants;

public class EnumSetting
extends Setting<Integer> {
    private static final byte[] S_252358 = new byte[]{12, 45, 44, 39};  // XOR0x42 "None"
    public float animHover;

    public EnumSetting(String name, int key) {
        super(name, key);
    }

    public static String name(int key) {
        if (key == 0) {
            return "None";
        }
        try {
            return InputConstants.getKey((int)key, (int)0).getDisplayName().getString();
        }
        catch (Throwable throwable) {
            return "None";
        }
    }

    @Override
    public void set(Integer val) {
        this.value = val;
    }

    @Override
    public void click(int mouseX, int mouseY, int x, int width) {
    }

    @Override
    public String getDisplay() {
        return EnumSetting.name((Integer)this.value);
    }

    @Override
    public void setUnchecked(Integer val) {
        if (val == null) {
            return;
        }
        this.value = val;
    }
}

