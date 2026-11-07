/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render.targethuds;

import dev.openhachimi.hud.DraggedHUDManager;
import dev.openhachimi.hud.RenderableHud;
import dev.openhachimi.modules.*;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Locale;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class OpaiTargetHUD
implements RenderableHud {
    private static final byte[] S_168BC2 = new byte[]{114, 114, 108, 114};  // XOR0x42 "00.0"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_B26E = new byte[]{108, 108, 108};  // XOR0x42 "..."
    private static final float WIDTH = 122.0f;
    private static final float HEIGHT = 40.0f;
    private static final float RADIUS = 6.0f;
    private static final float FACE_SIZE = 26.0f;
    private static final float FACE_RADIUS = 4.5f;
    private static final float TEXT_SIZE = 8.0f;
    private static final float EDGE_MARGIN = 2.0f;
    private static final int PANEL = -938471408;
    private static final int PANEL_EDGE = 0x1AFFFFFF;
    private static final int ACCENT = -4471809;
    private static final int TEXT = -1;
    private static final int BAR_BACK = 0x66000000;
    private static final int BAR_TRAIL = 0x66FFFFFF;
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final Health health = new Health();
    private LivingEntity target;
    private long lastActiveTime;
    private boolean visible;
    private float fade;
    private static final DraggedHUDManager.Draggable OWNER = new DraggedHUDManager.Draggable(){};

    @Override
    public void render(GuiGraphics graphics, float partialTick, TargethudModule config) {
        float fillWidth;
        Font font;
        boolean shouldShow;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null || mc.player == null || mc.level == null) {
            return;
        }
        LivingEntity resolved = OpaiTargetHUD.resolveTarget(mc);
        long now = System.currentTimeMillis();
        if (resolved != null) {
            this.lastActiveTime = now;
            if (resolved != this.target) {
                this.health.reset(resolved.getHealth(), resolved.getMaxHealth(), now);
            }
            this.target = resolved;
        }
        boolean bl = shouldShow = this.target != null && now - this.lastActiveTime < 300L;
        if (shouldShow != this.visible) {
            this.visible = shouldShow;
        }
        float targetFade = this.visible ? 1.0f : 0.0f;
        this.fade += (targetFade - this.fade) * Math.min(1.0f, 0.10666667f);
        if (Math.abs(targetFade - this.fade) < 0.01f) {
            this.fade = targetFade;
        }
        if (this.fade <= 0.01f || this.target == null) {
            return;
        }
        if (!this.target.isAlive() || this.target.isRemoved()) {
            this.target = null;
            return;
        }
        float x = ((Integer)config.posX.get()).intValue();
        float y = ((Integer)config.posY.get()).intValue();
        if (mc.screen instanceof ChatScreen) {
            float[] drag = OpaiTargetHUD.handleDrag(mc, x, y, 122.0f, 40.0f);
            x = drag[0];
            y = drag[1];
            config.posX.set((int)x);
            config.posY.set((int)y);
        }
        if ((font = FontManager.regular) == null) {
            return;
        }
        Health.Sample sample = this.health.update(this.target.getHealth(), this.target.getMaxHealth(), now);
        float fontScale = 8.0f / (float)font.getHeight();
        String name = NicknameModule.nameFor((Entity)this.target, this.target.getName().getString());
        float nameWidth = (float)font.getStringWidth(name) * fontScale;
        float labelWidth = Math.max((float)font.getStringWidth(Health.format(sample.maximum())) * fontScale, (float)font.getStringWidth("00.0") * fontScale);
        float panelWidth = Math.min(Math.max(122.0f, 35.0f + nameWidth + labelWidth + 4.0f), (float)mc.getWindow().getGuiScaledWidth() - 4.0f);
        x = Math.max(2.0f, Math.min(x, (float)mc.getWindow().getGuiScaledWidth() - panelWidth - 2.0f));
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        this.drawShadow(graphics, x, y, panelWidth, this.fade);
        RenderUtil.drawRoundedRect(graphics, x, y, panelWidth, 40.0f, 6.0f, OpaiTargetHUD.alpha(-938471408, this.fade));
        RenderUtil.drawRoundedRect(graphics, x, y, panelWidth, 40.0f, 6.0f, OpaiTargetHUD.alpha(0x1AFFFFFF, this.fade * 0.35f));
        this.drawFace(graphics, x + 3.0f, y + 3.0f, this.fade);
        String fittedName = OpaiTargetHUD.fit(font, name, panelWidth - 35.0f - labelWidth - 3.0f, fontScale);
        this.drawText(graphics, font, fittedName, x + 32.0f, y + 5.0f, OpaiTargetHUD.alpha(-1, this.fade), fontScale);
        this.drawText(graphics, font, Health.format(sample.health()), x + 32.0f + (float)font.getStringWidth(fittedName) * fontScale + 1.5f, y + 5.0f, OpaiTargetHUD.alpha(-4471809, this.fade), fontScale);
        if (((Boolean)config.showArmor.get()).booleanValue()) {
            this.drawArmor(graphics, x, y);
        }
        float barWidth = Math.max(0.0f, panelWidth - 7.0f);
        RenderUtil.drawRoundedRect(graphics, x + 3.0f, y + 32.0f, barWidth, 5.0f, 2.5f, OpaiTargetHUD.alpha(0x66000000, this.fade));
        float trailWidth = barWidth * sample.trailFraction();
        if (trailWidth > 0.0f) {
            RenderUtil.drawRoundedRect(graphics, x + 3.0f, y + 31.25f, trailWidth, 4.5f, 2.25f, OpaiTargetHUD.alpha(0x66FFFFFF, this.fade));
        }
        if ((fillWidth = barWidth * sample.fraction()) > 0.0f) {
            RenderUtil.drawRoundedRect(graphics, x + 3.0f, y + 31.25f, fillWidth, 4.5f, 2.25f, OpaiTargetHUD.alpha(-4471809, this.fade));
        }
        graphics.flush();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private void drawShadow(GuiGraphics graphics, float x, float y, float width, float fade) {
        graphics.flush();
        Shader.glow(graphics.pose().last().pose(), x, y - 1.0f, width, 40.0f, 6.0f, 2.4f, Math.round(78.0f * fade) << 24);
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

    private static LivingEntity resolveTarget(Minecraft mc) {
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
        return target;
    }

    private void drawFace(GuiGraphics graphics, float x, float y, float fade) {
        LivingEntity livingEntity = this.target;
        if (!(livingEntity instanceof AbstractClientPlayer)) {
            return;
        }
        AbstractClientPlayer player = (AbstractClientPlayer)livingEntity;
        ResourceLocation skin = player.getSkinTextureLocation();
        OpaiTargetHUD.drawRoundedTexture(graphics, skin, x, y, 26.0f, 4.5f, 0.125f, 0.125f, 0.125f, fade);
        OpaiTargetHUD.drawRoundedTexture(graphics, skin, x, y, 26.0f, 4.5f, 0.625f, 0.125f, 0.125f, fade);
    }

    private void drawArmor(GuiGraphics graphics, float x, float y) {
        if (this.target == null) {
            return;
        }
        for (int slot = 0; slot < ARMOR_SLOTS.length; ++slot) {
            ItemStack stack = this.target.getItemBySlot(ARMOR_SLOTS[slot]);
            if (stack.isEmpty()) continue;
            float itemX = x + 31.5f + (float)slot * 15.5f;
            float itemY = y + 14.5f;
            graphics.renderItem(stack, (int)itemX, (int)itemY);
        }
    }

    private static int alpha(int color, float fade) {
        int a = Math.round((float)(color >>> 24 & 0xFF) * Math.max(0.0f, Math.min(1.0f, fade)));
        return a << 24 | color & 0xFFFFFF;
    }

    private static String fit(Font font, String text, float width, float fontScale) {
        int step;
        if (text == null) {
            return "";
        }
        if ((float)font.getStringWidth(text) * fontScale <= width) {
            return text;
        }
        String suffix = "...";
        float suffixWidth = (float)font.getStringWidth(suffix) * fontScale;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i += step) {
            String candidate;
            step = 1;
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                step = 2;
            }
            if ((float)font.getStringWidth(candidate = builder.toString() + text.substring(i, i + step)) * fontScale + suffixWidth > width) break;
            builder.append(text, i, i + step);
        }
        while (builder.length() > 0 && builder.charAt(builder.length() - 1) == '§') {
            builder.setLength(builder.length() - 1);
        }
        return builder.length() == 0 ? "" : builder + suffix;
    }

    private static void drawRoundedTexture(GuiGraphics graphics, ResourceLocation tex, float x, float y, float size, float radius, float u0, float v0, float uvSize, float alpha) {
        graphics.flush();
        RoundRectRender.roundedTexture(graphics.pose().last().pose(), tex, x, y, size, radius, u0, v0, uvSize, 1.0f, 1.0f, 1.0f, alpha);
    }

    private void drawText(GuiGraphics graphics, Font font, String text, float x, float y, int color, float fontScale) {
        if (text == null || text.isEmpty() || (color >>> 24 & 0xFF) < 4) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(fontScale, fontScale, 1.0f);
        font.drawString(graphics, text, 0.0, 0.0, color, false);
        graphics.flush();
        graphics.pose().popPose();
    }

    public static final class Health {
        private static final byte[] S_1184BE = new byte[]{103, 108, 115, 36};  // XOR0x42 "%.1f"
        private float goal;
        private float maximum = 20.0f;
        private float fillFrom;
        private float trailFrom;
        private long changedAt;
        private boolean damage;
        private boolean initialized;

        public Sample update(float health, float maximum, long now) {
            float max = Health.maximum(maximum);
            float value = Health.health(health, max);
            if (!this.initialized) {
                this.reset(value, max, now);
            }
            if (value != this.goal || max != this.maximum) {
                Sample current = this.sample(now);
                this.damage = value < current.health();
                this.fillFrom = current.health();
                this.trailFrom = Math.max(current.health(), current.trail());
                this.goal = value;
                this.maximum = max;
                this.changedAt = now;
            }
            return this.sample(now);
        }

        public static String format(float health) {
            float rounded = (float)Math.round(health * 10.0f) / 10.0f;
            return rounded == (float)Math.round(rounded) ? Integer.toString(Math.round(rounded)) : String.format(Locale.ROOT, "%.1f", Float.valueOf(rounded));
        }

        public void reset(float health, float maximum, long now) {
            this.maximum = Health.maximum(maximum);
            this.fillFrom = this.goal = Health.health(health, this.maximum);
            this.trailFrom = this.goal;
            this.changedAt = now;
            this.damage = false;
            this.initialized = true;
        }

        private static float response(float from, float to, double elapsed, double decay) {
            float value = (float)((double)to + ((double)from - (double)to) * Math.exp(-elapsed / decay));
            return Math.abs(value - to) < 0.025f ? to : value;
        }

        private static float maximum(float value) {
            return Float.isFinite(value) ? Math.max(1.0f, value) : 20.0f;
        }

        public Sample sample(long now) {
            double elapsed = Math.max(0L, now - this.changedAt);
            float fill = Health.response(this.fillFrom, this.goal, elapsed, 65.0);
            float trail = this.damage ? Math.max(fill, Health.response(this.trailFrom, this.goal, Math.max(0.0, elapsed - 100.0), 220.0)) : fill;
            return new Sample(Health.health(fill, this.maximum), Health.health(trail, this.maximum), this.maximum);
        }

        private static float health(float value, float maximum) {
            if (!Float.isFinite(value)) {
                return 0.0f;
            }
            return Math.max(0.0f, Math.min(maximum, value));
        }

        public record Sample(float health, float trail, float maximum) {
            public float fraction() {
                return this.maximum <= 0.0f ? 0.0f : this.health / this.maximum;
            }

            public float trailFraction() {
                return this.maximum <= 0.0f ? 0.0f : this.trail / this.maximum;
            }
        }
    }
}

