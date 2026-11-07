/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;


public class IntSetting
extends Setting<String> {
    private static final byte[] S_0 = new byte[0];
    public final int maxLength;

    public IntSetting(String name, String value, int maxLength) {
        super(name, value == null ? "" : value);
        this.maxLength = maxLength <= 0 ? 32 : maxLength;
    }

    public boolean append(char codePoint) {
        String current;
        if (Character.isISOControl(codePoint)) {
            return false;
        }
        String string = current = this.value == null ? "" : (String)this.value;
        if (current.length() >= this.maxLength) {
            return false;
        }
        this.value = current + codePoint;
        return true;
    }

    @Override
    public void set(String val) {
        String trimmed;
        String string = trimmed = val == null ? "" : val;
        if (trimmed.length() > this.maxLength) {
            trimmed = trimmed.substring(0, this.maxLength);
        }
        this.value = trimmed;
    }

    @Override
    public void click(int mouseX, int mouseY, int x, int width) {
    }

    @Override
    public String getDisplay() {
        return this.value == null ? "" : (String)this.value;
    }
}

