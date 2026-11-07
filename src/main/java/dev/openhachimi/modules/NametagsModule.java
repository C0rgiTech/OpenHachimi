/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.utils.render.font.Font;
import dev.openhachimi.utils.W2SUtil;
import dev.openhachimi.utils.render.font.FontManager;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2f;

public class NametagsModule
extends Module {
    private static final byte[] S_718F4964 = new byte[]{12, 35, 47, 39, 22, 35, 37, 49};  // XOR0x42 "NameTags"
    private static final byte[] S_27941A0 = new byte[]{17, 42, 45, 53, 98, 22, 39, 35, 47};  // XOR0x42 "Show Team"
    private static final byte[] S_2713ACF = new byte[]{17, 42, 45, 53, 98, 0, 45, 54, 49};  // XOR0x42 "Show Bots"
    private static final byte[] S_4BA94A5C = new byte[]{17, 42, 45, 53, 98, 3, 48, 47, 45, 48};  // XOR0x42 "Show Armor"
    private static final byte[] S_34B53CDF = new byte[]{17, 42, 45, 53, 98, 10, 39, 35, 46, 54, 42};  // XOR0x42 "Show Health"
    private static final byte[] S_4DAA96 = new byte[]{(byte)128, (byte)229, 48, (byte)128, (byte)229, 48};
    private static final byte[] S_14AB = new byte[]{(byte)128, (byte)229, 48};
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_A7 = new byte[]{(byte)128, (byte)229};
    private static final byte[] S_2DAD9907 = new byte[]{(byte)128, (byte)229, 38, 25, 12, 43, 33, 41, 31, 98, (byte)128, (byte)229, 36, 62, 98};
    private static final byte[] S_1A9CB06C = new byte[]{(byte)128, (byte)229, 35, 22, 39, 35, 47, 98, (byte)128, (byte)229, 36, 62, 98};
    private static final byte[] S_32438B78 = new byte[]{(byte)128, (byte)229, 33, 0, 45, 54, 98, (byte)128, (byte)229, 36, 62, 98};
    private static final byte[] S_110ACD = new byte[]{98, (byte)128, (byte)229, 32, 106};
    private static final byte[] S_AE94 = new byte[]{107, (byte)128, (byte)229, 48};
    private static final byte[] S_9603A45 = new byte[]{(byte)128, (byte)229, 36, 98, 62, 98};
    private static final byte[] S_2B = new byte[]{105};
    private static final byte[] S_908 = new byte[]{10, 18};  // XOR0x42 "HP"
    public static NametagsModule instance;
    private final BooleanSetting showTeam = new BooleanSetting("Show Team", true);
    private final BooleanSetting showBots = new BooleanSetting("Show Bots", true);
    private final BooleanSetting showArmor = new BooleanSetting("Show Armor", true);
    private final BooleanSetting showHealth = new BooleanSetting("Show Health", true);
    private final Map<Player, Integer> aimTicks = new HashMap<Player, Integer>();
    private final Map<Player, Float> animationMap = new HashMap<Player, Float>();
    private final Map<Player, Vector2f> projectedPositions = new HashMap<Player, Vector2f>();

    public NametagsModule() {
        super("NameTags", Category.RENDER);
        instance = this;
        this.values.add(this.showTeam);
        this.values.add(this.showBots);
        this.values.add(this.showArmor);
        this.values.add(this.showHealth);
    }

    private String toLegacy(Component component) {
        final StringBuilder sb = new StringBuilder();
        component.visit((FormattedText.StyledContentConsumer)new FormattedText.StyledContentConsumer<Object>(){
            private static final byte[] S_A7 = new byte[]{(byte)128, (byte)229};
            private static final byte[] S_14A5 = new byte[]{(byte)128, (byte)229, 46};
            private static final byte[] S_14A8 = new byte[]{(byte)128, (byte)229, 45};
            private static final byte[] S_14A7 = new byte[]{(byte)128, (byte)229, 44};
            private static final byte[] S_14A6 = new byte[]{(byte)128, (byte)229, 47};
            private static final byte[] S_14A4 = new byte[]{(byte)128, (byte)229, 41};
            private static final byte[] S_14AB = new byte[]{(byte)128, (byte)229, 48};

            public Optional<Object> accept(Style style, String text) {
                TextColor color = style.getColor();
                if (color != null) {
                    for (ChatFormatting cf : ChatFormatting.values()) {
                        if (cf.getColor() == null || cf.getColor().intValue() != color.getValue()) continue;
                        sb.append("§").append(cf.getChar());
                        break;
                    }
                }
                if (style.isBold()) {
                    sb.append("§l");
                }
                if (style.isItalic()) {
                    sb.append("§o");
                }
                if (style.isUnderlined()) {
                    sb.append("§n");
                }
                if (style.isStrikethrough()) {
                    sb.append("§m");
                }
                if (style.isObfuscated()) {
                    sb.append("§k");
                }
                sb.append(text);
                sb.append("§r");
                return Optional.empty();
            }
        }, Style.EMPTY);
        return sb.toString().replace("§r§r", "§r");
    }

    private void renderNameTag(GuiGraphics graphics, Player player, float x, float y) {
        Minecraft mc = Minecraft.getInstance();
        Font font = FontManager.regular;
        Component nameComp = player.getDisplayName();
        if (player == mc.player) {
            nameComp = NicknameModule.rewrite(nameComp);
        }
        String displayName = this.toLegacy(nameComp);
        String prefix = "";
        if (AntinickModule.isNicked(player)) {
            prefix = prefix + "§d[Nick] §f| ";
        }
        if (TeamsModule.isSameTeam((Entity)player)) {
            prefix = prefix + "§aTeam §f| ";
        }
        if (AntibotModule.isBot((Entity)player)) {
            prefix = prefix + "§cBot §f| ";
        }
        displayName = prefix + displayName;
        if (AntinickModule.isNicked(player)) {
            displayName = displayName + " §b(" + AntinickModule.getRealName(player) + ")§r";
        }
        float health = player.getHealth();
        float absorption = player.getAbsorptionAmount();
        if (((Boolean)this.showHealth.get()).booleanValue()) {
            displayName = absorption > 0.0f ? displayName + "§f | " + Math.round(health) + "+" + Math.round(absorption) + "HP" : displayName + "§f | " + Math.round(health) + "HP";
        }
        float f = font.getHeight();
        Objects.requireNonNull(mc.font);
        float vanillaScale = Math.max(0.01f, f / (float)Math.max(1, 9));
        float textWidth = this.getMixedStringWidth(font, mc.font, displayName, vanillaScale);
        float addon = this.animationMap.getOrDefault(player, Float.valueOf(0.0f)).floatValue();
        float itemScale = 1.75f;
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        if (((Boolean)this.showArmor.get()).booleanValue()) {
            items.add(player.getMainHandItem());
            items.add(player.getItemBySlot(EquipmentSlot.HEAD));
            items.add(player.getItemBySlot(EquipmentSlot.CHEST));
            items.add(player.getItemBySlot(EquipmentSlot.LEGS));
            items.add(player.getItemBySlot(EquipmentSlot.FEET));
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.translate(0.0, -2.5, 0.0);
        pose.scale(0.5f, 0.5f, 1.0f);
        float fontHeight = font.getHeight();
        if (fontHeight < 9.0f) {
            fontHeight = 9.0f;
        }
        float drawY = -fontHeight - 4.0f - addon;
        if (((Boolean)this.showArmor.get()).booleanValue()) {
            int itemCount = 0;
            for (ItemStack stack : items) {
                if (stack.isEmpty()) continue;
                ++itemCount;
            }
            if (itemCount > 0) {
                float itemSize = 16.0f * itemScale;
                float itemGap = 4.0f;
                float totalW = (float)itemCount * itemSize + (float)(itemCount - 1) * itemGap;
                float itemX = -totalW / 2.0f;
                float itemY = drawY - itemSize - 4.0f;
                for (ItemStack stack : items) {
                    if (!stack.isEmpty() && !(stack.getItem() instanceof BlockItem)) {
                        pose.pushPose();
                        pose.translate(itemX, itemY, 0.0f);
                        pose.scale(itemScale, itemScale, 1.0f);
                        graphics.renderItem(stack, 0, 0);
                        pose.popPose();
                    }
                    if (stack.isEmpty()) continue;
                    itemX += itemSize + itemGap;
                }
            }
        }
        int bgX1 = (int)(-textWidth / 2.0f - 4.0f);
        int bgY1 = (int)drawY;
        int bgX2 = (int)(textWidth / 2.0f + 4.0f);
        int bgY2 = (int)(-addon + 2.0f);
        graphics.fill(bgX1, bgY1, bgX2, bgY2, new Color(0, 0, 0, 190).getRGB());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        float textX = -textWidth / 2.0f;
        this.drawMixedString(graphics, font, mc.font, displayName, textX, drawY + 3.0f, vanillaScale);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private boolean isIconCodePoint(int codePoint) {
        if (Character.isSupplementaryCodePoint(codePoint)) {
            return true;
        }
        if (codePoint >= 57344 && codePoint <= 63743) {
            return true;
        }
        if (codePoint >= 983040 && codePoint <= 1048573) {
            return true;
        }
        if (codePoint >= 0x100000 && codePoint <= 1114109) {
            return true;
        }
        if (codePoint >= 8192 && codePoint <= 8303) {
            return true;
        }
        if (codePoint >= 8592 && codePoint <= 8703) {
            return true;
        }
        if (codePoint >= 8960 && codePoint <= 9215) {
            return true;
        }
        if (codePoint >= 9312 && codePoint <= 9471) {
            return true;
        }
        if (codePoint >= 9472 && codePoint <= 9599) {
            return true;
        }
        if (codePoint >= 9728 && codePoint <= 10175) {
            return true;
        }
        if (codePoint >= 10496 && codePoint <= 11263) {
            return true;
        }
        if (codePoint >= 11776 && codePoint <= 11903) {
            return true;
        }
        if (codePoint >= 65024 && codePoint <= 65039) {
            return true;
        }
        return codePoint >= 65520 && codePoint <= 65535;
    }

    private float drawMixedString(GuiGraphics graphics, Font font, net.minecraft.client.gui.Font vanilla, String text, float x, float y, float vanillaScale) {
        float cursor = x;
        StringBuilder customRun = new StringBuilder();
        StringBuilder vanillaRun = new StringBuilder();
        int i = 0;
        String currentColor = "";
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);
            if (cp == 167 && i + 1 < text.length()) {
                currentColor = "§" + text.charAt(i + 1);
                customRun.appendCodePoint(cp).appendCodePoint(text.codePointAt(i + 1));
                i += 2;
                continue;
            }
            if (this.isIconCodePoint(cp)) {
                if (customRun.length() > 0) {
                    font.drawString(graphics, customRun.toString(), cursor, y, -1, false);
                    cursor += (float)font.getStringWidth(customRun.toString());
                    customRun.setLength(0);
                }
                vanillaRun.appendCodePoint(cp);
            } else {
                if (vanillaRun.length() > 0) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(cursor, y + 1.0f, 0.0f);
                    graphics.pose().scale(vanillaScale, vanillaScale, 1.0f);
                    graphics.drawString(vanilla, currentColor + vanillaRun.toString(), 0, 0, -1, false);
                    graphics.pose().popPose();
                    graphics.flush();
                    cursor += (float)vanilla.width(currentColor + vanillaRun.toString()) * vanillaScale;
                    vanillaRun.setLength(0);
                }
                customRun.appendCodePoint(cp);
            }
            i += charCount;
        }
        if (customRun.length() > 0) {
            font.drawString(graphics, customRun.toString(), cursor, y, -1, false);
            cursor += (float)font.getStringWidth(customRun.toString());
        }
        if (vanillaRun.length() > 0) {
            graphics.pose().pushPose();
            graphics.pose().translate(cursor, y + 1.0f, 0.0f);
            graphics.pose().scale(vanillaScale, vanillaScale, 1.0f);
            graphics.drawString(vanilla, currentColor + vanillaRun.toString(), 0, 0, -1, false);
            graphics.pose().popPose();
            graphics.flush();
            cursor += (float)vanilla.width(currentColor + vanillaRun.toString()) * vanillaScale;
        }
        return cursor;
    }

    private float getMixedStringWidth(Font font, net.minecraft.client.gui.Font vanilla, String text, float vanillaScale) {
        float width = 0.0f;
        StringBuilder customRun = new StringBuilder();
        StringBuilder vanillaRun = new StringBuilder();
        int i = 0;
        String currentColor = "";
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            int charCount = Character.charCount(cp);
            if (cp == 167 && i + 1 < text.length()) {
                currentColor = "§" + text.charAt(i + 1);
                customRun.appendCodePoint(cp).appendCodePoint(text.codePointAt(i + 1));
                i += 2;
                continue;
            }
            if (this.isIconCodePoint(cp)) {
                if (customRun.length() > 0) {
                    width += (float)font.getStringWidth(customRun.toString());
                    customRun.setLength(0);
                }
                vanillaRun.appendCodePoint(cp);
            } else {
                if (vanillaRun.length() > 0) {
                    width += (float)vanilla.width(currentColor + vanillaRun.toString()) * vanillaScale;
                    vanillaRun.setLength(0);
                }
                customRun.appendCodePoint(cp);
            }
            i += charCount;
        }
        if (customRun.length() > 0) {
            width += (float)font.getStringWidth(customRun.toString());
        }
        if (vanillaRun.length() > 0) {
            width += (float)vanilla.width(currentColor + vanillaRun.toString()) * vanillaScale;
        }
        return width;
    }

    public static boolean isRenderingNameTag(Entity entity) {
        return instance != null && NametagsModule.instance.enabled && entity instanceof Player && NametagsModule.instance.projectedPositions.containsKey((Player)entity);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        Entity crosshairTarget = mc.crosshairPickEntity;
        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;
            int ticks = this.aimTicks.getOrDefault(p, 0);
            if (p == crosshairTarget) {
                if (ticks < 10) {
                    ++ticks;
                }
            } else if (ticks > 0) {
                --ticks;
            }
            this.aimTicks.put(p, ticks);
            float targetAnim = ticks >= 10 ? 13.0f : 0.0f;
            float currentAnim = this.animationMap.getOrDefault(p, Float.valueOf(0.0f)).floatValue();
            currentAnim = Mth.lerp((float)0.2f, (float)currentAnim, (float)targetAnim);
            this.animationMap.put(p, Float.valueOf(currentAnim));
        }
    }

    @Override
    public void onRender3D() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        W2SUtil.updateMatrices();
        this.projectedPositions.clear();
        float pTicks = mc.getFrameTime();
        for (Player p : mc.level.players()) {
            double z;
            double y;
            double x;
            Vector2f pos;
            if (p == mc.player || TeamsModule.isSameTeam((Entity)p) && !((Boolean)this.showTeam.get()).booleanValue() || AntibotModule.isBot((Entity)p) && !((Boolean)this.showBots.get()).booleanValue() || (pos = W2SUtil.project(x = Mth.lerp((double)pTicks, (double)p.xo, (double)p.getX()), y = Mth.lerp((double)pTicks, (double)p.yo, (double)p.getY()) + (double)p.getBbHeight() + 0.25, z = Mth.lerp((double)pTicks, (double)p.zo, (double)p.getZ()))) == null) continue;
            this.projectedPositions.put(p, pos);
        }
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        for (Map.Entry<Player, Vector2f> entry : this.projectedPositions.entrySet()) {
            this.renderNameTag(graphics, entry.getKey(), entry.getValue().x, entry.getValue().y);
        }
    }
}

