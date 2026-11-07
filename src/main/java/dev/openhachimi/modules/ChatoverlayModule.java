/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.render.shader.Shader;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.RegistryManager;
import dev.openhachimi.utils.render.font.FontManager;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;

public class ChatoverlayModule
extends Module {
    private static final byte[] S_17D3FDB8 = new byte[]{1, 42, 35, 54, 13, 52, 39, 48, 46, 35, 59};  // XOR0x42 "ChatOverlay"
    private static final byte[] S_56CAF42B = new byte[]{13, 36, 36, 49, 39, 54, 98, 26};  // XOR0x42 "Offset X"
    private static final byte[] S_56CAF42C = new byte[]{13, 36, 36, 49, 39, 54, 98, 27};  // XOR0x42 "Offset Y"
    private static final byte[] S_4FB3DA6 = new byte[]{21, 43, 38, 54, 42};  // XOR0x42 "Width"
    private static final byte[] S_9D7D47 = new byte[]{10, 39, 43, 37, 42, 54};  // XOR0x42 "Height"
    private static final byte[] S_1822B132 = new byte[]{4, 45, 44, 54, 98, 17, 43, 56, 39};  // XOR0x42 "Font Size"
    private static final byte[] S_33FF94C2 = new byte[]{4, 45, 44, 54, 98, 0, 48, 43, 37, 42, 54, 44, 39, 49, 49};  // XOR0x42 "Font Brightness"
    private static final byte[] S_4A9B9CAA = new byte[]{14, 43, 44, 39, 98, 5, 35, 50};  // XOR0x42 "Line Gap"
    private static final byte[] S_276A314E = new byte[]{0, 35, 33, 41, 37, 48, 45, 55, 44, 38};  // XOR0x42 "Background"
    private static final byte[] S_2E49D5A3 = new byte[]{0, 5, 98, 3, 46, 50, 42, 35};  // XOR0x42 "BG Alpha"
    private static final byte[] S_1FE8007B = new byte[]{11, 44, 50, 55, 54, 98, 0, 5};  // XOR0x42 "Input BG"
    private static final byte[] S_77545548 = new byte[]{11, 44, 50, 55, 54, 98, 3, 46, 50, 42, 35};  // XOR0x42 "Input Alpha"
    private static final byte[] S_6C28466E = new byte[]{3, 46, 53, 35, 59, 49, 98, 17, 42, 45, 53};  // XOR0x42 "Always Show"
    private static final byte[] S_7E717373 = new byte[]{22, 39, 58, 54, 98, 17, 42, 35, 38, 45, 53};  // XOR0x42 "Text Shadow"
    private static final byte[] S_60E4C402 = new byte[]{15, 39, 49, 49, 35, 37, 39, 98, 14, 43, 36, 39, 54, 43, 47, 39};  // XOR0x42 "Message Lifetime"
    private static final byte[] S_6E87E6B = new byte[]{9, 39, 39, 50, 98, 1, 45, 46, 45, 48, 49};  // XOR0x42 "Keep Colors"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_41 = new byte[]{3};
    private static final byte[] S_42 = new byte[]{0};
    private static final byte[] S_18E70626 = new byte[]{49, 33, 48, 45, 46, 46, 32, 35, 48};  // XOR0x42 "scrollbar"
    private static final byte[] S_49E5E6CD = new byte[]{49, 33, 48, 45, 46, 46};  // XOR0x42 "scroll"
    private static final byte[] S_A = new byte[]{72};
    private static final float DEFAULT_WIDTH = 320.0f;
    private static final float DEFAULT_HEIGHT = 180.0f;
    private static final float PADDING = 8.0f;
    private static final float INPUT_HEIGHT = 13.0f;
    private static final float RADIUS = 8.0f;
    private static final int MAX_LINES = 40;
    private final NumberSetting offsetX = new NumberSetting("Offset X", 0.0f, -4000.0f, 4000.0f);
    private final NumberSetting offsetY = new NumberSetting("Offset Y", 0.0f, -4000.0f, 4000.0f);
    private final NumberSetting widthSetting = new NumberSetting("Width", 320.0f, 180.0f, 700.0f);
    private final NumberSetting heightSetting = new NumberSetting("Height", 180.0f, 60.0f, 600.0f);
    private final NumberSetting fontSize = new NumberSetting("Font Size", 7.5f, 4.0f, 20.0f);
    private final NumberSetting fontBrightness = new NumberSetting("Font Brightness", 1.0f, 0.0f, 1.0f);
    private final NumberSetting lineGap = new NumberSetting("Line Gap", 2.0f, 0.0f, 10.0f);
    private final RegistryManager bgColor = new RegistryManager("Background", 658449);
    private final NumberSetting bgAlpha = new NumberSetting("BG Alpha", 0.82f, 0.0f, 1.0f);
    private final RegistryManager inputColor = new RegistryManager("Input BG", 0);
    private final NumberSetting inputAlpha = new NumberSetting("Input Alpha", 0.6f, 0.0f, 1.0f);
    private final BooleanSetting alwaysShow = new BooleanSetting("Always Show", true);
    private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", true);
    private final NumberSetting fadeSeconds = new NumberSetting("Message Lifetime", 10.0f, 0.0f, 120.0f);
    private final BooleanSetting keepColors = new BooleanSetting("Keep Colors", true);
    private final List<ChatLine> lines = new ArrayList<ChatLine>();
    private String lastSignature = "";
    private int scrollOffset = 0;
    private int lastPageRows = 1;
    private int lastTotalRows = 0;
    private boolean wasChatting = false;
    private boolean discardStale = false;
    private List<ChatLine> linesAll = new ArrayList<ChatLine>();
    private List<Long> prevTimes = new ArrayList<Long>();
    private int prevRawSize = 0;
    private List<String> prevTexts = new ArrayList<String>();
    private final Map<String, Long> seenAt = new HashMap<String, Long>();
    private float animatedHeight = -1.0f;
    private float animatedInput = 0.0f;
    private float animatedInputAlpha = 0.0f;
    private long lastFrameNanos = System.nanoTime();

    public ChatoverlayModule() {
        super("ChatOverlay", Category.RENDER);
        this.offsetX.setVisible(() -> false);
        this.offsetY.setVisible(() -> false);
        this.values.add(this.offsetX);
        this.values.add(this.offsetY);
        this.values.add(this.widthSetting);
        this.values.add(this.heightSetting);
        this.values.add(this.fontSize);
        this.values.add(this.fontBrightness);
        this.values.add(this.lineGap);
        this.values.add(this.bgColor);
        this.values.add(this.bgAlpha);
        this.values.add(this.inputColor);
        this.values.add(this.inputAlpha);
        this.values.add(this.alwaysShow);
        this.values.add(this.textShadow);
        this.values.add(this.fadeSeconds);
        this.values.add(this.keepColors);
    }

    private String wrap(Font font, String text, float maxWidth, float fontScale) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (maxWidth <= 1.0f) {
            return text;
        }
        float limit = maxWidth / Math.max(0.01f, fontScale);
        ArrayList<String> parts = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        float lineWidth = 0.0f;
        int lastSpace = -1;
        float widthAtSpace = 0.0f;
        String activeColor = "";
        int i = 0;
        int len = text.length();
        while (i < len) {
            char ch = text.charAt(i);
            if (ch == '§' && i + 1 < len) {
                char next = text.charAt(i + 1);
                int seqEnd = next == 'x' || next == 'X' ? Math.min(len, i + 14) : i + 2;
                String seq = text.substring(i, seqEnd);
                line.append(seq);
                activeColor = seq;
                i = seqEnd;
                continue;
            }
            if (ch == '\n') {
                parts.add(line.toString());
                line.setLength(0);
                if (!activeColor.isEmpty()) {
                    line.append(activeColor);
                }
                lineWidth = 0.0f;
                lastSpace = -1;
                ++i;
                continue;
            }
            float cw = font.getStringWidth(String.valueOf(ch));
            if (lineWidth + cw > limit && lineWidth > 0.0f) {
                if (lastSpace > 0) {
                    String full = line.toString();
                    String head = full.substring(0, lastSpace);
                    String tail = full.substring(lastSpace);
                    parts.add(head);
                    line.setLength(0);
                    if (!activeColor.isEmpty()) {
                        line.append(activeColor);
                    }
                    line.append(tail.trim());
                    lineWidth -= widthAtSpace;
                } else {
                    parts.add(line.toString());
                    line.setLength(0);
                    if (!activeColor.isEmpty()) {
                        line.append(activeColor);
                    }
                    lineWidth = 0.0f;
                }
                lastSpace = -1;
            }
            if (ch == ' ') {
                lastSpace = line.length();
                widthAtSpace = lineWidth;
            }
            line.append(ch);
            lineWidth += cw;
            ++i;
        }
        if (line.length() > 0) {
            parts.add(line.toString());
        }
        StringBuilder out = new StringBuilder();
        for (int k = 0; k < parts.size(); ++k) {
            if (k > 0) {
                out.append('\n');
            }
            out.append((String)parts.get(k));
        }
        return out.toString();
    }

    public void render(Minecraft mc, GuiGraphics graphics) {
        EditBox input;
        if (mc.getWindow() == null) {
            return;
        }
        boolean chatting = mc.screen instanceof ChatScreen;
        if (!chatting && !((Boolean)this.alwaysShow.get()).booleanValue()) {
            return;
        }
        Font font = FontManager.regular;
        if (font == null) {
            if (chatting) {
                this.renderFallback(mc, graphics);
            }
            return;
        }
        EditBox editBox = input = chatting ? this.findInput(mc.screen) : null;
        if (input != null) {
            input.setVisible(true);
        }
        float guiWidth = mc.getWindow().getGuiScaledWidth();
        float guiHeight = mc.getWindow().getGuiScaledHeight();
        float panelWidth = Math.min(((Float)this.widthSetting.get()).floatValue(), guiWidth - 8.0f);
        float fullHeight = Math.min(((Float)this.heightSetting.get()).floatValue(), guiHeight - 8.0f);
        float fontSizeNow = ((Float)this.fontSize.get()).floatValue();
        float fontScale = fontSizeNow / (float)font.getHeight();
        float rowHeight = fontSizeNow + ((Float)this.lineGap.get()).floatValue();
        float targetInput = chatting ? 18.0f : 0.0f;
        float targetInputAlpha = chatting ? 1.0f : 0.0f;
        int maxRows = Math.max(1, (int)((fullHeight - 16.0f) / rowHeight));
        if (chatting != this.wasChatting) {
            this.wasChatting = chatting;
            this.scrollOffset = 0;
            this.lastSignature = "";
            this.lastFrameNanos = System.nanoTime();
        }
        this.collectLines(mc, chatting);
        this.applyLifetime(chatting);
        if (this.lines.isEmpty() && !chatting) {
            return;
        }
        this.lastPageRows = maxRows;
        int maxOffset = Math.max(0, this.lines.size() - maxRows);
        if (this.scrollOffset > maxOffset) {
            this.scrollOffset = maxOffset;
        }
        int scrollPos = this.scrollOffset;
        int totalLines = this.lines.size();
        int visibleRows = Math.min(totalLines, maxRows);
        float panelHeight = fullHeight;
        if (!chatting && scrollPos == 0 && visibleRows > 0 && (panelHeight = Math.min(fullHeight, 16.0f + (float)visibleRows * rowHeight)) < rowHeight + 16.0f) {
            panelHeight = rowHeight + 16.0f;
        }
        long frameNow = System.nanoTime();
        float deltaMs = Math.min(50.0f, (float)(frameNow - this.lastFrameNanos) / 1000000.0f);
        this.lastFrameNanos = frameNow;
        if (this.animatedHeight < 0.0f) {
            this.animatedHeight = panelHeight;
            this.animatedInput = targetInput;
            this.animatedInputAlpha = targetInputAlpha;
        } else {
            float follow = Math.min(1.0f, deltaMs / 80.0f);
            this.animatedHeight += (panelHeight - this.animatedHeight) * follow;
            if (Math.abs(panelHeight - this.animatedHeight) < 0.4f) {
                this.animatedHeight = panelHeight;
            }
            this.animatedInput += (targetInput - this.animatedInput) * follow;
            if (Math.abs(targetInput - this.animatedInput) < 0.3f) {
                this.animatedInput = targetInput;
            }
            float fadeStep = Math.min(1.0f, deltaMs / 110.0f);
            this.animatedInputAlpha += (targetInputAlpha - this.animatedInputAlpha) * fadeStep;
            if (Math.abs(targetInputAlpha - this.animatedInputAlpha) < 0.01f) {
                this.animatedInputAlpha = targetInputAlpha;
            }
        }
        float drawHeight = this.animatedHeight;
        float anchorX = 4.0f;
        float anchorY = guiHeight - drawHeight - 4.0f;
        float x = anchorX + ((Float)this.offsetX.get()).floatValue();
        float y = anchorY + ((Float)this.offsetY.get()).floatValue();
        float[] dragged = RenderUtil.hudDragOffset(mc, x, y, anchorX, anchorY, panelWidth, drawHeight, this.offsetX, this.offsetY, this);
        x = Math.max(0.0f, Math.min(guiWidth - panelWidth, dragged[0]));
        y = Math.max(0.0f, Math.min(guiHeight - drawHeight, dragged[1]));
        this.renderPanel(graphics, font, input, chatting, x, y, panelWidth, drawHeight, fontScale, rowHeight, this.animatedInput, this.animatedInputAlpha, scrollPos);
    }

    private void applyLifetime(boolean keepAll) {
        float seconds = ((Float)this.fadeSeconds.get()).floatValue();
        this.lines.clear();
        if (keepAll || seconds <= 0.01f) {
            this.lines.addAll(this.linesAll);
            return;
        }
        long cutoff = System.currentTimeMillis() - (long)(seconds * 1000.0f);
        for (ChatLine line : this.linesAll) {
            if (line.addedAt < cutoff) continue;
            this.lines.add(line);
        }
    }

    private EditBox findInput(Screen screen) {
        if (screen == null) {
            return null;
        }
        for (Class<?> c = screen.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                if (!EditBox.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(screen);
                    if (!(value instanceof EditBox)) continue;
                    return (EditBox)value;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    private void renderFallback(Minecraft mc, GuiGraphics graphics) {
        float guiHeight = mc.getWindow().getGuiScaledHeight();
        float guiWidth = mc.getWindow().getGuiScaledWidth();
        float w = Math.min(((Float)this.widthSetting.get()).floatValue(), guiWidth - 8.0f);
        float h = Math.min(((Float)this.heightSetting.get()).floatValue(), guiHeight - 8.0f);
        graphics.fill(4, (int)(guiHeight - h - 4.0f), (int)(4.0f + w), (int)(guiHeight - 4.0f), ChatoverlayModule.withAlpha((Integer)this.bgColor.get(), ((Float)this.bgAlpha.get()).floatValue()));
    }

    private void renderPanel(GuiGraphics graphics, Font font, EditBox input, boolean chatting, float x, float y, float panelWidth, float panelHeight, float fontScale, float rowHeight, float inputOffset, float inputAlpha, int scrollPos) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.flush();
        int bg = ChatoverlayModule.withAlpha((Integer)this.bgColor.get(), ((Float)this.bgAlpha.get()).floatValue());
        ChatoverlayModule.drawRounded(graphics, x, y, panelWidth, panelHeight, 8.0f, bg);
        float textX = x + 8.0f;
        float historyBottom = y + panelHeight - 8.0f - inputOffset;
        float historyTop = y + 8.0f;
        float inputTop = y + panelHeight - 8.0f - 13.0f;
        this.drawHistory(graphics, font, textX, historyTop, historyBottom, panelWidth, fontScale, rowHeight, scrollPos);
        if (input != null && inputAlpha > 0.01f) {
            this.drawInputBox(graphics, font, input, textX, inputTop, panelWidth, fontScale, inputAlpha);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void resetTracking() {
        this.prevTexts.clear();
        this.prevTimes.clear();
        this.prevRawSize = 0;
    }

    public int scrollOffset() {
        return this.scrollOffset;
    }

    private void collectLines(Minecraft mc, boolean keepAll) {
        List<String> raw = this.readMessages(mc);
        if (raw.isEmpty()) {
            if (!this.linesAll.isEmpty()) {
                this.linesAll = new ArrayList<ChatLine>();
            }
            this.lastSignature = "";
            this.discardStale = false;
            return;
        }
        StringBuilder sig = new StringBuilder();
        for (String line : raw) {
            sig.append(line).append('\u0001');
        }
        String signature = (keepAll ? "A" : "B") + sig;
        if (signature.equals(this.lastSignature)) {
            return;
        }
        this.lastSignature = signature;
        long now = System.currentTimeMillis();
        int size = raw.size();
        if (this.discardStale) {
            this.discardStale = false;
            this.linesAll = new ArrayList<ChatLine>();
            this.prevTexts = new ArrayList<String>(raw);
            ArrayList<Long> stale = new ArrayList<Long>();
            for (int i = 0; i < size; ++i) {
                stale.add(0L);
            }
            this.prevTimes = stale;
            this.prevRawSize = size;
            this.lastSignature = signature;
            return;
        }
        int prevSize = this.prevRawSize;
        ArrayList<Long> times = new ArrayList<Long>();
        if (prevSize <= 0 || this.prevTexts.isEmpty()) {
            long span = (long)(Math.max(1.0f, ((Float)this.fadeSeconds.get()).floatValue()) * 1000.0f);
            for (int i = 0; i < size; ++i) {
                long age = (long)(size - 1 - i) * (span / (long)Math.max(1, size));
                times.add(now - Math.min(age, span - 1L));
            }
        } else {
            ArrayDeque<Long> q;
            HashMap<String, ArrayDeque<Long>> seen = new HashMap<String, ArrayDeque<Long>>();
            int prevCount = Math.min(this.prevTexts.size(), this.prevTimes.size());
            for (int i = 0; i < prevCount; ++i) {
                String t = this.prevTexts.get(i);
                if (t == null) continue;
                q = (ArrayDeque<Long>)seen.get(t);
                if (q == null) {
                    q = new ArrayDeque<Long>();
                    seen.put(t, q);
                }
                q.addLast(this.prevTimes.get(i));
            }
            for (int i = 0; i < size; ++i) {
                String text = raw.get(i);
                ArrayDeque<Long> arrayDeque = q = text == null ? null : (ArrayDeque<Long>)seen.get(text);
                if (q != null && !q.isEmpty()) {
                    times.add((Long)q.pollFirst());
                    continue;
                }
                times.add(now);
            }
        }
        this.prevTimes = times;
        this.prevTexts = new ArrayList<String>(raw);
        this.prevRawSize = size;
        ArrayList<ChatLine> rebuilt = new ArrayList<ChatLine>();
        for (int i = 0; i < size; ++i) {
            String text = raw.get(i);
            if (text == null || text.isEmpty()) continue;
            rebuilt.add(new ChatLine(text, times.get(i)));
        }
        this.linesAll = rebuilt;
    }

    private String styledText(Component content) {
        if (content == null) {
            return "";
        }
        if (!((Boolean)this.keepColors.get()).booleanValue()) {
            return content.getString();
        }
        try {
            return this.plain(content.getVisualOrderText());
        }
        catch (Throwable ignored) {
            return content.getString();
        }
    }

    private int readScrollPos(Minecraft mc) {
        try {
            ChatComponent chat = mc.gui.getChat();
            for (Class<?> c = chat.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    boolean nameHit;
                    if (field.getType() != Integer.TYPE) continue;
                    String name = field.getName().toLowerCase();
                    boolean bl = nameHit = name.contains("scrollbar") || name.contains("scroll");
                    if (!nameHit) continue;
                    field.setAccessible(true);
                    int v = field.getInt(chat);
                    if (v < 0) continue;
                    return v;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0;
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = (int)(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f);
        return a << 24 | rgb & 0xFFFFFF;
    }

    private List<String> readMessages(Minecraft mc) {
        ArrayList<String> out = new ArrayList<String>();
        try {
            ChatComponent chat = mc.gui.getChat();
            List best = null;
            block2: for (Class<?> c = chat.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    List list;
                    if (!List.class.isAssignableFrom(field.getType())) continue;
                    field.setAccessible(true);
                    Object value = field.get(chat);
                    if (!(value instanceof List) || (list = (List)value).isEmpty()) continue;
                    boolean hasComponent = false;
                    for (Object entry : list) {
                        if (entry == null || this.extractComponent(entry) == null) continue;
                        hasComponent = true;
                        break;
                    }
                    if (!hasComponent) continue;
                    best = list;
                    continue block2;
                }
            }
            if (best == null) {
                return out;
            }
            for (Object entry : best) {
                if (entry == null) continue;
                Component content = this.extractComponent(entry);
                if (content == null) {
                    out.add("");
                    continue;
                }
                String text = this.styledText(content);
                out.add(text == null ? "" : text);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return out;
    }

    private void drawInputBox(GuiGraphics graphics, Font font, EditBox input, float textX, float inputTop, float panelWidth, float fontScale, float inputAlpha) {
        int inBg = ChatoverlayModule.withAlpha((Integer)this.inputColor.get(), ((Float)this.inputAlpha.get()).floatValue() * inputAlpha);
        float inputWidth = panelWidth - 16.0f;
        ChatoverlayModule.drawRounded(graphics, textX, inputTop, inputWidth, 13.0f, 4.0f, inBg);
        String value = input.getValue();
        float textY = inputTop + (13.0f - (float)font.getHeight() * fontScale) / 2.0f;
        if (value != null && !value.isEmpty() && inputAlpha > 0.5f) {
            graphics.pose().pushPose();
            graphics.pose().translate(textX + 4.0f, textY, 0.0f);
            graphics.pose().scale(fontScale, fontScale, 1.0f);
            int color = this.textColor();
            if (((Boolean)this.textShadow.get()).booleanValue()) {
                font.drawStringWithShadow(graphics, value, 0.0, 0.0, color);
            } else {
                font.drawString(graphics, value, 0.0, 0.0, color);
            }
            graphics.flush();
            graphics.pose().popPose();
        }
        if (input.isFocused() && inputAlpha > 0.9f && System.currentTimeMillis() / 500L % 2L == 0L) {
            float maxCaret;
            float typed = value != null && !value.isEmpty() ? (float)font.getStringWidth(value) * fontScale : 0.0f;
            float caretX = textX + 4.0f + typed;
            if (caretX > (maxCaret = textX + inputWidth - 4.0f)) {
                caretX = maxCaret;
            }
            graphics.fill((int)caretX, (int)(inputTop + 3.0f), (int)caretX + 1, (int)(inputTop + 13.0f - 3.0f), -1777178);
        }
    }

    private static int styleRgb(Style style) {
        try {
            TextColor color = style.getColor();
            if (color != null) {
                return color.getValue() & 0xFFFFFF;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 0xFFFFFF;
    }

    private static void drawRounded(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
        if (width < 0.5f || height < 0.5f) {
            return;
        }
        graphics.flush();
        Shader.corners(graphics.pose().last().pose(), x, y, width, height, radius, radius, radius, radius, color);
    }

    private void drawLine(GuiGraphics graphics, Font font, String line, float textX, float ly, float fontScale) {
        graphics.pose().pushPose();
        graphics.pose().translate(textX, ly, 0.0f);
        graphics.pose().scale(fontScale, fontScale, 1.0f);
        int color = this.textColor();
        if (((Boolean)this.textShadow.get()).booleanValue()) {
            font.drawStringWithShadow(graphics, line, 0.0, 0.0, color);
        } else {
            font.drawString(graphics, line, 0.0, 0.0, color);
        }
        graphics.flush();
        graphics.pose().popPose();
    }

    private void drawHistory(GuiGraphics graphics, Font font, float textX, float historyTop, float historyBottom, float panelWidth, float fontScale, float rowHeight, int scrollPos) {
        int idx;
        float textWidth = panelWidth - 16.0f;
        ArrayList<String> wrapped = new ArrayList<String>();
        for (ChatLine line : this.lines) {
            String w = this.wrap(font, line.text, textWidth, fontScale);
            if (w.isEmpty()) continue;
            for (String part : w.split("\n", -1)) {
                wrapped.add(part);
            }
        }
        int maxRows = Math.max(1, (int)((historyBottom - historyTop) / rowHeight));
        this.lastTotalRows = wrapped.size();
        int total = wrapped.size();
        int shown = Math.min(Math.max(0, total - scrollPos), maxRows);
        float baseY = historyBottom - (float)shown * rowHeight;
        for (int row = 0; row < shown && (idx = row + scrollPos) < total; ++row) {
            String line = (String)wrapped.get(idx);
            if (line == null || line.isEmpty()) continue;
            float ly = baseY + (float)(shown - 1 - row) * rowHeight;
            this.drawLine(graphics, font, line, textX, ly, fontScale);
        }
    }

    private String plain(FormattedCharSequence sequence) {
        if (sequence == null) {
            return "";
        }
        boolean colors = (Boolean)this.keepColors.get();
        StringBuilder builder = new StringBuilder();
        int[] lastColor = new int[]{-1};
        ArrayList<int[]> segColors = new ArrayList<int[]>();
        ArrayList<String> segText = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        int curColor = -1;
        int[] curColorBox = new int[]{-1};
        sequence.accept((index, style, code) -> {
            int rgb;
            int n = rgb = colors && style != null ? ChatoverlayModule.styleRgb(style) : 0xFFFFFF;
            if (rgb != curColorBox[0]) {
                if (cur.length() > 0) {
                    segColors.add(new int[]{curColorBox[0]});
                    segText.add(cur.toString());
                    cur.setLength(0);
                }
                curColorBox[0] = rgb;
            }
            cur.appendCodePoint(code);
            return true;
        });
        if (cur.length() > 0) {
            segColors.add(new int[]{curColorBox[0]});
            segText.add(cur.toString());
        }
        if (!colors) {
            StringBuilder plainOut = new StringBuilder();
            for (String t : segText) {
                plainOut.append(t);
            }
            return plainOut.toString();
        }
        for (int i = 0; i < segText.size(); ++i) {
            int rgb = ((int[])segColors.get(i))[0];
            if (rgb != 0xFFFFFF) {
                builder.append('§').append('x');
                for (int shift = 20; shift >= 0; shift -= 4) {
                    builder.append('§').append(Integer.toHexString(rgb >> shift & 0xF));
                }
            } else {
                builder.append('§').append('r');
            }
            builder.append((String)segText.get(i));
        }
        return builder.toString();
    }

    @Override
    public void onDisable() {
        this.lines.clear();
        this.lastSignature = "";
        this.animatedHeight = -1.0f;
        this.animatedInput = 0.0f;
        this.animatedInputAlpha = 0.0f;
        this.lastFrameNanos = System.nanoTime();
        this.resetTracking();
    }

    @Override
    public void onEnable() {
        this.lines.clear();
        this.lastSignature = "";
        this.animatedHeight = -1.0f;
        this.animatedInput = 0.0f;
        this.animatedInputAlpha = 0.0f;
        this.lastFrameNanos = System.nanoTime();
    }

    private Component extractComponent(Object entry) {
        for (Class<?> c = entry.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field part : c.getDeclaredFields()) {
                if (!Component.class.isAssignableFrom(part.getType())) continue;
                try {
                    part.setAccessible(true);
                    Object value = part.get(entry);
                    if (!(value instanceof Component)) continue;
                    return (Component)value;
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    public boolean suppressesVanillaScreen() {
        return this.enabled;
    }

    public void onScroll(double delta) {
        if (delta == 0.0) {
            return;
        }
        int page = Math.max(1, this.lastPageRows);
        int total = Math.max(this.lastTotalRows, this.lines.size());
        int maxOffset = Math.max(0, total - page);
        int step = delta > 0.0 ? 1 : -1;
        this.scrollOffset = Math.max(0, Math.min(maxOffset, this.scrollOffset + step));
    }

    @Override
    public void onWorldChange() {
        this.lines.clear();
        this.linesAll.clear();
        this.lastSignature = "";
        this.scrollOffset = 0;
        this.wasChatting = false;
        this.discardStale = true;
        this.resetTracking();
    }

    private int textColor() {
        float b = Math.max(0.0f, Math.min(1.0f, ((Float)this.fontBrightness.get()).floatValue()));
        int r = (int)(255.0f * b);
        int g = (int)(255.0f * b);
        int bl = (int)(255.0f * b);
        if (b > 0.02f && b < 0.5f) {
            bl = Math.min(255, bl + (int)(18.0f * (1.0f - b * 2.0f)));
        }
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }

    private static final class ChatLine {
        final String text;
        final long addedAt;

        ChatLine(String text, long addedAt) {
            this.text = text;
            this.addedAt = addedAt;
        }
    }
}

