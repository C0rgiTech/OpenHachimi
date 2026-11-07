/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.font;

import dev.openhachimi.modules.ClickguiModule;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.Supplier;

import dev.openhachimi.modules.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public class Font {
    private static final byte[] S_711D735B = new byte[]{20, 35, 44, 43, 46, 46, 35};  // XOR0x42 "Vanilla"
    private static final byte[] S_799994CD = new byte[]{114, 115, 112, 113, 118, 119, 116, 117, 122, 123, 35, 32, 33, 38, 39, 36, 41, 46, 47, 44, 45, 48};  // XOR0x42 "0123456789abcdefklmnor"
    private final TrueTypeFont font;
    private final TrueTypeFont[] blocks;
    private final int[] colorCode = new int[32];

    public Font(ByteBuffer buffer, int height, int charRangeFrom, int charRangeTo, int textureSize, int oversample) {
        this.font = new TrueTypeFont(buffer, height, charRangeFrom, charRangeTo, textureSize, oversample);
        this.blocks = null;
        this.setupMinecraftColorcodes();
    }

    public Font(ByteBuffer buffer, int height, int charRangeFrom, int charRangeTo, int textureSize) {
        this(buffer, height, charRangeFrom, charRangeTo, textureSize, 2);
    }

    public Font(TrueTypeFont primary, TrueTypeFont[] blocks) {
        this.font = primary;
        this.blocks = blocks;
        this.setupMinecraftColorcodes();
    }

    public float drawString(GuiGraphics graphics, String text, double x, double y, int color, boolean shadow) {
        if (text == null) {
            return 0.0f;
        }
        if (this.useVanilla()) {
            double d = this.font.getHeight();
            Objects.requireNonNull(Minecraft.getInstance().font);
            float scale = (float)(d / 9.0);
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);
            int endX = graphics.drawString(Minecraft.getInstance().font, text, (int)(x / (double)scale), (int)(y / (double)scale), color, shadow);
            graphics.pose().popPose();
            return (float)endX * scale;
        }
        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }
        if (shadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }
        graphics.flush();
        graphics.pose().pushPose();
        Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionTexColorShader();
            }
        });
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = null;
        TrueTypeFont boundBlock = null;
        Color currentColor = new Color(color, true);
        double renderY = y + (double)(this.font.getAscent() * this.font.getScale());
        double renderX = x;
        float scale = 1.0f;
        int size = text.length();
        for (int i = 0; i < size; ++i) {
            char code = text.charAt(i);
            if (code == '§' && i + 1 < size) {
                char ctrl = text.charAt(i + 1);
                if ((ctrl == 'x' || ctrl == 'X') && i + 13 < size && TrueTypeFont.isLegacyHexColorSequence(text, i)) {
                    int rgb;
                    if (!shadow && (rgb = TrueTypeFont.parseLegacyHexColor(text, i)) != -1) {
                        currentColor = new Color(rgb);
                    }
                    i += 13;
                    continue;
                }
                int colorIndex = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(ctrl));
                if (colorIndex < 16 && colorIndex >= 0 && !shadow) {
                    int colorcode = this.colorCode[colorIndex];
                    currentColor = new Color(colorcode);
                } else if (colorIndex == 21 && !shadow) {
                    currentColor = new Color(color, true);
                }
                ++i;
                continue;
            }
            TrueTypeFont blk = this.blockFor(code);
            Glyph c = blk.metricsFor(code);
            if (c == null) continue;
            if (blk != boundBlock) {
                if (boundBlock != null) {
                    tesselator.end();
                }
                RenderSystem.setShaderTexture((int)0, (int)blk.texture.getId());
                bufferBuilder = tesselator.getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                boundBlock = blk;
            }
            int r = currentColor.getRed();
            int g = currentColor.getGreen();
            int b = currentColor.getBlue();
            int a = currentColor.getAlpha();
            float x0 = (float)(renderX + (double)(c.x0 * scale));
            float y0 = (float)(renderY + (double)(c.y0 * scale));
            float x1 = (float)(renderX + (double)(c.x1 * scale));
            float y1 = (float)(renderY + (double)(c.y1 * scale));
            bufferBuilder.vertex(matrix, x0, y1, 0.0f).uv(c.u0, c.v1).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(matrix, x1, y1, 0.0f).uv(c.u1, c.v1).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(matrix, x1, y0, 0.0f).uv(c.u1, c.v0).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(matrix, x0, y0, 0.0f).uv(c.u0, c.v0).color(r, g, b, a).endVertex();
            renderX += (double)c.xAdvance * (double)scale;
        }
        if (boundBlock != null) {
            tesselator.end();
        }
        RenderSystem.disableBlend();
        graphics.pose().popPose();
        return (float)renderX;
    }

    public float drawString(GuiGraphics graphics, String text, double x, double y, int color) {
        if (this.useVanilla()) {
            double d = this.font.getHeight();
            Objects.requireNonNull(Minecraft.getInstance().font);
            float scale = (float)(d / 9.0);
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);
            int endX = graphics.drawString(Minecraft.getInstance().font, text, (int)(x / (double)scale), (int)(y / (double)scale), color, false);
            graphics.pose().popPose();
            return (float)endX * scale;
        }
        return this.drawString(graphics, text, x, y, color, false);
    }

    public int getHeight() {
        return (int)this.font.getHeight();
    }

    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        }
        if (this.useVanilla()) {
            double d = this.font.getHeight();
            Objects.requireNonNull(Minecraft.getInstance().font);
            float scale = (float)(d / 9.0);
            return (int)((float)Minecraft.getInstance().font.width(text) * scale);
        }
        if (!this.useBlocks()) {
            return (int)Math.round(this.font.getWidth(text));
        }
        double width = 0.0;
        for (int i = 0; i < text.length(); ++i) {
            Glyph m = this.blockFor(text.charAt(i)).metricsFor(text.charAt(i));
            if (m == null) continue;
            width += (double)m.xAdvance;
        }
        return (int)Math.round(width);
    }

    public TrueTypeFont getFont() {
        return this.font;
    }

    public TrueTypeFont[] getBlocks() {
        return this.blocks;
    }

    private boolean useVanilla() {
        if (this.font == null) {
            return true;
        }
        if (!this.font.isPackOk()) {
            return true;
        }
        return ModuleManager.getModule(ClickguiModule.class) != null && ModuleManager.getModule(ClickguiModule.class).fontMode.isMode("Vanilla");
    }

    private boolean useBlocks() {
        return this.blocks != null && this.blocks.length > 0;
    }

    private TrueTypeFont blockFor(int code) {
        if (!this.useBlocks()) {
            return this.font;
        }
        TrueTypeFont[] arr = this.blocks;
        for (int i = 0; i < arr.length; ++i) {
            TrueTypeFont b = arr[i];
            if (b == null || code < b.getFrom() || code > b.getTo()) continue;
            return b;
        }
        return this.font;
    }

    public float drawStringWithShadow(GuiGraphics graphics, String text, double x, double y, int color) {
        if (this.useVanilla()) {
            double d = this.font.getHeight();
            Objects.requireNonNull(Minecraft.getInstance().font);
            float scale = (float)(d / 9.0);
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1.0f);
            int endX = graphics.drawString(Minecraft.getInstance().font, text, (int)(x / (double)scale), (int)(y / (double)scale), color, true);
            graphics.pose().popPose();
            return (float)endX * scale;
        }
        float shadowWidth = this.drawString(graphics, text, x + 0.5, y + 0.5, color, true);
        return Math.max(shadowWidth, this.drawString(graphics, text, x, y, color, false));
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; ++index) {
            int noClue = (index >> 3 & 1) * 85;
            int red = (index >> 2 & 1) * 170 + noClue;
            int green = (index >> 1 & 1) * 170 + noClue;
            int blue = (index >> 0 & 1) * 170 + noClue;
            if (index == 6) {
                red += 85;
            }
            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCode[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
    }
}

