/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;


public class DisablebossbarModule
extends Module {
    private static final byte[] S_263E217E = new byte[]{6, 43, 49, 35, 32, 46, 39, 0, 45, 49, 49, 0, 35, 48};  // XOR0x42 "DisableBossBar"
    public static DisablebossbarModule instance;

    public DisablebossbarModule() {
        super("DisableBossBar", Category.RENDER);
        instance = this;
    }

    public static boolean active() {
        DisablebossbarModule module = instance;
        return module != null && module.enabled;
    }
}

