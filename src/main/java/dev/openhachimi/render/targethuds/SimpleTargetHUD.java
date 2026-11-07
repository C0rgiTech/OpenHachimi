/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render.targethuds;

import dev.openhachimi.hud.DraggedHUDManager;
import dev.openhachimi.hud.RenderableHud;
import dev.openhachimi.modules.AimassistModule;
import dev.openhachimi.modules.KillauraModule;
import dev.openhachimi.modules.NicknameModule;
import dev.openhachimi.modules.TargethudModule;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.DoubleUnaryOperator;

import dev.openhachimi.modules.ModuleManager;
import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.font.FontManager;
import dev.openhachimi.utils.render.shader.RoundRectRender;
import dev.openhachimi.utils.render.shader.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public class SimpleTargetHUD
implements RenderableHud {
    private float lastHealth;
    private float healthDelta;
    private boolean visible;
    private long lastActiveTime;
    private LivingEntity currentTarget;
    private int lastHurtTime;
    private final Timer fadeAnim = new Timer();
    private final Timer slideAnim = new Timer();
    private final Timer contentAnim = new Timer();
    private final Timer scaleAnim = new Timer();
    private final Timer healthAnim = new Timer();
    private final Timer healthLagAnim = new Timer();
    private boolean slideStarted;
    private boolean contentStarted;
    private static final DraggedHUDManager.Draggable OWNER = new DraggedHUDManager.Draggable(){};

    public SimpleTargetHUD() {
        this.scaleAnim.setCurrent(1.0f);
        this.fadeAnim.setCurrent(0.0f);
        this.slideAnim.setCurrent(5.0f);
        this.contentAnim.setCurrent(0.0f);
    }

    @Override
    public void render(GuiGraphics graphics, float partialTick, TargethudModule config) {
        float barWidth;
        Font font;
        float fade;
        boolean shouldShow;
        boolean hasTarget;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return;
        }
        LivingEntity target = null;
        if (mc.screen instanceof ChatScreen) {
            target = mc.player;
        } else {
            LivingEntity aimTarget;
            AimassistModule aimAssist = ModuleManager.getModule(AimassistModule.class);
            LivingEntity livingEntity = aimTarget = aimAssist != null ? aimAssist.getTarget() : null;
            if (aimAssist != null && aimAssist.enabled && aimTarget instanceof LivingEntity) {
                target = aimTarget;
            } else {
                KillauraModule killAura = ModuleManager.getModule(KillauraModule.class);
                if (killAura != null && killAura.enabled && KillauraModule.currentTarget instanceof LivingEntity) {
                    target = (LivingEntity)KillauraModule.currentTarget;
                } else if (mc.crosshairPickEntity instanceof LivingEntity) {
                    target = (LivingEntity)mc.crosshairPickEntity;
                }
            }
        }
        if (target != null && !target.isAlive()) {
            target = null;
        }
        long now = System.currentTimeMillis();
        boolean bl = hasTarget = target != null;
        if (hasTarget) {
            this.lastActiveTime = now;
            if (this.currentTarget != target) {
                this.currentTarget = target;
                this.lastHealth = target.getHealth();
                this.healthDelta = 0.0f;
            }
        }
        boolean bl2 = shouldShow = hasTarget || now - this.lastActiveTime < 300L;
        if (shouldShow != this.visible || shouldShow && this.fadeAnim.value() <= 0.01f) {
            this.visible = shouldShow;
            if (this.visible) {
                this.fadeAnim.animate(1.0f, 0.35f, SimpleTargetHUD::easeOutPow3);
                this.slideAnim.setCurrent(5.0f);
                this.contentAnim.setCurrent(0.0f);
                this.scaleAnim.setCurrent(1.0f);
                this.slideStarted = false;
                this.contentStarted = false;
            } else {
                this.fadeAnim.animate(0.0f, 0.15f, SimpleTargetHUD::easeInPow3);
                this.slideAnim.animate(5.0f, 0.15f, SimpleTargetHUD::easeInPow3);
                this.contentAnim.animate(0.0f, 0.15f, SimpleTargetHUD::easeInPow3);
            }
        } else if (hasTarget && this.currentTarget != target && this.visible) {
            this.fadeAnim.animate(1.0f, 0.35f, SimpleTargetHUD::easeOutPow3);
            this.slideAnim.setCurrent(5.0f);
            this.contentAnim.setCurrent(0.0f);
            this.slideStarted = false;
            this.contentStarted = false;
        }
        this.fadeAnim.tick();
        if (this.visible) {
            if (this.fadeAnim.value() >= 0.08f && !this.slideStarted) {
                this.slideStarted = true;
                this.slideAnim.animate(0.0f, 0.3f, SimpleTargetHUD::easeOutPow3);
            }
            if (this.fadeAnim.value() >= 0.15f && !this.contentStarted) {
                this.contentStarted = true;
                this.contentAnim.animate(1.0f, 0.4f, SimpleTargetHUD::easeOutPow3);
            }
        }
        if (this.slideStarted) {
            this.slideAnim.tick();
        }
        if (this.contentStarted) {
            this.contentAnim.tick();
        }
        if ((fade = this.fadeAnim.value()) <= 0.01f) {
            return;
        }
        LivingEntity drawTarget = this.currentTarget;
        if (drawTarget == null) {
            return;
        }
        if (hasTarget) {
            if (!Mth.equal((float)this.lastHealth, (float)drawTarget.getHealth())) {
                this.healthDelta = drawTarget.getHealth() - this.lastHealth;
                this.lastHealth = drawTarget.getHealth();
            }
            float currentHealth = Math.min(drawTarget.getHealth(), 20.0f);
            float maxHealth = Math.min(drawTarget.getMaxHealth(), 20.0f);
            float ratio = maxHealth > 0.0f ? currentHealth / maxHealth : 0.0f;
            this.healthAnim.animate(ratio, 0.5f, SimpleTargetHUD::easeOutPow4);
            this.healthLagAnim.animate(ratio, 1.5f, SimpleTargetHUD::easeOutPow5);
        } else {
            this.healthDelta = 0.0f;
        }
        this.healthAnim.tick();
        this.healthLagAnim.tick();
        if (hasTarget && drawTarget.hurtTime > this.lastHurtTime) {
            this.scaleAnim.setCurrent(0.7f);
            this.scaleAnim.animate(1.0f, 1.5f, SimpleTargetHUD::easeOutElastic);
        }
        if (hasTarget) {
            this.lastHurtTime = drawTarget.hurtTime;
        }
        this.scaleAnim.tick();
        float x = ((Integer)config.posX.get()).intValue();
        float y = ((Integer)config.posY.get()).intValue();
        if (mc.screen instanceof ChatScreen) {
            float[] drag = SimpleTargetHUD.handleDrag(mc, x, y, 120.0f, 38.0f);
            x = drag[0];
            y = drag[1];
            config.posX.set((int)x);
            config.posY.set((int)y);
        }
        if ((font = FontManager.regular) == null) {
            return;
        }
        float nameScale = 15.0f / (float)font.getHeight();
        float subScale = 14.0f / (float)font.getHeight();
        float contentX = x + 4.0f + 30.0f + 4.0f;
        float contentWidth = 120.0f - (contentX - x) - 3.0f;
        float nameY = y + 5.0f;
        float healthY = y + 31.0f;
        float healthH = 3.0f;
        float healthW = contentWidth - 2.0f;
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        SimpleTargetHUD.drawBlurBackdrop(graphics, x, y, fade);
        RenderUtil.drawRoundedRect(graphics, x, y, 120.0f, 38.0f, 5.0f, (int)(70.0f * fade) << 24);
        graphics.flush();
        float scaleValue = this.scaleAnim.value();
        float minScale = Math.max(0.7f, fade);
        float combinedScale = scaleValue * minScale;
        float headSize = 30.0f * combinedScale;
        float headX = x + 4.0f + (30.0f - headSize) / 2.0f;
        float headY = y + (38.0f - headSize) / 2.0f;
        if (drawTarget instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer)drawTarget;
            ResourceLocation skin = player.getSkinTextureLocation();
            SimpleTargetHUD.drawRoundedTexture(graphics, skin, headX, headY, headSize, 5.0f * combinedScale, 0.125f, 0.125f, 0.125f, fade);
            SimpleTargetHUD.drawRoundedTexture(graphics, skin, headX, headY, headSize, 5.0f * combinedScale, 0.625f, 0.125f, 0.125f, fade);
        }
        float slideOff = this.slideAnim.value();
        float contentVal = this.contentAnim.value();
        String displayName = NicknameModule.nameFor((Entity)drawTarget, drawTarget.getName().getString());
        String displayHealth = "HP: " + (int)Math.ceil(drawTarget.getHealth());
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(contentX, nameY + 1.0f + slideOff, 0.0f);
        graphics.pose().scale(nameScale, nameScale, 1.0f);
        font.drawString(graphics, displayName, 0.0, 0.0, (int)(255.0f * fade) << 24 | 0xFFFFFF, true);
        graphics.flush();
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(contentX, nameY + 14.0f + slideOff, 0.0f);
        graphics.pose().scale(subScale, subScale, 1.0f);
        font.drawString(graphics, displayHealth, 0.0, 0.0, (int)(255.0f * fade) << 24 | 0xFFFFFF, true);
        graphics.flush();
        graphics.pose().popPose();
        RenderUtil.drawRoundedRect(graphics, contentX, healthY, healthW, healthH, healthH / 2.0f, (int)(90.0f * fade) << 24);
        float lagWidth = this.healthLagAnim.value() * healthW;
        if (lagWidth > 0.0f) {
            RenderUtil.drawRoundedRect(graphics, contentX, healthY, lagWidth, healthH, healthH / 2.0f, (int)(120.0f * fade) << 24 | 0x961E4E);
        }
        if ((barWidth = this.healthAnim.value() * healthW * contentVal) > 0.0f) {
            RenderUtil.drawRoundedRect(graphics, contentX, healthY, barWidth, healthH, healthH / 2.0f, (int)(255.0f * fade) << 24 | 0xFFFFFF);
        }
        graphics.flush();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static float[] handleDrag(Minecraft mc, float x, float y, float width, float height) {
        double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
        boolean mouseDown = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
        DraggedHUDManager.updateMouse(mouseX, mouseY, mouseDown);
        DraggedHUDManager.offer(OWNER, x, y, width, height);
        if (!DraggedHUDManager.isActive(OWNER)) {
            return new float[]{x, y};
        }
        return new float[]{(float)mouseX - DraggedHUDManager.grabOffsetX(), (float)mouseY - DraggedHUDManager.grabOffsetY()};
    }

    private static float easeOutPow3(double t) {
        return (float)(1.0 - Math.pow(1.0 - t, 3.0));
    }

    private static float easeInPow3(double t) {
        return (float)Math.pow(t, 3.0);
    }

    private static void drawRoundedTexture(GuiGraphics graphics, ResourceLocation tex, float x, float y, float size, float radius, float u0, float v0, float uvSize, float alpha) {
        graphics.flush();
        RoundRectRender.roundedTexture(graphics.pose().last().pose(), tex, x, y, size, radius, u0, v0, uvSize, 1.0f, 1.0f, 1.0f, alpha);
    }

    private static float easeOutElastic(double t) {
        if (t == 0.0 || t == 1.0) {
            return (float)t;
        }
        return (float)(Math.pow(2.0, -10.0 * t) * Math.sin((t * 10.0 - 0.75) * 2.0943951023931953) + 1.0);
    }

    private static void drawBlurBackdrop(GuiGraphics graphics, float x, float y, float fade) {
        graphics.flush();
        Shader.glow(graphics.pose().last().pose(), x, y, 120.0f, 38.0f, 5.0f, 5.0f, Math.round(96.0f * fade) << 24);
    }

    private static float easeOutPow5(double t) {
        return (float)(1.0 - Math.pow(1.0 - t, 5.0));
    }

    private static float easeOutPow4(double t) {
        return (float)(1.0 - Math.pow(1.0 - t, 4.0));
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

        public float value() {
            return this.value;
        }

        public void tick() {
            float t = (float)((double)System.currentTimeMillis() - (double)this.start) / this.durationMs;
            if (t < 0.0f) {
                t = 0.0f;
            }
            if (t > 1.0f) {
                t = 1.0f;
            }
            this.value = (float)((double)this.from + ((double)this.to - (double)this.from) * this.easing.applyAsDouble(t));
        }

        public void setCurrent(float v) {
            this.value = v;
            this.from = v;
            this.to = v;
        }

        public void animate(float target, float durationSec, DoubleUnaryOperator easingFn) {
            this.from = this.value;
            this.to = target;
            this.durationMs = Math.max(1.0f, durationSec * 1000.0f);
            this.start = System.currentTimeMillis();
            this.easing = easingFn;
        }
    }
}

