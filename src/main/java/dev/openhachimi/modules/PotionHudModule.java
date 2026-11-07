/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.utils.render.shader.Shader;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.FrameHelper;
import dev.openhachimi.utils.render.font.FontManager;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import org.lwjgl.glfw.GLFW;

public class PotionHudModule
extends Module {
    private static final byte[] S_289DC8EA = new byte[]{18, 45, 54, 43, 45, 44, 98, 10, 23, 6};  // XOR0x42 "Potion HUD"
    private static final byte[] S_58 = new byte[]{26};
    private static final byte[] S_59 = new byte[]{27};
    private static final byte[] S_151E9E45 = new byte[]{22, 39, 58, 54, 98, 26};  // XOR0x42 "Text X"
    private static final byte[] S_49 = new byte[]{11};
    private static final byte[] S_920 = new byte[]{11, 11};  // XOR0x42 "II"
    private static final byte[] S_11B29 = new byte[]{11, 11, 11};  // XOR0x42 "III"
    private static final byte[] S_92D = new byte[]{11, 20};  // XOR0x42 "IV"
    private static final byte[] S_56 = new byte[]{20};
    private static final byte[] S_221E = new byte[]{(byte)160, (byte)202, (byte)220};
    private static final float SPACING = 2.0f;
    private static final float ENTRY_HEIGHT = 18.0f;
    private static final float RADIUS = 4.5f;
    private static final float MIN_WIDTH = 100.0f;
    private static final float ICON_BOX = 20.0f;
    private static final float PADDING = 5.0f;
    private static final float NAME_SIZE = 16.0f;
    private static final float AMP_SIZE = 16.0f;
    private static final float DURATION_SIZE = 14.0f;
    private static final int FALLBACK_COLOR = 3376639;
    private static final int EASE_OUT_SINE = 0;
    private static final int EASE_OUT_POW3 = 1;
    private static final int EASE_IN_POW3 = 2;
    public final ListSetting posX = new ListSetting("X", 0, 0, 4000);
    public final ListSetting posY = new ListSetting("Y", 0, 0, 4000);
    public final ListSetting textOffset = new ListSetting("Text X", 0, 0, 40);
    private final List<EffectEntry> entries = new ArrayList<EffectEntry>();
    private static final FrameHelper.Draggable OWNER = new FrameHelper.Draggable(){};

    private static float ease(int easing, float t) {
        switch (easing) {
            case 1: {
                float inv = 1.0f - t;
                return 1.0f - inv * inv * inv;
            }
            case 2: {
                return t * t * t;
            }
        }
        return (float)Math.sin((double)t * 1.5707963267948966);
    }

    public PotionHudModule() {
        super("Potion HUD", Category.RENDER);
        this.posX.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return false;
            }
        });
        this.posY.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return false;
            }
        });
        this.values.add(this.posX);
        this.values.add(this.posY);
        this.values.add(this.textOffset);
    }

    private static long now() {
        return System.nanoTime() / 1000000L;
    }

    private static int argb(int alpha, int red, int green, int blue) {
        int a = Math.max(0, Math.min(255, alpha));
        int r = Math.max(0, Math.min(255, red));
        int g = Math.max(0, Math.min(255, green));
        int b = Math.max(0, Math.min(255, blue));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static String formatDuration(MobEffectInstance instance) {
        if (instance.isInfiniteDuration() || instance.getDuration() > 72000) {
            return "∞";
        }
        return MobEffectUtil.formatDuration((MobEffectInstance)instance, (float)1.0f).getString();
    }

    private void render(GuiGraphics graphics) {
        float originY;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.getWindow() == null) {
            for (EffectEntry entry2 : this.entries) {
                entry2.startRemove();
            }
            this.entries.removeIf(entry -> entry.removing && entry.isRemoveDone());
            return;
        }
        this.syncEntries(player.getActiveEffects());
        this.entries.removeIf(entry -> entry.removing && entry.isRemoveDone());
        if (this.entries.isEmpty()) {
            return;
        }
        Font nameFont = FontManager.regular;
        Font boldFont = FontManager.bold22;
        if (nameFont == null) {
            return;
        }
        if (boldFont == null) {
            boldFont = nameFont;
        }
        float nameScale = 16.0f / (float)nameFont.getHeight();
        float ampScale = 16.0f / (float)boldFont.getHeight();
        float timeScale = 14.0f / (float)boldFont.getHeight();
        this.entries.sort(Comparator.comparingDouble(entry -> -entry.totalWidth));
        float cursor = 0.0f;
        float maxWidth = 0.0f;
        for (EffectEntry entry3 : this.entries) {
            entry3.tick();
            float nameWidth = FontManager.measureExact(nameFont, entry3.name, nameScale);
            entry3.totalWidth = Math.max(100.0f, 20.0f + nameWidth + 10.0f);
            if (entry3.visible) {
                entry3.visible = false;
                entry3.fadeAnim.setValue(cursor);
                entry3.show(18.0f);
            }
            entry3.updateRowHeight(18.0f);
            entry3.fadeAnim.animate(cursor, 0.15f, 0);
            entry3.animHeight = entry3.heightAnim.getValue();
            entry3.animWidth = entry3.widthAnim.getValue();
            entry3.layoutY = entry3.fadeAnim.getValue();
            if (entry3.animHeight > 0.01f) {
                maxWidth = Math.max(maxWidth, entry3.totalWidth);
            }
            cursor += entry3.animWidth + (entry3.animWidth > 0.0f ? 2.0f : 0.0f);
        }
        float height = Math.max(0.0f, cursor - 2.0f);
        if (maxWidth < 1.0f || height < 1.0f) {
            return;
        }
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float originX = (Integer)this.posX.get() > 0 ? (float)((Integer)this.posX.get()).intValue() : (float)screenWidth - maxWidth - 10.0f;
        float f = originY = (Integer)this.posY.get() > 0 ? (float)((Integer)this.posY.get()).intValue() : ((float)screenHeight - height) / 2.0f;
        if (mc.screen != null) {
            float[] drag = PotionHudModule.handleDrag(mc, originX, originY, maxWidth, height);
            originX = drag[0];
            originY = drag[1];
            this.posX.set((int)originX);
            this.posY.set((int)originY);
        } else {
            FrameHelper.release();
        }
        for (EffectEntry entry4 : this.entries) {
            float pct;
            float animHeight = entry4.animHeight;
            if (animHeight <= 0.01f) continue;
            float entryY = originY + entry4.layoutY;
            float rowHeight = entry4.animWidth;
            float totalWidth = entry4.totalWidth;
            int effectColor = PotionHudModule.effectColor(entry4.instance.getEffect());
            int red = effectColor >> 16 & 0xFF;
            int green = effectColor >> 8 & 0xFF;
            int blue = effectColor & 0xFF;
            float barX = originX + 20.0f;
            float barWidth = totalWidth - 20.0f;
            PotionHudModule.fillRadii(graphics, barX, entryY, barWidth, rowHeight, 0.0f, 4.5f, 4.5f, 0.0f, PotionHudModule.argb((int)(80.0f * animHeight), 30, 30, 35));
            float f2 = pct = entry4.instance.isInfiniteDuration() ? 1.0f : Math.max(0.0f, Math.min(1.0f, (float)entry4.instance.getDuration() / (float)Math.max(1L, entry4.originalDuration)));
            if (pct > 0.0f) {
                float fillWidth = barWidth * pct;
                float fillRadius = 4.5f * pct;
                PotionHudModule.fillRadii(graphics, barX, entryY, fillWidth, rowHeight, 0.0f, fillRadius, fillRadius, 0.0f, PotionHudModule.argb((int)(140.0f * animHeight), red, green, blue));
            }
            PotionHudModule.fillRadii(graphics, originX, entryY, 20.0f, rowHeight, 4.5f, 0.0f, 0.0f, 4.5f, PotionHudModule.argb((int)(160.0f * animHeight), (int)((float)red * 0.7f + 76.5f), (int)((float)green * 0.7f + 76.5f), (int)((float)blue * 0.7f + 76.5f)));
            int textAlpha = (int)(185.0f * animHeight);
            float nameHeight = (float)nameFont.getHeight() * nameScale;
            float ampHeight = (float)boldFont.getHeight() * ampScale;
            float nameW = FontManager.measureExact(nameFont, entry4.name, nameScale);
            float ampWidth = FontManager.measureExact(boldFont, entry4.amplifier, ampScale);
            float ampY = entryY + (rowHeight - ampHeight) / 2.0f;
            PotionHudModule.drawText(graphics, boldFont, ampScale, entry4.amplifier, originX + (20.0f - ampWidth) / 2.0f, ampY, PotionHudModule.argb(textAlpha, 255, 255, 255));
            float baseX = barX + 5.0f;
            float limitX = barX + barWidth - nameW - 5.0f;
            float nameX = baseX + (float)((Integer)this.textOffset.get()).intValue();
            if (nameX > limitX) {
                nameX = Math.max(baseX, limitX);
            }
            float nameY = entryY + (rowHeight - nameHeight) / 2.0f;
            PotionHudModule.drawText(graphics, nameFont, nameScale, entry4.name, nameX, nameY, PotionHudModule.argb(textAlpha, 255, 255, 255));
        }
        graphics.flush();
    }

    private static float[] handleDrag(Minecraft mc, float x, float y, float width, float height) {
        double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
        boolean mouseDown = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
        FrameHelper.updateMouse(mouseX, mouseY, mouseDown);
        FrameHelper.offer(OWNER, x - 5.0f, y - 5.0f, width + 10.0f, height + 10.0f);
        if (!FrameHelper.isActive(OWNER)) {
            return new float[]{x, y};
        }
        return new float[]{(float)mouseX - FrameHelper.grabOffsetX(), (float)mouseY - FrameHelper.grabOffsetY()};
    }

    @Override
    public void onEnable() {
        this.entries.clear();
    }

    private static String formatAmplifier(int amplifier) {
        switch (amplifier) {
            case 1: {
                return "I";
            }
            case 2: {
                return "II";
            }
            case 3: {
                return "III";
            }
            case 4: {
                return "IV";
            }
            case 5: {
                return "V";
            }
        }
        return String.valueOf(amplifier);
    }

    private static void fillRadiiStepped(GuiGraphics graphics, float fx, float fy, float fw, float fh, float topLeft, float topRight, float bottomRight, float bottomLeft, int color) {
        int x = Math.round(fx);
        int y = Math.round(fy);
        int w = Math.round(fw);
        int h = Math.round(fh);
        if (w <= 0 || h <= 0 || color >>> 24 == 0) {
            return;
        }
        int x1 = x + w;
        int y1 = y + h;
        int limit = Math.min(w, h) / 2;
        int rtl = Math.min((int)Math.ceil(topLeft), limit);
        int rtr = Math.min((int)Math.ceil(topRight), limit);
        int rbr = Math.min((int)Math.ceil(bottomRight), limit);
        int rbl = Math.min((int)Math.ceil(bottomLeft), limit);
        if (rtl <= 0 && rtr <= 0 && rbr <= 0 && rbl <= 0) {
            graphics.fill(x, y, x1, y1, color);
            return;
        }
        for (int row = 0; row < h; ++row) {
            int dy;
            int leftInset = 0;
            int rightInset = 0;
            if (row < rtl) {
                dy = rtl - row;
                leftInset = rtl - (int)Math.round(Math.sqrt(rtl * rtl - dy * dy));
            }
            if (row < rtr) {
                dy = rtr - row;
                rightInset = rtr - (int)Math.round(Math.sqrt(rtr * rtr - dy * dy));
            }
            if (row >= h - rbl) {
                dy = row - (h - rbl) + 1;
                leftInset = Math.max(leftInset, rbl - (int)Math.round(Math.sqrt(rbl * rbl - dy * dy)));
            }
            if (row >= h - rbr) {
                dy = row - (h - rbr) + 1;
                rightInset = Math.max(rightInset, rbr - (int)Math.round(Math.sqrt(rbr * rbr - dy * dy)));
            }
            graphics.fill(x + leftInset, y + row, x1 - rightInset, y + row + 1, color);
        }
    }

    private static int effectColor(MobEffect effect) {
        int color = effect.getColor();
        return color != 0 ? color : 3376639;
    }

    private static void drawText(GuiGraphics graphics, Font font, float scale, String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (FontManager.drawExact(graphics, font, text, x, y, color, false, scale)) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.scale(scale, scale, 1.0f);
        font.drawString(graphics, text, 0.0, 0.0, color, false);
        pose.popPose();
    }

    private void syncEntries(Collection<MobEffectInstance> active) {
        for (EffectEntry entry : this.entries) {
            boolean stillActive = false;
            for (MobEffectInstance instance : active) {
                if (instance.getEffect() != entry.instance.getEffect()) continue;
                stillActive = true;
                break;
            }
            if (stillActive) continue;
            entry.startRemove();
        }
        for (MobEffectInstance instance : active) {
            EffectEntry existing = null;
            for (EffectEntry entry : this.entries) {
                if (entry.removing || entry.instance.getEffect() != instance.getEffect()) continue;
                existing = entry;
                break;
            }
            if (existing == null) {
                this.entries.add(new EffectEntry(instance));
                continue;
            }
            existing.updateEffect(instance);
        }
    }

    private static void fillRadii(GuiGraphics graphics, float fx, float fy, float fw, float fh, float topLeft, float topRight, float bottomRight, float bottomLeft, int color) {
        if (fw <= 0.0f || fh <= 0.0f || color >>> 24 == 0) {
            return;
        }
        if (topLeft <= 0.0f && topRight <= 0.0f && bottomRight <= 0.0f && bottomLeft <= 0.0f) {
            graphics.fill(Math.round(fx), Math.round(fy), Math.round(fx + fw), Math.round(fy + fh), color);
            return;
        }
        graphics.flush();
        Shader.corners(graphics.pose().last().pose(), fx, fy, fw, fh, topLeft, topRight, bottomRight, bottomLeft, color);
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        try {
            this.render(graphics);
        }
        catch (Throwable throwable) {
            this.entries.clear();
        }
    }

    private static final class EffectEntry {
        private final Anim fadeAnim = new Anim();
        private final Anim heightAnim = new Anim();
        private final Anim widthAnim = new Anim();
        private MobEffectInstance instance;
        private long originalDuration;
        private String name;
        private String time;
        private String amplifier;
        private boolean removing;
        private boolean visible = true;
        private float rowHeight = -1.0f;
        private float totalWidth;
        private float layoutY;
        private float animHeight;
        private float animWidth;

        void refresh() {
            this.name = this.instance.getEffect().getDisplayName().getString();
            this.time = PotionHudModule.formatDuration(this.instance);
            this.amplifier = PotionHudModule.formatAmplifier(this.instance.getAmplifier() + 1);
        }

        EffectEntry(MobEffectInstance instance) {
            this.instance = instance;
            this.originalDuration = instance.getDuration();
            this.refresh();
            this.heightAnim.setValue(0.0f);
            this.widthAnim.setValue(0.0f);
        }

        void tick() {
            this.fadeAnim.tick();
            this.heightAnim.tick();
            this.widthAnim.tick();
            if (!this.instance.getEffect().isInstantenous()) {
                this.refresh();
            }
        }

        void show(float targetHeight) {
            this.rowHeight = targetHeight;
            this.heightAnim.animate(1.0f, 0.3f, 1);
            this.widthAnim.animate(targetHeight, 0.3f, 1);
        }

        void startRemove() {
            if (this.removing) {
                return;
            }
            this.removing = true;
            this.heightAnim.animate(0.0f, 0.2f, 2);
            this.widthAnim.animate(0.0f, 0.2f, 2);
        }

        boolean isRemoveDone() {
            return this.removing && this.heightAnim.isDone() && this.widthAnim.isDone();
        }

        void updateEffect(MobEffectInstance instance) {
            if (instance.getDuration() > this.instance.getDuration()) {
                this.originalDuration = instance.getDuration();
            }
            this.instance = instance;
            this.refresh();
        }

        void updateRowHeight(float targetHeight) {
            if (this.removing || this.rowHeight == targetHeight) {
                return;
            }
            this.rowHeight = targetHeight;
            this.widthAnim.animate(targetHeight, 0.3f, 1);
        }
    }

    private static final class Anim {
        private float value;
        private float start;
        private float target;
        private float duration;
        private long startTime;
        private int easing;
        private boolean animating;

        private Anim() {
        }

        float getValue() {
            return this.value;
        }

        void setValue(float value) {
            this.value = value;
            this.start = value;
            this.target = value;
            this.animating = false;
        }

        void tick() {
            if (!this.animating) {
                return;
            }
            float t = (float)(PotionHudModule.now() - this.startTime) / (this.duration * 1000.0f);
            if (t >= 1.0f) {
                this.value = this.target;
                this.animating = false;
                return;
            }
            if (t < 0.0f) {
                t = 0.0f;
            }
            this.value = this.start + (this.target - this.start) * PotionHudModule.ease(this.easing, t);
        }

        boolean isDone() {
            return !this.animating;
        }

        void animate(float target, float duration, int easing) {
            if (this.animating && Math.abs(target - this.target) < 0.01f) {
                return;
            }
            this.start = this.value;
            this.target = target;
            this.duration = duration;
            this.easing = easing;
            this.startTime = PotionHudModule.now();
            this.animating = true;
        }
    }
}

