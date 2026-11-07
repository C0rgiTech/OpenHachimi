/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import dev.openhachimi.hud.RenderableHud;
import dev.openhachimi.modules.AimassistModule;
import dev.openhachimi.modules.KillauraModule;
import dev.openhachimi.modules.NicknameModule;
import dev.openhachimi.modules.TargethudModule;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.openhachimi.utils.render.RenderUtil;
import dev.openhachimi.utils.render.font.FontManager;
import dev.openhachimi.utils.render.shader.RoundRectRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public class TargetHUD
implements RenderableHud {
    private static final byte[] S_7583DA68 = new byte[]{3, 54, 54, 35, 33, 41};  // XOR0x42 "Attack"
    private static final byte[] S_10125 = new byte[]{3, 43, 47};  // XOR0x42 "Aim"
    private static final byte[] S_7941AA60 = new byte[]{10, 39, 35, 46, 54, 42, 120, 98, 103, 108, 115, 36};  // XOR0x42 "Health: %.1f"
    private static final FrameHelper.Draggable OWNER = new FrameHelper.Draggable(){};
    private float c2Animation;
    private AbstractClientPlayer c2LastTarget;
    private long c2LastMillis;
    private float c2AnimatedHealth = -1.0f;
    private long c2LastAttackTime;

    private static int argb(int a, int r, int g, int b) {
        if (a < 0) {
            a = 0;
        }
        if (a > 255) {
            a = 255;
        }
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override
    public void render(GuiGraphics g, float partialTick, TargethudModule config) {
        float f;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) {
            return;
        }
        AbstractClientPlayer target = null; // was LocalPlayer: assignments below are all AbstractClientPlayer
        if (mc.screen instanceof ChatScreen) {
            target = mc.player;
        } else {
            AimassistModule aimAssist = ModuleManager.getModule(AimassistModule.class);
            LivingEntity aimTarget = null;
            if (aimAssist != null) {
                aimTarget = aimAssist.getTarget();
            }
            if (aimAssist != null && aimAssist.enabled && aimTarget instanceof AbstractClientPlayer) {
                target = (AbstractClientPlayer)aimTarget;
            } else {
                KillauraModule killAura = ModuleManager.getModule(KillauraModule.class);
                if (killAura != null && killAura.enabled && KillauraModule.currentTarget instanceof AbstractClientPlayer) {
                    target = (AbstractClientPlayer)KillauraModule.currentTarget;
                } else {
                    if (mc.options.keyAttack.isDown() && mc.crosshairPickEntity instanceof AbstractClientPlayer) {
                        this.c2LastAttackTime = System.currentTimeMillis();
                        if (config.displayMode.isMode("Attack")) {
                            target = (AbstractClientPlayer)mc.crosshairPickEntity;
                        }
                    }
                    if (config.displayMode.isMode("Attack")) {
                        if (System.currentTimeMillis() - this.c2LastAttackTime < 1000L && this.c2LastTarget != null && this.c2LastTarget.isAlive()) {
                            target = this.c2LastTarget;
                        }
                    } else if (config.displayMode.isMode("Aim") && mc.crosshairPickEntity instanceof AbstractClientPlayer) {
                        target = (AbstractClientPlayer)mc.crosshairPickEntity;
                    }
                }
            }
        }
        if (target != null && !target.isAlive()) {
            target = null;
        }
        if (target != null) {
            if (this.c2LastTarget != target) {
                this.c2AnimatedHealth = target.getHealth();
            }
            this.c2LastTarget = target;
        }
        long currentMillis = System.currentTimeMillis();
        if (this.c2LastMillis == 0L) {
            this.c2LastMillis = currentMillis;
        }
        long deltaMillis = currentMillis - this.c2LastMillis;
        this.c2LastMillis = currentMillis;
        float speed = (float)deltaMillis / 400.0f;
        if (target != null) {
            this.c2Animation += speed;
            if (this.c2Animation > 1.0f) {
                this.c2Animation = 1.0f;
            }
        } else {
            this.c2Animation -= speed;
            if (this.c2Animation < 0.0f) {
                this.c2Animation = 0.0f;
            }
        }
        if (target == null && this.c2Animation <= 0.0f) {
            this.c2LastMillis = 0L;
            this.c2LastTarget = null;
            this.c2AnimatedHealth = -1.0f;
            return;
        }
        if (this.c2LastTarget == null) {
            return;
        }
        AbstractClientPlayer drawTarget = this.c2LastTarget;
        float realHealth = drawTarget.getHealth();
        if (this.c2AnimatedHealth == -1.0f) {
            f = realHealth;
        } else {
            float f2 = 0.0f; // initialised: assignment path not guaranteed
            float diff = realHealth - this.c2AnimatedHealth;
            f = Math.abs(f2) < 0.05f ? realHealth : (this.c2AnimatedHealth = this.c2AnimatedHealth + diff * (1.0f - (float)Math.pow(0.5, (double)deltaMillis / 30.0)));
        }
        this.c2AnimatedHealth = f;
        String name = NicknameModule.nameFor((Entity)drawTarget, drawTarget.getScoreboardName());
        String healthStr = String.format("Health: %.1f", Float.valueOf(this.c2AnimatedHealth));
        float nameW = FontManager.smallFont != null ? (float) FontManager.smallFont.getStringWidth(name) : 50.0f;
        float healthStrW = FontManager.smallFont != null ? (float) FontManager.smallFont.getStringWidth(healthStr) : 50.0f;
        float maxTextW = Math.max(nameW, healthStrW);
        float neededWidth = 51.0f + maxTextW + 10.0f;
        float width = Math.max(160.0f, neededWidth);
        float height = 50.0f;
        float radius = 8.0f;
        float x = ((Integer)config.posX.get()).intValue();
        float y = ((Integer)config.posY.get()).intValue();
        if (target != null && mc.screen instanceof ChatScreen) {
            double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
            boolean mouseDown = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
            FrameHelper.updateMouse(mouseX, mouseY, mouseDown);
            FrameHelper.offer(OWNER, x, y, width, height);
            if (FrameHelper.isActive(OWNER)) {
                x = (float)mouseX - FrameHelper.grabOffsetX();
                y = (float)mouseY - FrameHelper.grabOffsetY();
                config.posX.set((int)x);
                config.posY.set((int)y);
            }
        }
        float centerX = x + width / 2.0f;
        float centerY = y + height / 2.0f;
        float minScale = 0.3f;
        float scale = minScale + TargetHUD.easeOutBackC2(this.c2Animation) * (1.0f - minScale);
        g.pose().pushPose();
        g.pose().translate(centerX, centerY, 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        g.pose().translate(-centerX, -centerY, 0.0f);
        RenderUtil.drawRoundedRect(g, x, y, width, height, radius, TargetHUD.argb(100, 0, 0, 0));
        float headSize = 40.0f;
        float headX = x + 5.0f;
        float headY = y + 5.0f;
        float headRadius = 6.0f;
        float hurtTimer = Math.max(0.0f, (float)drawTarget.hurtTime - partialTick);
        float customHurt = Math.max(0.0f, hurtTimer - 2.0f);
        float hurtIntensity = (float)Math.sin((double)(customHurt / 8.0f) * Math.PI);
        if (customHurt <= 0.0f) {
            hurtIntensity = 0.0f;
        }
        float headScale = 1.0f - hurtIntensity * 0.1f;
        float gbTint = 1.0f - hurtTimer / 10.0f * 0.7f;
        g.pose().pushPose();
        float hCX = headX + headSize / 2.0f;
        float hCY = headY + headSize / 2.0f;
        g.pose().translate(hCX, hCY, 0.0f);
        g.pose().scale(headScale, headScale, 1.0f);
        g.pose().translate(-hCX, -hCY, 0.0f);
        ResourceLocation skin = drawTarget.getSkinTextureLocation();
        TargetHUD.drawRoundedTexture(g, skin, headX, headY, headSize, headRadius, 0.125f, 0.125f, 0.125f, 1.0f, gbTint, gbTint, 1.0f);
        g.pose().popPose();
        float textX = headX + headSize + 6.0f;
        int textColor = TargetHUD.argb(255, 255, 255, 255);
        g.pose().pushPose();
        g.pose().translate(textX, y + 4.0f, 0.0f);
        g.pose().scale(1.1f, 1.1f, 1.0f);
        if (FontManager.smallFont != null) {
            RenderSystem.disableDepthTest();
            FontManager.smallFont.drawString(g, name, 0.0, 0.0, textColor, false);
            FontManager.smallFont.drawString(g, healthStr, 0.0, 13.0, textColor, false);
            g.flush();
            RenderSystem.enableDepthTest();
        }
        g.pose().popPose();
        float barX = textX;
        float barY = y + 37.0f;
        float barW = width - (textX - x) - 6.0f;
        float barH = 8.0f;
        RenderUtil.drawRoundedRect(g, barX, barY, barW, barH, barH / 2.0f, TargetHUD.argb(160, 0, 0, 0));
        float healthPct = Math.max(0.0f, Math.min(this.c2AnimatedHealth / drawTarget.getMaxHealth(), 1.0f));
        if (healthPct > 0.0f) {
            float curBarW = barW * healthPct;
            int rr = (int)(255.0f * (1.0f - healthPct));
            int green = (int)(255.0f * healthPct);
            int healthColor = TargetHUD.argb(255, rr, green, 0);
            float curRadius = Math.min(barH / 2.0f, curBarW / 2.0f);
            RenderUtil.drawRoundedRect(g, barX, barY, curBarW, barH, curRadius, healthColor);
        }
        g.pose().popPose();
    }

    private static void drawRoundedTexture(GuiGraphics g, ResourceLocation tex, float x, float y, float size, float radius, float u0, float v0, float uvSize, float tintR, float tintG, float tintB, float alpha) {
        g.flush();
        RoundRectRender.roundedTexture(g.pose().last().pose(), tex, x, y, size, radius, u0, v0, uvSize, tintR, tintG, tintB, alpha);
    }

    private static float easeOutBackC2(float x) {
        float c1 = 2.5f;
        float c3 = c1 + 1.0f;
        float f = x - 1.0f;
        return 1.0f + c3 * (f * f * f) + c1 * (f * f);
    }
}

