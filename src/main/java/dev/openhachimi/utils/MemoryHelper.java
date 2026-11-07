/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

public final class MemoryHelper {
    private final float[] opacitySpring = new float[]{0.0f, 0.0f};
    private final float[] widthSpring = new float[]{28.0f, 0.0f};
    private final float[] ySpring = new float[]{0.0f, 0.0f};
    private final float[] iconSpring = new float[]{0.0f, 0.0f};
    private final float[] countSpring = new float[]{0.0f, 0.0f};
    private final float[] trackSpring = new float[]{0.0f, 0.0f};
    private final float[] fillSpring = new float[]{0.0f, 0.0f};
    private final float[] urgencySpring = new float[]{0.0f, 0.0f};
    private final float[] slotSpring = new float[]{0.0f, 0.0f};
    private final float[] layoutWidthSpring = new float[]{28.0f, 0.0f};
    private final float[] cellsSpring = new float[]{1.0f, 0.0f};
    private final float[] digitPosition = new float[6];
    private final float[] digitVelocity = new float[6];
    private final float[] digitTarget = new float[6];
    private final float[] digitPending = new float[6];
    private final long[] digitReleaseAt = new long[6];
    private final float[] digitFlash = new float[6];
    private final float[] digitPop = new float[6];
    private final float[] digitPopVelocity = new float[6];
    private final float[] digitAlpha = new float[6];
    private final float[] digitAlphaVelocity = new float[6];
    private final List<DeltaPulse> deltaPulses = new ArrayList<DeltaPulse>();
    private boolean initialized;
    private boolean showing;
    private boolean anchorReady;
    private int anchorScreenWidth;
    private float anchorLeft;
    private float targetOffset;
    private int currentCount;
    private int previousCount;
    private int referenceCount = 64;
    private Item currentItem;
    private ItemStack displayStack = ItemStack.EMPTY;
    private long appearedAt;
    private long dismissedAt;
    private long impactAt;
    private long itemChangedAt;
    private long sweepAt;
    private long lastFrameTime;
    private int pulseDirection = 1;

    private static String createDeltaText(int delta) {
        StringBuilder text = new StringBuilder();
        if (delta > 0) {
            text.append('+');
        }
        text.append(delta);
        return text.toString();
    }

