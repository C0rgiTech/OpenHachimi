/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.event.EventRegistrar;


public class KeepsprintModule
extends Module {
    private static final byte[] S_45167E1F = new byte[]{9, 39, 39, 50, 17, 50, 48, 43, 44, 54};  // XOR0x42 "KeepSprint"

    public KeepsprintModule() {
        super("KeepSprint", Category.MOVEMENT);
    }

    @Override
    public void onAttackSlowdown(EventRegistrar.AttackSlowdownEvent event) {
        event.setCanceled(true);
    }
}

