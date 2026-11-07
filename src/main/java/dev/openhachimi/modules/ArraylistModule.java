/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.render.shader.Shader;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.shader.ShaderUtil;
import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.render.font.FontManager;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ArraylistModule
extends Module {
    private static final byte[] S_227FE277 = new byte[]{3, 48, 48, 35, 59, 14, 43, 49, 54};  // XOR0x42 "ArrayList"
    private static final byte[] S_56CAF42B = new byte[]{13, 36, 36, 49, 39, 54, 98, 26};  // XOR0x42 "Offset X"
    private static final byte[] S_56CAF42C = new byte[]{13, 36, 36, 49, 39, 54, 98, 27};  // XOR0x42 "Offset Y"
    private static final byte[] S_485E31D4 = new byte[]{17, 42, 45, 53, 98, 17, 55, 36, 36, 43, 58};  // XOR0x42 "Show Suffix"
    private static final byte[] S_276A314E = new byte[]{0, 35, 33, 41, 37, 48, 45, 55, 44, 38};  // XOR0x42 "Background"
    private static final byte[] S_21E9AD = new byte[]{5, 46, 45, 53};  // XOR0x42 "Glow"
    private static final byte[] S_6B044802 = new byte[]{11, 47, 50, 45, 48, 54, 35, 44, 54};  // XOR0x42 "Important"
    private static final byte[] S_275421 = new byte[]{17, 43, 56, 39};  // XOR0x42 "Size"
    private static final byte[] S_4C82D91 = new byte[]{17, 54, 59, 46, 39};  // XOR0x42 "Style"
    private static final byte[] S_1D403196 = new byte[]{16, 35, 43, 44, 32, 45, 53};  // XOR0x42 "Rainbow"
    private static final byte[] S_78FAF0BE = new byte[]{1, 55, 48, 45, 48, 35};  // XOR0x42 "Curora"
    private static final byte[] S_1446B6B6 = new byte[]{17, 55, 44, 49, 39, 54};  // XOR0x42 "Sunset"
    private static final byte[] S_487C8DE = new byte[]{13, 33, 39, 35, 44};  // XOR0x42 "Ocean"
    private static final byte[] S_72FA3ED0 = new byte[]{1, 45, 54, 54, 45, 44, 98, 1, 35, 44, 38, 59};  // XOR0x42 "Cotton Candy"
    private static final byte[] S_313469E7 = new byte[]{14, 35, 52, 39, 44, 38, 39, 48};  // XOR0x42 "Lavender"
    private static final byte[] S_496BA51 = new byte[]{18, 39, 35, 33, 42};  // XOR0x42 "Peach"
    private static final byte[] S_249882 = new byte[]{15, 43, 44, 54};  // XOR0x42 "Mint"
    private static final byte[] S_5A4E8E3B = new byte[]{1, 59, 32, 39, 48, 50, 55, 44, 41};  // XOR0x42 "Cyberpunk"
    private static final byte[] S_3F3A789 = new byte[]{6, 48, 43, 36, 54};  // XOR0x42 "Drift"
    private static final byte[] S_20 = new byte[]{98};
    private static final byte[] S_E394DE1 = new byte[]{106, 25, 3, 111, 24, 31, 105, 107, 106, 25, 3, 111, 24, 31, 25, 35, 111, 56, 31, 107};  // XOR0x42 "([A-Z]+)([A-Z][a-z])"
    private static final byte[] S_2121101 = new byte[]{102, 115, 98, 102, 112};  // XOR0x42 "$1 $2"
    private static final byte[] S_18AA889A = new byte[]{106, 25, 35, 111, 56, 114, 111, 123, 31, 107, 106, 25, 3, 111, 24, 31, 107};  // XOR0x42 "([a-z0-9])([A-Z])"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_118D7 = new byte[]{10, 23, 6};  // XOR0x42 "HUD"
    private static final byte[] S_39E166B3 = new byte[]{1, 46, 43, 33, 41, 5, 23, 11};  // XOR0x42 "ClickGUI"
    private static final byte[] S_4D085A9 = new byte[]{22, 42, 39, 47, 39};  // XOR0x42 "Theme"
    private static final byte[] S_5FC31201 = new byte[]{9, 43, 46, 46, 3, 55, 48, 35};  // XOR0x42 "KillAura"
    private static final byte[] S_58DB4BFE = new byte[]{3, 55, 54, 45, 0, 46, 45, 33, 41};  // XOR0x42 "AutoBlock"
    private static final float OPAI_FONT_SIZE = 9.08f;
    private static final float OPAI_ROW_HEIGHT = 12.0f;
    private static final float OPAI_LEFT_PAD = 3.0f;
    private static final float OPAI_RIGHT_PAD = 2.0f;
    private static final float OPAI_EDGE_WIDTH = 1.0f;
    private static final float OPAI_EDGE_INSET = 0.5f;
    private static final float OPAI_TOP_INSET = 0.25f;
    private static final float OPAI_RADIUS = 5.0f;
    private static final int OPAI_BACKGROUND = -871362544;
    private static final int OPAI_TEXT = -1;
    private static final int OPAI_ACCENT = -4471809;
    private static final float OPAI_CORNER_RADIUS = 2.7f;
    private static final float OPAI_GLOW_SIGMA = 2.0f;
    private static final int OPAI_GLOW_ALPHA = 66;
    private final NumberSetting offsetX = new NumberSetting("Offset X", 0.0f, -4000.0f, 4000.0f);
    private final NumberSetting offsetY = new NumberSetting("Offset Y", 0.0f, -4000.0f, 4000.0f);
    private final BooleanSetting showSuffix = new BooleanSetting("Show Suffix", true);
    private final BooleanSetting background = new BooleanSetting("Background", true);
    private final BooleanSetting glow = new BooleanSetting("Glow", true);
    private final BooleanSetting important = new BooleanSetting("Important", true);
    private final NumberSetting size = new NumberSetting("Size", 1.0f, 0.5f, 2.0f);
    private final ModeSetting theme = new ModeSetting("Style", new String[]{"Rainbow", "Curora", "Sunset", "Ocean", "Cotton Candy", "Lavender", "Peach", "Mint", "Cyberpunk", "Drift"}, "Drift");
    private final Map<Module, Row> rowStates = new IdentityHashMap<Module, Row>();

    public ArraylistModule() {
        super("ArrayList", Category.RENDER);
        this.offsetX.setVisible(() -> false);
        this.offsetY.setVisible(() -> false);
        this.values.add(this.offsetX);
        this.values.add(this.offsetY);
        this.values.add(this.showSuffix);
        this.values.add(this.background);
        this.values.add(this.glow);
        this.values.add(this.important);
        this.values.add(this.size);
        this.values.add(this.theme);
    }

    private String displayName(Module module) {
        String name = module.name;
        if (!((Boolean)this.showSuffix.get()).booleanValue()) {
            return name;
        }
        String suffix = null;
        for (int i = 0; i < module.values.size(); ++i) {
            if (!(module.values.get(i) instanceof ModeSetting)) continue;
            suffix = (String)((ModeSetting)module.values.get(i)).get();
            break;
        }
        if (suffix == null || suffix.isBlank()) {
            return name;
        }
        return name + " " + suffix.trim();
    }

    private void renderOpai(GuiGraphics graphics, Minecraft mc, List<Row> rows, float scale, float fontScale) {
        Font font = FontManager.regular;
        if (font == null) {
            return;
        }
        float rowHeight = 12.0f * scale;
        float boxWidth = 0.0f;
        float boxHeight = 0.0f;
        for (int i = 0; i < rows.size(); ++i) {
            Row row = rows.get(i);
            float progress = row.progress();
            if (progress <= 0.01f) continue;
            boxWidth = Math.max(boxWidth, row.opaiWidth);
            boxHeight += rowHeight * progress;
        }
        if (boxWidth <= 0.0f || boxHeight <= 0.0f) {
            return;
        }
        float anchorX = (float)mc.getWindow().getGuiScaledWidth() - 0.5f * scale - boxWidth;
        float anchorY = 0.25f * scale;
        float x = anchorX + ((Float)this.offsetX.get()).floatValue();
        float y = anchorY + ((Float)this.offsetY.get()).floatValue();
        float[] dragged = RenderUtil.hudDragOffset(mc, x, y, anchorX, anchorY, boxWidth, boxHeight, this.offsetX, this.offsetY, this);
        float right = dragged[0] + boxWidth;
        float cursorY = dragged[1];
        ArrayList<Placed> placed = new ArrayList<Placed>();
        for (int i = 0; i < rows.size(); ++i) {
            Row row = rows.get(i);
            float progress = row.progress();
            if (progress <= 0.01f) continue;
            float nextWidth = 0.0f;
            for (int j = i + 1; j < rows.size(); ++j) {
                if (rows.get(j).progress() <= 0.01f) continue;
                nextWidth = rows.get((int)j).opaiWidth;
                break;
            }
            float radius = Math.min(5.0f * scale, Math.max(0.0f, row.opaiWidth - nextWidth));
            float slide = (row.opaiWidth + 8.0f * scale) * (1.0f - progress);
            placed.add(new Placed(row, right - row.opaiWidth + slide, cursorY, row.opaiWidth, rowHeight, radius, progress));
            cursorY += rowHeight * progress;
        }
        if (placed.isEmpty()) {
            return;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.flush();
        if (!this.drawOpaiPanel(graphics, placed, scale)) {
            this.drawOpaiRowsFallback(graphics, placed, scale);
        }
        float textOffset = (rowHeight - (float)font.getHeight() * fontScale) / 2.0f;
        for (int i = 0; i < placed.size(); ++i) {
            Placed item = placed.get(i);
            int alpha = Math.round(255.0f * item.progress) << 24;
            float textY = item.y + textOffset;
            float textX = item.x + 3.0f * scale;
            this.drawLabel(graphics, font, item.row.opaiName, alpha | 0xFFFFFF, textX, textY, fontScale);
            if (item.row.opaiSuffix.isEmpty()) continue;
            float suffixX = textX + (item.row.opaiNameWidth + item.row.opaiGap) * fontScale;
            int suffixColor = alpha | this.colorForPosition(i, Math.max(1, placed.size() - 1)) & 0xFFFFFF;
            this.drawLabel(graphics, font, item.row.opaiSuffix, suffixColor, suffixX, textY, fontScale);
        }
        graphics.flush();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawLabel(GuiGraphics graphics, Font font, String text, int color, float x, float y, float fontScale) {
        if (text == null || text.isEmpty()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(fontScale, fontScale, 1.0f);
        font.drawString(graphics, text, 0.0, 0.0, color, false);
        graphics.flush();
        graphics.pose().popPose();
    }

    private boolean drawOpaiPanel(GuiGraphics graphics, List<Placed> placed, float scale) {
        int count = placed.size();
        if (count > 96) {
            return false;
        }
        float[] rows = new float[count * 4];
        for (int i = 0; i < count; ++i) {
            Placed item = placed.get(i);
            rows[i * 4] = item.x;
            rows[i * 4 + 1] = item.y;
            rows[i * 4 + 2] = item.x + item.width;
            rows[i * 4 + 3] = item.y + item.height * item.progress;
        }
        int fill = (Boolean)this.background.get() != false ? -871362544 : 0x101010;
        int shadow = (Boolean)this.glow.get() != false ? 0x42000000 : 0;
        float sigma = 2.7f * scale / 1.86f;
        return ShaderUtil.stairs(graphics.pose().last().pose(), rows, count, sigma, 2.0f * scale, fill, shadow, -4471809, 1.0f * scale);
    }

    private List<Row> updateRows(float scale, float fontScale, boolean opai) {
        Font font = FontManager.regular;
        for (Module module : ModuleManager.modules) {
            if (module == this || module.name == null || module.name.isEmpty() || !module.canBeEnabled) {
                this.rowStates.remove(module);
                continue;
            }
            if (((Boolean)module.invisible.get()).booleanValue()) {
                Row hiddenRow = this.rowStates.get(module);
                if (hiddenRow == null) continue;
                hiddenRow.setTargetVisible(false);
                continue;
            }
            if (((Boolean)this.important.get()).booleanValue() && module.category == Category.RENDER) {
                this.rowStates.remove(module);
                continue;
            }
            Row row = this.rowStates.get(module);
            if (module.enabled) {
                if (row == null) {
                    row = new Row(module);
                    this.rowStates.put(module, row);
                }
                String displayName = this.displayName(module);
                float textWidth = font != null && !displayName.isEmpty() ? (float)font.getStringWidth(displayName) : 0.0f;
                row.updateMetrics(displayName, textWidth, textWidth);
                String opaiName = ArraylistModule.spacedName(module.name);
                String opaiSuffix = this.opaiSuffix(module);
                float nameWidth = font == null ? 0.0f : (float)font.getStringWidth(opaiName);
                float gap = opaiSuffix.isEmpty() || font == null ? 0.0f : (float)font.getStringWidth(" ");
                float suffixWidth = font == null ? 0.0f : (float)font.getStringWidth(opaiSuffix);
                float opaiWidth = 6.0f * scale + (nameWidth + gap + suffixWidth) * fontScale;
                row.updateOpaiMetrics(opaiName, opaiSuffix, nameWidth, gap, opaiWidth);
                row.setTargetVisible(true);
                continue;
            }
            if (row == null) continue;
            row.setTargetVisible(false);
        }
        for (Row row : this.rowStates.values()) {
            row.tick();
        }
        this.rowStates.values().removeIf(Row::isFinishedRemoving);
        ArrayList<Row> rows = new ArrayList<Row>(this.rowStates.values());
        rows.sort((a, b) -> Float.compare(b.opaiWidth, a.opaiWidth));
        return rows;
    }

    private int colorForPosition(int rowIndex, int maxRowIndex) {
        int speed = 5;
        int offset = rowIndex * 77;
        String themeName = (String)this.theme.get();
        double position = (double)((System.currentTimeMillis() / (long)speed + (long)offset) % 1000L) / 1000.0;
        return ArraylistModule.themeColor(themeName, position, 90.0f, 100.0f);
    }

    private float pixelSnap() {
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        return scale <= 0.0 ? 1.0f : (float)(1.0 / scale);
    }

    private static String spacedName(String name) {
        return name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2").replaceAll("([a-z0-9])([A-Z])", "$1 $2");
    }

    private static int themeColor(String name, double position, float saturation, float brightness) {
        float[][] colors = ArrayListThemeColors.get(name);
        if (colors.length == 0) {
            return -1;
        }
        if ((position %= 1.0) < 0.0) {
            position += 1.0;
        }
        double scaledPos = position * (double)(colors.length - 1);
        int index = (int)Math.floor(scaledPos);
        double fraction = scaledPos - (double)index;
        fraction = fraction * fraction * (3.0 - 2.0 * fraction);
        if (index >= colors.length - 1) {
            float[] c = colors[colors.length - 1];
            return Color.getHSBColor(c[0], c[1] * saturation / 100.0f, c[2] * brightness / 100.0f).getRGB();
        }
        float[] c2 = colors[index + 1];
        float hue2 = c2[0];
        float[] c1 = colors[index];
        float hue1 = c1[0];
        float hueDiff = hue2 - hue1;
        if (Math.abs(hueDiff) > 0.5f) {
            if (hueDiff > 0.0f) {
                hue1 += 1.0f;
            } else {
                hue2 += 1.0f;
            }
        }
        float hue = (float)((double)(hue1 + (hue2 - hue1) * (float)fraction) % 1.0);
        float sat = (float)((double)(c1[1] + (c2[1] - c1[1]) * (float)fraction));
        float bri = (float)((double)(c1[2] + (c2[2] - c1[2]) * (float)fraction));
        sat = Math.min(1.0f, sat * saturation / 100.0f);
        bri = Math.min(1.0f, bri * brightness / 100.0f);
        return Color.getHSBColor(hue, sat, bri).getRGB();
    }

    private static float easeOutSine(double t) {
        return (float)Math.sin(t * Math.PI / 2.0);
    }

    private static void drawRounded(GuiGraphics graphics, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, int color) {
        if (width < 0.5f || height < 0.5f) {
            return;
        }
        graphics.flush();
        Shader.corners(graphics.pose().last().pose(), x, y, width, height, topLeft, topRight, bottomRight, bottomLeft, color);
    }

    private static float easeOutPow3(double t) {
        return (float)(1.0 - Math.pow(1.0 - t, 3.0));
    }

    private String opaiSuffix(Module module) {
        if (!((Boolean)this.showSuffix.get()).booleanValue()) {
            return "";
        }
        String name = module.name;
        if ("HUD".equals(name) || "ClickGUI".equals(name) || "Theme".equals(name)) {
            return "";
        }
        if ("KillAura".equals(name)) {
            for (int i = 0; i < module.values.size(); ++i) {
                if (!(module.values.get(i) instanceof ModeSetting)) continue;
                ModeSetting setting = (ModeSetting)module.values.get(i);
                for (String mode : setting.getModes()) {
                    if (!"AutoBlock".equals(mode)) continue;
                    return ((String)setting.get()).trim();
                }
            }
            return "";
        }
        for (int i = 0; i < module.values.size(); ++i) {
            if (!(module.values.get(i) instanceof ModeSetting)) continue;
            String value = (String)((ModeSetting)module.values.get(i)).get();
            return value == null ? "" : value.trim();
        }
        return "";
    }

    private static float easeInPow3(double t) {
        return (float)Math.pow(t, 3.0);
    }

    private void drawOpaiRowsFallback(GuiGraphics graphics, List<Placed> placed, float scale) {
        int alpha;
        Placed item;
        int i;
        if (((Boolean)this.glow.get()).booleanValue()) {
            graphics.flush();
            for (i = 0; i < placed.size(); ++i) {
                item = placed.get(i);
                alpha = Math.round(66.0f * item.progress) << 24;
                if (alpha <= 0) continue;
                Shader.glowCorners(graphics.pose().last().pose(), item.x, item.y, item.width, item.height, 0.0f, 0.0f, 0.0f, item.radius, 2.0f * scale, alpha);
            }
        }
        if (((Boolean)this.background.get()).booleanValue()) {
            for (i = 0; i < placed.size(); ++i) {
                item = placed.get(i);
                alpha = Math.round(204.0f * item.progress) << 24;
                ArraylistModule.drawRounded(graphics, item.x, item.y, item.width, item.height, 0.0f, 0.0f, 0.0f, item.radius, alpha | 0x101010);
            }
        }
        for (i = 0; i < placed.size(); ++i) {
            item = placed.get(i);
            alpha = Math.round(255.0f * item.progress) << 24;
            ArraylistModule.drawRounded(graphics, item.x + item.width - 1.0f * scale, item.y, 1.0f * scale, item.height, 0.0f, 0.0f, 0.0f, 0.0f, alpha | 0xBBC3FF);
        }
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return;
        }
        float scale = ((Float)this.size.get()).floatValue();
        Font font = FontManager.regular;
        float opaiFontScale = font == null ? 1.0f : 9.08f * scale / (float)font.getHeight();
        List<Row> rows = this.updateRows(scale, opaiFontScale, true);
        this.renderOpai(graphics, mc, rows, scale, opaiFontScale);
    }

    private static final class Row {
        private static final byte[] S_0 = new byte[0];
        final Module module;
        final Timer progress = new Timer();
        String name = "";
        String opaiName = "";
        String opaiSuffix = "";
        float rowWidth;
        float opaiNameWidth;
        float opaiGap;
        float opaiWidth;
        boolean targetVisible;

        Row(Module module) {
            this.module = module;
            this.progress.setCurrent(0.0f);
        }

        void tick() {
            this.progress.tick();
        }

        float progress() {
            return Math.max(0.0f, Math.min(1.0f, this.progress.value()));
        }

        void setTargetVisible(boolean visible) {
            if (this.targetVisible == visible) {
                return;
            }
            this.targetVisible = visible;
            float current = this.progress.value();
            this.progress.setFrom(current);
            this.progress.setTo(visible ? 1.0f : 0.0f);
            this.progress.setStart(System.currentTimeMillis());
            this.progress.setDuration(visible ? 240.0f : 180.0f);
            this.progress.setEasing(visible ? ArraylistModule::easeOutPow3 : ArraylistModule::easeInPow3);
        }

        void updateOpaiMetrics(String name, String suffix, float nameWidth, float gap, float width) {
            this.opaiName = name;
            this.opaiSuffix = suffix;
            this.opaiNameWidth = nameWidth;
            this.opaiGap = gap;
            this.opaiWidth = width;
        }

        boolean isFinishedRemoving() {
            return !this.targetVisible && this.progress() <= 0.01f && this.progress.done();
        }

        void updateMetrics(String displayName, float textWidth, float rowWidth) {
            this.name = displayName;
            this.rowWidth = rowWidth;
        }
    }

    private static final class Placed {
        final Row row;
        final float x;
        final float y;
        final float width;
        final float height;
        final float radius;
        final float progress;

        Placed(Row row, float x, float y, float width, float height, float radius, float progress) {
            this.row = row;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.radius = radius;
            this.progress = progress;
        }
    }

    private static final class ArrayListThemeColors {
        private static final byte[] S_3A0799B6 = new byte[]{48, 35, 43, 44, 32, 45, 53};  // XOR0x42 "rainbow"
        private static final byte[] S_2C2C5360 = new byte[]{35, 55, 48, 45, 48, 35};  // XOR0x42 "aurora"
        private static final byte[] S_2F96049E = new byte[]{33, 55, 48, 45, 48, 35};  // XOR0x42 "curora"
        private static final byte[] S_4AE1CA96 = new byte[]{49, 55, 44, 49, 39, 54};  // XOR0x42 "sunset"
        private static final byte[] S_64AB8FE = new byte[]{45, 33, 39, 35, 44};  // XOR0x42 "ocean"
        private static final byte[] S_6AF1DAD0 = new byte[]{33, 45, 54, 54, 45, 44, 98, 33, 35, 44, 38, 59};  // XOR0x42 "cotton candy"
        private static final byte[] S_2D5A05C7 = new byte[]{46, 35, 52, 39, 44, 38, 39, 48};  // XOR0x42 "lavender"
        private static final byte[] S_659AA71 = new byte[]{50, 39, 35, 33, 42};  // XOR0x42 "peach"
        private static final byte[] S_332462 = new byte[]{47, 43, 44, 54};  // XOR0x42 "mint"
        private static final byte[] S_62DC6E5B = new byte[]{33, 59, 32, 39, 48, 50, 55, 44, 41};  // XOR0x42 "cyberpunk"
        private static final float[][] DRIFT = new float[][]{{0.6f, 0.65f, 0.9f}, {0.65f, 0.55f, 0.95f}, {0.7f, 0.5f, 1.0f}, {0.75f, 0.55f, 0.95f}, {0.68f, 0.6f, 0.9f}, {0.6f, 0.65f, 0.9f}};
        private static final float[][] RAINBOW = new float[][]{{0.0f, 0.5f, 1.0f}, {0.17f, 0.5f, 1.0f}, {0.33f, 0.5f, 1.0f}, {0.5f, 0.5f, 1.0f}, {0.67f, 0.5f, 1.0f}, {0.83f, 0.5f, 1.0f}, {1.0f, 0.5f, 1.0f}};
        private static final float[][] AURORA = new float[][]{{0.45f, 0.7f, 1.0f}, {0.52f, 0.6f, 1.0f}, {0.72f, 0.55f, 0.95f}, {0.82f, 0.5f, 0.9f}, {0.9f, 0.55f, 0.95f}, {0.45f, 0.7f, 1.0f}};
        private static final float[][] SUNSET = new float[][]{{0.98f, 0.7f, 1.0f}, {0.05f, 0.75f, 1.0f}, {0.12f, 0.65f, 1.0f}, {0.15f, 0.5f, 1.0f}, {0.95f, 0.55f, 0.95f}, {0.98f, 0.7f, 1.0f}};
        private static final float[][] OCEAN = new float[][]{{0.57f, 0.8f, 0.9f}, {0.53f, 0.7f, 0.95f}, {0.5f, 0.6f, 1.0f}, {0.47f, 0.55f, 0.95f}, {0.43f, 0.5f, 0.9f}, {0.57f, 0.8f, 0.9f}};
        private static final float[][] COTTON = new float[][]{{0.92f, 0.4f, 1.0f}, {0.88f, 0.35f, 1.0f}, {0.8f, 0.3f, 1.0f}, {0.7f, 0.35f, 1.0f}, {0.6f, 0.4f, 1.0f}, {0.92f, 0.4f, 1.0f}};
        private static final float[][] LAVENDER = new float[][]{{0.75f, 0.5f, 0.9f}, {0.78f, 0.45f, 0.95f}, {0.82f, 0.4f, 1.0f}, {0.85f, 0.35f, 1.0f}, {0.8f, 0.45f, 0.95f}, {0.75f, 0.5f, 0.9f}};
        private static final float[][] PEACH = new float[][]{{0.05f, 0.5f, 1.0f}, {0.08f, 0.45f, 1.0f}, {0.1f, 0.4f, 1.0f}, {0.98f, 0.45f, 1.0f}, {0.95f, 0.5f, 1.0f}, {0.05f, 0.5f, 1.0f}};
        private static final float[][] MINT = new float[][]{{0.42f, 0.55f, 0.95f}, {0.45f, 0.5f, 1.0f}, {0.48f, 0.45f, 1.0f}, {0.5f, 0.4f, 0.95f}, {0.47f, 0.5f, 1.0f}, {0.42f, 0.55f, 0.95f}};
        private static final float[][] CYBER = new float[][]{{0.85f, 0.75f, 1.0f}, {0.9f, 0.7f, 1.0f}, {0.95f, 0.65f, 0.95f}, {0.55f, 0.75f, 1.0f}, {0.5f, 0.7f, 1.0f}, {0.85f, 0.75f, 1.0f}};

        private ArrayListThemeColors() {
        }

        static float[][] get(String name) {
            String key;
            switch (key = name.toLowerCase(Locale.ROOT)) {
                case "rainbow": {
                    return RAINBOW;
                }
                case "aurora": 
                case "curora": {
                    return AURORA;
                }
                case "sunset": {
                    return SUNSET;
                }
                case "ocean": {
                    return OCEAN;
                }
                case "cotton candy": {
                    return COTTON;
                }
                case "lavender": {
                    return LAVENDER;
                }
                case "peach": {
                    return PEACH;
                }
                case "mint": {
                    return MINT;
                }
                case "cyberpunk": {
                    return CYBER;
                }
            }
            return DRIFT;
        }
    }

    static final class Timer {
        private float value;
        private float from;
        private float to;
        private long start;
        private float durationMs = 1.0f;
        private DoubleUnaryOperator easing = t -> t;

        Timer() {
        }

        float value() {
            return this.value;
        }

        void tick() {
            float t = (float)((double)System.currentTimeMillis() - (double)this.start) / this.durationMs;
            if (t < 0.0f) {
                t = 0.0f;
            }
            if (t > 1.0f) {
                t = 1.0f;
            }
            this.value = (float)((double)this.from + ((double)this.to - (double)this.from) * this.easing.applyAsDouble(t));
        }

        boolean done() {
            return (float)((double)System.currentTimeMillis() - (double)this.start) >= this.durationMs;
        }

        void setStart(long t) {
            this.start = t;
        }

        void setDuration(float ms) {
            this.durationMs = Math.max(1.0f, ms);
        }

        void setTo(float v) {
            this.to = v;
        }

        void setFrom(float v) {
            this.from = v;
            this.value = v;
        }

        void setEasing(DoubleUnaryOperator e) {
            this.easing = e;
        }

        void setCurrent(float v) {
            this.value = v;
            this.from = v;
            this.to = v;
        }

        void animate(float target, float durationSec, DoubleUnaryOperator easingFn) {
            this.from = this.value;
            this.to = target;
            this.durationMs = Math.max(1.0f, durationSec * 1000.0f);
            this.start = System.currentTimeMillis();
            this.easing = easingFn;
        }
    }
}

