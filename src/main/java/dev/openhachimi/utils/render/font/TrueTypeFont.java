/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.font;

import dev.openhachimi.render.DynamicTexture2D;

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

public class TrueTypeFont {
    public static final int DEFAULT_OVERSAMPLE = 2;
    public static final int MAX_OVERSAMPLE = 4;
    public static final int MAX_ATLAS = 8192;
    public AbstractTexture texture;
    private boolean packOk;
    private final int height;
    private final float scale;
    private final float ascent;
    private final Glyph[] charData;
    private final int from;
    private final int oversample;
    private final int atlasSize;
    private final HashMap<String, Double> widthCache = new HashMap();

    public TrueTypeFont(ByteBuffer buffer, int height, int charRangeFrom, int charRangeTo, int requestedSize, int requestedOversample) {
        int wantOversample;
        this.height = height;
        this.from = charRangeFrom;
        STBTTFontinfo fontInfo = STBTTFontinfo.create();
        buffer.position(0);
        STBTruetype.stbtt_InitFont((STBTTFontinfo)fontInfo, (ByteBuffer)buffer);
        int count = charRangeTo + 1 - charRangeFrom;
        int os = wantOversample = requestedOversample < 1 ? 1 : Math.min(requestedOversample, 4);
        int size = requestedSize < 256 ? 256 : requestedSize;
        STBTTPackedchar.Buffer cdata = null;
        ByteBuffer bitmap = null;
        while (true) {
            cdata = STBTTPackedchar.create((int)count);
            bitmap = BufferUtils.createByteBuffer((int)(size * size));
            STBTTPackContext packContext = STBTTPackContext.create();
            STBTruetype.stbtt_PackBegin((STBTTPackContext)packContext, (ByteBuffer)bitmap, (int)size, (int)size, (int)0, (int)1);
            STBTruetype.stbtt_PackSetOversampling((STBTTPackContext)packContext, (int)os, (int)os);
            buffer.position(0);
            boolean packed = STBTruetype.stbtt_PackFontRange((STBTTPackContext)packContext, (ByteBuffer)buffer, (int)0, (float)height, (int)this.from, (STBTTPackedchar.Buffer)cdata);
            STBTruetype.stbtt_PackEnd((STBTTPackContext)packContext);
            if (packed) {
                this.packOk = true;
                break;
            }
            if (os > 1) {
                --os;
                continue;
            }
            if (size >= 8192) break;
            size *= 2;
            os = wantOversample;
        }
        this.oversample = os;
        this.atlasSize = size;
        this.texture = new DynamicTexture2D(size, size, bitmap, DynamicTexture2D.Format.A, DynamicTexture2D.Filter.Linear, DynamicTexture2D.Filter.Linear);
        this.scale = STBTruetype.stbtt_ScaleForPixelHeight((STBTTFontinfo)fontInfo, (float)height);
        try (MemoryStack stack = MemoryStack.stackPush();){
            IntBuffer ascentBuf = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics((STBTTFontinfo)fontInfo, (IntBuffer)ascentBuf, null, null);
            this.ascent = ascentBuf.get(0);
        }
        this.charData = new Glyph[count];
        for (int i = 0; i < this.charData.length; ++i) {
            STBTTPackedchar packedChar = (STBTTPackedchar)cdata.get(i);
            float ipw = 1.0f / (float)size;
            float iph = 1.0f / (float)size;
            this.charData[i] = new Glyph(packedChar.xoff(), packedChar.yoff(), packedChar.xoff2(), packedChar.yoff2(), (float)packedChar.x0() * ipw, (float)packedChar.y0() * iph, (float)packedChar.x1() * ipw, (float)packedChar.y1() * iph, packedChar.xadvance());
        }
    }

    public TrueTypeFont(ByteBuffer buffer, int height, int charRangeFrom, int charRangeTo, int textureSize) {
        this(buffer, height, charRangeFrom, charRangeTo, textureSize, 2);
    }

    public int getFrom() {
        return this.from;
    }

    public double getHeight() {
        return this.height;
    }

    public double getWidth(String string) {
        if (this.widthCache.containsKey(string)) {
            return this.widthCache.get(string);
        }
        double width = 0.0;
        for (int i = 0; i < string.length(); ++i) {
            Glyph c;
            int cp = string.charAt(i) - this.from;
            if (cp == 167 && i + 1 < string.length()) {
                char ctrl = string.charAt(i + 1);
                if ((ctrl == 'x' || ctrl == 'X') && i + 13 < string.length() && TrueTypeFont.isLegacyHexColorSequence(string, i)) {
                    i += 13;
                    continue;
                }
                ++i;
                continue;
            }
            if (cp < 0 || cp >= this.charData.length) {
                cp = 0;
            }
            if ((c = this.charData[cp]) == null) continue;
            width += (double)c.xAdvance;
        }
        this.widthCache.put(string, width);
        return width;
    }

    public float getAscent() {
        return this.ascent;
    }

    public Glyph[] getCharData() {
        return this.charData;
    }

    public int getTo() {
        return this.from + this.charData.length - 1;
    }

    public int getOversample() {
        return this.oversample;
    }

    public int getAtlasSize() {
        return this.atlasSize;
    }

    public boolean isPackOk() {
        return this.packOk;
    }

    public Glyph metricsFor(int code) {
        int i = code - this.from;
        if (i < 0 || i >= this.charData.length) {
            return null;
        }
        return this.charData[i];
    }

    public float getScale() {
        return this.scale;
    }

    public static boolean isLegacyHexColorSequence(String string, int startIndex) {
        if (startIndex + 13 >= string.length()) {
            return false;
        }
        if (string.charAt(startIndex) != '§') {
            return false;
        }
        char x = string.charAt(startIndex + 1);
        if (x != 'x' && x != 'X') {
            return false;
        }
        for (int pair = 0; pair < 6; ++pair) {
            int sectionIndex = startIndex + 2 + pair * 2;
            if (string.charAt(sectionIndex) != '§') {
                return false;
            }
            char hexDigit = string.charAt(sectionIndex + 1);
            if (Character.digit(hexDigit, 16) != -1) continue;
            return false;
        }
        return true;
    }

    public static int parseLegacyHexColor(String string, int startIndex) {
        if (!TrueTypeFont.isLegacyHexColorSequence(string, startIndex)) {
            return -1;
        }
        int rgb = 0;
        for (int i = 0; i < 6; ++i) {
            char digitChar = string.charAt(startIndex + 3 + i * 2);
            int digit = Character.digit(digitChar, 16);
            rgb = rgb << 4 | digit;
        }
        return rgb;
    }
}

