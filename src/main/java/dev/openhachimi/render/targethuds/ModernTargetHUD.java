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
import dev.openhachimi.render.ShaderUniforms;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import dev.openhachimi.modules.ModuleManager;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.render.font.FontManager;
import dev.openhachimi.utils.render.shader.RoundRectRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class ModernTargetHUD
implements RenderableHud {
    private static final byte[] S_7583DA68 = new byte[]{3, 54, 54, 35, 33, 41};  // XOR0x42 "Attack"
    private static final byte[] S_10125 = new byte[]{3, 43, 47};  // XOR0x42 "Aim"
    private static final byte[] S_1184BE = new byte[]{103, 108, 115, 36};  // XOR0x42 "%.1f"
    private static final byte[] S_41C3518B = new byte[]{103, 108, 115, 36, 98, 47};  // XOR0x42 "%.1f m"
    private static final byte[] S_26F774A = new byte[]{105, 103, 108, 114, 36};  // XOR0x42 "+%.0f"
    private static final byte[] S_28BA66B = new byte[]{111, 103, 108, 115, 36};  // XOR0x42 "-%.1f"
    private static final byte[] S_26F7769 = new byte[]{105, 103, 108, 115, 36};  // XOR0x42 "+%.1f"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_2026 = new byte[]{(byte)160, (byte)194, (byte)228};
    private Player target;
    private int lastTargetId = -1;
    private int lastHurtTime;
    private long lastSeenMs;
    private long lastFrameMs = System.currentTimeMillis();
    private final float[] cardSpring = new float[2];
    private final float[] avatarSpring = new float[]{1.0f, 0.0f};
    private float healthSmooth;
    private float healthLag;
    private float prevHealth;
    private float flash;
    private float shakeT;
    private float sweepClock = -1000.0f;
    private int chipFlip = 1;
    private final List<FloatingText> chips = new ArrayList<FloatingText>();
    private static final DraggedHUDManager.Draggable OWNER = new DraggedHUDManager.Draggable(){};
    private long c2LastAttackTime;

    private static float approach(float current, float targetValue, float speed, float dt) {
        float f = 0;
        float factor = 1.0f - (float)Math.exp(-speed * dt);
        current += (targetValue - current) * factor;
        if (Math.abs(targetValue - f) < 5.0E-4f) {
            return targetValue;
        }
        return current;
    }

    private void spawnChip(float delta) {
        FloatingText ft = new FloatingText();
        if (delta < 0.0f) {
            ft.text = String.format("-%.1f", Float.valueOf(-delta));
            ft.heal = false;
        } else {
            ft.text = String.format("+%.1f", Float.valueOf(delta));
            ft.heal = true;
        }
        this.chipFlip = -this.chipFlip;
        ft.drift = 4.0f * (float)this.chipFlip;
        this.chips.add(ft);
        if (this.chips.size() > 5) {
            this.chips.remove(0);
        }
    }

    private static int lerpI(int a, int b, float t) {
        return (int)((float)a + (float)(b - a) * t);
    }

    private static String trim(Font font, String text, float maxWidth) {
        int step;
        if (text == null) {
            return "";
        }
        if (ModernTargetHUD.textWidth(font, text) <= maxWidth) {
            return text;
        }
        String ellipsis = "…";
        float ellipsisW = ModernTargetHUD.textWidth(font, ellipsis);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i += step) {
            char c = text.charAt(i);
            step = 1;
            if (c == '§' && i + 1 < text.length()) {
                step = 2;
            }
            if (ModernTargetHUD.textWidth(font, sb.toString() + text.substring(i, i + step)) + ellipsisW > maxWidth) break;
            sb.append(text, i, i + step);
        }
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == '§') {
            sb.setLength(sb.length() - 1);
        }
        return sb + ellipsis;
    }

    private static int argb(int a, int r, int g, int b) {
        if (a < 0) {
            a = 0;
        }
        if (a > 255) {
            a = 255;
        }
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static float textWidth(Font font, String text) {
        if (font != null) {
            return font.getStringWidth(text);
        }
        return Minecraft.getInstance().font.width(text);
    }

    private static void spring(float[] s, float targetV, float k, float c, float dt) {
        float step;
        for (float remaining = dt; remaining > 0.0f; remaining -= step) {
            step = remaining < 0.008f ? remaining : 0.008f;
            float a = (targetV - s[0]) * k - s[1] * c;
            s[1] = s[1] + a * step;
            s[0] = s[0] + s[1] * step;
        }
        if (Math.abs(s[0] - targetV) < 8.0E-4f && Math.abs(s[1]) < 0.01f) {
            s[0] = targetV;
            s[1] = 0.0f;
        }
    }

    @Override
    public void render(GuiGraphics g, float partialTick, TargethudModule config) {
        float f = 0;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (mc.options.hideGui) {
            return;
        }
        FontManager.init();
        long now = System.currentTimeMillis();
        float dt = (float)(now - this.lastFrameMs) / 1000.0f;
        this.lastFrameMs = now;
        if (dt < 0.001f) {
            dt = 0.001f;
        }
        if (dt > 0.05f) {
            dt = 0.05f;
        }
        LivingEntity found = null;
        if (mc.screen instanceof ChatScreen) {
            found = mc.player;
        } else {
            AimassistModule aimAssist = ModuleManager.getModule(AimassistModule.class);
            LivingEntity aimTarget = null;
            if (aimAssist != null) {
                aimTarget = aimAssist.getTarget();
            }
            if (aimAssist != null && aimAssist.enabled && aimTarget instanceof Player) {
                found = (Player)aimTarget;
            } else {
                KillauraModule killAura = ModuleManager.getModule(KillauraModule.class);
                if (killAura != null && killAura.enabled && KillauraModule.currentTarget instanceof Player) {
                    found = (Player)KillauraModule.currentTarget;
                } else {
                    if (mc.options.keyAttack.isDown() && mc.crosshairPickEntity instanceof Player) {
                        this.c2LastAttackTime = now;
                        if (config.displayMode.isMode("Attack")) {
                            found = (Player)mc.crosshairPickEntity;
                        }
                    }
                    if (config.displayMode.isMode("Attack")) {
                        if (now - this.c2LastAttackTime < 1000L && this.target != null && this.target.isAlive()) {
                            found = this.target;
                        }
                    } else if (config.displayMode.isMode("Aim") && mc.crosshairPickEntity instanceof Player) {
                        found = (Player)mc.crosshairPickEntity;
                    }
                }
            }
        }
        if (found != null) {
            this.target = (Player) found;
            this.lastSeenMs = now;
        }
        boolean show = this.target != null && now - this.lastSeenMs < 350L;
        ModernTargetHUD.spring(this.cardSpring, show ? 1.0f : 0.0f, show ? 320.0f : 520.0f, show ? 23.0f : 46.0f, dt);
        float sp = this.cardSpring[0];
        if (!show && sp < 0.02f) {
            this.target = null;
            this.lastTargetId = -1;
            this.flash = 0.0f;
            this.shakeT = 0.0f;
            this.chips.clear();
            DraggedHUDManager.release();
            return;
        }
        if (this.target == null) {
            return;
        }
        float maxHealth = Math.max(this.target.getMaxHealth(), 1.0f);
        float health = Math.max(0.0f, Math.min(this.target.getHealth(), maxHealth));
        float ratio = health / maxHealth;
        if (this.target.getId() != this.lastTargetId) {
            this.lastTargetId = this.target.getId();
            this.healthSmooth = ratio;
            this.healthLag = ratio;
            this.prevHealth = health;
            this.flash = 0.0f;
            this.shakeT = 0.0f;
            this.lastHurtTime = this.target.hurtTime;
            this.avatarSpring[0] = 0.35f;
            this.avatarSpring[1] = 0.0f;
            this.chips.clear();
            this.sweepClock = -0.55f;
        }
        float delta = health - this.prevHealth;
        if (f < -0.05f) {
            this.spawnChip(delta);
        } else if (delta > 0.45f) {
            this.spawnChip(delta);
        }
        this.prevHealth = health;
        if (this.target.hurtTime > this.lastHurtTime) {
            this.flash = 1.0f;
            this.shakeT = 1.0f;
        }
        this.lastHurtTime = this.target.hurtTime;
        ModernTargetHUD.spring(this.avatarSpring, 1.0f, 430.0f, 20.0f, dt);
        this.healthSmooth = ModernTargetHUD.approach(this.healthSmooth, ratio, 11.0f, dt);
        this.healthLag = this.healthLag > this.healthSmooth + 0.002f ? ModernTargetHUD.approach(this.healthLag, this.healthSmooth, 2.6f, dt) : this.healthSmooth;
        this.flash = ModernTargetHUD.approach(this.flash, 0.0f, 5.5f, dt);
        this.shakeT = Math.max(0.0f, this.shakeT - dt / 0.42f);
        this.sweepClock += dt;
        for (int i = this.chips.size() - 1; i >= 0; --i) {
            FloatingText ft = this.chips.get(i);
            ft.life += dt / 0.95f;
            if (!(ft.life >= 1.0f)) continue;
            this.chips.remove(i);
        }
        float fadeP = Math.max(0.0f, Math.min(sp, 1.0f));
        float fade = fadeP * fadeP * (3.0f - 2.0f * fadeP);
        float neededNameW = ModernTargetHUD.textWidth(FontManager.smallFont, NicknameModule.nameFor((Entity)this.target, this.target.getName().getString()));
        float numTextW = ModernTargetHUD.textWidth(FontManager.smallFont, String.format("%.1f", Float.valueOf(this.healthSmooth * maxHealth)));
        float neededFullW = neededNameW + numTextW + 68.0f;
        float fullW = Math.max(164.0f, neededFullW);
        float h = 50.0f;
        float r = 16.0f;
        float minW = 56.0f;
        float animW = minW + (fullW - minW) * Math.min(sp, 1.12f);
        float baseX = ((Integer)config.posX.get()).intValue();
        float baseY = ((Integer)config.posY.get()).intValue();
        if (show && mc.screen instanceof ChatScreen) {
            double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
            double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
            boolean mouseDown = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
            DraggedHUDManager.updateMouse(mouseX, mouseY, mouseDown);
            DraggedHUDManager.offer(OWNER, baseX, baseY, fullW, h);
            if (DraggedHUDManager.isActive(OWNER)) {
                baseX = (float)mouseX - DraggedHUDManager.grabOffsetX();
                baseY = (float)mouseY - DraggedHUDManager.grabOffsetY();
                config.posX.set((int)baseX);
                config.posY.set((int)baseY);
            }
        }
        float shakeOff = 0.0f;
        if (this.shakeT > 0.0f) {
            float ph2 = (1.0f - this.shakeT) * 17.5f;
            shakeOff = (float)Math.sin(ph2) * 2.6f * this.shakeT * this.shakeT;
        }
        float x = baseX + shakeOff;
        float y = baseY + (1.0f - fadeP) * 6.0f;
        float cx = x + animW / 2.0f;
        float cy = y + h / 2.0f;
        float scale = (0.88f + 0.12f * sp) * (1.0f + 0.02f * this.flash);
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0.0f);
        g.pose().scale(scale, scale, 1.0f);
        g.pose().translate(-cx, -cy, 0.0f);
        int sc = ModernTargetHUD.argb((int)(30.0f * fade), 0, 0, 0);
        ModernTargetHUD.fillRounded(g, x - 2.4f, y - 2.4f + 1.4f, animW + 4.8f, h + 4.8f, r + 1.92f, sc, sc, false);
        ModernTargetHUD.fillRounded(g, x - 0.8f, y - 0.8f, animW + 1.6f, h + 1.6f, r + 0.8f, ModernTargetHUD.argb((int)(78.0f * fade), 255, 255, 255), ModernTargetHUD.argb((int)(15.0f * fade), 255, 255, 255), false);
        float fl = this.flash * 0.55f;
        int cardTop = ModernTargetHUD.argb((int)(212.0f * fade), ModernTargetHUD.lerpI(46, 88, fl), ModernTargetHUD.lerpI(46, 40, fl), ModernTargetHUD.lerpI(50, 44, fl));
        int cardBot = ModernTargetHUD.argb((int)(226.0f * fade), ModernTargetHUD.lerpI(26, 62, fl), ModernTargetHUD.lerpI(26, 28, fl), ModernTargetHUD.lerpI(30, 32, fl));
        ModernTargetHUD.fillRounded(g, x, y, animW, h, r, cardTop, cardBot, false);
        if (Math.abs(sp - 1.0f) < 0.03f && this.sweepClock >= 0.0f) {
            float f2 = 0;
            float ph = this.sweepClock % 3.8f;
            if (f2 < 0.9f) {
                float prog = ph / 0.9f;
                float env = (float)Math.sin(Math.PI * (double)prog);
                g.enableScissor((int)(x + 2.0f), (int)(y + 2.0f), (int)(x + animW - 2.0f), (int)(y + h - 2.0f));
                ModernTargetHUD.drawSweep(g, x - 40.0f + (animW + 80.0f) * prog, y, y + h, 30.0f, 13.0f, 0.1f * fade * env);
                g.disableScissor();
            }
        }
        float ax = x + 10.0f;
        float ay = y + 10.0f;
        float avA = fade * ModernTargetHUD.stag(fadeP, 0.1f);
        if (avA > 0.02f && this.target instanceof AbstractClientPlayer) {
            ResourceLocation skin = ((AbstractClientPlayer)this.target).getSkinTextureLocation();
            float acx = ax + 15.0f;
            float acy = ay + 15.0f;
            float ap = Math.max(0.02f, Math.min(this.avatarSpring[0], 1.3f));
            g.pose().pushPose();
            g.pose().translate(acx, acy, 0.0f);
            g.pose().scale(ap, ap, 1.0f);
            g.pose().translate(-acx, -acy, 0.0f);
            int ringC = ModernTargetHUD.argb((int)(45.0f * avA), 255, 255, 255);
            ModernTargetHUD.fillRounded(g, ax - 1.0f, ay - 1.0f, 32.0f, 32.0f, 10.0f, ringC, ringC, false);
            ModernTargetHUD.drawRoundedTexture(g, skin, ax, ay, 30.0f, 9.0f, 0.125f, 0.125f, 0.125f, 1.0f, 1.0f, 1.0f, avA);
            ModernTargetHUD.drawRoundedTexture(g, skin, ax, ay, 30.0f, 9.0f, 0.625f, 0.125f, 0.125f, 1.0f, 1.0f, 1.0f, avA);
            g.pose().popPose();
        }
        float contentX = x + 50.0f;
        float contentR = x + animW - 12.0f;
        float contentW = contentR - contentX;
        Font font = FontManager.smallFont;
        int[] barCol = ModernTargetHUD.appleHealth(this.healthSmooth, this.flash);
        float s1 = ModernTargetHUD.stag(fadeP, 0.3f);
        float s2 = ModernTargetHUD.stag(fadeP, 0.44f);
        float s3 = ModernTargetHUD.stag(fadeP, 0.56f);
        if (s1 > 0.03f && contentW > 8.0f) {
            float rowY = y + 8.0f + (1.0f - s1) * 3.5f;
            String numText = String.format("%.1f", Float.valueOf(this.healthSmooth * maxHealth));
            float numW = ModernTargetHUD.textWidth(font, numText);
            ModernTargetHUD.drawText(g, font, numText, contentR - numW, rowY, ModernTargetHUD.argb((int)(255.0f * fade * s1), barCol[0], barCol[1], barCol[2]));
            String name = ModernTargetHUD.trim(font, NicknameModule.nameFor((Entity)this.target, this.target.getName().getString()), contentW - numW - 6.0f);
            ModernTargetHUD.drawText(g, font, name, contentX, rowY, ModernTargetHUD.argb((int)(255.0f * fade * s1), 245, 245, 247));
        }
        if (s2 > 0.03f && contentW > 8.0f) {
            int subA = (int)(205.0f * fade * s2);
            g.pose().pushPose();
            g.pose().translate(contentX, y + 24.5f + (1.0f - s2) * 3.5f, 0.0f);
            g.pose().scale(0.78f, 0.78f, 1.0f);
            String distText = String.format("%.1f m", Float.valueOf(mc.player.distanceTo((Entity)this.target)));
            ModernTargetHUD.drawText(g, font, distText, 0.0f, 0.0f, ModernTargetHUD.argb(subA, 152, 152, 157));
            float absorb = this.target.getAbsorptionAmount();
            if (absorb > 0.5f) {
                String abText = String.format("+%.0f", Float.valueOf(absorb));
                ModernTargetHUD.drawText(g, font, abText, contentW / 0.78f - ModernTargetHUD.textWidth(font, abText), 0.0f, ModernTargetHUD.argb(subA, 255, 214, 10));
            }
            g.pose().popPose();
        }
        if (s3 > 0.03f && contentW > 8.0f) {
            float by = y + 38.0f + (1.0f - s3) * 3.5f;
            float bh = 5.0f;
            int trackC = ModernTargetHUD.argb((int)(26.0f * fade * s3), 255, 255, 255);
            ModernTargetHUD.fillRounded(g, contentX, by, contentW, bh, bh / 2.0f, trackC, trackC, false);
            float fillW = contentW * this.healthSmooth;
            float lagW = contentW * this.healthLag;
            if (lagW > fillW + 0.5f) {
                int lagC = ModernTargetHUD.argb((int)(85.0f * fade * s3), 255, 255, 255);
                ModernTargetHUD.fillRounded(g, contentX, by, lagW, bh, bh / 2.0f, lagC, lagC, false);
            }
            if (fillW > 0.6f) {
                int cA = (int)(255.0f * fade * s3);
                int colL = ModernTargetHUD.argb(cA, (int)((float)barCol[0] * 0.84f), (int)((float)barCol[1] * 0.84f), (int)((float)barCol[2] * 0.84f));
                int colR = ModernTargetHUD.argb(cA, barCol[0], barCol[1], barCol[2]);
                ModernTargetHUD.fillRounded(g, contentX, by, fillW, bh, bh / 2.0f, colL, colR, true);
            }
        }
        for (int i = 0; i < this.chips.size(); ++i) {
            float out;
            FloatingText ft = this.chips.get(i);
            float rise = 1.0f - (1.0f - ft.life) * (1.0f - ft.life);
            float in = Math.min(ft.life / 0.12f, 1.0f);
            float ca = fade * in * (out = Math.max(0.0f, Math.min((1.0f - ft.life) / 0.38f, 1.0f)));
            if (ca <= 0.03f) continue;
            float pop = Math.min(ft.life / 0.16f, 1.0f);
            float chipX = contentR + ft.drift * rise;
            float chipY = y + 4.0f - rise * 18.0f;
            int col = ft.heal ? ModernTargetHUD.argb((int)(255.0f * ca), 48, 209, 88) : ModernTargetHUD.argb((int)(255.0f * ca), 255, 69, 58);
            g.pose().pushPose();
            g.pose().translate(chipX, chipY, 0.0f);
            float cs = 0.8f * Math.max(pop, 0.05f);
            g.pose().scale(cs, cs, 1.0f);
            ModernTargetHUD.drawText(g, font, ft.text, -ModernTargetHUD.textWidth(font, ft.text), 0.0f, col);
            g.pose().popPose();
        }
        g.pose().popPose();
    }

    private static void drawRoundedTexture(GuiGraphics g, ResourceLocation tex, float x, float y, float size, float radius, float u0, float v0, float uvSize, float tintR, float tintG, float tintB, float alpha) {
        g.flush();
        RoundRectRender.roundedTexture(g.pose().last().pose(), tex, x, y, size, radius, u0, v0, uvSize, tintR, tintG, tintB, alpha);
    }

    private static void fillRounded(GuiGraphics g, float x, float y, float w, float h, float radius, int col0, int col1, boolean horizontal) {
        if (w < 0.5f || h < 0.5f) {
            return;
        }
        float r = Math.min(radius, Math.min(w, h) / 2.0f);
        if (r < 0.0f) {
            r = 0.0f;
        }
        g.flush();
        Matrix4f matrix = g.pose().last().pose();
        if (ShaderUniforms.draw(matrix, x, y, w, h, r, col0, col1, horizontal ? 2 : 1)) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        float ca0 = (float)(col0 >>> 24) / 255.0f;
        float cr0 = (float)(col0 >> 16 & 0xFF) / 255.0f;
        float cg0 = (float)(col0 >> 8 & 0xFF) / 255.0f;
        float cb0 = (float)(col0 & 0xFF) / 255.0f;
        float ca1 = (float)(col1 >>> 24) / 255.0f;
        float cr1 = (float)(col1 >> 16 & 0xFF) / 255.0f;
        float cg1 = (float)(col1 >> 8 & 0xFF) / 255.0f;
        float cb1 = (float)(col1 & 0xFF) / 255.0f;
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        float ccx = x + w / 2.0f;
        float ccy = y + h / 2.0f;
        buf.vertex(matrix, ccx, ccy, 0.0f).color((cr0 + cr1) / 2.0f, (cg0 + cg1) / 2.0f, (cb0 + cb1) / 2.0f, (ca0 + ca1) / 2.0f).endVertex();
        float[] pcx = new float[]{x + w - r, x + w - r, x + r, x + r};
        float[] pcy = new float[]{y + r, y + h - r, y + h - r, y + r};
        int steps = 10;
        float fx = 0.0f;
        float fy = 0.0f;
        float fr = 0.0f;
        float fg = 0.0f;
        float fb = 0.0f;
        float fa = 0.0f;
        boolean first = true;
        for (int c = 0; c < 4; ++c) {
            for (int i = 0; i <= steps; ++i) {
                double ang = Math.toRadians((double)c * 90.0 + (double)i * 90.0 / (double)steps);
                float px = pcx[c] + (float)Math.sin(ang) * r;
                float py = pcy[c] - (float)Math.cos(ang) * r;
                float t = horizontal ? (px - x) / w : (py - y) / h;
                float f = t;
                if (t < 0.0f) {
                    t = 0.0f;
                }
                if (t > 1.0f) {
                    t = 1.0f;
                }
                float vr = cr0 + (cr1 - cr0) * t;
                float vg = cg0 + (cg1 - cg0) * t;
                float vb = cb0 + (cb1 - cb0) * t;
                float va = ca0 + (ca1 - ca0) * t;
                buf.vertex(matrix, px, py, 0.0f).color(vr, vg, vb, va).endVertex();
                if (!first) continue;
                fx = px;
                fy = py;
                fr = vr;
                fg = vg;
                fb = vb;
                fa = va;
                first = false;
            }
        }
        buf.vertex(matrix, fx, fy, 0.0f).color(fr, fg, fb, fa).endVertex();
        tess.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static void drawSweep(GuiGraphics g, float bx, float yTop, float yBot, float halfW, float slant, float alpha) {
        if (alpha <= 0.003f) {
            return;
        }
        g.flush();
        Matrix4f matrix = g.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int c = -1; c <= 1; ++c) {
            float colA = c == 0 ? alpha : 0.0f;
            float colX = bx + (float)c * halfW;
            buf.vertex(matrix, colX + slant, yTop, 0.0f).color(1.0f, 1.0f, 1.0f, colA).endVertex();
            buf.vertex(matrix, colX - slant, yBot, 0.0f).color(1.0f, 1.0f, 1.0f, colA).endVertex();
        }
        tess.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static void drawText(GuiGraphics g, Font font, String text, float x, float y, int color) {
        RenderSystem.disableDepthTest();
        if (font != null) {
            font.drawStringWithShadow(g, text, x, y, color);
        } else {
            g.drawString(Minecraft.getInstance().font, text, (int)x, (int)(y + 3.0f), color);
        }
        g.flush();
        RenderSystem.enableDepthTest();
    }

    private static float stag(float p, float d) {
        float t = (p - d) / (1.0f - d);
        if (t < 0.0f) {
            t = 0.0f;
        }
        if (t > 1.0f) {
            t = 1.0f;
        }
        return t * t * (3.0f - 2.0f * t);
    }

    private static int[] appleHealth(float ratio, float flash) {
        float r = Math.max(0.0f, Math.min(ratio, 1.0f));
        int[] out = new int[3];
        if (r >= 0.65f) {
            out[0] = 48;
            out[1] = 209;
            out[2] = 88;
        } else if (r >= 0.4f) {
            float t = (r - 0.4f) / 0.25f;
            out[0] = ModernTargetHUD.lerpI(255, 48, t);
            out[1] = ModernTargetHUD.lerpI(214, 209, t);
            out[2] = ModernTargetHUD.lerpI(10, 88, t);
        } else if (r >= 0.2f) {
            float t = (r - 0.2f) / 0.2f;
            out[0] = 255;
            out[1] = ModernTargetHUD.lerpI(159, 214, t);
            out[2] = 10;
        } else {
            float t = r / 0.2f;
            out[0] = 255;
            out[1] = ModernTargetHUD.lerpI(69, 159, t);
            out[2] = ModernTargetHUD.lerpI(58, 10, t);
        }
        float f = flash * 0.55f;
        out[0] = ModernTargetHUD.lerpI(out[0], 255, f);
        out[1] = ModernTargetHUD.lerpI(out[1], 255, f);
        out[2] = ModernTargetHUD.lerpI(out[2], 255, f);
        return out;
    }

    private static final class FloatingText {
        String text;
        boolean heal;
        float life;
        float drift;

        private FloatingText() {
        }
    }
}

