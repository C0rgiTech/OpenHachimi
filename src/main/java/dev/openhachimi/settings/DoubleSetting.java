/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.settings;


public class DoubleSetting
extends Setting<String> {
    private static final byte[] S_0 = new byte[0];
    private Runnable action;
    public final String label;
    public float animHover = 0.0f;

    public DoubleSetting(String name, Runnable action) {
        this(name, name, action);
    }

    public DoubleSetting(String name, String label, Runnable action) {
        super(name, "");
        this.label = label == null ? name : label;
        this.action = action;
    }

    public void run() {
        if (this.action == null) {
            return;
        }
        this.action.run();
    }

    @Override
    public void set(String val) {
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    @Override
    public void click(int mouseX, int mouseY, int x, int width) {
        this.run();
    }

    @Override
    public String getDisplay() {
        return this.label;
    }
}

