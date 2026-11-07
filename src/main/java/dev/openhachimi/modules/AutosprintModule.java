/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import net.minecraft.client.Minecraft;

public class AutosprintModule
extends Module {
    private static final byte[] S_5FCA68C9 = new byte[]{3, 55, 54, 45, 17, 50, 48, 43, 44, 54};  // XOR0x42 "AutoSprint"
    Minecraft mc = Minecraft.getInstance();

    public AutosprintModule() {
        super("AutoSprint", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        this.mc.options.keySprint.setDown(false);
    }

    @Override
    public void onTick() {
        if (AutosoupModule.SPRINT_LOCK) {
            this.mc.options.keySprint.setDown(false);
            if (this.mc.player != null) {
                this.mc.player.setSprinting(false);
            }
            return;
        }
        if (this.mc.player != null && this.mc.player.input.hasForwardImpulse() && !this.mc.player.isSprinting()) {
            this.mc.options.keySprint.setDown(true);
            this.mc.options.toggleSprint().set(false);
        }
    }
}

