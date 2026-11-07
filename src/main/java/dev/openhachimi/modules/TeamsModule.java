/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ModeSetting;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public class TeamsModule
extends Module {
    private static final byte[] S_4CF1996 = new byte[]{22, 39, 35, 47, 49};  // XOR0x42 "Teams"
    private static final byte[] S_24ADC3 = new byte[]{15, 45, 38, 39};  // XOR0x42 "Mode"
    private static final byte[] S_5561D7B4 = new byte[]{17, 33, 45, 48, 39, 32, 45, 35, 48, 38};  // XOR0x42 "Scoreboard"
    private static final byte[] S_3E43F43 = new byte[]{1, 45, 46, 45, 48};  // XOR0x42 "Color"
    private static final byte[] S_24EEAB = new byte[]{12, 35, 47, 39};  // XOR0x42 "Name"
    private static final byte[] S_0 = new byte[0];
    public ModeSetting mode = new ModeSetting("Mode", new String[]{"Scoreboard", "Color", "Name"}, "Scoreboard");

    public TeamsModule() {
        super("Teams", Category.MISC);
        this.values.add(this.mode);
    }

    public static boolean isSameTeam(Entity entity) {
        TeamsModule teams = ModuleManager.getModule(TeamsModule.class);
        Minecraft mc = Minecraft.getInstance();
        if (teams == null || !teams.enabled || mc.player == null) {
            return false;
        }
        if (!(entity instanceof Player) || entity == mc.player) {
            return false;
        }
        if (teams.mode.isMode("Color")) {
            int c1 = TeamsModule.getColor(entity);
            int c2 = TeamsModule.getColor((Entity)mc.player);
            if (c1 == 0xFFFFFF && c2 == 0xFFFFFF) {
                return false;
            }
            return c1 == c2;
        }
        if (teams.mode.isMode("Name")) {
            String selfName = TeamsModule.stripFormatting(mc.player.getDisplayName().getString());
            String otherName = TeamsModule.stripFormatting(entity.getDisplayName().getString());
            if (selfName.length() < 2 || otherName.length() < 2) {
                return false;
            }
            return otherName.regionMatches(true, 0, selfName, 0, 2);
        }
        String otherTeam = TeamsModule.getTeam(entity);
        String selfTeam = TeamsModule.getTeam((Entity)mc.player);
        if (otherTeam == null || selfTeam == null) {
            return false;
        }
        return Objects.equals(otherTeam, selfTeam);
    }

    private static String stripFormatting(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                ++i;
                continue;
            }
            sb.append(c);
        }
        return sb.toString().trim();
    }

    public static String getTeam(Entity entity) {
        if (entity == null) {
            return null;
        }
        Team direct = entity.getTeam();
        if (direct != null) {
            return direct.getName();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            return null;
        }
        PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(entity.getUUID());
        if (playerInfo == null || playerInfo.getTeam() == null) {
            return null;
        }
        return playerInfo.getTeam().getName();
    }

    private static int getColor(Entity entity) {
        int teamColor = entity.getTeamColor();
        if (teamColor != 0xFFFFFF) {
            return teamColor;
        }
        Component display = entity.getDisplayName();
        if (display != null) {
            TextColor styleColor = display.getStyle().getColor();
            if (styleColor != null) {
                return styleColor.getValue();
            }
            for (Component sibling : display.getSiblings()) {
                TextColor siblingColor = sibling.getStyle().getColor();
                if (siblingColor == null) continue;
                return siblingColor.getValue();
            }
        }
        return 0xFFFFFF;
    }
}

