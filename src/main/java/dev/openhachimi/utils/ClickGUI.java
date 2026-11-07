/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import dev.openhachimi.config.ConfigLoadManager;
import dev.openhachimi.modules.Category;
import dev.openhachimi.modules.ClickguiModule;
import dev.openhachimi.modules.Module;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ColorSetting;
import dev.openhachimi.settings.DoubleSetting;
import dev.openhachimi.settings.EnumSetting;
import dev.openhachimi.settings.IntSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.settings.Setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.font.FontManager;
import dev.openhachimi.utils.render.font.TrueTypeFont;
import dev.openhachimi.utils.render.shader.KawaseBlur;
import dev.openhachimi.utils.render.shader.RoundRectRender;
import dev.openhachimi.utils.render.shader.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class ClickGUI {
    private static final byte[] S_BEAB3C7 = new byte[]{10, 35, 33, 42, 43, 47, 43};  // XOR0x42 "Hachimi"
    private static final byte[] S_41B175C = new byte[]{5, 46, 35, 49, 49};  // XOR0x42 "Glass"
    private static final byte[] S_77E0E98D = new byte[]{1, 35, 48, 32, 45, 44};  // XOR0x42 "Carbon"
    private static final byte[] S_132B8C87 = new byte[]{17, 35, 41, 55, 48, 35};  // XOR0x42 "Sakura"
    private static final byte[] S_1619D072 = new byte[]{1, 46, 35, 49, 49, 43, 33};  // XOR0x42 "Classic"
    private static final byte[] S_39E16AB3 = new byte[]{1, 46, 43, 33, 41, 5, 55, 43};  // XOR0x42 "ClickGui"
    private static final byte[] S_7C = new byte[]{62};
    private static final byte[] S_7049EF5 = new byte[]{62, 43, 33, 45, 44};  // XOR0x42 "|icon"
    private static final byte[] S_711D735B = new byte[]{20, 35, 44, 43, 46, 46, 35};  // XOR0x42 "Vanilla"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_48 = new byte[]{10};
    private static final byte[] S_20E0198D = new byte[]{15, 43, 44, 39, 33, 48, 35, 36, 54};  // XOR0x42 "Minecraft"
    private static final byte[] S_E8B6 = new byte[]{(byte)172, (byte)224, (byte)244};
    private static final byte[] S_5F = new byte[]{29};
    private static final byte[] S_7DE9A6C = new byte[]{15, 13, 6, 23, 14, 7};  // XOR0x42 "MODULE"
    private static final byte[] S_3482321 = new byte[]{7, 44, 35, 32, 46, 39, 38};  // XOR0x42 "Enabled"
    private static final byte[] S_4D2620F4 = new byte[]{54, 59, 50, 39, 108, 108, 108};  // XOR0x42 "type..."
    private static final byte[] S_B26E = new byte[]{108, 108, 108};  // XOR0x42 "..."
    private static final byte[] S_220CCEF0 = new byte[]{15, 43, 38, 44, 43, 37, 42, 54};  // XOR0x42 "Midnight"
    public static boolean isOpen = false;
    private static float anim = 0.0f;
    private static float height = 400.0f;
    private static float width = 560.0f;
    private static float railWidth = 118.0f;
    private static float bodyX0;
    private static float bodyY0;
    private static float bodyX1;
    private static float bodyY1;
    private static float x;
    private static float y;
    private static final FrameHelper.Draggable TOKEN;
    private static boolean dragging;
    private static float dragX;
    private static float dragY;
    public static Category currentCategory;
    private static float scrollY;
    private static float targetScrollY;
    private static Setting<?> draggingValue;
    private static float dragTrackX;
    private static float dragTrackW;
    private static Map<Category, Float> categoryAlphas;
    private static Map<Category, Float> categoryHovers;
    private static Map<Module, Float> hoverAlphas;
    private static boolean glassReady;
    public static boolean isSearching;
    public static String searchText;

    private static final float TEXT = 0.78f;
    private static final float TEXT_SMALL = 0.62f;
    private static final float SLIDER_START_RATIO = 0.42f;
    private static final float SLIDER_VALUE_SCALE = 0.7f;
    private static final float SLIDER_VALUE_MIN_WIDTH = 26.0f;
    public static final String[] THEMES;
    private static final Palette MIDNIGHT;
    private static final Palette GLASS;
    private static final Palette CARBON;
    private static final Palette SAKURA;
    private static final Palette CLASSIC;
    private static final HashSet<String> FALLBACK_SEEN;
    public static Setting<?> editingValue;
    public static Setting<?> bindingValue;
    public static String editBuffer;
    private static boolean editSelectAll;
    private static float editRowY;
    private static float editRowX;
    private static float editRowW;
    private static float editRowH;
    private static final int[] scissorScratch;

    private static float blurLevel() {
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module != null) {
            try {
                return ((Float)module.blur.get()).floatValue();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return 3.0f;
    }

    private static float glassOpacity() {
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module != null) {
            try {
                return ((Float)module.opacity.get()).floatValue();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return 1.0f;
    }

    private static float glassBrightness() {
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module != null) {
            try {
                return ((Float)module.brightness.get()).floatValue();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return 1.0f;
    }

    private static int[] readScissor() {
        if (!GL11.glIsEnabled((int)3089)) {
            ClickGUI.scissorScratch[0] = 0;
            ClickGUI.scissorScratch[1] = 0;
            ClickGUI.scissorScratch[2] = 0;
            ClickGUI.scissorScratch[3] = 0;
            return scissorScratch;
        }
        GL11.glGetIntegerv((int)3088, (int[])scissorScratch);
        return scissorScratch;
    }

    private static boolean clipToBody(float rx, float ry, float rw, float rh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        int scale = Math.max(1, (int)mc.getWindow().getGuiScale());
        int winH = mc.getWindow().getHeight();
        float bx0 = x;
        float bx1 = x + width;
        float by0 = bodyY1 > bodyY0 ? bodyY0 : y;
        float by1 = bodyY1 > bodyY0 ? bodyY1 : y + height;
        float x0 = Math.max(bx0, rx);
        float y0 = Math.max(by0, ry);
        float x1 = Math.min(bx1, rx + rw);
        float y1 = Math.min(by1, ry + rh);
        if (x1 <= x0 || y1 <= y0) {
            return false;
        }
        GL11.glEnable((int)3089);
        GL11.glScissor((int)((int)(x0 * (float)scale)), (int)(winH - (int)(y1 * (float)scale)), (int)Math.max(1, (int)((x1 - x0) * (float)scale)), (int)Math.max(1, (int)((y1 - y0) * (float)scale)));
        return true;
    }

    private static int toggleOffColor(float fade) {
        ClickguiModule module = ClickGUI.clickGuiModule();
        int rgb = 10134445;
        if (module != null) {
            try {
                rgb = module.toggleOffColor.rgb();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return ClickGUI.fade(0xFF000000 | rgb & 0xFFFFFF, fade);
    }

    private static void restoreScissor(boolean enabled, int[] box) {
        if (!enabled) {
            GL11.glDisable((int)3089);
            return;
        }
        GL11.glEnable((int)3089);
        GL11.glScissor((int)box[0], (int)box[1], (int)box[2], (int)box[3]);
    }

    private static boolean clipToPanel(float rx, float ry, float rw, float rh) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        int scale = Math.max(1, (int)mc.getWindow().getGuiScale());
        int winH = mc.getWindow().getHeight();
        float x0 = Math.max(x, rx);
        float y0 = Math.max(y, ry);
        float x1 = Math.min(x + width, rx + rw);
        float y1 = Math.min(y + height, ry + rh);
        if (x1 <= x0 || y1 <= y0) {
            return false;
        }
        GL11.glEnable((int)3089);
        GL11.glScissor((int)((int)(x0 * (float)scale)), (int)(winH - (int)(y1 * (float)scale)), (int)Math.max(1, (int)((x1 - x0) * (float)scale)), (int)Math.max(1, (int)((y1 - y0) * (float)scale)));
        return true;
    }

    private static float settingHeight(Setting<?> v, float colWidth) {
        boolean twoLines = ClickGUI.usesTwoLines(v, colWidth);
        if (ClickGUI.isSlider(v)) {
            return twoLines ? ClickGUI.fontPixel() * 2.0f + 15.0f : 22.0f;
        }
        if (v instanceof DoubleSetting) {
            return ClickGUI.fontPixel() + 12.0f;
        }
        return twoLines ? 32.0f : 20.0f;
    }

    private static float groupHeight(Module m, float colWidth) {
        float h = 35.0f;
        for (Setting<?> v : m.values) {
            if (!v.isVisible()) continue;
            h += ClickGUI.settingHeight(v, colWidth);
        }
        return h + 8.0f;
    }

    private static float sliderTrackWidth(Group group, String display, boolean twoLines) {
        float reserved = Math.max(ClickGUI.sliderValueWidth(display), 26.0f);
        float end = group.x + group.w - reserved - 10.0f;
        return Math.max(16.0f, end - ClickGUI.sliderTrackStart(group, twoLines));
    }

    private static float sliderTrackStart(Group group, boolean twoLines) {
        return twoLines ? group.x : group.x + group.w * 0.42f;
    }

    private static float sliderTrackY(float rowY, float rowHeight, boolean twoLines) {
        return twoLines ? rowY + ClickGUI.fontPixel() * 2.0f + 5.0f : rowY + (rowHeight - 3.0f) / 2.0f;
    }

    private static List<Group> layoutGroups(float contentX, float contentTop, float colWidth, float gap) {
        ArrayList<Group> groups = new ArrayList<Group>();
        float[] columnY = new float[]{0.0f, 0.0f};
        for (Module m : ClickGUI.visibleModules()) {
            float gh = ClickGUI.groupHeight(m, colWidth);
            int col = columnY[0] <= columnY[1] ? 0 : 1;
            groups.add(new Group(m, contentX + (float)col * (colWidth + gap), contentTop + columnY[col], colWidth, gh));
            int n = col;
            columnY[n] = columnY[n] + (gh + 10.0f);
        }
        return groups;
    }

    private static void rememberEditField(float x, float y, float w, float h) {
        editRowX = x;
        editRowY = y;
        editRowW = w;
        editRowH = h;
    }

    private static boolean overEditField(int mx, int my) {
        if (editRowW <= 0.0f) {
            return false;
        }
        return RenderUtil.isHovering(editRowX, editRowY, editRowW, editRowH, mx, my);
    }

    private static float contentHeight(List<Group> groups, float contentTop) {
        float bottom = contentTop;
        for (Group group : groups) {
            bottom = Math.max(bottom, group.y + group.h);
        }
        return bottom - contentTop;
    }

    private static void graphicsShell(GuiGraphics g, Palette p, float radius) {
        g.flush();
        if (ClickGUI.liquidGlass()) {
            KawaseBlur.prepare(ClickGUI.blurLevel());
            glassReady = KawaseBlur.panel(g.pose().last().pose(), x, y, width, height, radius, ClickGUI.rgba(0, 0xFFFFFF), anim, ClickGUI.glassOpacity(), ClickGUI.glassBrightness());
        } else {
            glassReady = false;
        }
        if (!glassReady) {
            RenderUtil.drawRoundedRect(g, x, y, width, height, radius, ClickGUI.fade(p.shell, anim));
            Shader.corners(g.pose().last().pose(), x, y, railWidth, height, radius, 0.0f, 0.0f, radius, ClickGUI.fade(p.rail, anim));
        }
        RenderUtil.drawRect(g, x + railWidth, y + 1.0f, 1.0f, height - 2.0f, ClickGUI.fade(p.hairline, anim));
    }

    private static boolean isCtrlDown() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey((long)window, (int)341) == 1 || GLFW.glfwGetKey((long)window, (int)345) == 1;
    }

    private static void applyEdit() {
        if (editingValue instanceof IntSetting) {
            ((IntSetting)editingValue).set(editBuffer);
        }
    }

    private static boolean liquidGlass() {
        return "Glass".equals(ClickGUI.themeName()) && KawaseBlur.isAvailable();
    }

    public static void commitEdit() {
        Setting<?> p = editingValue;
        if (p instanceof IntSetting) {
            ClickGUI.finishEdit();
            return;
        }
        String raw = editBuffer == null ? "" : editBuffer.trim().replace(',', '.');
        editingValue = null;
        editBuffer = "";
        if (p == null || raw.isEmpty()) {
            return;
        }
        try {
            if (p instanceof NumberSetting) {
                ((NumberSetting)p).setUnchecked(Float.valueOf(Float.parseFloat(raw)));
            } else if (p instanceof ListSetting) {
                ((ListSetting)p).setUnchecked(Integer.parseInt(raw));
            }
        }
        catch (Throwable throwable) {
            return;
        }
        try {
            ConfigLoadManager.getInstance().saveConfig();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    static boolean glassActive() {
        return ClickGUI.liquidGlass() && KawaseBlur.isReady();
    }

    private static String categoryDisplay(Category cat) {
        String name = cat.name();
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private static boolean glassControl(GuiGraphics g, float cx, float cy, float cw, float ch, float cr, float fade, int tint) {
        if (!ClickGUI.liquidGlass()) {
            return false;
        }
        g.flush();
        boolean previous = GL11.glIsEnabled((int)3089);
        int[] saved = ClickGUI.readScissor();
        boolean visible = ClickGUI.clipToBody(cx, cy, cw, ch);
        boolean drawn = visible && KawaseBlur.control(g.pose().last().pose(), cx, cy, cw, ch, cr, tint, fade, fade, ClickGUI.glassBrightness());
        ClickGUI.restoreScissor(previous, saved);
        return drawn;
    }

    private static int pillTint(int alpha, int baseTint) {
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module == null) {
            return alpha << 24 | baseTint & 0xFFFFFF;
        }
        float mul = 1.0f;
        int rgb = baseTint;
        try {
            mul = ((Float)module.pillAlpha.get()).floatValue();
            if (module.pillColor != null) {
                rgb = baseTint;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        int a = Math.max(0, Math.min(255, Math.round((float)alpha * mul)));
        return a << 24 | rgb & 0xFFFFFF;
    }

    private static int pillTint(int alpha) {
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module == null) {
            return alpha << 24 | 0xFFFFFF;
        }
        float mul = 1.0f;
        int rgb = 0xFFFFFF;
        try {
            mul = ((Float)module.pillAlpha.get()).floatValue();
            rgb = module.pillColor.rgb();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        int a = Math.max(0, Math.min(255, Math.round((float)alpha * mul)));
        return a << 24 | rgb & 0xFFFFFF;
    }

    private static int pillFallback(int base, int alpha) {
        int rgb;
        float mul;
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module == null) {
            return base;
        }
        try {
            mul = ((Float)module.pillAlpha.get()).floatValue();
            rgb = module.pillColor.rgb();
        }
        catch (Throwable ignored) {
            return base;
        }
        int a = Math.max(0, Math.min(255, Math.round((float)alpha * mul)));
        int br = base >> 16 & 0xFF;
        int bg = base >> 8 & 0xFF;
        int bb = base & 0xFF;
        int r = Math.round((float)br * 0.45f + (float)(rgb >> 16 & 0xFF) * 0.55f);
        int g = Math.round((float)bg * 0.45f + (float)(rgb >> 8 & 0xFF) * 0.55f);
        int b = Math.round((float)bb * 0.45f + (float)(rgb & 0xFF) * 0.55f);
        return a << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    static {
        x = 100.0f;
        y = 100.0f;
        TOKEN = new FrameHelper.Draggable(){};
        dragging = false;
        THEMES = new String[]{"Midnight", "Glass", "Carbon", "Sakura", "Classic"};
        MIDNIGHT = ClickGUI.midnightPalette();
        GLASS = ClickGUI.glassPalette();
        CARBON = ClickGUI.carbonPalette();
        SAKURA = ClickGUI.sakuraPalette();
        CLASSIC = ClickGUI.classicPalette();
        FALLBACK_SEEN = new HashSet();
        editingValue = null;
        bindingValue = null;
        editBuffer = "";
        editSelectAll = false;
        editRowY = 0.0f;
        editRowX = 0.0f;
        editRowW = 0.0f;
        editRowH = 0.0f;
        scissorScratch = new int[4];
        currentCategory = Category.COMBAT;
        scrollY = 0.0f;
        targetScrollY = 0.0f;
        draggingValue = null;
        categoryAlphas = new HashMap<Category, Float>();
        categoryHovers = new HashMap<Category, Float>();
        hoverAlphas = new HashMap<Module, Float>();
        isSearching = false;
        searchText = "";
    }

    private static Palette make(int accent, int shell, int rail, int rowHover, int inset, int track, int label, int text, int value, int placeholder, float radius) {
        return ClickGUI.make(accent, shell, rail, rowHover, inset, track, label, text, value, placeholder, radius, -592135, -526342, ClickGUI.rgba(16, 0xFFFFFF));
    }

    private static Palette make(int accent, int shell, int rail, int rowHover, int inset, int track, int label, int text, int value, int placeholder, float radius, int title, int knob, int hairline) {
        Palette p = new Palette();
        p.shell = shell;
        p.rail = rail;
        p.rowHover = rowHover;
        p.inset = inset;
        p.chip = ClickGUI.rgba(30, accent & 0xFFFFFF);
        p.accent = accent;
        p.accentSoft = ClickGUI.rgba(34, accent & 0xFFFFFF);
        p.toggleOff = ClickGUI.rgba(160, 0x1B222B);
        p.knob = knob;
        p.track = track;
        p.hairline = hairline;
        p.title = title;
        p.text = text;
        p.label = label;
        p.value = value;
        p.placeholder = placeholder;
        p.radius = radius;
        return p;
    }

    private static int mix(int a, int b, float t) {
        if (t <= 0.0f) {
            return a;
        }
        if (t >= 1.0f) {
            return b;
        }
        int aa = a >>> 24;
        int ba = b >>> 24;
        int ar = a >> 16 & 0xFF;
        int br = b >> 16 & 0xFF;
        int ag = a >> 8 & 0xFF;
        int bg = b >> 8 & 0xFF;
        int ab = a & 0xFF;
        int bb = b & 0xFF;
        return (int)((float)aa + (float)(ba - aa) * t) << 24 | (int)((float)ar + (float)(br - ar) * t) << 16 | (int)((float)ag + (float)(bg - ag) * t) << 8 | (int)((float)ab + (float)(bb - ab) * t);
    }

    private static void text(GuiGraphics g, Font font, String value, float x, float y, int color, float scale) {
        Font exact;
        if (value == null || value.isEmpty()) {
            return;
        }
        if (font == null || !FontManager.usable()) {
            float vanillaScale = scale * 1.35f;
            g.pose().pushPose();
            g.pose().translate(ClickGUI.snap(x), ClickGUI.snap(y), 0.0f);
            g.pose().scale(vanillaScale, vanillaScale, 1.0f);
            g.drawString(Minecraft.getInstance().font, value, 0, 0, color, false);
            g.pose().popPose();
            return;
        }
        float gs = ClickGUI.guiScale();
        float target = (float)font.getHeight() * scale * gs;
        int devicePx = ClickGUI.snapDevicePx(target);
        boolean textFont = font.getFont() != null && font.getFont().getFrom() < 4096;
        Font containerManager = exact = textFont ? FontManager.fontFor(devicePx) : null;
        if (exact != null) {
            float s = target / ((float)devicePx * gs);
            g.pose().pushPose();
            g.pose().translate(ClickGUI.snap(x), ClickGUI.snap(y), 0.0f);
            g.pose().scale(s, s, 1.0f);
            exact.drawString(g, value, 0.0, 0.0, color, false);
            g.flush();
            g.pose().popPose();
            return;
        }
        ClickGUI.noteFallback(devicePx, textFont);
        g.pose().pushPose();
        g.pose().translate(ClickGUI.snap(x), ClickGUI.snap(y), 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        font.drawString(g, value, 0.0, 0.0, color, false);
        g.flush();
        g.pose().popPose();
    }

    private static int dim(int color, float factor) {
        if (factor >= 1.0f) {
            return color;
        }
        if (factor < 0.0f) {
            factor = 0.0f;
        }
        int a = color >>> 24;
        int r = Math.round((float)(color >> 16 & 0xFF) * factor);
        int g = Math.round((float)(color >> 8 & 0xFF) * factor);
        int b = Math.round((float)(color & 0xFF) * factor);
        return a << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    private static ClickguiModule guiModule() {
        return ClickGUI.clickGuiModule();
    }

    private static Palette carbonPalette() {
        return ClickGUI.make(-19641, -435089135, -1944841196, 0x1E1B1B1B, -938010857, -13619152, -7960954, -1315861, -19641, -10329502, 9.0f);
    }

    private static float settingFloat(NumberSetting setting, float fallback) {
        if (setting == null) {
            return fallback;
        }
        try {
            return ((Float)setting.get()).floatValue();
        }
        catch (Throwable ignored) {
            return fallback;
        }
    }

    private static ClickguiModule clickGuiModule() {
        try {
            Module module = ModuleManager.getModule("ClickGui");
            if (module instanceof ClickguiModule) {
                return (ClickguiModule)module;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static int chromeTint(Palette p) {
        int base = p.rail;
        int a = base >>> 24;
        int r = base >> 16 & 0xFF;
        int g = base >> 8 & 0xFF;
        int b = base & 0xFF;
        int luma = (r * 299 + g * 587 + b * 114) / 1000;
        int tint = luma > 140 ? ClickGUI.rgba(a, 14212580) : ClickGUI.rgba(a, 2764598);
        return tint;
    }

    private static float logoBackBrightness() {
        ClickguiModule module = ClickGUI.guiModule();
        return module == null ? 1.0f : ClickGUI.settingFloat(module.logoBackBrightness, 1.0f);
    }

    private static float categoryBrightness() {
        ClickguiModule module = ClickGUI.guiModule();
        return module == null ? 1.0f : ClickGUI.settingFloat(module.categoryBrightness, 1.0f);
    }

    private static float chipBrightness() {
        ClickguiModule module = ClickGUI.guiModule();
        return module == null ? 1.0f : ClickGUI.settingFloat(module.pillBrightness, 1.0f);
    }

    private static Palette glassPalette() {
        return ClickGUI.make(-14983522, -219155208, -1337306527, 709513825, -420219143, -7826527, -14998996, -16118252, -15459806, -10590092, 11.0f, -16447476, -1, 0x22FFFFFF);
    }

    private static Palette classicPalette() {
        return ClickGUI.make(-12743681, -435023338, -1944841191, 505092897, -938010852, -13816523, -8487287, -1907990, -12743681, -10987422, 9.0f);
    }

    private static float logoBrightness() {
        ClickguiModule module = ClickGUI.guiModule();
        return module == null ? 1.0f : ClickGUI.settingFloat(module.logoBrightness, 1.0f);
    }

    private static int scaleTint(int color, float factor) {
        if (Math.abs(factor - 1.0f) < 0.001f) {
            return color;
        }
        int a = color >>> 24;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        if (factor < 1.0f) {
            r = Math.round((float)r * factor);
            g = Math.round((float)g * factor);
            b = Math.round((float)b * factor);
        } else {
            float add = factor - 1.0f;
            r = Math.round((float)r + (float)(255 - r) * add);
            g = Math.round((float)g + (float)(255 - g) * add);
            b = Math.round((float)b + (float)(255 - b) * add);
        }
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static String themeName() {
        ClickguiModule module = ClickGUI.clickGuiModule();
        if (module != null) {
            try {
                return module.theme.getDisplay();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return THEMES[0];
    }

    private static Palette sakuraPalette() {
        return ClickGUI.make(-25916, -434761190, -1944513505, 505420328, -937749213, -13095870, -7043940, -858896, -25916, -9675146, 11.0f);
    }

    public static int brighten(int color, float factor) {
        if (factor <= 1.0f) {
            return color;
        }
        int a = color >>> 24;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        float peak = (float)Math.max(r, Math.max(g, b)) * factor;
        float scale = peak > 255.0f ? 255.0f / (float)Math.max(r, Math.max(g, b)) : factor;
        r = Math.min(255, Math.round((float)r * scale));
        g = Math.min(255, Math.round((float)g * scale));
        b = Math.min(255, Math.round((float)b * scale));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int snapDevicePx(float target) {
        int px = Math.round(target / 4.0f) * 4;
        if (px < 8) {
            px = 8;
        }
        if (px > 32) {
            px = 32;
        }
        return px;
    }

    private static float textWidth(Font font, String value, float scale) {
        Font exact;
        if (font == null || value == null) {
            return 0.0f;
        }
        float gs = ClickGUI.guiScale();
        float target = (float)font.getHeight() * scale * gs;
        int devicePx = ClickGUI.snapDevicePx(target);
        boolean textFont = font.getFont() != null && font.getFont().getFrom() < 4096;
        Font containerManager = exact = textFont ? FontManager.fontFor(devicePx) : null;
        if (exact != null) {
            float s = target / ((float)devicePx * gs);
            return (float)exact.getStringWidth(value) * s;
        }
        return (float)font.getStringWidth(value) * scale;
    }

    private static void noteFallback(int devicePx, boolean textFont) {
        String tag = devicePx + (textFont ? "|" : "|icon");
        if (FALLBACK_SEEN.add(tag)) {
            FontManager.noteFallback(tag);
        }
    }

    private static void glyphCentered(GuiGraphics g, Font font, String value, float centerX, float centerY, int color, float scale) {
        boolean vanilla;
        if (font == null || value == null || value.isEmpty()) {
            return;
        }
        boolean bl = vanilla = ModuleManager.getModule(ClickguiModule.class) != null && ModuleManager.getModule(ClickguiModule.class).fontMode.isMode("Vanilla");
        if (vanilla) {
            ClickGUI.text(g, font, value, centerX - ClickGUI.textWidth(font, value, scale) / 2.0f, centerY - ClickGUI.fontPixel() * scale / 2.0f, color, scale);
            return;
        }
        TrueTypeFont core = font.getFont();
        KinematicManager[] data = core.getCharData();
        int index = value.charAt(0) - core.getFrom();
        if (data == null || index < 0 || index >= data.length || data[index] == null) {
            ClickGUI.text(g, font, value, centerX - ClickGUI.textWidth(font, value, scale) / 2.0f, centerY - (float)font.getHeight() * scale / 2.0f, color, scale);
            return;
        }
        KinematicManager metrics = data[index];
        float inkCenterX = (metrics.x0 + metrics.x1) * 0.5f;
        float inkCenterY = core.getAscent() * core.getScale() + (metrics.y0 + metrics.y1) * 0.5f;
        ClickGUI.text(g, font, value, centerX - inkCenterX * scale, centerY - inkCenterY * scale, color, scale);
    }

    private static float fontPixel() {
        return FontManager.smallFont != null ? (float) FontManager.smallFont.getHeight() * 0.78f : 12.0f;
    }

    private static boolean isSlider(Setting<?> v) {
        return v instanceof NumberSetting || v instanceof ListSetting;
    }

    private static float labelLimit(Setting<?> v, float colWidth) {
        return ClickGUI.isSlider(v) ? colWidth * 0.42f - 8.0f : colWidth - 78.0f;
    }

    private static boolean usesTwoLines(Setting<?> v, float colWidth) {
        if (v instanceof DoubleSetting) {
            return false;
        }
        return FontManager.smallFont != null && ClickGUI.textWidth(FontManager.smallFont, v.name, 0.78f) > ClickGUI.labelLimit(v, colWidth);
    }

    private static List<Module> visibleModules() {
        ArrayList<Module> list = new ArrayList<Module>();
        for (Module m : ModuleManager.modules) {
            if (searchText.isEmpty() && m.category != currentCategory || !searchText.isEmpty() && !m.name.toLowerCase().contains(searchText.toLowerCase())) continue;
            list.add(m);
        }
        return list;
    }

    private static float sliderValueWidth(String display) {
        return ClickGUI.textWidth(FontManager.smallFont, display, 0.7f);
    }

    private static Palette midnightPalette() {
        return ClickGUI.make(-7561729, -435154923, -1944972776, 505553452, -938076644, -13421762, -7039840, -1184270, -7561729, -10461074, 11.0f);
    }

    private static int rgba(int alpha, int rgb) {
        if (alpha < 0) {
            alpha = 0;
        }
        if (alpha > 255) {
            alpha = 255;
        }
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    private static float snap(float value) {
        float scale;
        Minecraft mc = Minecraft.getInstance();
        float f = scale = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScale() : 1.0f;
        if (scale < 0.001f) {
            scale = 1.0f;
        }
        return (float)((double)Math.round(value * scale) / (double)scale);
    }

    private static int fade(int color, float factor) {
        if (factor < 0.0f) {
            factor = 0.0f;
        }
        if (factor > 1.0f) {
            factor = 1.0f;
        }
        int alpha = (int)((float)(color >>> 24) * factor);
        return alpha << 24 | color & 0xFFFFFF;
    }

    private static float guiScale() {
        Minecraft mc = Minecraft.getInstance();
        float s = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScale() : 1.0f;
        return s < 0.001f ? 1.0f : s;
    }

    private static Palette palette() {
        String name = ClickGUI.themeName();
        if (name == null) {
            return MIDNIGHT;
        }
        if (name.equals("Glass")) {
            return GLASS;
        }
        if (name.equals("Carbon")) {
            return CARBON;
        }
        if (name.equals("Sakura")) {
            return SAKURA;
        }
        if (name.equals("Classic")) {
            return CLASSIC;
        }
        return MIDNIGHT;
    }

    private static boolean glassControlInHeader(GuiGraphics g, float cx, float cy, float cw, float ch, float cr, float fade, int tint) {
        if (!ClickGUI.liquidGlass()) {
            return false;
        }
        g.flush();
        boolean previous = GL11.glIsEnabled((int)3089);
        int[] saved = ClickGUI.readScissor();
        boolean visible = ClickGUI.clipToPanel(cx, cy, cw, ch);
        boolean drawn = visible && KawaseBlur.control(g.pose().last().pose(), cx, cy, cw, ch, cr, tint, fade, fade, ClickGUI.glassBrightness());
        ClickGUI.restoreScissor(previous, saved);
        return drawn;
    }

    private static boolean clickedSearchIconZone(int mx, int my) {
        return RenderUtil.isHovering(x + width - 30.0f - 4.0f, y + 12.0f, 26.0f, 24.0f, mx, my);
    }

    public static void handleMouseReleased(int button, int mx, int my) {
        dragging = false;
        draggingValue = null;
    }

    public static boolean handleMousePressed(int button, int mx, int my) {
        float strW; // declaration lost by CFR
        if (!isOpen) {
            return false;
        }
        bindingValue = null;
        if (editingValue != null && !ClickGUI.overEditField(mx, my)) {
            ClickGUI.finishEdit();
        }
        if (button == 0) {
            float searchIconX = x + width - 30.0f;
            if (RenderUtil.isHovering(searchIconX - 4.0f, y + 12.0f, 26.0f, 24.0f, mx, my)) {
                boolean bl = isSearching = !isSearching;
                if (!isSearching) {
                    searchText = "";
                }
                return true;
            }
            if (isSearching) {
                float fieldW = 200.0f;
                float fieldX = x + width - 14.0f - fieldW;
                int clickedField = RenderUtil.isHovering(fieldX, y + 8.0f, fieldW, 26.0f, mx, my) ? 1 : 0;
                isSearching = clickedField != 0;
                if (ClickGUI.clickedSearchIconZone(mx, my) || clickedField != 0) {
                    return true;
                }
            }
            if (RenderUtil.isHovering(x, y, width, 52.0f, mx, my)) {
                FrameHelper.offer(TOKEN, x, y, width, 52.0f, 100);
                FrameHelper.updateMouse(mx, my, true);
                FrameHelper.commitForFrame();
                dragging = true;
                dragX = (float)mx - x;
                dragY = (float)my - y;
                return true;
            }
            float railY = y + 74.0f;
            for (Category cat : Category.values()) {
                if (RenderUtil.isHovering(x + 6.0f, railY, railWidth - 12.0f, 22.0f, mx, my)) {
                    currentCategory = cat;
                    targetScrollY = 0.0f;
                    scrollY = 0.0f;
                    return true;
                }
                railY += 24.0f;
            }
            float contentX = x + railWidth + 14.0f;
            float colW = (width - railWidth - 42.0f) / 2.0f;
            List<Group> groups = ClickGUI.layoutGroups(contentX, y + 66.0f + scrollY, colW, 14.0f);
            for (Group group : groups) {
                float rowY = group.y + 15.0f;
                if (RenderUtil.isHovering(group.x, rowY, group.w, 20.0f, mx, my)) {
                    for (Setting<?> v : group.module.values) {
                        float toggleX;
                        if (!(v instanceof BooleanSetting) || !RenderUtil.isHovering((toggleX = group.x + group.w - 22.0f) - 4.0f, rowY, 26.0f, 20.0f, mx, my)) continue;
                        group.module.toggle();
                        return true;
                    }
                    group.module.toggle();
                    return true;
                }
                rowY += 20.0f;
                for (Setting<?> v : group.module.values) {
                    if (!v.isVisible()) continue;
                    float sHeight = ClickGUI.settingHeight(v, group.w);
                    boolean twoLines = ClickGUI.usesTwoLines(v, group.w);
                    if (v instanceof BooleanSetting) {
                        float toggleX = group.x + group.w - 22.0f;
                        float toggleY = rowY + (twoLines ? ClickGUI.fontPixel() + 3.0f : (sHeight - 11.0f) / 2.0f);
                        if (RenderUtil.isHovering(toggleX - 4.0f, rowY, 26.0f, sHeight, mx, my)) {
                            v.click(mx, my, (int)group.x, (int)group.w);
                            return true;
                        }
                    } else if (v instanceof IntSetting) {
                        float fieldW = Math.min(96.0f, group.w);
                        if (RenderUtil.isHovering(group.x + group.w - fieldW, rowY, fieldW, sHeight, mx, my)) {
                            editingValue = v;
                            editBuffer = ((IntSetting)v).getDisplay();
                            editSelectAll = false;
                            ClickGUI.rememberEditField(group.x + group.w - fieldW, rowY, fieldW, sHeight);
                            return true;
                        }
                    } else if (v instanceof DoubleSetting) {
                        if (RenderUtil.isHovering(group.x, rowY, group.w, sHeight, mx, my)) {
                            v.click(mx, my, (int)group.x, (int)group.w);
                            return true;
                        }
                    } else if (v instanceof ColorSetting) {
                        float dot;
                        String display = ((ColorSetting)v).getDisplay();
                        strW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                        float chipW = Math.min(strW + (dot = ClickGUI.fontPixel()) + 18.0f, group.w);
                        if (RenderUtil.isHovering(group.x + group.w - chipW, rowY, chipW, sHeight, mx, my)) {
                            v.click(mx, my, (int)group.x, (int)group.w);
                            return true;
                        }
                    } else if (v instanceof ModeSetting) {
                        String display = ((ModeSetting)v).getDisplay();
                        strW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                        if (RenderUtil.isHovering(group.x + group.w - strW - 10.0f, rowY, strW + 14.0f, sHeight, mx, my)) {
                            v.click(mx, my, (int)group.x, (int)group.w);
                            return true;
                        }
                    } else if (v instanceof EnumSetting) {
                        String display = ((EnumSetting)v).getDisplay();
                        strW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                        float chipW = Math.min(strW + 10.0f, group.w);
                        if (RenderUtil.isHovering(group.x + group.w - chipW, rowY, chipW, sHeight, mx, my)) {
                            bindingValue = v;
                            return true;
                        }
                    } else if (v instanceof NumberSetting || v instanceof ListSetting) {
                        String display = v instanceof NumberSetting ? ((NumberSetting)v).getDisplay() : ((ListSetting)v).getDisplay();
                        strW = ClickGUI.sliderValueWidth(display);
                        float trackX = ClickGUI.sliderTrackStart(group, twoLines);
                        float trackW = ClickGUI.sliderTrackWidth(group, display, twoLines);
                        float trackY = ClickGUI.sliderTrackY(rowY, sHeight, twoLines);
                        if ((float)mx >= group.x + group.w - strW - 8.0f && (float)mx <= group.x + group.w && (float)my >= rowY && (float)my <= rowY + sHeight) {
                            editingValue = v;
                            editBuffer = display;
                            return true;
                        }
                        if (RenderUtil.isHovering(trackX - 4.0f, trackY - 4.0f, trackW + 8.0f, 11.0f, mx, my)) {
                            draggingValue = v;
                            dragTrackX = trackX;
                            dragTrackW = trackW;
                            if (v instanceof NumberSetting) {
                                ((NumberSetting)v).setByMouse(mx, (int)trackX, (int)trackW);
                            }
                            if (v instanceof ListSetting) {
                                ((ListSetting)v).setByMouse(mx, (int)trackX, (int)trackW);
                            }
                            return true;
                        }
                    }
                    rowY += sHeight;
                }
            }
        }
        return false;
    }

    public static void finishEdit() {
        bindingValue = null;
        editSelectAll = false;
        if (editingValue == null) {
            editBuffer = "";
            editRowW = 0.0f;
            return;
        }
        if (editingValue instanceof IntSetting) {
            ((IntSetting)editingValue).set(editBuffer);
            editingValue = null;
            editBuffer = "";
            editRowW = 0.0f;
            try {
                ConfigLoadManager.getInstance().saveConfig();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            return;
        }
        ClickGUI.commitEdit();
    }

    public static void handleScroll(double d, int mx, int my) {
        if (RenderUtil.isHovering(x + railWidth, y, width - railWidth, height, mx, my)) {
            targetScrollY += (float)(d * 26.0);
        }
    }

    public static boolean onCharTyped(char codePoint) {
        if (editingValue != null) {
            if (editingValue instanceof IntSetting) {
                IntSetting sv = (IntSetting)editingValue;
                if (!Character.isISOControl(codePoint)) {
                    if (editSelectAll) {
                        editBuffer = "";
                        editSelectAll = false;
                    }
                    if (editBuffer.length() < sv.maxLength) {
                        editBuffer = editBuffer + codePoint;
                        ClickGUI.applyEdit();
                    }
                }
                return true;
            }
            if (Character.isDigit(codePoint) || codePoint == '.' || codePoint == '-' || codePoint == ',') {
                editBuffer = editBuffer + codePoint;
            }
            return true;
        }
        if (isSearching) {
            searchText = searchText + codePoint;
            return true;
        }
        return false;
    }

    private static void toggle(GuiGraphics g, float tx, float ty, float alphaMul, float onOffset, float onAlpha, Palette p) {
        RenderUtil.drawRoundedRect(g, tx, ty, 20.0f, 11.0f, 5.5f, ClickGUI.toggleOffColor(alphaMul));
        if (onAlpha > 0.01f) {
            RenderUtil.drawRoundedRect(g, tx, ty, 20.0f, 11.0f, 5.5f, ClickGUI.fade(p.accent, alphaMul * onAlpha));
        }
        RenderUtil.drawRoundedRect(g, ClickGUI.snap(tx + onOffset * 0.75f), ClickGUI.snap(ty + 1.0f), 9.0f, 9.0f, 4.5f, ClickGUI.fade(p.knob, alphaMul));
    }

    public static void onRender2D(GuiGraphics g, int mx, int my) {
        float strW, chipW, chipH, valueY; // declarations lost by CFR
        RenderUtil.updateDeltaTime();
        FontManager.init();
        anim = RenderUtil.animateSmooth(anim, isOpen ? 1.0f : 0.0f, 15.0f);
        if (anim < 0.01f) {
            return;
        }
        if (dragging) {
            x = (float)mx - dragX;
            y = (float)my - dragY;
        }
        if (draggingValue != null && dragTrackW > 0.0f) {
            if (draggingValue instanceof NumberSetting) {
                ((NumberSetting)draggingValue).setByMouse(mx, (int)dragTrackX, (int)dragTrackW);
            } else if (draggingValue instanceof ListSetting) {
                ((ListSetting)draggingValue).setByMouse(mx, (int)dragTrackX, (int)dragTrackW);
            }
        }
        scrollY = RenderUtil.animateSmooth(scrollY, targetScrollY, 15.0f);
        Palette p = ClickGUI.palette();
        float radius = p.radius;
        bodyX0 = x + railWidth + 1.0f;
        bodyY0 = y + 62.0f;
        bodyX1 = x + width - 1.0f;
        bodyY1 = y + height - 2.0f;
        ClickGUI.graphicsShell(g, p, radius);
        int badge = ClickGUI.scaleTint(ClickGUI.chromeTint(p), ClickGUI.logoBackBrightness());
        if (ClickGUI.liquidGlass()) {
            if (!ClickGUI.glassActive() || !ClickGUI.glassControl(g, x + 14.0f, y + 12.0f, 24.0f, 24.0f, 12.0f, anim, ClickGUI.pillTint(96))) {
                RenderUtil.drawRoundedRect(g, x + 14.0f, y + 12.0f, 24.0f, 24.0f, 12.0f, ClickGUI.fade(badge, anim));
            }
        } else {
            RenderUtil.drawRoundedRect(g, x + 14.0f, y + 12.0f, 24.0f, 24.0f, 12.0f, ClickGUI.fade(badge, anim));
        }
        ClickGUI.glyphCentered(g, FontManager.smallFont, "H", x + 26.0f, y + 24.0f, ClickGUI.fade(ClickGUI.scaleTint(p.title, ClickGUI.logoBrightness()), anim), 0.9f);
        ClickGUI.text(g, FontManager.regular, "Hachimi", x + 46.0f, y + 10.0f, ClickGUI.fade(p.title, anim), 0.82f);
        ClickGUI.text(g, FontManager.smallFont, "Minecraft", x + 46.0f, y + 31.0f, ClickGUI.fade(p.label, anim), 0.6f);
        float searchIconX = x + width - 30.0f;
        int searchColor = isSearching ? p.accent : p.label;
        ClickGUI.text(g, FontManager.materialIcon20, "", searchIconX, y + 15.0f, ClickGUI.fade(searchColor, anim), 0.85f);
        if (isSearching) {
            float fieldW = 200.0f;
            float fieldX = x + width - 14.0f - fieldW;
            if (!ClickGUI.glassControlInHeader(g, fieldX, y + 8.0f, fieldW, 26.0f, 13.0f, anim, ClickGUI.pillTint(96))) {
                RenderUtil.drawRoundedRect(g, fieldX, y + 8.0f, fieldW, 26.0f, 13.0f, ClickGUI.fade(p.inset, anim));
            }
            String displaySearch = searchText;
            if (System.currentTimeMillis() % 1000L > 500L) {
                displaySearch = displaySearch + "_";
            }
            ClickGUI.text(g, FontManager.regular, displaySearch, fieldX + 10.0f, y + 13.0f, ClickGUI.fade(p.value, anim), 0.72f);
        }
        ClickGUI.text(g, FontManager.smallFont, "MODULE", x + 14.0f, y + 56.0f, ClickGUI.fade(ClickGUI.mix(p.label, p.label, 0.0f), anim * 0.8f), 0.55f);
        float railY = y + 74.0f;
        for (Category cat : Category.values()) {
            boolean selected = cat == currentCategory;
            float target = selected ? 1.0f : 0.0f;
            float current = categoryAlphas.getOrDefault((Object)cat, Float.valueOf(0.0f)).floatValue();
            current = RenderUtil.animateSmooth(current, target, 10.0f);
            categoryAlphas.put(cat, Float.valueOf(current));
            boolean hovered = RenderUtil.isHovering(x + 6.0f, railY, railWidth - 12.0f, 22.0f, mx, my);
            float hoverTarget = hovered && !selected ? 1.0f : 0.0f;
            float hover = categoryHovers.getOrDefault((Object)cat, Float.valueOf(0.0f)).floatValue();
            hover = RenderUtil.animateSmooth(hover, hoverTarget, 12.0f);
            categoryHovers.put(cat, Float.valueOf(hover));
            if (ClickGUI.liquidGlass()) {
                int catGlass = ClickGUI.scaleTint(ClickGUI.chromeTint(p), ClickGUI.categoryBrightness());
                if ((current > 0.01f || hover > 0.01f) && !ClickGUI.glassControl(g, x + 6.0f, railY, railWidth - 12.0f, 22.0f, 7.0f, Math.max(current, hover) * anim, ClickGUI.pillTint(110, catGlass))) {
                    RenderUtil.drawRoundedRect(g, x + 6.0f, railY, railWidth - 12.0f, 22.0f, 7.0f, ClickGUI.fade(catGlass, Math.max(current, hover) * anim));
                }
                if (current > 0.01f) {
                    RenderUtil.drawRoundedRect(g, x + 6.0f, railY + 4.0f, 2.0f, 14.0f, 1.0f, ClickGUI.fade(p.accent, current * anim));
                }
            } else {
                int catTint = ClickGUI.scaleTint(ClickGUI.chromeTint(p), ClickGUI.categoryBrightness());
                if (current > 0.01f) {
                    RenderUtil.drawRoundedRect(g, x + 6.0f, railY, railWidth - 12.0f, 22.0f, 7.0f, ClickGUI.fade(catTint, current * anim));
                } else if (hover > 0.01f) {
                    RenderUtil.drawRoundedRect(g, x + 6.0f, railY, railWidth - 12.0f, 22.0f, 7.0f, ClickGUI.fade(catTint, hover * 0.62f * anim));
                }
            }
            int nameColor = ClickGUI.brighten(selected ? p.title : ClickGUI.mix(p.label, p.text, hover), 3.0f);
            ClickGUI.text(g, FontManager.smallFont, ClickGUI.categoryDisplay(cat), x + 16.0f, railY + (22.0f - ClickGUI.fontPixel()) / 2.0f, ClickGUI.fade(nameColor, anim), 0.78f);
            railY += 24.0f;
        }
        Minecraft mc = Minecraft.getInstance();
        float avatarY = y + height - 40.0f;
        float avatarSize = 24.0f;
        float avatarRadius = 7.0f;
        RenderUtil.drawRoundedRect(g, x + 14.0f, avatarY, 24.0f, 24.0f, 7.0f, ClickGUI.fade(ClickGUI.rgba(40, 0xFFFFFF), anim));
        if (mc != null && mc.player instanceof AbstractClientPlayer) {
            g.flush();
            ResourceLocation skin = mc.player.getSkinTextureLocation();
            Matrix4f avatarPose = g.pose().last().pose();
            RoundRectRender.roundedTexture(avatarPose, skin, x + 15.0f, avatarY + 1.0f, 22.0f, 6.0f, 0.125f, 0.125f, 0.125f, 1.0f, 1.0f, 1.0f, 1.0f);
            RoundRectRender.roundedTexture(avatarPose, skin, x + 15.0f, avatarY + 1.0f, 22.0f, 6.0f, 0.625f, 0.125f, 0.125f, 1.0f, 1.0f, 1.0f, 1.0f);
        }
        if (mc != null && mc.getUser() != null) {
            ClickGUI.text(g, FontManager.smallFont, mc.getUser().getName(), x + 44.0f, avatarY + (24.0f - ClickGUI.fontPixel() * 0.72f) / 2.0f, ClickGUI.fade(p.text, anim), 0.72f);
        }
        RenderUtil.drawRect(g, x + railWidth, y + 1.0f, 1.0f, height - 2.0f, ClickGUI.fade(p.hairline, anim));
        g.enableScissor((int)bodyX0, (int)bodyY0, (int)bodyX1, (int)bodyY1);
        float contentX = x + railWidth + 14.0f;
        float colW = (width - railWidth - 42.0f) / 2.0f;
        List<Group> groups = ClickGUI.layoutGroups(contentX, y + 66.0f + scrollY, colW, 14.0f);
        for (Group group : groups) {
            float hoverTarget = RenderUtil.isHovering(group.x, group.y, group.w, group.h, mx, my) ? 1.0f : 0.0f;
            Float prevHover = hoverAlphas.get(group.module);
            float hover = RenderUtil.animateSmooth(prevHover == null ? 0.0f : prevHover.floatValue(), hoverTarget, 14.0f);
            hoverAlphas.put(group.module, Float.valueOf(hover));
            String header = group.module.name.toUpperCase();
            int headerColor = ClickGUI.mix(p.label, p.placeholder, 0.55f);
            ClickGUI.text(g, FontManager.smallFont, header, group.x, group.y + 2.0f, ClickGUI.fade(headerColor, anim), 0.62f);
            float rowY = group.y + 15.0f;
            int rowColor = ClickGUI.fade(ClickGUI.rgba((int)(10.0f * hover), 0xFFFFFF), anim);
            if (hover > 0.02f) {
                RenderUtil.drawRoundedRect(g, group.x - 4.0f, group.y - 2.0f, group.w + 8.0f, group.h + 4.0f, 8.0f, rowColor);
            }
            group.module.animOffset = RenderUtil.animateSmooth(group.module.animOffset, group.module.enabled ? 12.0f : 0.0f, 15.0f);
            group.module.animAlpha = RenderUtil.animateSmooth(group.module.animAlpha, group.module.enabled ? 1.0f : 0.0f, 10.0f);
            ClickGUI.text(g, FontManager.smallFont, "Enabled", group.x, rowY + (20.0f - ClickGUI.fontPixel()) / 2.0f, ClickGUI.fade(group.module.enabled ? p.title : p.label, anim), 0.78f);
            ClickGUI.toggle(g, group.x + group.w - 22.0f, rowY + 4.5f, anim, group.module.animOffset, group.module.animAlpha, p);
            rowY += 20.0f;
            for (Setting<?> v : group.module.values) {
                if (!v.isVisible()) continue;
                float sHeight = ClickGUI.settingHeight(v, group.w);
                boolean twoLines = ClickGUI.usesTwoLines(v, group.w);
                float alphaMul = anim;
                if (!(v instanceof DoubleSetting)) {
                    if (twoLines) {
                        int splitIdx = v.name.lastIndexOf(32);
                        if (splitIdx == -1) {
                            splitIdx = v.name.length() / 2;
                        }
                        ClickGUI.text(g, FontManager.smallFont, v.name.substring(0, splitIdx), group.x, rowY + 1.0f, ClickGUI.fade(p.label, alphaMul), 0.78f);
                        ClickGUI.text(g, FontManager.smallFont, v.name.substring(splitIdx).trim(), group.x, rowY + 1.0f + ClickGUI.fontPixel() + 1.0f, ClickGUI.fade(p.label, alphaMul), 0.78f);
                    } else {
                        ClickGUI.text(g, FontManager.smallFont, v.name, group.x, rowY + (20.0f - ClickGUI.fontPixel()) / 2.0f, ClickGUI.fade(p.label, alphaMul), 0.78f);
                    }
                }
                if (v instanceof BooleanSetting) {
                    BooleanSetting bv = (BooleanSetting)v;
                    bv.animOffset = RenderUtil.animateSmooth(bv.animOffset, (Boolean)bv.get() != false ? 12.0f : 0.0f, 15.0f);
                    bv.animAlpha = RenderUtil.animateSmooth(bv.animAlpha, (Boolean)bv.get() != false ? 1.0f : 0.0f, 10.0f);
                    float toggleY = rowY + (twoLines ? ClickGUI.fontPixel() + 3.0f : (sHeight - 11.0f) / 2.0f);
                    ClickGUI.toggle(g, group.x + group.w - 22.0f, toggleY, alphaMul, bv.animOffset, bv.animAlpha, p);
                } else if (v instanceof IntSetting) {
                    float shownScale;
                    float shownW;
                    String shown = null; // initialised: assignment path not guaranteed
                    IntSetting sv = (IntSetting)v;
                    boolean editingNow = editingValue == sv;
                    String display = editingNow ? editBuffer : sv.getDisplay();
                    float fieldW = Math.min(96.0f, group.w);
                    float fieldX = group.x + group.w - fieldW;
                    float fieldH = ClickGUI.fontPixel() + 6.0f;
                    float fieldY = rowY + (sHeight - fieldH) / 2.0f;
                    if (editingNow) {
                        ClickGUI.rememberEditField(fieldX, rowY + 1.0f, fieldW, Math.max(sHeight, fieldH) - 2.0f);
                    }
                    if (!ClickGUI.glassControl(g, fieldX, fieldY, fieldW, fieldH, 3.0f, alphaMul, ClickGUI.pillTint(editingNow ? 200 : 150))) {
                        RenderUtil.drawRoundedRect(g, fieldX, fieldY, fieldW, fieldH, 3.0f, ClickGUI.pillFallback(editingNow ? p.inset : p.chip, Math.round(alphaMul * (float)(editingNow ? 200 : 150))));
                    }
                    String string = display.isEmpty() ? (editingNow ? "" : "type...") : (shown = display);
                    if (editingNow) {
                        shown = shown + "_";
                    }
                    if ((shownW = ClickGUI.textWidth(FontManager.smallFont, shown, shownScale = 0.78f)) > fieldW - 8.0f && shownW > 0.0f) {
                        shownScale = Math.max(0.4f, shownScale * (fieldW - 8.0f) / shownW);
                    }
                    ClickGUI.text(g, FontManager.smallFont, shown, fieldX + 4.0f, fieldY + (fieldH - ClickGUI.fontPixel()) * 0.5f, ClickGUI.fade(editingNow ? p.title : p.value, alphaMul), shownScale);
                } else if (v instanceof DoubleSetting) {
                    DoubleSetting av = (DoubleSetting)v;
                    String display = av.getDisplay();
                    float btnH = Math.min(Math.max(18.0f, ClickGUI.fontPixel() + 7.0f), Math.max(18.0f, sHeight - 2.0f));
                    float btnY = rowY + (sHeight - btnH) * 0.5f;
                    float btnX = group.x + 1.0f;
                    float btnW = group.w - 2.0f;
                    boolean hovered = RenderUtil.isHovering(btnX, btnY, btnW, btnH, mx, my);
                    av.animHover = RenderUtil.animateSmooth(av.animHover, hovered ? 1.0f : 0.0f, 14.0f);
                    int base = ClickGUI.mix(p.accentSoft, p.accent, 0.1f + 0.28f * av.animHover);
                    if (!ClickGUI.glassControl(g, btnX, btnY, btnW, btnH, 5.0f, alphaMul, ClickGUI.pillTint(140))) {
                        RenderUtil.drawRoundedRect(g, btnX, btnY, btnW, btnH, 5.0f, ClickGUI.pillFallback(base, Math.round(alphaMul * 140.0f)));
                    }
                    float barH = btnH - 6.0f;
                    float barW = 2.0f + 1.0f * av.animHover;
                    RenderUtil.drawRoundedRect(g, btnX + 1.0f, btnY + (btnH - barH) * 0.5f, barW, barH, barW * 0.5f, ClickGUI.fade(p.accent, alphaMul * (0.45f + 0.55f * av.animHover)));
                    float textW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                    float textScale = 0.78f;
                    if (textW > btnW - 14.0f && textW > 0.0f) {
                        textScale = Math.max(0.5f, 0.78f * (btnW - 14.0f) / textW);
                        textW = ClickGUI.textWidth(FontManager.smallFont, display, textScale);
                    }
                    ClickGUI.text(g, FontManager.smallFont, display, btnX + (btnW - textW) * 0.5f, btnY + (btnH - ClickGUI.fontPixel()) * 0.5f, ClickGUI.fade(ClickGUI.mix(p.accent, p.title, av.animHover), alphaMul), textScale);
                } else if (v instanceof ColorSetting) {
                    ColorSetting pv = (ColorSetting)v;
                    String display = pv.getDisplay();
                    strW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                    valueY = rowY + (sHeight - ClickGUI.fontPixel()) / 2.0f;
                    chipH = ClickGUI.fontPixel() + 4.0f;
                    float dot = chipH - 6.0f;
                    chipW = Math.min(strW + dot + 18.0f, group.w);
                    float chipX = group.x + group.w - chipW;
                    if (!ClickGUI.glassControl(g, chipX, valueY - 2.0f, chipW, chipH, chipH * 0.5f, alphaMul, ClickGUI.pillTint(150))) {
                        RenderUtil.drawRoundedRect(g, chipX, valueY - 2.0f, chipW, chipH, chipH * 0.5f, ClickGUI.pillFallback(p.chip, Math.round(alphaMul * 150.0f)));
                    }
                    RenderUtil.drawRoundedRect(g, chipX + 5.0f, valueY + 1.0f, dot, dot, dot * 0.5f, ClickGUI.fade(ClickGUI.rgba(255, pv.rgb()), alphaMul));
                    float labelY = valueY - 2.0f + (chipH - ClickGUI.fontPixel()) * 0.5f;
                    ClickGUI.text(g, FontManager.smallFont, display, chipX + 8.0f + dot, labelY, ClickGUI.fade(p.accent, alphaMul), 0.78f);
                } else if (v instanceof ModeSetting) {
                    ModeSetting mv = (ModeSetting)v;
                    String display = mv.getDisplay();
                    strW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                    valueY = rowY + (sHeight - ClickGUI.fontPixel()) / 2.0f;
                    chipH = ClickGUI.fontPixel() + 4.0f;
                    chipW = Math.min(strW + 10.0f, group.w);
                    float chipX = group.x + group.w - chipW;
                    float labelX = chipX + Math.max(0.0f, (chipW - strW) * 0.5f);
                    boolean mvHovered = RenderUtil.isHovering(chipX, valueY - 2.0f, chipW, chipH, mx, my);
                    mv.animHover = RenderUtil.animateSmooth(mv.animHover, mvHovered ? 1.0f : 0.0f, 14.0f);
                    int mvBase = ClickGUI.scaleTint(ClickGUI.chromeTint(p), ClickGUI.chipBrightness() + 0.18f * mv.animHover);
                    if (!ClickGUI.glassControl(g, chipX, valueY - 2.0f, chipW, chipH, chipH * 0.5f, alphaMul * (0.88f + 0.12f * mv.animHover), ClickGUI.pillTint(150, mvBase))) {
                        RenderUtil.drawRoundedRect(g, chipX, valueY - 2.0f, chipW, chipH, chipH * 0.5f, ClickGUI.fade(mvBase, alphaMul));
                    }
                    float labelY = valueY - 2.0f + (chipH - ClickGUI.fontPixel()) * 0.5f;
                    ClickGUI.text(g, FontManager.smallFont, display, labelX, labelY, ClickGUI.fade(ClickGUI.mix(p.accent, p.title, mv.animHover), alphaMul), 0.78f);
                } else if (v instanceof EnumSetting) {
                    EnumSetting kv = (EnumSetting)v;
                    boolean binding = bindingValue == v;
                    String display = binding ? "..." : kv.getDisplay();
                    strW = ClickGUI.textWidth(FontManager.smallFont, display, 0.78f);
                    valueY = rowY + (sHeight - ClickGUI.fontPixel()) / 2.0f;
                    chipH = ClickGUI.fontPixel() + 4.0f;
                    chipW = Math.min(strW + 10.0f, group.w);
                    float chipX = group.x + group.w - chipW;
                    float labelX = chipX + Math.max(0.0f, (chipW - strW) * 0.5f);
                    boolean kvHovered = binding || RenderUtil.isHovering(chipX, valueY - 2.0f, chipW, chipH, mx, my);
                    kv.animHover = RenderUtil.animateSmooth(kv.animHover, kvHovered ? 1.0f : 0.0f, 14.0f);
                    int kvBase = ClickGUI.scaleTint(ClickGUI.chromeTint(p), ClickGUI.chipBrightness() + 0.18f * kv.animHover);
                    if (!ClickGUI.glassControl(g, chipX, valueY - 2.0f, chipW, chipH, chipH * 0.5f, alphaMul * (0.88f + 0.12f * kv.animHover), ClickGUI.pillTint(150, kvBase))) {
                        RenderUtil.drawRoundedRect(g, chipX, valueY - 2.0f, chipW, chipH, chipH * 0.5f, ClickGUI.fade(kvBase, alphaMul));
                    }
                    float labelY = valueY - 2.0f + (chipH - ClickGUI.fontPixel()) * 0.5f;
                    ClickGUI.text(g, FontManager.smallFont, display, labelX, labelY, ClickGUI.fade(ClickGUI.mix(p.accent, p.title, kv.animHover), alphaMul), 0.78f);
                } else if (v instanceof NumberSetting || v instanceof ListSetting) {
                    boolean editingNow;
                    float percent = 0.0f;
                    String display = "";
                    if (v instanceof NumberSetting) {
                        percent = ((NumberSetting)v).getPercent();
                        display = ((NumberSetting)v).getDisplay();
                    }
                    if (v instanceof ListSetting) {
                        percent = ((ListSetting)v).getPercent();
                        display = ((ListSetting)v).getDisplay();
                    }
                    boolean bl = editingNow = editingValue == v;
                    if (editingNow) {
                        display = editBuffer + "_";
                    }
                    percent = Math.max(0.0f, Math.min(1.0f, percent));
                    strW = ClickGUI.sliderValueWidth(display);
                    float trackX = ClickGUI.sliderTrackStart(group, twoLines);
                    float trackW = ClickGUI.sliderTrackWidth(group, display, twoLines);
                    float trackY = ClickGUI.sliderTrackY(rowY, sHeight, twoLines);
                    RenderUtil.drawRoundedRect(g, trackX, trackY, trackW, 3.0f, 1.5f, ClickGUI.fade(p.track, alphaMul));
                    RenderUtil.drawRoundedRect(g, trackX, trackY, trackW * percent, 3.0f, 1.5f, ClickGUI.fade(p.accent, alphaMul));
                    RenderUtil.drawRoundedRect(g, ClickGUI.snap(trackX + trackW * percent - 3.5f), ClickGUI.snap(trackY - 2.0f), 7.0f, 7.0f, 3.5f, ClickGUI.fade(p.knob, alphaMul));
                    valueY = twoLines ? trackY - (ClickGUI.fontPixel() - 3.0f) / 2.0f : rowY + (sHeight - ClickGUI.fontPixel()) / 2.0f;
                    ClickGUI.text(g, FontManager.smallFont, display, group.x + group.w - strW, valueY, ClickGUI.fade(editingNow ? p.title : p.value, alphaMul), 0.7f);
                }
                rowY += sHeight;
            }
        }
        g.disableScissor();
        float viewport = height - 74.0f;
        float maxContentY = ClickGUI.contentHeight(groups, y + 66.0f + scrollY);
        float maxScroll = Math.max(0.0f, maxContentY - viewport);
        if (targetScrollY > 20.0f) {
            targetScrollY = 20.0f;
        }
        if (targetScrollY < -maxScroll - 20.0f) {
            targetScrollY = -maxScroll - 20.0f;
        }
        if (targetScrollY < -maxScroll) {
            targetScrollY = RenderUtil.animateSmooth(targetScrollY, -maxScroll, 12.0f);
        }
        if (targetScrollY > 0.0f) {
            targetScrollY = RenderUtil.animateSmooth(targetScrollY, 0.0f, 12.0f);
        }
        if (maxScroll > 1.0f) {
            float trackX = x + width - 4.0f;
            float trackY = y + 68.0f;
            float trackH = height - 76.0f;
            float ratio = viewport / Math.max(1.0f, maxContentY);
            if (ratio > 1.0f) {
                ratio = 1.0f;
            }
            float thumbH = Math.max(18.0f, trackH * ratio);
            float progress = Math.min(1.0f, Math.max(0.0f, -scrollY / maxScroll));
            float thumbY = trackY + (trackH - thumbH) * progress;
            RenderUtil.drawRoundedRect(g, trackX, thumbY, 2.0f, thumbH, 1.0f, ClickGUI.fade(p.accent, anim * 0.8f));
        }
    }

    public static boolean onKeyPressed(int keyCode) {
        if (bindingValue != null) {
            Setting<?> target = bindingValue;
            bindingValue = null;
            if (keyCode != 256 && target instanceof EnumSetting) {
                int bound = keyCode == 259 || keyCode == 261 ? 0 : keyCode;
                ((EnumSetting)target).setUnchecked(bound);
                try {
                    ConfigLoadManager.getInstance().saveConfig();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            return true;
        }
        if (editingValue != null) {
            boolean ctrl = ClickGUI.isCtrlDown();
            if (ctrl && keyCode == 65) {
                editSelectAll = !editBuffer.isEmpty();
                return true;
            }
            if (ctrl && keyCode == 67) {
                Minecraft.getInstance().keyboardHandler.setClipboard(editBuffer == null ? "" : editBuffer);
                return true;
            }
            if (ctrl && keyCode == 88) {
                Minecraft.getInstance().keyboardHandler.setClipboard(editBuffer == null ? "" : editBuffer);
                editBuffer = "";
                editSelectAll = false;
                ClickGUI.applyEdit();
                return true;
            }
            if (ctrl && keyCode == 86) {
                String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
                if (clip != null && !clip.isEmpty()) {
                    int max = editingValue instanceof IntSetting ? ((IntSetting) ClickGUI.editingValue).maxLength : 32;
                    StringBuilder sb = new StringBuilder(editSelectAll ? "" : editBuffer);
                    for (int i = 0; i < clip.length() && sb.length() < max; ++i) {
                        char ch = clip.charAt(i);
                        if (Character.isISOControl(ch)) continue;
                        sb.append(ch);
                    }
                    editBuffer = sb.toString();
                    editSelectAll = false;
                    ClickGUI.applyEdit();
                }
                return true;
            }
            if (keyCode == 259) {
                if (editSelectAll) {
                    editBuffer = "";
                    editSelectAll = false;
                } else if (!editBuffer.isEmpty()) {
                    editBuffer = editBuffer.substring(0, editBuffer.length() - 1);
                }
                ClickGUI.applyEdit();
            } else if (keyCode == 257) {
                ClickGUI.commitEdit();
            } else if (keyCode == 256) {
                ClickGUI.finishEdit();
            }
            return true;
        }
        if (isSearching) {
            if (keyCode == 259 && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
            } else if (keyCode == 256) {
                isSearching = false;
                searchText = "";
            }
            return true;
        }
        return false;
    }

    private static final class Palette {
        int chip;
        int shell;
        int rail;
        int rowHover;
        int inset;
        int accent;
        int accentSoft;
        int toggleOff;
        int knob;
        int track;
        int hairline;
        int title;
        int text;
        int label;
        int value;
        int placeholder;
        float radius;

        private Palette() {
        }
    }

    private static final class Group {
        final Module module;
        final float x;
        final float y;
        final float w;
        final float h;

        Group(Module module, float x, float y, float w, float h) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}

