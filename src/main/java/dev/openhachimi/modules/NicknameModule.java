/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ColorSetting;
import dev.openhachimi.settings.DoubleSetting;
import dev.openhachimi.settings.IntSetting;
import dev.openhachimi.settings.Setting;

import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public class NicknameModule
extends Module {
    private static final byte[] S_8110C8E = new byte[]{12, 43, 33, 41, 44, 35, 47, 39};  // XOR0x42 "Nickname"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_7A7FC274 = new byte[]{3, 38, 38, 98, 17, 39, 37, 47, 39, 44, 54};  // XOR0x42 "Add Segment"
    private static final byte[] S_B685FF = new byte[]{105, 98, 3, 38, 38, 98, 17, 39, 37, 47, 39, 44, 54};  // XOR0x42 "+ Add Segment"
    private static final byte[] S_612059A5 = new byte[]{16, 39, 47, 45, 52, 39, 98, 14, 35, 49, 54, 98, 17, 39, 37, 47, 39, 44, 54};  // XOR0x42 "Remove Last Segment"
    private static final byte[] S_319D293F = new byte[]{111, 98, 16, 39, 47, 45, 52, 39, 98, 14, 35, 49, 54};  // XOR0x42 "- Remove Last"
    private static final byte[] S_4CF7073 = new byte[]{22, 39, 58, 54, 98};  // XOR0x42 "Text "
    private static final byte[] S_78A3A93D = new byte[]{1, 45, 46, 45, 48, 98};  // XOR0x42 "Color "
    private static final byte[] S_4FADC09 = new byte[]{21, 42, 43, 54, 39};  // XOR0x42 "White"
    public static NicknameModule instance;
    private static final Map<Class<?>, List<Field>> TEXT_FIELDS;
    private static final int MAX_SEGMENTS = 64;
    private final List<IntSetting> texts = new ArrayList<IntSetting>();
    private final List<ColorSetting> colors = new ArrayList<ColorSetting>();
    private final DoubleSetting addSegment;
    private final DoubleSetting removeSegment;
    private String tabSignature = "";
    private String cachedOrigin = "";
    private List<String> cachedAliases = new ArrayList<String>();
    private String lastItemKey = "";
    private float itemTimer = 0.0f;

    private void dropSegment() {
        if (this.texts.size() <= 1) {
            return;
        }
        this.texts.remove(this.texts.size() - 1);
        this.colors.remove(this.colors.size() - 1);
        this.rebuild();
    }

    public static String legacy() {
        NicknameModule module = instance;
        if (module == null || !module.isActive()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < module.texts.size(); ++i) {
            String text = (String)module.texts.get(i).get();
            if (text == null || text.isEmpty()) continue;
            builder.append('§').append(module.codeAt(i)).append(text);
        }
        return builder.toString();
    }

    private String signature() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.texts.size(); ++i) {
            String text = (String)this.texts.get(i).get();
            builder.append(text == null ? "" : text).append(':').append(this.rgbAt(i)).append(';');
        }
        return builder.toString();
    }

    public NicknameModule() {
        super("Nickname", Category.RENDER);
        instance = this;
        this.addSegment = new DoubleSetting("Add Segment", "+ Add Segment", new Runnable(){

            @Override
            public void run() {
                NicknameModule.this.appendSegment();
            }
        });
        this.removeSegment = new DoubleSetting("Remove Last Segment", "- Remove Last", new Runnable(){

            @Override
            public void run() {
                NicknameModule.this.dropSegment();
            }
        });
        this.appendSegment();
    }

    static {
        TEXT_FIELDS = new ConcurrentHashMap();
    }

    public static MutableComponent component() {
        MutableComponent result = Component.empty();
        NicknameModule module = instance;
        if (module == null) {
            return result;
        }
        for (int i = 0; i < module.texts.size(); ++i) {
            String text = (String)module.texts.get(i).get();
            if (text == null || text.isEmpty()) continue;
            result.append((Component)Component.literal((String)text).setStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)module.rgbAt(i)))));
        }
        return result;
    }

    public boolean isActive() {
        return this.enabled && !this.buildPlain().isEmpty();
    }

    public static boolean active() {
        NicknameModule module = instance;
        return module != null && module.isActive();
    }

    private void rebuild() {
        while (this.colors.size() < this.texts.size()) {
            this.colors.add(new ColorSetting("Color " + (this.colors.size() + 1), "White"));
        }
        while (this.texts.size() < this.colors.size()) {
            this.texts.add(new IntSetting("Text " + (this.texts.size() + 1), "", 32));
        }
        this.values.clear();
        for (int i = 0; i < this.texts.size(); ++i) {
            this.values.add((Setting)this.texts.get(i));
            this.values.add((Setting)this.colors.get(i));
        }
        this.values.add(this.addSegment);
        this.values.add(this.removeSegment);
        this.values.add(this.invisible);
    }

    public static String plain() {
        NicknameModule module = instance;
        return module == null ? "" : module.buildPlain();
    }

    public static Component rewrite(Component source) {
        NicknameModule module = instance;
        if (source == null || module == null || !module.isActive()) {
            return source;
        }
        List<String> aliases = module.aliasList();
        if (aliases.isEmpty() || !NicknameModule.containsAlias(source.getString())) {
            return source;
        }
        MutableComponent result = Component.empty();
        boolean changed = false;
        for (Component run : source.toFlatList()) {
            Style style = run.getStyle();
            String text = run.getString();
            int cursor = 0;
            while (cursor < text.length()) {
                int bestAt = -1;
                String bestAlias = null;
                for (String alias : aliases) {
                    int at = text.indexOf(alias, cursor);
                    if (at < 0 || bestAt >= 0 && at >= bestAt) continue;
                    bestAt = at;
                    bestAlias = alias;
                }
                if (bestAt < 0) break;
                if (bestAt > cursor) {
                    result.append((Component)Component.literal((String)text.substring(cursor, bestAt)).setStyle(style));
                }
                result.append((Component)NicknameModule.styledSegments(module, style));
                changed = true;
                cursor = bestAt + bestAlias.length();
            }
            if (cursor >= text.length()) continue;
            result.append((Component)Component.literal((String)text.substring(cursor)).setStyle(style));
        }
        return changed ? result : source;
    }

    public static boolean containsAlias(String text) {
        NicknameModule module = instance;
        if (module == null || text == null || text.isEmpty() || !module.isActive()) {
            return false;
        }
        for (String alias : module.aliasList()) {
            if (!text.contains(alias)) continue;
            return true;
        }
        return false;
    }

    private void appendSegment() {
        if (this.texts.size() >= 64) {
            return;
        }
        int index = this.texts.size() + 1;
        this.texts.add(new IntSetting("Text " + index, "", 32));
        this.colors.add(new ColorSetting("Color " + index, "White"));
        this.rebuild();
    }

    @Override
    public void onDisable() {
        this.tabSignature = "";
        this.clearTabList();
    }

    @Override
    public void onEnable() {
        this.refreshOrigin();
    }

    public static String segmentText(int index) {
        NicknameModule module = instance;
        if (module == null || index < 0 || index >= module.texts.size()) {
            return "";
        }
        String value = (String)module.texts.get(index).get();
        return value == null ? "" : value;
    }

    private static MutableComponent styledSegments(NicknameModule module, Style base) {
        MutableComponent result = Component.empty();
        for (int i = 0; i < module.texts.size(); ++i) {
            String text = (String)module.texts.get(i).get();
            if (text == null || text.isEmpty()) continue;
            result.append((Component)Component.literal((String)text).setStyle(base.withColor(TextColor.fromRgb((int)module.rgbAt(i)))));
        }
        return result;
    }

    private List<String> aliasList() {
        if (this.cachedAliases.isEmpty()) {
            this.refreshOrigin();
        }
        return this.cachedAliases;
    }

    public static String nameFor(Entity entity, String fallback) {
        NicknameModule module = instance;
        if (module == null || entity == null || !module.isActive()) {
            return fallback;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity != mc.player) {
            return fallback;
        }
        String legacy = NicknameModule.legacy();
        if (!legacy.isEmpty()) {
            return legacy;
        }
        String plain = module.buildPlain();
        return plain.isEmpty() ? fallback : plain;
    }

    private int rgbAt(int index) {
        if (index < 0 || index >= this.colors.size()) {
            return 0xFFFFFF;
        }
        return this.colors.get(index).rgb();
    }

    private void refreshOrigin() {
        String user;
        String display;
        String scoreboard;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            this.cachedOrigin = "";
            this.cachedAliases = new ArrayList<String>();
            return;
        }
        String name = mc.player.getGameProfile().getName();
        this.cachedOrigin = name == null ? "" : name;
        ArrayList<String> list = new ArrayList<String>();
        if (!this.cachedOrigin.isEmpty()) {
            list.add(this.cachedOrigin);
        }
        if ((scoreboard = mc.player.getScoreboardName()) != null && !scoreboard.isEmpty() && !list.contains(scoreboard)) {
            list.add(scoreboard);
        }
        String string = display = mc.player.getName() == null ? null : mc.player.getName().getString();
        if (display != null && !display.isEmpty() && !list.contains(display)) {
            list.add(display);
        }
        String string2 = user = mc.getUser() == null ? null : mc.getUser().getName();
        if (user != null && !user.isEmpty() && !list.contains(user)) {
            list.add(user);
        }
        this.cachedAliases = list;
    }

    public static String plainOr(String fallback) {
        NicknameModule module = instance;
        if (module == null || !module.isActive()) {
            return fallback;
        }
        String plain = module.buildPlain();
        return plain.isEmpty() ? fallback : plain;
    }

    private String buildPlain() {
        StringBuilder builder = new StringBuilder();
        for (IntSetting setting : this.texts) {
            String value = (String)setting.get();
            if (value == null) continue;
            builder.append(value);
        }
        return builder.toString();
    }

    public static boolean isAlias(String text) {
        NicknameModule module = instance;
        if (module == null || text == null || text.isEmpty() || !module.isActive()) {
            return false;
        }
        for (String alias : module.aliasList()) {
            if (!text.equals(alias)) continue;
            return true;
        }
        return false;
    }

    private char codeAt(int index) {
        if (index < 0 || index >= this.colors.size()) {
            return 'f';
        }
        return this.colors.get(index).codeChar();
    }

    private void trackHighlight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            this.itemTimer = 0.0f;
            this.lastItemKey = "";
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (stack == null || stack.isEmpty()) {
            this.itemTimer = 0.0f;
            this.lastItemKey = "";
            return;
        }
        String key = stack.getItem().toString() + '|' + stack.getCount() + '|' + stack.getHoverName().getString();
        if (!key.equals(this.lastItemKey)) {
            this.lastItemKey = key;
            this.itemTimer = 40.0f;
            return;
        }
        if (this.itemTimer > 0.0f) {
            this.itemTimer = Math.max(0.0f, this.itemTimer - 1.0f);
        }
    }

    public static int segmentRgb(int index) {
        NicknameModule module = instance;
        if (module == null || index < 0 || index >= module.colors.size()) {
            return 0xFFFFFF;
        }
        return module.rgbAt(index) & 0xFFFFFF;
    }

    private static void rewritePacket(Object packet) {
        for (Field field : NicknameModule.textFields(packet.getClass())) {
            try {
                Component original = (Component)field.get(packet);
                Component replaced = NicknameModule.rewrite(original);
                if (replaced == original) continue;
                field.set(packet, replaced);
            }
            catch (Throwable throwable) {}
        }
    }

    private static List<Field> textFields(Class<?> type) {
        List<Field> cached = TEXT_FIELDS.get(type);
        if (cached != null) {
            return cached;
        }
        ArrayList<Field> found = new ArrayList<Field>();
        for (Field field : type.getDeclaredFields()) {
            if (field.getType() != Component.class) continue;
            try {
                field.setAccessible(true);
            }
            catch (Throwable throwable) {
                continue;
            }
            found.add(field);
        }
        TEXT_FIELDS.put(type, found);
        return found;
    }

    private void clearTabList() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null) {
            return;
        }
        if (info.getTabListDisplayName() != null) {
            info.setTabListDisplayName(null);
        }
    }

    private void syncTabList() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null) {
            return;
        }
        if (!this.isActive()) {
            if (info.getTabListDisplayName() != null) {
                info.setTabListDisplayName(null);
            }
            return;
        }
        String signature = this.signature();
        PlayerTeam team = info.getTeam();
        if ((signature = signature + '|' + (team == null ? "" : team.getName())).equals(this.tabSignature) && info.getTabListDisplayName() != null) {
            return;
        }
        this.tabSignature = signature;
        info.setTabListDisplayName((Component)PlayerTeam.formatNameForTeam((Team)team, (Component)NicknameModule.component()));
    }

    public static int segmentCount() {
        NicknameModule module = instance;
        return module == null ? 0 : module.texts.size();
    }

    public static void rewriteTooltip(List<Component> tooltip) {
        if (tooltip == null || tooltip.isEmpty() || !NicknameModule.active()) {
            return;
        }
        for (int i = 0; i < tooltip.size(); ++i) {
            Component line = tooltip.get(i);
            Component replaced = NicknameModule.rewrite(line);
            if (replaced == line) continue;
            tooltip.set(i, replaced);
        }
    }

    public boolean renderItemName(GuiGraphics graphics) {
        int alpha;
        if (!this.isActive()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.font == null || mc.gameMode == null) {
            return false;
        }
        if (mc.options.hideGui) {
            return false;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (stack == null || stack.isEmpty() || this.itemTimer <= 0.0f) {
            return false;
        }
        Component hover = stack.getHoverName();
        if (!NicknameModule.containsAlias(hover.getString())) {
            return false;
        }
        MutableComponent line = Component.empty().append(hover).withStyle(stack.getRarity().getStyleModifier());
        if (stack.hasCustomHoverName()) {
            line.withStyle(ChatFormatting.ITALIC);
        }
        Component text = NicknameModule.rewrite((Component)line);
        int width = mc.font.width((FormattedText)text);
        int left = (graphics.guiWidth() - width) / 2;
        int top = graphics.guiHeight() - 59;
        if (!mc.gameMode.canHurtPlayer()) {
            top += 14;
        }
        if ((alpha = (int)(this.itemTimer * 256.0f / 10.0f)) > 255) {
            alpha = 255;
        }
        if (alpha <= 0) {
            return false;
        }
        graphics.fill(left - 2, top - 2, left + width + 2, top + 9 + 2, mc.options.getBackgroundColor(0));
        graphics.drawString(mc.font, text, left, top, 0xFFFFFF + (alpha << 24));
        return true;
    }

    @Override
    public void onTick() {
        this.refreshOrigin();
        this.syncTabList();
        this.trackHighlight();
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        if (!this.isActive()) {
            return false;
        }
        if (packet instanceof ClientboundSetTitleTextPacket || packet instanceof ClientboundSetSubtitleTextPacket || packet instanceof ClientboundSetActionBarTextPacket || packet instanceof ClientboundPlayerCombatKillPacket || packet instanceof ClientboundTabListPacket) {
            NicknameModule.rewritePacket(packet);
        }
        return false;
    }

    @Override
    public void onWorldChange() {
        this.tabSignature = "";
        this.refreshOrigin();
    }

    @Override
    public void prepareSettings(JsonObject values) {
        int count = 1;
        if (values != null) {
            for (int i = 1; i <= 64; ++i) {
                if (!values.has("Text " + i) && !values.has("Color " + i)) continue;
                count = i;
            }
        }
        while (this.texts.size() < count) {
            int index = this.texts.size() + 1;
            this.texts.add(new IntSetting("Text " + index, "", 32));
            this.colors.add(new ColorSetting("Color " + index, "White"));
        }
        this.rebuild();
    }
}

