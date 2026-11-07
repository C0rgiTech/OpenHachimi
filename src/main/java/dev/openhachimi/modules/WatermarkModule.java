/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.hud.Notifications;
import dev.openhachimi.render.Render2D;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.ModuleManager;
import dev.openhachimi.utils.render.shader.Shader;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.EventDispatcherImpl;
import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.ClickGUI;
import dev.openhachimi.utils.render.font.FontManager;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.lwjgl.opengl.GL11;

public class WatermarkModule
extends Module {
    private static final byte[] S_6AB98844 = new byte[]{21, 35, 54, 39, 48, 47, 35, 48, 41};  // XOR0x42 "Watermark"
    private static final byte[] S_56CAF42B = new byte[]{13, 36, 36, 49, 39, 54, 98, 26};  // XOR0x42 "Offset X"
    private static final byte[] S_56CAF42C = new byte[]{13, 36, 36, 49, 39, 54, 98, 27};  // XOR0x42 "Offset Y"
    private static final byte[] S_27780B5 = new byte[]{17, 42, 45, 53, 98, 18, 43, 44, 37};  // XOR0x42 "Show Ping"
    private static final byte[] S_6F901546 = new byte[]{17, 42, 45, 53, 98, 4, 18, 17};  // XOR0x42 "Show FPS"
    private static final byte[] S_275421 = new byte[]{17, 43, 56, 39};  // XOR0x42 "Size"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_716BC539 = new byte[]{10, 35, 33, 42, 43, 47, 43, 98};  // XOR0x42 "Hachimi "
    private static final byte[] S_1CC4C929 = new byte[]{49, 43, 44, 37, 46, 39, 50, 46, 35, 59, 39, 48};  // XOR0x42 "singleplayer"
    private static final byte[] S_29DDC94 = new byte[]{108, 108, 108, 47, 49};  // XOR0x42 "...ms"
    private static final byte[] S_DA6 = new byte[]{47, 49};  // XOR0x42 "ms"
    private static final byte[] S_1018C9 = new byte[]{98, 36, 50, 49};  // XOR0x42 " fps"
    private static final byte[] S_104CE5 = new byte[]{98, 54, 45, 98};  // XOR0x42 " to "
    private static final byte[] S_A = new byte[]{72};
    private static final byte[] S_EFD1F45 = new byte[]{18, 46, 35, 59, 39, 48, 98, 14, 43, 49, 54, 98, 106};  // XOR0x42 "Player List ("
    private static final byte[] S_29 = new byte[]{107};
    private static final byte[] S_58E75241 = new byte[]{25, 12, 43, 33, 41, 31, 98};  // XOR0x42 "[Nick] "
    private static final byte[] S_110ACD = new byte[]{98, (byte)128, (byte)229, 32, 106};
    private static final byte[] S_5F292168 = new byte[]{25, 17, 54, 35, 36, 36, 31, 98};  // XOR0x42 "[Staff] "
    private static final byte[] S_B26E = new byte[]{108, 108, 108};  // XOR0x42 "..."
    private static final float TOP = 21.0f;
    private static final float HEIGHT = 23.0f;
    private static final float INSET = 9.0f;
    private static final float RIGHT_INSET = 12.5f;
    private static final float ICON_SIZE = 10.5f;
    private static final float ICON_GAP = 3.5f;
    private static final float FONT_SIZE = 8.0f;
    private static final float LIST_PAD = 8.0f;
    private static final float LIST_ROW = 12.0f;
    private static final float LIST_COLUMN = 118.0f;
    private static final float LIST_COLUMN_GAP = 8.0f;
    private static final int LIST_ROWS_PER_COLUMN = 20;
    private static final int BACKGROUND = -1744830464;
    private static final int FOREGROUND = -1777178;
    private static final int ACCENT = -4471809;
    private static final int MUTED = -6645856;
    private static final int ONLINE = -8337392;
    private static final int PING_LIME = -5848022;
    private static final int PING_ORANGE = -3637976;
    private static final int PING_RED = -3523026;
    private static final int MAX_NOTICES = 3;
    private static final int ELEMENT_IDLE = 0;
    private static final int ELEMENT_ALERT = 1;
    private static final int ELEMENT_LIST = 2;
    private static final int SYMBOL_CHROME = 0;
    private static final int SYMBOL_USER = 1;
    private static final int SYMBOL_GAUGE = 3;
    private static final int SYMBOL_TOGGLE = 6;
    private static final int SYMBOL_TOGGLE_OFF = 8;
    private static final int SYMBOL_INFO = 7;
    private static final Queue<Notice> PENDING = new ConcurrentLinkedQueue<Notice>();
    private static Component tabHeader;
    private static Component tabFooter;
    private final NumberSetting offsetX = new NumberSetting("Offset X", 0.0f, -4000.0f, 4000.0f);
    private final NumberSetting offsetY = new NumberSetting("Offset Y", 0.0f, -4000.0f, 4000.0f);
    private final BooleanSetting showPing = new BooleanSetting("Show Ping", true);
    private final BooleanSetting showFps = new BooleanSetting("Show FPS", true);
    private final NumberSetting size = new NumberSetting("Size", 1.0f, 0.5f, 2.0f);
    private final List<Notice> notices = new ArrayList<Notice>();
    private float islandWidth = 160.0f;
    private float islandHeight = 23.0f;
    private float settledWidth = 160.0f;
    private float contentFade = 1.0f;
    private int activeElement = -1;
    private float smoothedPing = -1.0f;
    private long lastFrameNanos = System.nanoTime();
    private boolean tabSuppressed;
    private PlayerTabOverlay originalTabList;
    private PlayerTabOverlay tabReplacement;

    private void signal(GuiGraphics graphics, float x, float centerY, float size, int ping, int fade) {
        int color = fade << 24 | WatermarkModule.pingColor(ping) & 0xFFFFFF;
        int bars = WatermarkModule.pingBars(ping);
        int dim = color & 0xFFFFFF | 0x33000000;
        graphics.pose().pushPose();
        graphics.pose().translate(x, centerY - size / 2.0f, 0.0f);
        graphics.pose().scale(size / 24.0f, size / 24.0f, 1.0f);
        for (int i = 0; i < 4; ++i) {
            float barHeight = 5.0f + (float)i * 3.0f;
            WatermarkModule.drawRounded(graphics, 2.5f + (float)i * 5.0f, 18.5f - barHeight, 3.5f, barHeight, 1.2f, i < bars ? color : dim);
        }
        graphics.pose().popPose();
    }

    public WatermarkModule() {
        super("Watermark", Category.RENDER);
        this.offsetX.setVisible(() -> false);
        this.offsetY.setVisible(() -> false);
        this.values.add(this.offsetX);
        this.values.add(this.offsetY);
        this.values.add(this.showPing);
        this.values.add(this.showFps);
        this.values.add(this.size);
        this.enabled = true;
    }

    private static void line(GuiGraphics graphics, float x1, float y1, float x2, float y2, float thickness, int color) {
        graphics.flush();
        Shader.capsule(graphics.pose().last().pose(), x1, y1, x2, y2, thickness, color);
    }

    private float separator(GuiGraphics graphics, Font font, float x, float centerY, float fontScale, float scale, int color) {
        return this.label(graphics, font, WatermarkModule.separatorText(), color, x + 1.25f * scale, centerY, fontScale) + 0.75f * scale;
    }

    private float label(GuiGraphics graphics, Font font, String text, int color, float x, float centerY, float fontScale) {
        if (text == null || text.isEmpty()) {
            return x;
        }
        float width = FontManager.measureExact(font, text, fontScale);
        float textY = centerY - (float)font.getHeight() * fontScale / 2.0f;
        if (!FontManager.drawExact(graphics, font, text, x, textY, color, false, fontScale)) {
            graphics.pose().pushPose();
            graphics.pose().translate(x, textY, 0.0f);
            graphics.pose().scale(fontScale, fontScale, 1.0f);
            font.drawString(graphics, text, 0.0, 0.0, color, false);
            graphics.flush();
            graphics.pose().popPose();
        }
        return x + width;
    }

    private void symbol(GuiGraphics graphics, int symbol, float x, float centerY, float size, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, centerY - size / 2.0f, 0.0f);
        graphics.pose().scale(size / 24.0f, size / 24.0f, 1.0f);
        switch (symbol) {
            case 0: {
                WatermarkModule.brandMark(graphics, color);
                break;
            }
            case 1: {
                WatermarkModule.drawRounded(graphics, 7.0f, 2.5f, 10.0f, 10.0f, 5.0f, color);
                WatermarkModule.drawRounded(graphics, 3.5f, 14.5f, 17.0f, 7.5f, 3.75f, color);
                break;
            }
            case 3: {
                WatermarkModule.arcRing(graphics, 12.0f, 13.0f, 7.0f, 10.0f, 150.0f, 390.0f, color);
                double angle = Math.toRadians(230.0);
                WatermarkModule.line(graphics, 12.0f, 13.0f, 12.0f + (float)Math.cos(angle) * 6.0f, 13.0f + (float)Math.sin(angle) * 6.0f, 2.0f, color);
                WatermarkModule.drawRounded(graphics, 10.5f, 11.5f, 3.0f, 3.0f, 1.5f, color);
                break;
            }
            case 6: {
                WatermarkModule.drawRounded(graphics, 2.0f, 7.0f, 20.0f, 10.0f, 5.0f, color);
                WatermarkModule.drawRounded(graphics, 15.5f, 9.0f, 6.0f, 6.0f, 3.0f, -14671840);
                break;
            }
            case 8: {
                WatermarkModule.drawRounded(graphics, 2.0f, 7.0f, 20.0f, 10.0f, 5.0f, color);
                WatermarkModule.drawRounded(graphics, 2.5f, 9.0f, 6.0f, 6.0f, 3.0f, -1777178);
                break;
            }
            default: {
                WatermarkModule.drawRounded(graphics, 10.0f, 2.0f, 4.0f, 4.0f, 2.0f, color);
                WatermarkModule.drawRounded(graphics, 10.0f, 8.0f, 4.0f, 14.0f, 2.0f, color);
            }
        }
        graphics.pose().popPose();
    }

    private float listWidth(Font font, float fontScale, float scale, List<PlayerInfo> players) {
        int columns = Math.max(1, (players.size() + 20 - 1) / 20);
        float columnWidth = 118.0f * scale;
        float width = 21.5f * scale + (float)columns * columnWidth + (float)(columns - 1) * 8.0f * scale;
        float headerWidth = 0.0f;
        if (tabHeader != null && !tabHeader.getString().isEmpty()) {
            for (String line : tabHeader.getString().split("\n")) {
                headerWidth = Math.max(headerWidth, (float)font.getStringWidth(line) * fontScale);
            }
        } else {
            headerWidth = (float)font.getStringWidth("Player List (" + players.size() + ")") * fontScale;
        }
        return Math.max(width, headerWidth + 21.5f * scale);
    }

    private float listHeight(Font font, float fontScale, float scale, List<PlayerInfo> players) {
        int rows = Math.min(players.size(), 20);
        float height = 8.0f * scale * 2.0f;
        height += (float)this.headerLines() * (float)font.getHeight() * fontScale * 1.2f;
        height += 5.0f * scale;
        height += (float)rows * 12.0f * scale;
        if (tabFooter != null && !tabFooter.getString().isEmpty()) {
            height += 5.0f * scale;
            height += (float)WatermarkModule.lineCount(tabFooter) * (float)font.getHeight() * fontScale * 1.2f;
        }
        return height;
    }

    public static boolean postNotification(String title, String message, Notifications.NotificationType type, int duration) {
        try {
            WatermarkModule module = ModuleManager.getModule(WatermarkModule.class);
            if (module == null || !module.enabled) {
                return false;
            }
            NotificationModule config = NotificationModule.active();
            int color = -1777178;
            int iconColor = -4471809;
            int icon = 7;
            if (type == Notifications.NotificationType.ENABLED) {
                color = -11141291;
                iconColor = -11141291;
                icon = 6;
            } else if (type == Notifications.NotificationType.DISABLED) {
                color = -43691;
                iconColor = -6645856;
                icon = 8;
            } else if (type == Notifications.NotificationType.WARNING) {
                color = -3637976;
                iconColor = -3637976;
            } else if (type == Notifications.NotificationType.ERROR) {
                color = -3523026;
                iconColor = -3523026;
            }
            int titleColor = -1777178;
            int textAlpha = 205;
            if (config != null) {
                if (((Boolean)config.rectangleCustom.get()).booleanValue()) {
                    iconColor = config.rectangleColor.rgb();
                }
                titleColor = config.titleColor.rgb();
                textAlpha = (Integer)config.textAlpha.get();
                if (duration <= 0) {
                    duration = config.durationFor(type);
                }
            }
            if (duration <= 0) {
                duration = 4000;
            }
            Notice notice = new Notice(title == null ? "" : title, message == null ? "" : message, icon, color, iconColor, duration);
            notice.titleColor = titleColor;
            notice.textAlpha = textAlpha;
            PENDING.add(notice);
            return true;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    public static boolean postNotification(String title, String message, Notifications.NotificationType type) {
        return WatermarkModule.postNotification(title, message, type, -1);
    }

    private static void drawRounded(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
        if (width < 0.5f || height < 0.5f) {
            return;
        }
        graphics.flush();
        Shader.box(graphics.pose().last().pose(), x, y, width, height, radius, color);
    }

    private void renderList(GuiGraphics graphics, Font font, Minecraft mc, List<PlayerInfo> players, float x, float y, float scale, float fontScale, float alpha) {
        int fade = (int)(255.0f * alpha);
        int fg = fade << 24 | 0xE4E1E6;
        int muted = fade << 24 | 0x9A97A0;
        int accent = fade << 24 | 0xBBC3FF;
        float lineHeight = (float)font.getHeight() * fontScale * 1.2f;
        float cursorY = y + 8.0f * scale;
        if (tabHeader != null && !tabHeader.getString().isEmpty()) {
            for (String line : tabHeader.getString().split("\n")) {
                float lineWidth = (float)font.getStringWidth(line) * fontScale;
                this.label(graphics, font, line, fg, x + (this.islandWidth - lineWidth) / 2.0f, cursorY + lineHeight / 2.0f, fontScale);
                cursorY += lineHeight;
            }
        } else {
            String title = "Player List (" + players.size() + ")";
            float titleWidth = (float)font.getStringWidth(title) * fontScale;
            this.label(graphics, font, title, accent, x + (this.islandWidth - titleWidth) / 2.0f, cursorY + lineHeight / 2.0f, fontScale);
            cursorY += lineHeight;
        }
        cursorY += 5.0f * scale;
        float columnWidth = 118.0f * scale;
        for (int i = 0; i < players.size(); ++i) {
            int column = i / 20;
            int row = i % 20;
            PlayerInfo info = players.get(i);
            float rowX = x + 9.0f * scale + (float)column * (columnWidth + 8.0f * scale);
            float rowCenter = cursorY + (float)row * 12.0f * scale + 12.0f * scale / 2.0f;
            int rowPing = info.getLatency();
            int rowColor = fade << 24 | WatermarkModule.pingColor(rowPing) & 0xFFFFFF;
            boolean self = info.getProfile().getId().equals(mc.player.getUUID());
            WatermarkModule.drawRounded(graphics, rowX, rowCenter - 1.75f * scale, 3.5f * scale, 3.5f * scale, 1.75f * scale, rowColor);
            String latency = Math.max(0, rowPing) + "ms";
            float latencyWidth = (float)font.getStringWidth(latency) * fontScale;
            float nameMax = columnWidth - 8.0f * scale - latencyWidth - 6.0f * scale;
            String rawName = info.getProfile().getName();
            String tag = "";
            int tagColor = fg;
            String suffix = "";
            if (AntinickModule.isNickedName(rawName)) {
                tag = "[Nick] ";
                tagColor = fade << 24 | 0xE08BFF;
                String real = AntinickModule.getRealName(rawName);
                if (real != null && !real.isEmpty()) {
                    suffix = " §b(" + real + ")";
                }
            } else if (AntistaffModule.isStaff(rawName)) {
                tag = "[Staff] ";
                tagColor = fade << 24 | 0xFF6B6B;
            }
            float tagWidth = (float)font.getStringWidth(tag) * fontScale;
            float suffixWidth = (float)font.getStringWidth(suffix) * fontScale;
            String name = WatermarkModule.fit(font, rawName, nameMax - tagWidth - suffixWidth, fontScale);
            float nameX = rowX + 8.0f * scale;
            if (!tag.isEmpty()) {
                this.label(graphics, font, tag, tagColor, nameX, rowCenter, fontScale);
                nameX += tagWidth;
            }
            if (self && NicknameModule.active()) {
                int segments = NicknameModule.segmentCount();
                float remaining = rowX + columnWidth - latencyWidth - 6.0f * scale - nameX;
                for (int segment = 0; segment < segments; ++segment) {
                    String rawSegment = NicknameModule.segmentText(segment);
                    if (rawSegment.isEmpty()) continue;
                    if (remaining <= 4.0f) break;
                    String segText = WatermarkModule.fit(font, rawSegment, remaining, fontScale);
                    nameX = this.label(graphics, font, segText, fade << 24 | NicknameModule.segmentRgb(segment) & 0xFFFFFF, nameX, rowCenter, fontScale);
                    remaining -= (float)font.getStringWidth(segText) * fontScale;
                    if (segText.length() < rawSegment.length()) break;
                }
                this.label(graphics, font, suffix, self ? accent : fg, nameX, rowCenter, fontScale);
            } else {
                this.label(graphics, font, name + suffix, self ? accent : fg, nameX, rowCenter, fontScale);
            }
            this.label(graphics, font, latency, muted, rowX + columnWidth - latencyWidth, rowCenter, fontScale);
        }
        if (tabFooter != null && !tabFooter.getString().isEmpty()) {
            float footerY = cursorY + (float)Math.min(players.size(), 20) * 12.0f * scale + 5.0f * scale;
            for (String line : tabFooter.getString().split("\n")) {
                float lineWidth = (float)font.getStringWidth(line) * fontScale;
                this.label(graphics, font, line, muted, x + (this.islandWidth - lineWidth) / 2.0f, footerY + lineHeight / 2.0f, fontScale);
                footerY += lineHeight;
            }
        }
    }

    private static String fit(Font font, String text, float maxWidth, float fontScale) {
        String candidate;
        if (text == null) {
            return "";
        }
        if (maxWidth <= 4.0f || (float)font.getStringWidth(text) * fontScale <= maxWidth) {
            return text;
        }
        String suffix = "...";
        float suffixWidth = (float)font.getStringWidth(suffix) * fontScale;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length() && !((float)font.getStringWidth(candidate = builder.toString() + text.charAt(i)) * fontScale + suffixWidth > maxWidth); ++i) {
            builder.append(text.charAt(i));
        }
        return builder + suffix;
    }

    @Override
    public void onDisable() {
        this.restoreTabList();
        this.notices.clear();
        PENDING.clear();
    }

    private void ensureTabHidden(Minecraft mc) {
        if (mc.gui == null) {
            return;
        }
        try {
            Field target = null;
            for (Field field : Gui.class.getDeclaredFields()) {
                if (field.getType() != PlayerTabOverlay.class) continue;
                target = field;
                break;
            }
            if (target == null) {
                return;
            }
            target.setAccessible(true);
            Object current = target.get(mc.gui);
            if (this.tabSuppressed && current == this.tabReplacement) {
                return;
            }
            if (this.tabReplacement == null) {
                this.tabReplacement = new PlayerTabOverlay(mc, mc.gui){

                    public void render(GuiGraphics graphics, int width, Scoreboard scoreboard, Objective objective) {
                    }

                    public void setFooter(Component footer) {
                        tabFooter = footer;
                    }

                    public void setHeader(Component header) {
                        tabHeader = header;
                    }
                };
            }
            if (current instanceof PlayerTabOverlay) {
                this.originalTabList = (PlayerTabOverlay)current;
            }
            target.set(mc.gui, this.tabReplacement);
            this.tabSuppressed = true;
        }
        catch (Throwable throwable) {
            this.tabSuppressed = false;
        }
    }

    private void restoreTabList() {
        this.tabSuppressed = false;
        this.tabReplacement = null;
        if (this.originalTabList == null) {
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui == null) {
                return;
            }
            for (Field field : Gui.class.getDeclaredFields()) {
                if (field.getType() != PlayerTabOverlay.class) continue;
                field.setAccessible(true);
                if (field.get(mc.gui) instanceof PlayerTabOverlay) {
                    field.set(mc.gui, this.originalTabList);
                }
                break;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        this.originalTabList = null;
    }

    private List<PlayerInfo> collectPlayers(Minecraft mc) {
        ArrayList<PlayerInfo> list = new ArrayList<PlayerInfo>(mc.getConnection().getOnlinePlayers());
        if (NicknameModule.active() && mc.player != null) {
            UUID selfId = mc.player.getUUID();
            String selfName = mc.player.getGameProfile().getName();
            String plain = NicknameModule.plain();
            boolean hasSelf = false;
            for (PlayerInfo info2 : list) {
                if (!info2.getProfile().getId().equals(selfId)) continue;
                hasSelf = true;
                break;
            }
            for (int i = list.size() - 1; hasSelf && i >= 0; --i) {
                String name;
                PlayerInfo info2;
                info2 = list.get(i);
                if (info2.getProfile().getId().equals(selfId) || (name = info2.getProfile().getName()) == null || !NicknameModule.isAlias(name) && (selfName == null || !name.equals(selfName)) && (plain.isEmpty() || !name.equals(plain))) continue;
                list.remove(i);
            }
        }
        list.sort(Comparator.comparing(info -> info.getProfile().getName(), String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    private void drainPending(long now) {
        Notice incoming;
        while ((incoming = PENDING.poll()) != null) {
            incoming.born = now;
            boolean merged = false;
            for (int i = 0; i < this.notices.size(); ++i) {
                Notice existing = this.notices.get(i);
                if (!existing.title.equals(incoming.title)) continue;
                existing.detail = incoming.detail;
                existing.color = incoming.color;
                existing.iconColor = incoming.iconColor;
                existing.icon = incoming.icon;
                existing.born = now;
                existing.duration = incoming.duration;
                merged = true;
                break;
            }
            if (merged) continue;
            this.notices.add(0, incoming);
            while (this.notices.size() > 3) {
                this.notices.remove(this.notices.size() - 1);
            }
        }
    }

    private void pruneNotices(long now) {
        for (int i = this.notices.size() - 1; i >= 0; --i) {
            if (now - this.notices.get((int)i).born <= this.notices.get((int)i).duration) continue;
            this.notices.remove(i);
        }
    }

    private void renderAlert(GuiGraphics graphics, Font font, Notice notice, float x, float y, float scale, float fontScale, float alpha) {
        int fade = (int)(255.0f * alpha);
        float centerY = y + this.islandHeight / 2.0f;
        float cursor = x + 9.0f * scale;
        int iconColor = fade << 24 | notice.iconColor & 0xFFFFFF;
        this.symbol(graphics, notice.icon, cursor, centerY, 10.5f * scale, iconColor);
        cursor += 14.0f * scale;
        int fg = fade << 24 | notice.titleColor & 0xFFFFFF;
        float maxTitle = this.settledWidth - 21.5f * scale - 14.0f * scale - (notice.detail.isEmpty() ? 0.0f : 8.0f * scale) - WatermarkModule.measure(font, notice.detail, fontScale) + 2.0f;
        String title = notice.title;
        cursor = this.label(graphics, font, title, fg, cursor, centerY, fontScale);
        if (!notice.detail.isEmpty()) {
            int detailAlpha = Math.max(0, Math.min(255, notice.textAlpha));
            float maxDetail = x + this.settledWidth - 12.5f * scale - cursor - 6.0f * scale + 2.0f;
            String detail = WatermarkModule.fit(font, notice.detail, maxDetail, fontScale);
            this.label(graphics, font, detail, (int)((float)fade * ((float)detailAlpha / 255.0f)) << 24 | notice.color & 0xFFFFFF, cursor + 6.0f * scale, centerY, fontScale);
        }
    }

    private int headerLines() {
        if (tabHeader != null && !tabHeader.getString().isEmpty()) {
            return WatermarkModule.lineCount(tabHeader);
        }
        return 1;
    }

    private void renderIdle(GuiGraphics graphics, Font font, Minecraft mc, float x, float y, float scale, float fontScale, float alpha, String brand, String username, String server, String pingText, String fpsText, int tierPing) {
        int fade = (int)(255.0f * alpha);
        int fg = fade << 24 | 0xE4E1E6;
        int accent = fade << 24 | 0xBBC3FF;
        float centerY = y + this.islandHeight / 2.0f;
        float cursor = x + 9.0f * scale;
        this.symbol(graphics, 0, cursor, centerY, 10.5f * scale, ClickGUI.brighten(accent, 3.0f));
        cursor += 14.0f * scale;
        cursor = this.label(graphics, font, brand, accent, cursor, centerY, fontScale);
        cursor = this.separator(graphics, font, cursor, centerY, fontScale, scale, fg);
        this.symbol(graphics, 1, cursor, centerY, 10.5f * scale, fg);
        cursor += 14.0f * scale;
        if (NicknameModule.active()) {
            int segments = NicknameModule.segmentCount();
            for (int segment = 0; segment < segments; ++segment) {
                String text = NicknameModule.segmentText(segment);
                if (text.isEmpty()) continue;
                cursor = this.label(graphics, font, text, fade << 24 | NicknameModule.segmentRgb(segment) & 0xFFFFFF, cursor, centerY, fontScale);
            }
        } else {
            cursor = this.label(graphics, font, username, fg, cursor, centerY, fontScale);
        }
        cursor = this.separator(graphics, font, cursor, centerY, fontScale, scale, fg);
        int pingColor = fade << 24 | WatermarkModule.pingColor(tierPing) & 0xFFFFFF;
        this.signal(graphics, cursor, centerY, 10.5f * scale, tierPing, fade);
        cursor += 14.0f * scale;
        if (((Boolean)this.showPing.get()).booleanValue()) {
            cursor = this.label(graphics, font, pingText, pingColor, cursor, centerY, fontScale);
            cursor = this.label(graphics, font, " to ", fg, cursor, centerY, fontScale);
        }
        cursor = this.label(graphics, font, server, fg, cursor, centerY, fontScale);
        if (((Boolean)this.showFps.get()).booleanValue()) {
            cursor = this.separator(graphics, font, cursor, centerY, fontScale, scale, fg);
            this.symbol(graphics, 3, cursor, centerY, 10.5f * scale, fg);
            this.label(graphics, font, fpsText, fg, cursor += 14.0f * scale, centerY, fontScale);
        }
    }

    private static float separatorAdvance(Font font, float fontScale, float scale) {
        return 1.25f * scale + WatermarkModule.measure(font, WatermarkModule.separatorText(), fontScale) + 0.75f * scale;
    }

    private static int lineCount(Component component) {
        return component.getString().split("\n").length;
    }

    private float idleWidth(Font font, float fontScale, float scale, String brand, String username, String server, String pingText, String fpsText) {
        float cursor = 21.5f * scale;
        cursor += 14.0f * scale;
        cursor += WatermarkModule.measure(font, brand, fontScale);
        if (NicknameModule.active()) {
            for (int i = 0; i < NicknameModule.segmentCount(); ++i) {
                cursor += WatermarkModule.measure(font, NicknameModule.segmentText(i), fontScale);
            }
        } else {
            cursor += WatermarkModule.measure(font, username, fontScale);
        }
        cursor += WatermarkModule.separatorAdvance(font, fontScale, scale);
        cursor += 14.0f * scale;
        if (((Boolean)this.showPing.get()).booleanValue()) {
            cursor += WatermarkModule.measure(font, pingText, fontScale);
            cursor += WatermarkModule.measure(font, " to ", fontScale);
        }
        cursor += WatermarkModule.measure(font, server, fontScale);
        if (((Boolean)this.showFps.get()).booleanValue()) {
            cursor += WatermarkModule.separatorAdvance(font, fontScale, scale);
            cursor += 14.0f * scale;
            cursor += WatermarkModule.measure(font, fpsText, fontScale);
        }
        return cursor;
    }

    private static float measure(Font font, String text, float fontScale) {
        if (text == null || text.isEmpty()) {
            return 0.0f;
        }
        return FontManager.measureExact(font, text, fontScale);
    }

    private static String separatorText() {
        return Render2D.d(new byte[]{98, 98, -11, 98, 98});
    }

    private static int pingColor(int ping) {
        if (ping < 0) {
            return -1777178;
        }
        if (ping < 80) {
            return -8337392;
        }
        if (ping < 150) {
            return -5848022;
        }
        if (ping < 300) {
            return -3637976;
        }
        return -3523026;
    }

    private float alertWidth(Font font, float fontScale, float scale, Notice notice) {
        float cursor = 21.5f * scale;
        cursor += 14.0f * scale;
        cursor += WatermarkModule.measure(font, notice.title, fontScale);
        if (!notice.detail.isEmpty()) {
            cursor += 8.0f * scale;
            cursor += WatermarkModule.measure(font, notice.detail, fontScale);
        }
        return cursor;
    }

    private static int pingBars(int ping) {
        if (ping < 0) {
            return 0;
        }
        if (ping < 80) {
            return 4;
        }
        if (ping < 150) {
            return 3;
        }
        if (ping < 300) {
            return 2;
        }
        return 1;
    }

    private static void slab(GuiGraphics graphics, float vx, float vy, float vw, float vh, float vr, float scale, float offsetX, float viewMinX, float viewMinY, int color) {
        float x = offsetX + (vx - viewMinX) * scale;
        float y = (vy - viewMinY) * scale;
        float w = vw * scale;
        float h = vh * scale;
        WatermarkModule.drawRounded(graphics, x, y, w, h, vr * scale, color);
    }

    private static void brandMark(GuiGraphics graphics, int color) {
        float viewMinX = 116.0f;
        float viewMinY = 88.0f;
        float viewW = 280.0f;
        float viewH = 336.0f;
        float box = 24.0f;
        float scale = 0.071428575f;
        float offsetX = (24.0f - 280.0f * scale) * 0.5f;
        float barW = 68.0f;
        float crossTop = 222.0f;
        float crossH = 68.0f;
        float corner = 26.0f;
        WatermarkModule.slab(graphics, 116.0f, 88.0f, barW, 336.0f, corner, scale, offsetX, 116.0f, 88.0f, color);
        WatermarkModule.slab(graphics, 328.0f, 88.0f, barW, 336.0f, corner, scale, offsetX, 116.0f, 88.0f, color);
        WatermarkModule.slab(graphics, 184.0f, crossTop, 144.0f, crossH, corner, scale, offsetX, 116.0f, 88.0f, color);
    }

    private static void arcRing(GuiGraphics graphics, float cx, float cy, float inner, float outer, float startDeg, float endDeg, int color) {
        graphics.flush();
        Shader.arc(graphics.pose().last().pose(), cx, cy, inner, outer, startDeg, endDeg, color);
    }

    @Override
    public void onWorldChange() {
        this.notices.clear();
        PENDING.clear();
        tabHeader = null;
        tabFooter = null;
        this.tabSuppressed = false;
        this.tabReplacement = null;
        this.originalTabList = null;
        this.activeElement = -1;
        this.smoothedPing = -1.0f;
        this.contentFade = 1.0f;
        this.lastFrameNanos = System.nanoTime();
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        try {
            PlayerInfo info;
            boolean listOpen;
            Minecraft mc = Minecraft.getInstance();
            if (mc.getWindow() == null || mc.player == null) {
                return;
            }
            Font font = FontManager.regular;
            if (font == null) {
                return;
            }
            this.ensureTabHidden(mc);
            long now = System.currentTimeMillis();
            this.drainPending(now);
            this.pruneNotices(now);
            float scale = ((Float)this.size.get()).floatValue();
            float fontScale = 8.0f * scale / (float)font.getHeight();
            boolean bl = listOpen = mc.getConnection() != null && mc.screen == null && mc.getOverlay() == null && mc.options.keyPlayerList.isDown();
            int element = listOpen ? 2 : (this.notices.isEmpty() ? 0 : 1);
            List<PlayerInfo> players = element == 2 ? this.collectPlayers(mc) : null;
            Notice notice = element == 1 ? this.notices.get(0) : null;
            String brand = "Hachimi " + EventDispatcherImpl.version;
            String username = NicknameModule.plainOr(mc.getUser().getName());
            boolean singleplayer = mc.getCurrentServer() == null;
            String server = singleplayer ? "singleplayer" : mc.getCurrentServer().ip;
            int ping = -1;
            if (!singleplayer && mc.getConnection() != null && (info = mc.getConnection().getPlayerInfo(mc.player.getUUID())) != null) {
                ping = info.getLatency();
            }
            this.smoothedPing = ping >= 0 ? (this.smoothedPing < 0.0f ? (float)ping : this.smoothedPing + ((float)ping - this.smoothedPing) * 0.15f) : -1.0f;
            int tierPing = this.smoothedPing < 0.0f ? -1 : Math.round(this.smoothedPing);
            String pingText = ping < 0 ? "...ms" : ping + "ms";
            String fpsText = Math.max(0, mc.getFps()) + " fps";
            float targetWidth = element == 2 ? this.listWidth(font, fontScale, scale, players) : (element == 1 ? this.alertWidth(font, fontScale, scale, notice) : this.idleWidth(font, fontScale, scale, brand, username, server, pingText, fpsText));
            float targetHeight = element == 2 ? this.listHeight(font, fontScale, scale, players) : 23.0f * scale;
            long frameNow = System.nanoTime();
            float deltaMs = Math.min(50.0f, (float)(frameNow - this.lastFrameNanos) / 1000000.0f);
            this.lastFrameNanos = frameNow;
            if (this.activeElement != element) {
                this.activeElement = element;
                this.contentFade = 0.0f;
            }
            this.contentFade = Math.min(1.0f, this.contentFade + deltaMs / 110.0f);
            float follow = Math.min(1.0f, deltaMs / 80.0f);
            this.islandWidth += (targetWidth - this.islandWidth) * follow;
            this.settledWidth = targetWidth;
            this.islandHeight += (targetHeight - this.islandHeight) * follow;
            if (Math.abs(targetWidth - this.islandWidth) < 0.4f) {
                this.islandWidth = targetWidth;
            }
            if (Math.abs(targetHeight - this.islandHeight) < 0.4f) {
                this.islandHeight = targetHeight;
            }
            float anchorX = ((float)mc.getWindow().getGuiScaledWidth() - this.islandWidth) / 2.0f;
            float anchorY = 21.0f;
            float x = anchorX + ((Float)this.offsetX.get()).floatValue();
            float y = anchorY + ((Float)this.offsetY.get()).floatValue();
            x = Math.max(2.0f, Math.min((float)mc.getWindow().getGuiScaledWidth() - this.islandWidth - 2.0f, x));
            y = Math.max(2.0f, Math.min((float)mc.getWindow().getGuiScaledHeight() - this.islandHeight - 2.0f, y));
            float[] dragged = RenderUtil.hudDragOffset(mc, x, y, anchorX, anchorY, this.islandWidth, this.islandHeight, this.offsetX, this.offsetY, this);
            x = dragged[0];
            y = dragged[1];
            float radius = Math.min(11.5f * scale, this.islandHeight / 2.0f);
            if (RenderUtil.hudHover == this) {
                WatermarkModule.drawRounded(graphics, x - 2.5f, y - 2.5f, this.islandWidth + 5.0f, this.islandHeight + 5.0f, radius + 2.5f, 1854709247);
            }
            boolean prevDepthOn = GL11.glIsEnabled((int)2929);
            boolean prevBlendOn = GL11.glIsEnabled((int)3042);
            boolean prevScissorOn = GL11.glIsEnabled((int)3089);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.flush();
            Shader.glow(graphics.pose().last().pose(), x, y, this.islandWidth, this.islandHeight, radius, 2.6f * scale, 0x58000000);
            WatermarkModule.drawRounded(graphics, x, y, this.islandWidth, this.islandHeight, radius, -1744830464);
            graphics.enableScissor((int)x, (int)y, (int)(x + this.islandWidth), (int)(y + this.islandHeight));
            if (element == 2 && players != null) {
                this.renderList(graphics, font, mc, players, x, y, scale, fontScale, this.contentFade);
            } else if (element == 1 && notice != null) {
                this.renderAlert(graphics, font, notice, x, y, scale, fontScale, this.contentFade);
            } else {
                this.renderIdle(graphics, font, mc, x, y, scale, fontScale, this.contentFade, brand, username, server, pingText, fpsText, tierPing);
            }
            graphics.disableScissor();
            graphics.flush();
            if (prevDepthOn) {
                RenderSystem.enableDepthTest();
            } else {
                RenderSystem.disableDepthTest();
            }
            if (prevBlendOn) {
                RenderSystem.enableBlend();
            } else {
                RenderSystem.disableBlend();
            }
            if (prevScissorOn) {
                GL11.glEnable((int)3089);
            } else {
                GL11.glDisable((int)3089);
            }
        }
        catch (Throwable throwable) {
            this.notices.clear();
        }
    }

    private static final class Notice {
        private final String title;
        private String detail;
        private int color;
        private int iconColor;
        private int icon;
        private long born;
        private long duration;
        private int titleColor;
        private int textAlpha;

        private Notice(String title, String detail, int icon, int color, int iconColor, long duration) {
            this.title = title;
            this.detail = detail;
            this.icon = icon;
            this.color = color;
            this.iconColor = iconColor;
            this.duration = duration;
            this.born = System.currentTimeMillis();
            this.titleColor = -1777178;
            this.textAlpha = 205;
        }
    }
}

