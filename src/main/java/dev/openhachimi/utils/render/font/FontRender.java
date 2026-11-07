/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public class FontRender {
    private final FontChar font;

    public FontRender(ByteBuffer buffer, int fontHeight, int from, int to, int atlasSize) {
        this.font = new FontChar(buffer, fontHeight, from, to, atlasSize);
    }

    public float drawString(GuiGraphics guiGraphics, String text, double x, double y, int color) {
        return this.drawString(guiGraphics, text, x, y, color, false);
    }

    public float drawString(GuiGraphics guiGraphics, String text, double x, double y, int color, boolean shadow) {
        if (text == null) {
            return 0.0f;
        }
        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }
        if (shadow) {
            color = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
        }
        guiGraphics.flush();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        Matrix4f matrix = pose.last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShaderTexture((int)0, (int)this.font.texture.getId());
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorTexShader();
            }
        });
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >> 24 & 0xFF;
        double baseY = y + (double)(this.font.getAscent() * this.font.getScale());
        double penX = x;
        int from = this.font.getFrom();
        GlyphLayout[] charData = this.font.getCharData();
        for (int i = 0; i < text.length(); ++i) {
            GlyphLayout m;
            int idx = text.charAt(i) - from;
            if (idx < 0 || idx >= charData.length) {
                idx = 0;
            }
            if ((m = charData[idx]) == null) continue;
            float sx0 = (float)(penX + (double)m.x0);
            float sy0 = (float)(baseY + (double)m.y0);
            float sx1 = (float)(penX + (double)m.x1);
            float sy1 = (float)(baseY + (double)m.y1);
            builder.vertex(matrix, sx0, sy1, 0.0f).color(r, g, b, a).uv(m.u0, m.v1).endVertex();
            builder.vertex(matrix, sx1, sy1, 0.0f).color(r, g, b, a).uv(m.u1, m.v1).endVertex();
            builder.vertex(matrix, sx1, sy0, 0.0f).color(r, g, b, a).uv(m.u1, m.v0).endVertex();
            builder.vertex(matrix, sx0, sy0, 0.0f).color(r, g, b, a).uv(m.u0, m.v0).endVertex();
            penX += (double)m.xAdvance;
        }
        tesselator.end();
        RenderSystem.disableBlend();
        pose.popPose();
        return (float)penX;
    }

    public int getHeight() {
        return (int)this.font.getHeight();
    }

    public int getStringWidth(String text) {
        return (int)Math.round(this.font.getWidth(text));
    }

    public FontChar getFont() {
        return this.font;
    }

    public float drawStringWithShadow(GuiGraphics guiGraphics, String text, double x, double y, int color) {
        float w1 = this.drawString(guiGraphics, text, x + 0.5, y + 0.5, color, true);
        float w2 = this.drawString(guiGraphics, text, x, y, color, false);
        return Math.max(w1, w2);
    }
}