    private static void drawScaledText(GuiGraphics graphics, Font font, String text, float x, float y, float scale, int color) {
        if (color >>> 24 < 4 || scale <= 0.0f) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        if (font != null) {
            font.drawString(graphics, text, 0.0, 0.0, color);
        } else {
            graphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);
        }
        graphics.pose().popPose();
    }

    private static int wrapDigit(int value) {
        int wrapped = value % 10;
        if (wrapped < 0) {
            wrapped += 10;
        }
        return wrapped;
    }

    private static void drawCell(GuiGraphics graphics, Font font, int digit, float centerX, float y, float scale, float alpha, int color, float weight) {
        float shaped = MemoryHelper.clamp01(weight);
        float fade = shaped * shaped * (3.0f - 2.0f * shaped);
        int a = (int)(255.0f * alpha * (0.2f + 0.8f * fade));
        if (a < 4) {
            return;
        }
        String text = String.valueOf((char)(48 + digit));
        float width = MemoryHelper.textWidth(font, text) * scale;
        MemoryHelper.drawScaledText(graphics, font, text, centerX - width / 2.0f, y, scale, MemoryHelper.argb(a, color));
    }

    private static float easeOutBack(float value) {
        float shifted = value - 1.0f;
        return 1.0f + 2.70158f * shifted * shifted * shifted + 1.70158f * shifted * shifted;
    }

    private void initialize(ItemStack stack, int count, int reference, float targetOffset, long now) {
        this.initialized = true;
        this.showing = false;
        this.anchorReady = false;
        this.targetOffset = targetOffset;
        this.currentCount = count;
        this.previousCount = count;
        this.referenceCount = reference;
        this.currentItem = MemoryHelper.getItem(stack);
        this.displayStack = MemoryHelper.copyStack(stack);
        this.appearedAt = now;
        this.dismissedAt = now;
        this.impactAt = now - 460L;
        this.itemChangedAt = now - 460L;
        this.sweepAt = now - 440L;
        this.lastFrameTime = now;
        MemoryHelper.setSpring(this.opacitySpring, 0.0f);
        MemoryHelper.setSpring(this.widthSpring, 28.0f);
        MemoryHelper.setSpring(this.ySpring, targetOffset);
        MemoryHelper.setSpring(this.iconSpring, 0.0f);
        MemoryHelper.setSpring(this.countSpring, 0.0f);
        MemoryHelper.setSpring(this.trackSpring, 0.0f);
        MemoryHelper.setSpring(this.fillSpring, 0.0f);
        MemoryHelper.setSpring(this.urgencySpring, MemoryHelper.getUrgency(count));
        MemoryHelper.setSpring(this.slotSpring, 0.0f);
        MemoryHelper.setSpring(this.layoutWidthSpring, 28.0f);
        this.snapDigits(count);
    }

    public void reset() {
        this.initialized = false;
        this.showing = false;
        this.anchorReady = false;
        this.anchorScreenWidth = 0;
        this.anchorLeft = 0.0f;
        this.targetOffset = 0.0f;
        this.currentCount = 0;
        this.previousCount = 0;
        this.referenceCount = 64;
        this.currentItem = null;
        this.displayStack = ItemStack.EMPTY;
        this.appearedAt = 0L;
        this.dismissedAt = 0L;
        this.impactAt = 0L;
        this.itemChangedAt = 0L;
        this.sweepAt = 0L;
        this.lastFrameTime = MemoryHelper.nowMs();
        this.pulseDirection = 1;
        this.deltaPulses.clear();
        MemoryHelper.setSpring(this.opacitySpring, 0.0f);
        MemoryHelper.setSpring(this.widthSpring, 28.0f);
        MemoryHelper.setSpring(this.ySpring, 0.0f);
        MemoryHelper.setSpring(this.iconSpring, 0.0f);
        MemoryHelper.setSpring(this.countSpring, 0.0f);
        MemoryHelper.setSpring(this.trackSpring, 0.0f);
        MemoryHelper.setSpring(this.fillSpring, 0.0f);
        MemoryHelper.setSpring(this.urgencySpring, 0.0f);
        MemoryHelper.setSpring(this.slotSpring, 0.0f);
        MemoryHelper.setSpring(this.layoutWidthSpring, 28.0f);
        this.snapDigits(0);
    }

    private static int digitCount(int value) {
        int digits = 1;
        for (int remaining = Math.max(0, value); remaining >= 10 && digits < 6; remaining /= 10, ++digits) {
        }
        return digits;
    }

    private static Item getItem(ItemStack stack) {
        return stack == null || stack.isEmpty() ? null : stack.getItem();
    }

    private static int argb(int alpha, int rgb) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        return safeAlpha << 24 | rgb & 0xFFFFFF;
    }

    private void snapDigits(int count) {
        MemoryHelper.setSpring(this.cellsSpring, MemoryHelper.digitCount(count));
        for (int i = 0; i < 6; ++i) {
            this.digitPosition[i] = MemoryHelper.digitAt(count, i);
            this.digitVelocity[i] = 0.0f;
            this.digitTarget[i] = this.digitPosition[i];
            this.digitPending[i] = 0.0f;
            this.digitReleaseAt[i] = 0L;
            this.digitFlash[i] = 0.0f;
            this.digitPop[i] = 0.0f;
            this.digitPopVelocity[i] = 0.0f;
            this.digitAlpha[i] = i < MemoryHelper.digitCount(count) ? 1.0f : 0.0f;
            this.digitAlphaVelocity[i] = 0.0f;
        }
    }

    private static float getEntranceOffset(float targetOffset) {
        if (targetOffset < 25.0f) {
            return targetOffset - 18.0f;
        }
        if (targetOffset > 42.0f) {
            return targetOffset + 8.0f;
        }
        return targetOffset + 4.0f;
    }

    private static ItemStack copyStack(ItemStack stack) {
        return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    private void retargetDigits(int previous, int next, long now) {
        int cells = Math.max(MemoryHelper.digitCount(previous), MemoryHelper.digitCount(next));
        int cascade = 0;
        for (int i = 0; i < 6; ++i) {
            int to;
            int from = MemoryHelper.digitAt(previous, i);
            if (from == (to = MemoryHelper.digitAt(next, i)) && this.digitPending[i] == 0.0f) continue;
            float landing = MemoryHelper.wrapTarget(this.digitTarget[i] + this.digitPending[i], from, to, next >= previous);
            float shift = landing - (this.digitTarget[i] + this.digitPending[i]);
            if (Math.abs(shift) < 1.0E-4f) continue;
            int n;
            if (i == 0) {
                n = i;
                this.digitTarget[n] = this.digitTarget[n] + shift;
                this.digitFlash[i] = 1.0f;
                int n2 = i;
                this.digitPopVelocity[n2] = this.digitPopVelocity[n2] + 2.25f;
                this.digitPending[i] = 0.0f;
                this.digitReleaseAt[i] = now;
            } else {
                n = i;
                this.digitPending[n] = this.digitPending[n] + shift;
                this.digitReleaseAt[i] = now + 46L * (long)(++cascade);
            }
            if (i >= cells) break;
        }
    }

    private static int digitAt(int value, int index) {
        int remaining = Math.max(0, value);
        for (int i = 0; i < index; ++i) {
            remaining /= 10;
        }
        return remaining % 10;
    }

    private static void spring2(float[] position, float[] velocity, int index, float target, float stiffness, float damping, float deltaTime) {
        float step;
        for (float remaining = deltaTime; remaining > 0.0f; remaining -= step) {
            step = Math.min(remaining, 0.006f);
            float acceleration = (target - position[index]) * stiffness - velocity[index] * damping;
            int n = index;
            velocity[n] = velocity[n] + acceleration * step;
            int n2 = index;
            position[n2] = position[n2] + velocity[index] * step;
        }
        if (Math.abs(target - position[index]) < 8.0E-4f && Math.abs(velocity[index]) < 0.008f) {
            position[index] = target;
            velocity[index] = 0.0f;
        }
    }

    private static float wrapTarget(float current, int from, int to, boolean ascending) {
        int diff = to - from;
        if (ascending && diff < 0) {
            diff += 10;
        }
        if (!ascending && diff > 0) {
            diff -= 10;
        }
        return current + (float)diff;
    }

    public boolean isAnimating() {
        return this.initialized && (this.showing || this.opacitySpring[0] > 0.002f || this.widthSpring[0] > 28.05f || Math.abs(this.widthSpring[1]) > 0.02f);
    }

    private void triggerSweep(long now) {
        if (now - this.sweepAt >= 420L) {
            this.sweepAt = now;
        }
    }

    private void spawnDeltaPulse(int delta) {
        if (delta == 0) {
            return;
        }
        boolean positive = delta > 0;
        boolean bl = positive;
        if (!this.deltaPulses.isEmpty()) {
            DeltaPulse latest = this.deltaPulses.get(this.deltaPulses.size() - 1);
            if (latest.positive == positive && latest.life < 0.42f) {
                latest.value += delta;
                latest.text = MemoryHelper.createDeltaText(latest.value);
                latest.life = Math.min(latest.life, 0.17f);
                return;
            }
        }
        DeltaPulse pulse = new DeltaPulse();
        pulse.value = delta;
        pulse.text = MemoryHelper.createDeltaText(delta);
        pulse.positive = positive;
        pulse.downward = this.targetOffset > 42.0f;
        this.pulseDirection = -this.pulseDirection;
        pulse.drift = (float)this.pulseDirection * 2.6f;
        this.deltaPulses.add(pulse);
        if (this.deltaPulses.size() > 2) {
            this.deltaPulses.remove(0);
        }
    }

    private static int mixRgb(int first, int second, float progress) {
        float value = MemoryHelper.clamp01(progress);
        int red = (int)((float)(first >> 16 & 0xFF) + (float)((second >> 16 & 0xFF) - (first >> 16 & 0xFF)) * value);
        int green = (int)((float)(first >> 8 & 0xFF) + (float)((second >> 8 & 0xFF) - (first >> 8 & 0xFF)) * value);
        int blue = (int)((float)(first & 0xFF) + (float)((second & 0xFF) - (first & 0xFF)) * value);
        return red << 16 | green << 8 | blue;
    }

    private void drawEmptyIcon(GuiGraphics graphics, float alpha, int accent, long now) {
        float breath = (float)Math.sin((double)now / 430.0) * 0.5f + 0.5f;
        RenderUtil.drawRoundedRect(graphics, 2.5f, 2.5f, 11.0f, 11.0f, 2.2f, MemoryHelper.argb((int)((42.0f + breath * 28.0f) * alpha), accent));
        RenderUtil.drawRoundedRect(graphics, 4.2f, 4.2f, 7.6f, 7.6f, 1.4f, MemoryHelper.argb((int)((45.0f + (1.0f - breath) * 18.0f) * alpha), 0xF2F2F7));
    }

    private void drawOdometer(GuiGraphics graphics, Font font, float x, float baseY, float scale, float alpha, int color, int accent) {
        int cells = MemoryHelper.digitCount(this.currentCount);
        float cellWidth = 0.0f;
        for (int d = 0; d < 10; ++d) {
            float w = MemoryHelper.textWidth(font, String.valueOf((char)(48 + d))) * scale;
            if (!(w > cellWidth)) continue;
            cellWidth = w;
        }
        float travel = 9.4f * scale;
        float visibleCells = Math.max(1.0f, this.cellsSpring[0]);
        for (int i = 0; i < 6; ++i) {
            float cellAlpha = MemoryHelper.clamp01(this.digitAlpha[i]);
            if (cellAlpha <= 0.004f) continue;
            float column = Math.max(0.0f, visibleCells - 1.0f - (float)i);
            float cellX = x + column * cellWidth;
            float position = this.digitPosition[i];
            float lower = (float)Math.floor(position);
            float fraction = position - lower;
            int lowerDigit = MemoryHelper.wrapDigit((int)lower);
            int upperDigit = MemoryHelper.wrapDigit(lowerDigit + 1);
            float flash = MemoryHelper.clamp01(this.digitFlash[i]);
            int cellColor = flash > 0.01f ? MemoryHelper.mixRgb(color, MemoryHelper.mixRgb(0xFFFFFF, accent, 0.35f), flash * 0.55f) : color;
            float pop = MemoryHelper.clamp(this.digitPop[i], -0.35f, 0.35f);
            float settleScale = scale * (1.0f + flash * 0.03f + pop * 0.12f);
            float centerX = cellX + cellWidth / 2.0f;
            if (fraction <= 0.0015f) {
                MemoryHelper.drawCell(graphics, font, lowerDigit, centerX, baseY, settleScale, alpha * cellAlpha, cellColor, 1.0f);
                continue;
            }
            float ease = MemoryHelper.smoothStep(fraction);
            MemoryHelper.drawCell(graphics, font, lowerDigit, centerX, baseY - ease * travel, settleScale, alpha * cellAlpha, cellColor, 1.0f - ease);
            MemoryHelper.drawCell(graphics, font, upperDigit, centerX, baseY + (1.0f - ease) * travel, settleScale, alpha * cellAlpha, cellColor, ease);
        }
    }

    private static float textWidth(Font font, String text) {
        if (font != null) {
            return font.getStringWidth(text);
        }
        return Minecraft.getInstance().font.width(text);
    }

    private static float textHeight(Font font) {
        if (font != null) {
            return font.getHeight();
        }
        Objects.requireNonNull(Minecraft.getInstance().font);
        return 9.0f;
    }

    private static void spring(float[] state, float target, float stiffness, float damping, float deltaTime) {
        float step;
        for (float remaining = deltaTime; remaining > 0.0f; remaining -= step) {
            step = Math.min(remaining, 0.006f);
            float acceleration = (target - state[0]) * stiffness - state[1] * damping;
            state[1] = state[1] + acceleration * step;
            state[0] = state[0] + state[1] * step;
        }
        if (Math.abs(target - state[0]) < 8.0E-4f && Math.abs(state[1]) < 0.008f) {
            state[0] = target;
            state[1] = 0.0f;
        }
    }

    public void render(GuiGraphics graphics, ItemStack stack, int count, int reference, float targetOffset, boolean visible) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = MemoryHelper.nowMs();
        if (minecraft.player == null || minecraft.level == null || minecraft.getWindow() == null || minecraft.options.hideGui) {
            this.reset();
            return;
        }
        int safeCount = Math.max(0, count);
        int safeReference = Math.max(1, reference);
        this.targetOffset = targetOffset;
        float deltaTime = 0.0f;
        if (!this.initialized) {
            this.initialize(stack, safeCount, safeReference, targetOffset, now);
        } else {
            deltaTime = this.getDeltaTime(now);
        }
        boolean entering = visible && !this.showing;
        boolean bl = entering;
        if (entering) {
            this.syncContent(stack, safeCount, safeReference, now);
        }
        if (visible != this.showing) {
            this.setShowing(visible, targetOffset, now);
        }
        if (visible && !entering) {
            this.updateContent(stack, safeCount, safeReference, now);
        }
        this.updateDeltaPulses(deltaTime);
        this.updateDigits(deltaTime, now);
        if (!this.showing && this.isSettledHidden()) {
            this.settleHidden(targetOffset, now);
            return;
        }
        FontManager.init();
        Font numberFont = FontManager.titleFont != null ? FontManager.titleFont : FontManager.smallFont;
        Font pulseFont = FontManager.smallFont;
        float rollingWidth = this.getRollingWidth(numberFont);
        MemoryHelper.spring(this.slotSpring, rollingWidth, 340.0f, 28.0f, deltaTime);
        float numberSlot = Math.max(rollingWidth, Math.max(0.0f, this.slotSpring[0]));
        float fullWidth = 35.0f + numberSlot + 12.0f + 25.0f + 9.0f;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        float maximumWidth = Math.max(28.0f, (float)screenWidth - 12.0f);
        fullWidth = MemoryHelper.clamp(fullWidth, 28.0f, maximumWidth);
        if (this.showing && !this.anchorReady) {
            this.anchorLeft = (float)screenWidth / 2.0f - fullWidth / 2.0f;
            this.anchorScreenWidth = screenWidth;
            this.anchorReady = true;
            MemoryHelper.setSpring(this.layoutWidthSpring, fullWidth);
        } else if (this.anchorReady && this.anchorScreenWidth != screenWidth) {
            this.anchorLeft += (float)(screenWidth - this.anchorScreenWidth) / 2.0f;
            this.anchorScreenWidth = screenWidth;
        }
        MemoryHelper.spring(this.layoutWidthSpring, fullWidth, 290.0f, 29.0f, deltaTime);
        float layoutWidth = MemoryHelper.clamp(this.layoutWidthSpring[0], 28.0f, maximumWidth);
        MemoryHelper.spring(this.widthSpring, this.showing ? fullWidth : 28.0f, this.showing ? 305.0f : 420.0f, this.showing ? 26.0f : 38.0f, deltaTime);
        float width = MemoryHelper.clamp(this.widthSpring[0], 28.0f, maximumWidth);
        float expansion = MemoryHelper.getExpansion(width, fullWidth);
        boolean holdSurface = this.showing || expansion > 0.025f || Math.abs(this.widthSpring[1]) > 0.65f;
        float ratio = MemoryHelper.clamp01((float)this.currentCount / (float)this.referenceCount);
        MemoryHelper.spring(this.opacitySpring, holdSurface ? 1.0f : 0.0f, holdSurface ? 290.0f : 380.0f, holdSurface ? 25.0f : 35.0f, deltaTime);
        MemoryHelper.spring(this.ySpring, targetOffset, 290.0f, 27.0f, deltaTime);
        MemoryHelper.spring(this.iconSpring, this.showing || expansion > 0.02f ? 1.0f : 0.0f, 380.0f, 24.0f, deltaTime);
        MemoryHelper.spring(this.countSpring, expansion > 0.17f ? 1.0f : 0.0f, 345.0f, 25.0f, deltaTime);
        MemoryHelper.spring(this.trackSpring, expansion > 0.58f ? 1.0f : 0.0f, 310.0f, 26.0f, deltaTime);
        MemoryHelper.spring(this.fillSpring, holdSurface ? ratio : 0.0f, 165.0f, 22.0f, deltaTime);
        MemoryHelper.spring(this.urgencySpring, MemoryHelper.getUrgency(this.currentCount), 175.0f, 23.0f, deltaTime);
        float alpha = MemoryHelper.smoothStep(MemoryHelper.clamp01(this.opacitySpring[0]));
        if (!this.showing && alpha <= 0.002f && width <= 28.06f) {
            this.settleHidden(targetOffset, now);
            return;
        }
        if (alpha <= 0.002f || !this.anchorReady) {
            return;
        }
        float left = this.anchorLeft;
        float top = (float)screenHeight / 2.0f + this.ySpring[0];
        float centerY = top + 14.0f;
        int accent = MemoryHelper.getAccentColor(this.urgencySpring[0]);
        float impact = MemoryHelper.getEnvelope(now - Math.max(this.impactAt, this.itemChangedAt), 460);
        boolean blendEnabled = GL11.glIsEnabled((int)3042);
        boolean depthEnabled = GL11.glIsEnabled((int)2929);
        boolean cullEnabled = GL11.glIsEnabled((int)2884);
        int blendSourceRgb = GL11.glGetInteger((int)32969);
        int blendDestinationRgb = GL11.glGetInteger((int)32968);
        int blendSourceAlpha = GL11.glGetInteger((int)32971);
        int blendDestinationAlpha = GL11.glGetInteger((int)32970);
        ShaderInstance shader = RenderSystem.getShader();
        int shaderTexture = RenderSystem.getShaderTexture((int)0);
        float[] shaderColor = RenderSystem.getShaderColor();
        float shaderRed = shaderColor[0];
        float shaderGreen = shaderColor[1];
        float shaderBlue = shaderColor[2];
        float shaderAlpha = shaderColor[3];
        this.drawShadow(graphics, left, top, width, alpha);
        this.drawSurface(graphics, left, top, width, alpha, accent, impact, now);
        int clipLeft = Math.max(0, (int)Math.floor(left + 0.5f));
        int clipTop = Math.max(0, (int)Math.floor(top + 0.5f));
        int clipRight = Math.min(screenWidth, (int)Math.ceil(left + width - 0.5f));
        int clipBottom = Math.min(screenHeight, (int)Math.ceil(top + 28.0f - 0.5f));
        if (clipRight > clipLeft && clipBottom > clipTop) {
            graphics.enableScissor(clipLeft, clipTop, clipRight, clipBottom);
            this.drawSheen(graphics, left, top, width, alpha, now);
            this.drawExpansionEdge(graphics, left, top, width, alpha, accent, now);
            this.drawIcon(graphics, left, centerY, alpha, accent, impact, now);
            if (expansion > 0.08f) {
                this.drawNumber(graphics, numberFont, left + 28.0f + 7.0f, centerY, alpha, accent, expansion, now);
                this.drawTrack(graphics, left, centerY, layoutWidth, alpha, accent, expansion, now);
            }
            graphics.disableScissor();
        }
        this.drawDeltaPulses(graphics, pulseFont, left, top, width, alpha);
        graphics.flush();
        MemoryHelper.restoreRenderState(blendEnabled, depthEnabled, cullEnabled, blendSourceRgb, blendDestinationRgb, blendSourceAlpha, blendDestinationAlpha, shader, shaderTexture, shaderRed, shaderGreen, shaderBlue, shaderAlpha);
    }

    private void drawExpansionEdge(GuiGraphics graphics, float left, float top, float width, float alpha, int accent, long now) {
        float exit;
        float entrance;
        float speed = MemoryHelper.clamp01(Math.abs(this.widthSpring[1]) / 125.0f);
        float energy = Math.max(speed, Math.max((entrance = MemoryHelper.getEnvelope(now - this.appearedAt, 660)) * 0.58f, (exit = MemoryHelper.getEnvelope(now - this.dismissedAt, 390)) * 0.44f));
        if (energy <= 0.015f || width <= 29.0f) {
            return;
        }
        float x = left + width - 1.35f;
        int color = this.showing ? MemoryHelper.mixRgb(0xFFFFFF, accent, 0.16f) : MemoryHelper.mixRgb(0xFFFFFF, accent, 0.38f);
        RenderUtil.drawGradientRoundedRect(graphics, x, top + 6.0f, 0.9f, 16.0f, 0.45f, MemoryHelper.argb((int)(7.0f * alpha * energy), color), MemoryHelper.argb((int)(82.0f * alpha * energy), color), false);
    }

    private void drawIcon(GuiGraphics graphics, float left, float centerY, float alpha, int accent, float impact, long now) {
        float raw = MemoryHelper.clamp(this.iconSpring[0], 0.0f, 1.16f);
        if (raw <= 0.01f) {
            return;
        }
        float iconAlpha = alpha * MemoryHelper.clamp01(raw);
        float itemPulse = MemoryHelper.getEnvelope(now - this.itemChangedAt, 500);
        float pop = 0.73f + MemoryHelper.clamp01(raw) * 0.27f;
        float hover = (float)Math.sin((double)now / 590.0) * 0.22f * MemoryHelper.clamp01(raw);
        float sway = (float)Math.sin((double)now / 940.0) * 0.12f * MemoryHelper.clamp01(raw);
        float direction = this.currentCount >= this.previousCount ? -1.0f : 1.0f;
        float tilt = (float)Math.sin((double)now / 1100.0) * 0.85f + direction * impact * 1.75f;
        float scale = pop * (0.9f + itemPulse * 0.055f + impact * 0.03f);
        float cx = left + 14.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(cx + sway, centerY + hover, 0.0f);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(tilt));
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.pose().translate(-8.0f, -8.0f, 0.0f);
        if (this.displayStack.isEmpty()) {
            this.drawEmptyIcon(graphics, iconAlpha, accent, now);
        } else {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)iconAlpha);
            graphics.renderItem(this.displayStack, 0, 0);
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
        graphics.pose().popPose();
    }

    private void drawTrack(GuiGraphics graphics, float left, float centerY, float layoutWidth, float alpha, int accent, float expansion, long now) {
        float raw = MemoryHelper.clamp(this.trackSpring[0], 0.0f, 1.1f);
        float gate = MemoryHelper.smoothStep(MemoryHelper.clamp01((expansion - 0.52f) / 0.34f));
        float trackAlpha = alpha * MemoryHelper.clamp01(raw) * gate;
        if (trackAlpha <= 0.01f) {
            return;
        }
        float x = left + layoutWidth - 9.0f - 25.0f;
        float revealWidth = 25.0f * gate;
        float impact = MemoryHelper.getEnvelope(now - this.impactAt, 460);
        float warning = this.urgencySpring[0] > 0.52f ? ((float)Math.sin((double)now / 300.0) * 0.5f + 0.5f) * this.urgencySpring[0] : 0.0f;
        float height = 2.2f + impact * 0.35f + warning * 0.25f;
        float y = centerY - height / 2.0f;
        RenderUtil.drawRoundedRect(graphics, x, y, revealWidth, height, height / 2.0f, MemoryHelper.argb((int)((22.0f + warning * 10.0f) * trackAlpha), 0xFFFFFF));
        float ratio = MemoryHelper.clamp01(this.fillSpring[0]);
        float fillWidth = revealWidth * ratio;
        if (this.currentCount <= 0) {
            float zeroPulse = (float)Math.sin((double)now / 250.0) * 0.5f + 0.5f;
            fillWidth = Math.min(revealWidth, 1.7f + zeroPulse * 1.5f);
        }
        if (fillWidth <= 0.35f) {
            return;
        }
        int brightAccent = MemoryHelper.mixRgb(accent, 0xFFFFFF, 0.2f + impact * 0.08f);
        RenderUtil.drawGradientRoundedRect(graphics, x, y, fillWidth, height, height / 2.0f, MemoryHelper.argb((int)((196.0f + impact * 38.0f) * trackAlpha), accent), MemoryHelper.argb((int)(132.0f * trackAlpha), brightAccent), true);
        RenderUtil.drawGradientRoundedRect(graphics, x + 0.7f, y + 0.35f, Math.max(0.5f, fillWidth - 1.4f), 0.5f, 0.25f, MemoryHelper.argb((int)(38.0f * trackAlpha), 0xFFFFFF), MemoryHelper.argb(0, 0xFFFFFF), true);
    }

    private void drawNumber(GuiGraphics graphics, Font font, float x, float centerY, float alpha, int accent, float expansion, long now) {
        float raw = MemoryHelper.clamp(this.countSpring[0], 0.0f, 1.14f);
        float gate = MemoryHelper.smoothStep(MemoryHelper.clamp01((expansion - 0.14f) / 0.28f));
        float numberAlpha = alpha * MemoryHelper.clamp01(raw) * gate;
        if (numberAlpha <= 0.01f) {
            return;
        }
        float slot = Math.max(this.getRollingWidth(font), Math.max(0.0f, this.slotSpring[0]));
        float slide = (1.0f - gate) * 6.5f;
        float drawX = x - slide;
        int color = MemoryHelper.mixRgb(0xF5F5F7, accent, this.urgencySpring[0] * 0.2f);
        float impact = MemoryHelper.getEnvelope(now - this.impactAt, 460);
        float scale = 0.68f * (0.965f + MemoryHelper.clamp01(raw) * 0.035f + impact * 0.022f);
        float baseY = centerY - MemoryHelper.textHeight(font) * scale / 2.0f + 0.25f;
        this.drawOdometer(graphics, font, drawX, baseY, scale, numberAlpha, color, accent);
        if (impact > 0.02f) {
            float lineWidth = Math.max(3.5f, slot * impact);
            RenderUtil.drawGradientRoundedRect(graphics, drawX, centerY + 8.8f, lineWidth, 0.65f, 0.3f, MemoryHelper.argb((int)(70.0f * numberAlpha * impact), accent), MemoryHelper.argb(0, accent), true);
        }
    }

    private void drawDeltaPulses(GuiGraphics graphics, Font font, float left, float top, float width, float alpha) {
        for (int i = 0; i < this.deltaPulses.size(); ++i) {
            DeltaPulse pulse = this.deltaPulses.get(i);
            float appear = MemoryHelper.easeOutBack(MemoryHelper.clamp01(pulse.life / 0.16f));
            float disappear = MemoryHelper.smoothStep(MemoryHelper.clamp01((1.0f - pulse.life) / 0.3f));
            float pulseAlpha = alpha * MemoryHelper.clamp01(appear) * disappear;
            if (pulseAlpha <= 0.01f) continue;
            float travel = 1.0f - (1.0f - pulse.life) * (1.0f - pulse.life);
            float scale = 0.38f * (0.84f + appear * 0.16f);
            float textWidth = MemoryHelper.textWidth(font, pulse.text) * scale;
            float x = left + width - 6.0f - textWidth + pulse.drift * travel;
            float y = pulse.downward ? top + 28.0f + 1.0f + travel * 7.0f : top - 2.0f - travel * 7.0f;
            int color = pulse.positive ? 3199320 : 16729402;
            MemoryHelper.drawScaledText(graphics, font, pulse.text, x + 0.35f, y + 0.35f, scale, MemoryHelper.argb((int)(58.0f * pulseAlpha), 0));
            MemoryHelper.drawScaledText(graphics, font, pulse.text, x, y, scale, MemoryHelper.argb((int)(230.0f * pulseAlpha), MemoryHelper.mixRgb(color, 0xFFFFFF, 0.3f)));
        }
    }

    private static void restoreRenderState(boolean blendEnabled, boolean depthEnabled, boolean cullEnabled, int blendSourceRgb, int blendDestinationRgb, int blendSourceAlpha, int blendDestinationAlpha, final ShaderInstance shader, int shaderTexture, float red, float green, float blue, float alpha) {
        RenderSystem.setShaderColor((float)red, (float)green, (float)blue, (float)alpha);
        RenderSystem.blendFuncSeparate((int)blendSourceRgb, (int)blendDestinationRgb, (int)blendSourceAlpha, (int)blendDestinationAlpha);
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return shader;
            }
        });
        RenderSystem.setShaderTexture((int)0, (int)shaderTexture);
        if (blendEnabled) {
            RenderSystem.enableBlend();
        } else {
            RenderSystem.disableBlend();
        }
        if (depthEnabled) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        if (cullEnabled) {
            RenderSystem.enableCull();
        } else {
            RenderSystem.disableCull();
        }
    }

    private static long nowMs() {
        return System.nanoTime() / 1000000L;
    }

    private void syncContent(ItemStack stack, int count, int reference, long now) {
        this.currentCount = count;
        this.previousCount = count;
        this.referenceCount = reference;
        this.currentItem = MemoryHelper.getItem(stack);
        this.displayStack = MemoryHelper.copyStack(stack);
        this.impactAt = now - 460L;
        this.itemChangedAt = now - 460L;
        this.sweepAt = now - 440L;
        this.snapDigits(count);
    }

    private void updateContent(ItemStack stack, int count, int reference, long now) {
        Item nextItem;
        if (count != this.currentCount) {
            int delta = count - this.currentCount;
            this.previousCount = this.currentCount;
            this.retargetDigits(this.currentCount, count, now);
            this.currentCount = count;
            this.spawnDeltaPulse(delta);
        }
        if ((nextItem = MemoryHelper.getItem(stack)) != this.currentItem) {
            this.currentItem = nextItem;
            this.displayStack = MemoryHelper.copyStack(stack);
            this.itemChangedAt = now;
            this.triggerSweep(now);
            this.iconSpring[0] = Math.min(this.iconSpring[0], 0.62f);
            this.iconSpring[1] = Math.min(this.iconSpring[1] + 2.7f, 3.8f);
        }
        this.referenceCount = reference;
    }

    private void updateDeltaPulses(float deltaTime) {
        for (int i = this.deltaPulses.size() - 1; i >= 0; --i) {
            DeltaPulse pulse = this.deltaPulses.get(i);
            pulse.life += deltaTime / 0.78f;
            if (!(pulse.life >= 1.0f)) continue;
            this.deltaPulses.remove(i);
        }
    }

    private void updateDigits(float deltaTime, long now) {
        int cells = MemoryHelper.digitCount(this.currentCount);
        MemoryHelper.spring(this.cellsSpring, cells, 330.0f, 27.0f, deltaTime);
        for (int i = 0; i < 6; ++i) {
            boolean active = i < cells;
            MemoryHelper.spring2(this.digitAlpha, this.digitAlphaVelocity, i, active ? 1.0f : 0.0f, 360.0f, 26.0f, deltaTime);
            if (this.digitPending[i] != 0.0f && now >= this.digitReleaseAt[i]) {
                int n = i;
                this.digitTarget[n] = this.digitTarget[n] + this.digitPending[i];
                this.digitPending[i] = 0.0f;
                this.digitFlash[i] = 1.0f;
                int n2 = i;
                this.digitPopVelocity[n2] = this.digitPopVelocity[n2] + 2.25f;
            }
            MemoryHelper.spring2(this.digitPosition, this.digitVelocity, i, this.digitTarget[i], 205.0f, 26.5f, deltaTime);
            MemoryHelper.spring2(this.digitPop, this.digitPopVelocity, i, 0.0f, 300.0f, 15.5f, deltaTime);
            if (!(this.digitFlash[i] > 0.0f)) continue;
            this.digitFlash[i] = Math.max(0.0f, this.digitFlash[i] - deltaTime * 3.6f);
        }
    }

    private float getDeltaTime(long now) {
        float deltaTime = (float)(now - this.lastFrameTime) / 1000.0f;
        this.lastFrameTime = now;
        return MemoryHelper.clamp(deltaTime, 0.0f, 0.05f);
    }

    private void setShowing(boolean visible, float targetOffset, long now) {
        this.showing = visible;
        if (visible) {
            this.dismissedAt = 0L;
            if (this.isSettledHidden()) {
                this.anchorReady = false;
                this.appearedAt = now;
                this.deltaPulses.clear();
                MemoryHelper.setSpring(this.opacitySpring, 0.0f);
                MemoryHelper.setSpring(this.widthSpring, 28.0f);
                MemoryHelper.setSpring(this.ySpring, MemoryHelper.getEntranceOffset(targetOffset));
                MemoryHelper.setSpring(this.iconSpring, 0.0f);
                MemoryHelper.setSpring(this.countSpring, 0.0f);
                MemoryHelper.setSpring(this.trackSpring, 0.0f);
                MemoryHelper.setSpring(this.fillSpring, 0.0f);
                MemoryHelper.setSpring(this.slotSpring, 0.0f);
                MemoryHelper.setSpring(this.layoutWidthSpring, 28.0f);
            } else {
                this.sweepAt = now;
            }
        } else {
            this.dismissedAt = now;
        }
    }

    private void settleHidden(float targetOffset, long now) {
        MemoryHelper.setSpring(this.opacitySpring, 0.0f);
        MemoryHelper.setSpring(this.widthSpring, 28.0f);
        MemoryHelper.setSpring(this.ySpring, targetOffset);
        MemoryHelper.setSpring(this.iconSpring, 0.0f);
        MemoryHelper.setSpring(this.countSpring, 0.0f);
        MemoryHelper.setSpring(this.trackSpring, 0.0f);
        MemoryHelper.setSpring(this.fillSpring, 0.0f);
        MemoryHelper.setSpring(this.slotSpring, 0.0f);
        MemoryHelper.setSpring(this.layoutWidthSpring, 28.0f);
        this.deltaPulses.clear();
        this.anchorReady = false;
        this.anchorScreenWidth = 0;
        this.lastFrameTime = now;
    }

    private boolean isSettledHidden() {
        return this.opacitySpring[0] <= 0.001f && this.widthSpring[0] <= 28.04f && Math.abs(this.widthSpring[1]) < 0.02f;
    }

    private static float getEnvelope(long elapsed, int duration) {
        if (elapsed < 0L || elapsed >= (long)duration) {
            return 0.0f;
        }
        return (float)Math.sin(Math.PI * (double)elapsed / (double)duration);
    }

    private static void setSpring(float[] state, float value) {
        state[0] = value;
        state[1] = 0.0f;
    }

    private void drawShadow(GuiGraphics graphics, float left, float top, float width, float alpha) {
        RenderUtil.drawRoundedRect(graphics, left - 2.0f, top + 1.1f, width + 4.0f, 30.8f, 10.0f, MemoryHelper.argb((int)(24.0f * alpha), 0));
    }

    private float getRollingWidth(Font font) {
        float cellWidth = 0.0f;
        for (int d = 0; d < 10; ++d) {
            float w = MemoryHelper.textWidth(font, String.valueOf((char)(48 + d))) * 0.68f;
            if (!(w > cellWidth)) continue;
            cellWidth = w;
        }
        float cells = Math.max((float)MemoryHelper.digitCount(this.currentCount), Math.max(1.0f, this.cellsSpring[0]));
        return cellWidth * cells;
    }

    private void drawSurface(GuiGraphics graphics, float left, float top, float width, float alpha, int accent, float impact, long now) {
        float idle = (float)Math.sin((double)now / 1050.0) * 0.5f + 0.5f;
        float warning = this.urgencySpring[0] > 0.5f ? ((float)Math.sin((double)now / 330.0) * 0.5f + 0.5f) * this.urgencySpring[0] : 0.0f;
        RenderUtil.drawGradientRoundedRect(graphics, left - 0.65f, top - 0.65f, width + 1.3f, 29.3f, 9.4f, MemoryHelper.argb((int)((47.0f + idle * 7.0f + impact * 30.0f + warning * 15.0f) * alpha), 0xFFFFFF), MemoryHelper.argb((int)(9.0f * alpha), 0xFFFFFF), false);
        int upper = MemoryHelper.mixRgb(2566189, accent, this.urgencySpring[0] * 0.055f + impact * 0.035f);
        int lower = MemoryHelper.mixRgb(0x111216, accent, this.urgencySpring[0] * 0.02f);
        RenderUtil.drawGradientRoundedRect(graphics, left, top, width, 28.0f, 8.8f, MemoryHelper.argb((int)(233.0f * alpha), upper), MemoryHelper.argb((int)(246.0f * alpha), lower), false);
        RenderUtil.drawGradientRoundedRect(graphics, left + 5.0f, top + 0.65f, Math.max(3.0f, width - 10.0f), 0.65f, 0.3f, MemoryHelper.argb((int)((20.0f + idle * 7.0f + impact * 19.0f) * alpha), 0xFFFFFF), MemoryHelper.argb((int)(3.0f * alpha), 0xFFFFFF), true);
        float dividerHeight = 13.0f * MemoryHelper.clamp01(this.iconSpring[0]);
        if (dividerHeight > 0.5f && width > 30.0f) {
            RenderUtil.drawGradientRoundedRect(graphics, left + 28.0f - 0.45f, top + (28.0f - dividerHeight) / 2.0f, 0.75f, dividerHeight, 0.35f, MemoryHelper.argb((int)((9.0f + warning * 13.0f) * alpha), 0xFFFFFF), MemoryHelper.argb((int)((31.0f + impact * 28.0f) * alpha), 0xFFFFFF), false);
        }
    }

    private static float getExpansion(float width, float fullWidth) {
        float span = Math.max(1.0f, fullWidth - 28.0f);
        return MemoryHelper.clamp01((width - 28.0f) / span);
    }

    private static float getUrgency(int count) {
        if (count <= 0) {
            return 1.0f;
        }
        if (count < 8) {
            return 0.82f;
        }
        if (count < 16) {
            return 0.55f;
        }
        if (count < 32) {
            return 0.22f;
        }
        return 0.0f;
    }

    private void drawSheen(GuiGraphics graphics, float left, float top, float width, float alpha, long now) {
        long idlePhase;
        if (width <= 31.0f) {
            return;
        }
        float progress = -1.0f;
        float strength = 0.0f;
        long entryElapsed = now - this.appearedAt;
        if (entryElapsed >= 110L && entryElapsed < 690L) {
            progress = MemoryHelper.clamp01((float)(entryElapsed - 110L) / 580.0f);
            strength = (float)Math.sin(Math.PI * (double)progress) * 0.68f;
        } else if (entryElapsed >= 1300L && (idlePhase = (entryElapsed - 1300L) % 7200L) < 650L) {
            progress = MemoryHelper.clamp01((float)idlePhase / 650.0f);
            strength = (float)Math.sin(Math.PI * (double)progress) * 0.16f;
        }
        float eventProgress = MemoryHelper.clamp01((float)(now - this.sweepAt) / 440.0f);
        if (eventProgress < 1.0f) {
            progress = MemoryHelper.smoothStep(eventProgress);
            strength = Math.max(strength, (float)Math.sin(Math.PI * (double)eventProgress) * 0.62f);
        }
        if (progress < 0.0f || strength <= 0.01f) {
            return;
        }
        float x = left - 13.0f + (width + 26.0f) * MemoryHelper.smoothStep(progress);
        float band = 13.0f;
        int peak = (int)(23.0f * alpha * strength);
        RenderUtil.drawGradientRoundedRect(graphics, x - band, top + 1.1f, band, 25.8f, 2.2f, MemoryHelper.argb(0, 0xFFFFFF), MemoryHelper.argb(peak, 0xFFFFFF), true);
        RenderUtil.drawGradientRoundedRect(graphics, x, top + 1.1f, band, 25.8f, 2.2f, MemoryHelper.argb(peak, 0xFFFFFF), MemoryHelper.argb(0, 0xFFFFFF), true);
    }

    private static int getAccentColor(float urgency) {
        float value = MemoryHelper.clamp01(urgency);
        if (value <= 0.0f) {
            return 6607615;
        }
        if (value <= 0.55f) {
            return MemoryHelper.mixRgb(6607615, 16766474, value / 0.55f);
        }
        return MemoryHelper.mixRgb(16766474, 16729402, (value - 0.55f) / 0.45f);
    }

    private static float clamp01(float value) {
        return MemoryHelper.clamp(value, 0.0f, 1.0f);
    }

    private static float clamp(float value, float minimum, float maximum) {
        if (value < minimum) {
            return minimum;
        }
        if (value > maximum) {
            return maximum;
        }
        return value;
    }

    private static float smoothStep(float value) {
        return value * value * (3.0f - 2.0f * value);
    }

    private static final class DeltaPulse {
        private int value;
        private String text;
        private boolean positive;
        private boolean downward;
        private float life;
        private float drift;

        private DeltaPulse() {
        }
    }
}

