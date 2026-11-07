/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;


public class ModeSetting
extends Setting<String> {
    private static final byte[] S_574 = new byte[]{110, 98};  // XOR0x42 ", "
    private String[] modes;
    private int index;
    public float animHover;

    public ModeSetting(String name, String[] modes, String defaultMode) {
        super(name, defaultMode);
        this.modes = modes;
        for (int i = 0; i < modes.length; ++i) {
            if (!modes[i].equals(defaultMode)) continue;
            this.index = i;
        }
    }

    public void next() {
        this.index = (this.index + 1) % this.modes.length;
        this.value = this.modes[this.index];
    }

    @Override
    public void set(String val) {
        for (int i = 0; i < this.modes.length; ++i) {
            if (!this.modes[i].equals(val)) continue;
            this.index = i;
            this.value = this.modes[i];
            return;
        }
    }

    public boolean isMode(String m) {
        return ((String)this.get()).equals(m);
    }

    public String[] getModes() {
        return (String[])this.modes.clone();
    }

    @Override
    public void click(int mouseX, int mouseY, int x, int width) {
        this.next();
    }

    public boolean setIgnoreCase(String val) {
        if (val == null) {
            return false;
        }
        String wanted = val.trim();
        for (int i = 0; i < this.modes.length; ++i) {
            if (!this.modes[i].equalsIgnoreCase(wanted)) continue;
            this.index = i;
            this.value = this.modes[i];
            return true;
        }
        return false;
    }

    public String getModeList() {
        StringBuilder sb = new StringBuilder(48);
        for (int i = 0; i < this.modes.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.modes[i]);
        }
        return sb.toString();
    }

    @Override
    public String getDisplay() {
        return (String)this.value;
    }
}

