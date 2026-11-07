/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;

public class NojumpdelayModule
extends Module {
    private static final byte[] S_684BE514 = new byte[]{12, 45, 8, 55, 47, 50, 6, 39, 46, 35, 59};  // XOR0x42 "NoJumpDelay"
    private static final byte[] S_5D394BFE = new byte[]{36, 29, 112, 114, 123, 119, 118, 29};  // XOR0x42 "f_20954_"
    private static final byte[] S_4E23D34 = new byte[]{44, 45, 8, 55, 47, 50, 6, 39, 46, 35, 59};  // XOR0x42 "noJumpDelay"
    private Field jumpDelayField;

    public NojumpdelayModule() {
        super("NoJumpDelay", Category.MOVEMENT);
        try {
            this.jumpDelayField = LivingEntity.class.getDeclaredField("f_20954_");
            this.jumpDelayField.setAccessible(true);
        }
        catch (Exception e) {
            try {
                this.jumpDelayField = LivingEntity.class.getDeclaredField("noJumpDelay");
                this.jumpDelayField.setAccessible(true);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    public void onTick() {
        if (Minecraft.getInstance().player != null && this.jumpDelayField != null) {
            try {
                this.jumpDelayField.setInt(Minecraft.getInstance().player, 0);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

