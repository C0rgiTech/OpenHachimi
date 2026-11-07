/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.font;

import dev.openhachimi.render.InterpolationTexture;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

public class FontChar {
    public AbstractTexture texture;
    private final int height;
    private final float scale;
    private final float ascent;
    private final GlyphLayout[] charData;
    private final int from;
    private final HashMap<String, Double> widthCache;

    public FontChar(ByteBuffer fontBuffer, int fontHeight, int from, int to, int atlasSize) {
        this.height = fontHeight;
        this.from = from;
        this.widthCache = new HashMap();
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        STBTruetype.stbtt_InitFont((STBTTFontinfo)fontInfo, (ByteBuffer)fontBuffer);
        this.charData = new GlyphLayout[to - from + 1];
        STBTTPackedchar.Buffer packedChars = STBTTPackedchar.create((int)this.charData.length);
        ByteBuffer atlas = BufferUtils.createByteBuffer((int)(atlasSize * atlasSize));
        STBTTPackContext packContext = STBTTPackContext.create();
        STBTruetype.stbtt_PackBegin((STBTTPackContext)packContext, (ByteBuffer)atlas, (int)atlasSize, (int)atlasSize, (int)0, (int)1);
        STBTruetype.stbtt_PackSetOversampling((STBTTPackContext)packContext, (int)2, (int)2);
        STBTruetype.stbtt_PackFontRange((STBTTPackContext)packContext, (ByteBuffer)fontBuffer, (int)0, (float)fontHeight, (int)from, (STBTTPackedchar.Buffer)packedChars);
        STBTruetype.stbtt_PackEnd((STBTTPackContext)packContext);
        this.texture = new InterpolationTexture(atlasSize, atlasSize, atlas, InterpolationTexture.Format.A, InterpolationTexture.Filter.Linear, InterpolationTexture.Filter.Linear);
        this.scale = STBTruetype.stbtt_ScaleForPixelHeight((STBTTFontinfo)fontInfo, (float)fontHeight);
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer ascentBuf = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics((STBTTFontinfo)fontInfo, (IntBuffer)ascentBuf, null, null);
            this.ascent = ascentBuf.get(0);
        }
        float invAtlas = 1.0f / (float)atlasSize;
        for (int i = 0; i < this.charData.length; ++i) {
            STBTTPackedchar pc = (STBTTPackedchar)packedChars.get(i);
            this.charData[i] = new GlyphLayout(pc.xoff(), pc.yoff(), pc.xoff2(), pc.yoff2(), (float)pc.x0() * invAtlas, (float)pc.y0() * invAtlas, (float)pc.x1() * invAtlas, (float)pc.y1() * invAtlas, pc.xadvance());
        }
    }

    public int getFrom() {
        return this.from;
    }

    public double getHeight() {
        return this.height;
    }

    public double getWidth(String text) {
        if (text == null) {
            return 0.0;
        }
        Double cached = this.widthCache.get(text);
        if (cached != null) {
            return cached;
        }
        double width = 0.0;
        for (int i = 0; i < text.length(); ++i) {
            GlyphLayout m;
            int idx = text.charAt(i) - this.from;
            if (idx < 0 || idx >= this.charData.length) {
                idx = 0;
            }
            if ((m = this.charData[idx]) == null) continue;
            width += (double)m.xAdvance;
        }
        this.widthCache.put(text, width);
        return width;
    }

    public float getAscent() {
        return this.ascent;
    }

    public GlyphLayout[] getCharData() {
        return this.charData;
    }

    public float getScale() {
        return this.scale;
    }
}

