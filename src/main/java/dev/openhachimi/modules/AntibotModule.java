/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.utils.ModuleManager;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AntibotModule
extends Module {
    private static final byte[] S_30BD86E5 = new byte[]{3, 44, 54, 43, 0, 45, 54};  // XOR0x42 "AntiBot"

    public AntibotModule() {
        super("AntiBot", Category.MISC);
    }

    public static boolean isBot(Entity entity) {
        if (!(entity instanceof Player)) {
            return false;
        }
        AntibotModule antiBot = ModuleManager.getModule(AntibotModule.class);
        if (antiBot != null && antiBot.enabled && Minecraft.getInstance().getConnection() != null) {
            return !Minecraft.getInstance().getConnection().getOnlinePlayerIds().contains(entity.getUUID());
        }
        return false;
    }
}

