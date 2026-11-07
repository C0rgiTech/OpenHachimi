/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class CriticalsModule
extends Module {
    private static final byte[] S_E859C94 = new byte[]{1, 48, 43, 54, 43, 33, 35, 46, 49};  // XOR0x42 "Criticals"
    private int state = 0;
    private int hitCount = 0;

    public CriticalsModule() {
        super("Criticals", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && this.state == 1) {
            mc.player.setSprinting(true);
        }
        this.state = 0;
        this.hitCount = 0;
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (this.state == 0) {
            if (KillauraModule.INSTANCE != null && KillauraModule.INSTANCE.enabled && KillauraModule.INSTANCE.attacks > 0 && !mc.player.onGround() && mc.player.fallDistance > 0.0f && mc.player.isSprinting()) {
                if (VelocityModule.INSTANCE != null && VelocityModule.INSTANCE.enabled && (VelocityModule.delay || VelocityModule.jump)) {
                    return;
                }
                mc.player.setSprinting(false);
                this.state = 1;
                this.hitCount = 0;
            }
        } else if (KillauraModule.INSTANCE == null || !KillauraModule.INSTANCE.enabled || KillauraModule.INSTANCE.attacks == 0 && KillauraModule.currentTarget == null) {
            if (VelocityModule.INSTANCE == null || !VelocityModule.INSTANCE.enabled || !VelocityModule.delay && !VelocityModule.jump) {
                mc.player.setSprinting(true);
            }
            this.state = 0;
        }
    }

    @Override
    public void onAttackEntity(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getEntity() != mc.player) {
            return;
        }
        if (this.state == 1) {
            ++this.hitCount;
            if (this.hitCount >= 3) {
                mc.player.setSprinting(true);
                this.state = 0;
            }
        }
    }
}

