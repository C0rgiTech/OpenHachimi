/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import dev.openhachimi.settings.ListSetting;

import java.util.Locale;

public class ColorValue
extends ListSetting {
    private static final byte[] S_1FEC370 = new byte[]{97, 103, 114, 116, 26};  // XOR0x42 "#%06X"

    public ColorValue(String name, int rgb) {
        super(name, rgb & 0xFFFFFF, 0, 0xFFFFFF);
    }

    @Override
    public void set(Integer val) {
        super.set(val == null ? Integer.valueOf(0) : val);
    }

    public int argb(float alpha) {
        int a = (int)alpha;
        if (a < 0) {
            a = 0;
        }
        if (a > 255) {
            a = 255;
        }
        return a << 24 | this.rgb();
    }

    @Override
    public String getDisplay() {
        return String.format(Locale.ROOT, "#%06X", (Integer)this.value & 0xFFFFFF);
    }

    public int rgb() {
        return (Integer)this.value & 0xFFFFFF;
    }
}

