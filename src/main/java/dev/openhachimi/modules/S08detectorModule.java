/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.hud.Notifications;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;

public class S08detectorModule
extends Module {
    private static final byte[] S_4538EE81 = new byte[]{17, 114, 122, 6, 39, 54, 39, 33, 54, 45, 48};  // XOR0x42 "S08Detector"
    private static final byte[] S_306140A4 = new byte[]{6, 39, 54, 39, 33, 54, 39, 38, 98, 17, 114, 122, 99};  // XOR0x42 "Detected S08!"
    private long lastFlagTick = -1L;

    public S08detectorModule() {
        super("S08Detector", Category.MISC);
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerPositionPacket) {
            long currentTick;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.level != null && (currentTick = mc.level.getGameTime()) != this.lastFlagTick) {
                Notifications.show("Detected S08!", Notifications.NotificationType.NOTIFY, 1000);
                this.lastFlagTick = currentTick;
            }
        }
        return false;
    }
}

