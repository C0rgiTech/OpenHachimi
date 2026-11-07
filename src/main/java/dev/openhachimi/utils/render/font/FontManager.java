/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dev.openhachimi.utils.ResourcesList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.BufferUtils;

public class FontManager {
    private static final byte[] S_252B5192 = new byte[]{17, 10, 3, 111, 112, 119, 116};  // XOR0x42 "SHA-256"
    private static final byte[] S_234D5FFC = new byte[]{42, 35, 33, 42, 43, 47, 43, 111, 36, 45, 44, 54, 111, 46, 45, 37, 111, 52, 115};  // XOR0x42 "hachimi-font-log-v1"
    private static final byte[] S_180D5BF2 = new byte[]{3, 7, 17, 109, 5, 1, 15, 109, 12, 45, 18, 35, 38, 38, 43, 44, 37};  // XOR0x42 "AES/GCM/NoPadding"
    private static final byte[] S_FCAF = new byte[]{3, 7, 17};  // XOR0x42 "AES"
    private static final byte[] S_52828E0E = new byte[]{42, 35, 33, 42, 43, 47, 43, 111, 36, 45, 44, 54, 108, 32, 43, 44};  // XOR0x42 "hachimi-font.bin"
    private static final byte[] S_3F08781E = new byte[]{50, 43, 44, 37, 36, 35, 44, 37, 108, 54, 54, 36};  // XOR0x42 "pingfang.ttf"
    private static final byte[] S_F13D4F4 = new byte[]{48, 39, 35, 38, 4, 45, 44, 54, 98, (byte)170, (byte)253, (byte)214, (byte)167, (byte)217, (byte)220, 98, 44, 55, 46, 46, (byte)173, (byte)254, (byte)206, (byte)167, (byte)239, (byte)213, (byte)166, (byte)255, (byte)209, (byte)170, (byte)247, (byte)198, (byte)164, (byte)248, (byte)210, (byte)166, (byte)250, (byte)207, (byte)167, (byte)205, (byte)237, (byte)165, (byte)214, (byte)234};
    private static final byte[] S_4491F950 = new byte[]{(byte)167, (byte)239, (byte)213, (byte)166, (byte)255, (byte)209, (byte)167, (byte)239, (byte)213, (byte)170, (byte)200, (byte)192, (byte)167, (byte)245, (byte)240, (byte)165, (byte)254, (byte)209, (byte)167, (byte)239, (byte)218, 98};
    private static final byte[] S_3C38D44B = new byte[]{98, 32, 59, 54, 39, 49};  // XOR0x42 " bytes"
    private static final byte[] S_686205C = new byte[]{49, 43, 56, 39, 127};  // XOR0x42 "size="
    private static final byte[] S_7F0C = new byte[]{98, (byte)165, (byte)238, (byte)238};
    private static final byte[] S_32129DCB = new byte[]{(byte)164, (byte)238, (byte)227, (byte)167, (byte)230, (byte)243, (byte)170, (byte)246, (byte)231, 98};
    private static final byte[] S_7171569F = new byte[]{(byte)167, (byte)239, (byte)213, (byte)166, (byte)255, (byte)209, (byte)167, (byte)239, (byte)213, (byte)170, (byte)200, (byte)192, (byte)166, (byte)250, (byte)207, (byte)167, (byte)205, (byte)237, (byte)165, (byte)214, (byte)234};
    private static final byte[] S_2DF518A6 = new byte[]{(byte)164, (byte)203, (byte)209, (byte)167, (byte)206, (byte)199, (byte)167, (byte)230, (byte)243, (byte)170, (byte)246, (byte)231};
    private static final byte[] S_162B244 = new byte[]{98, (byte)167, (byte)223, (byte)213, (byte)164, (byte)215, (byte)242, 127};
    private static final byte[] S_3CE9E252 = new byte[]{98, (byte)165, (byte)254, (byte)209, (byte)167, (byte)239, (byte)218, (byte)164, (byte)215, (byte)242, 127};
    private static final byte[] S_352F3B9F = new byte[]{(byte)167, (byte)217, (byte)220, (byte)171, (byte)194, (byte)194, (byte)165, (byte)249, (byte)218, (byte)167, (byte)202, (byte)244, 98, 38, 39, 52, 43, 33, 39, 18, 58, 127};
    private static final byte[] S_71EA1AFD = new byte[]{32, 45, 46, 38, 108, 54, 54, 36};  // XOR0x42 "bold.ttf"
    private static final byte[] S_645F43F1 = new byte[]{15, 35, 54, 39, 48, 43, 35, 46, 17, 59, 47, 32, 45, 46, 49, 16, 45, 55, 44, 38, 39, 38, 108, 54, 54, 36};  // XOR0x42 "MaterialSymbolsRounded.ttf"
    private static final byte[] S_6FF6D721 = new byte[]{15, 35, 54, 39, 48, 43, 35, 46, 17, 59, 47, 32, 45, 46, 49, 16, 45, 55, 44, 38, 39, 38, 29, 4, 43, 46, 46, 108, 54, 54, 36};  // XOR0x42 "MaterialSymbolsRounded_Fill.ttf"
    private static final byte[] S_48DB4580 = new byte[]{32, 55, 43, 46, 38, 111, 39, 58, 33, 39, 50, 54, 43, 45, 44, 98, 35, 54, 54, 39, 47, 50, 54, 127};  // XOR0x42 "build-exception attempt="
    private static final byte[] S_20 = new byte[]{98};
    private static final byte[] S_726 = new byte[]{120, 98};  // XOR0x42 ": "
    private static final byte[] S_6F6257DF = new byte[]{50, 35, 33, 41, 111, 36, 35, 43, 46, 39, 38, 98, 35, 54, 54, 39, 47, 50, 54, 127};  // XOR0x42 "pack-failed attempt="
    private static final byte[] S_2F = new byte[]{109};
    private static final byte[] S_4FA5585F = new byte[]{98, 45, 52, 39, 48, 49, 35, 47, 50, 46, 39, 127};  // XOR0x42 " oversample="
    private static final byte[] S_4763A26D = new byte[]{98, (byte)167, (byte)242, (byte)196, (byte)167, (byte)217, (byte)220, (byte)171, (byte)194, (byte)194, (byte)167, (byte)204, (byte)221, (byte)165, (byte)203, (byte)202, (byte)167, (byte)239, (byte)213, (byte)166, (byte)255, (byte)209};
    private static final byte[] S_63E0FFD3 = new byte[]{48, 39, 35, 38, 59, 98, 47, 55, 46, 54, 43, 98, 32, 46, 45, 33, 41, 49, 127};  // XOR0x42 "ready multi blocks="
    private static final byte[] S_7E3F4A06 = new byte[]{98, (byte)167, (byte)210, (byte)198, (byte)167, (byte)223, (byte)213, 45, 49, 127};
    private static final byte[] S_1996A83 = new byte[]{98, (byte)164, (byte)218, (byte)252, (byte)167, (byte)239, (byte)218, 127};
    private static final byte[] S_995 = new byte[]{15, 0};  // XOR0x42 "MB"
    private static final byte[] S_3EEA8ADC = new byte[]{48, 39, 35, 38, 59, 98, 45, 49, 127};  // XOR0x42 "ready os="
    private static final byte[] S_48E2B532 = new byte[]{98, 35, 54, 46, 35, 49, 127};  // XOR0x42 " atlas="
    private static final byte[] S_9012C0C = new byte[]{35, 49, 49, 39, 54, 49, 109, 42, 35, 33, 42, 43, 47, 43, 109, 36, 45, 44, 54, 109};  // XOR0x42 "assets/hachimi/font/"
    private static final byte[] S_59163EBB = new byte[]{109, 35, 49, 49, 39, 54, 49, 109, 42, 35, 33, 42, 43, 47, 43, 109, 36, 45, 44, 54, 109};  // XOR0x42 "/assets/hachimi/font/"
    public static Font regular;
    public static Font museo16;
    public static Font museo18;
    public static Font museo20;
    public static Font bold22;
    public static Font bold38;
    public static Font icon16;
    public static Font titleFont;
    public static Font categoryFont;
    public static Font smallFont;
    public static Font materialIcon20;
    public static Font materialIcon28;
    public static Font materialIconFill20;
    public static Font materialIconFill28;
    private static int builtOversample;
    private static long lastBuildMillis;
    private static final long REBUILD_COOLDOWN_MS = 800L;
    private static final long RETRY_COOLDOWN_MS = 250L;
    private static final int MAX_BUILD_ATTEMPTS = 3;
    private static int failedBuilds;
    private static final HashMap<Integer, Font> sizeCache;
    private static final HashMap<Integer, Integer> sizeFails;
    private static final HashMap<Integer, Long> sizeFailTime;
    private static final int MAX_CACHED_SIZES = 8;
    private static final int MAX_SIZE_ATTEMPTS = 4;
    private static final long RETRY_DELAY_MS = 1500L;
    private static ByteBuffer goldenFont;

    private static boolean ready(Font f) {
        return f != null && f.getFont() != null && f.getFont().isPackOk();
    }

    static {
        builtOversample = -1;
        lastBuildMillis = 0L;
        failedBuilds = 0;
        sizeCache = new HashMap();
        sizeFails = new HashMap();
        sizeFailTime = new HashMap();
    }

    public static void init() {
        if (failedBuilds >= 3) {
            return;
        }
        int oversample = FontManager.targetOversample();
        long now = System.currentTimeMillis();
        if (regular != null) {
            if (FontManager.texturesReady()) {
                if (oversample == builtOversample) {
                    return;
                }
                if (now - lastBuildMillis < 800L) {
                    return;
                }
            } else if (now - lastBuildMillis < 250L) {
                return;
            }
        }
        FontManager.releaseAll();
        lastBuildMillis = now;
        ByteBuffer pingfang = FontManager.readFont("pingfang.ttf");
        ByteBuffer boldFont = FontManager.readFont("bold.ttf");
        ByteBuffer materialFont = FontManager.readFont("MaterialSymbolsRounded.ttf");
        ByteBuffer materialFillFont = FontManager.readFont("MaterialSymbolsRounded_Fill.ttf");
        if (pingfang == null) {
            return;
        }
        int bigOs = Math.min(oversample, 1);
        try {
            regular = new Font(pingfang.duplicate(), 20, 0, 65535, 4096, bigOs);
            smallFont = new Font(pingfang.duplicate(), 16, 0, 65535, 4096, bigOs);
            museo16 = new Font(pingfang.duplicate(), 32, 0, 255, 2048, oversample);
            museo18 = new Font(pingfang.duplicate(), 36, 0, 255, 2048, oversample);
            museo20 = new Font(pingfang.duplicate(), 40, 0, 255, 2048, oversample);
            ByteBuffer boldFallback = boldFont != null ? boldFont.duplicate() : pingfang.duplicate();
            bold22 = new Font(boldFallback, 44, 0, 255, 2048, oversample);
            bold38 = new Font(boldFallback, 76, 0, 255, 2048, oversample);
            titleFont = new Font(boldFallback, 28, 0, 255, 2048, oversample);
            icon16 = new Font(pingfang.duplicate(), 32, 0, 255, 2048, oversample);
            categoryFont = new Font(pingfang.duplicate(), 22, 0, 255, 2048, oversample);
            if (materialFont != null) {
                materialIcon20 = new Font(materialFont.duplicate(), 22, 57000, 64000, 2048, oversample);
                materialIcon28 = new Font(materialFont.duplicate(), 30, 57000, 64000, 2048, oversample);
            }
            if (materialFillFont != null) {
                materialIconFill20 = new Font(materialFillFont.duplicate(), 22, 57000, 64000, 2048, oversample);
                materialIconFill28 = new Font(materialFillFont.duplicate(), 30, 57000, 64000, 2048, oversample);
            }
        }
        catch (Throwable t) {
            FontManager.releaseAll();
            FontManager.diag("build-exception attempt=" + ++failedBuilds + " " + t.getClass().getName() + ": " + t.getMessage());
            return;
        }
        if (!FontManager.texturesReady()) {
            FontManager.releaseAll();
            FontManager.diag("pack-failed attempt=" + ++failedBuilds + "/" + 3 + " oversample=" + oversample + " 将回退原版字体");
            return;
        }
        failedBuilds = 0;
        builtOversample = oversample;
        TrueTypeFont[] blocks = regular.getBlocks();
        if (blocks != null && blocks.length > 0) {
            long vram = 0L;
            StringBuilder detail = new StringBuilder();
            for (int i = 0; i < blocks.length; ++i) {
                TrueTypeFont b = blocks[i];
                vram += (long)b.getAtlasSize() * (long)b.getAtlasSize();
                if (i > 0) {
                    detail.append(',');
                }
                detail.append(b.getOversample());
            }
            FontManager.diag("ready multi blocks=" + blocks.length + " 各块os=" + detail + " 显存=" + vram / 0x100000L + "MB");
            return;
        }
        FontManager.diag("ready os=" + regular.getFont().getOversample() + " atlas=" + regular.getFont().getAtlasSize() + " 显存=" + regular.getFont().getAtlasSize() * regular.getFont().getAtlasSize() / 0x100000 + "MB");
    }

    private static void release(Font renderer) {
        if (renderer == null) {
            return;
        }
        TrueTypeFont[] blocks = renderer.getBlocks();
        if (blocks != null && blocks.length > 0) {
            for (int i = 0; i < blocks.length; ++i) {
                FontManager.releaseCore(blocks[i]);
            }
            return;
        }
        FontManager.releaseCore(renderer.getFont());
    }

    public static boolean usable() {
        return FontManager.texturesReady();
    }

    public static Font fontFor(int px) {
        Font cached;
        if (px < 6) {
            px = 6;
        }
        if (px > 32) {
            px = 32;
        }
        if ((cached = sizeCache.get(px)) != null) {
            return cached;
        }
        Integer fails = sizeFails.get(px);
        if (fails != null) {
            if (fails >= 4) {
                return null;
            }
            Long at = sizeFailTime.get(px);
            if (at != null && System.currentTimeMillis() - at < 1500L) {
                return null;
            }
        }
        if (sizeCache.size() >= 8) {
            return null;
        }
        try {
            TrueTypeFont[] trueTypeFontArray;
            TrueTypeFont cjkA;
            ByteBuffer buf = FontManager.fontBuffer();
            if (buf == null) {
                FontManager.failSize(px, "字体字节不可用");
                return null;
            }
            TrueTypeFont ascii = new TrueTypeFont(buf.duplicate(), px, 32, 255, 512, 1);
            TrueTypeFont punct = new TrueTypeFont(buf.duplicate(), px, 8192, 8303, 512, 1);
            TrueTypeFont cjkPunct = new TrueTypeFont(buf.duplicate(), px, 12288, 12543, 512, 1);
            TrueTypeFont full = new TrueTypeFont(buf.duplicate(), px, 65280, 65519, 512, 1);
            TrueTypeFont cjkB = null;
            if (px <= 26) {
                cjkA = new TrueTypeFont(buf.duplicate(), px, 19968, 40959, 4096, 1);
            } else {
                cjkA = new TrueTypeFont(buf.duplicate(), px, 19968, 30463, 4096, 1);
                cjkB = new TrueTypeFont(buf.duplicate(), px, 30464, 40959, 4096, 1);
            }
            if (cjkB == null) {
                TrueTypeFont[] trueTypeFontArray2 = new TrueTypeFont[5];
                trueTypeFontArray2[0] = ascii;
                trueTypeFontArray2[1] = punct;
                trueTypeFontArray2[2] = cjkPunct;
                trueTypeFontArray2[3] = cjkA;
                trueTypeFontArray = trueTypeFontArray2;
                trueTypeFontArray2[4] = full;
            } else {
                TrueTypeFont[] trueTypeFontArray3 = new TrueTypeFont[6];
                trueTypeFontArray3[0] = ascii;
                trueTypeFontArray3[1] = punct;
                trueTypeFontArray3[2] = cjkPunct;
                trueTypeFontArray3[3] = cjkA;
                trueTypeFontArray3[4] = cjkB;
                trueTypeFontArray = trueTypeFontArray3;
                trueTypeFontArray3[5] = full;
            }
            TrueTypeFont[] cores = trueTypeFontArray;
            for (int i = 0; i < cores.length; ++i) {
                if (cores[i] != null && cores[i].isPackOk()) continue;
                FontManager.failSize(px, "打包失败");
                return null;
            }
            Font font = new Font(cores[0], cores);
            sizeCache.put(px, font);
            sizeFails.remove(px);
            sizeFailTime.remove(px);
            FontManager.diag("size=" + px + " 块数=" + cores.length + " 缓存数=" + sizeCache.size());
            return font;
        }
        catch (Throwable t) {
            FontManager.failSize(px, t.getClass().getSimpleName());
            return null;
        }
    }

    public static int snapDevicePx(float target) {
        int px = Math.round(target / 4.0f) * 4;
        if (px < 8) {
            px = 8;
        }
        if (px > 32) {
            px = 32;
        }
        return px;
    }

    public static void noteFallback(String tag) {
        FontManager.diag("回退绘制 devicePx=" + tag);
    }

    public static float measureExact(Font font, String text, float scale) {
        if (font == null || text == null) {
            return 0.0f;
        }
        TrueTypeFont core = font.getFont();
        if (core == null || core.getFrom() >= 4096) {
            return (float)font.getStringWidth(text) * scale;
        }
        float gs = FontManager.guiScaleNow();
        float target = (float)font.getHeight() * scale * gs;
        int dp = FontManager.snapDevicePx(target);
        Font exact = FontManager.fontFor(dp);
        if (exact == null) {
            return (float)font.getStringWidth(text) * scale;
        }
        float s = target / ((float)dp * gs);
        return (float)exact.getStringWidth(text) * s;
    }

    public static boolean drawExact(GuiGraphics g, Font font, String text, float x, float y, int color, boolean shadow, float scale) {
        if (font == null || text == null || text.isEmpty()) {
            return true;
        }
        TrueTypeFont core = font.getFont();
        if (core == null || core.getFrom() >= 4096) {
            return false;
        }
        float gs = FontManager.guiScaleNow();
        float target = (float)font.getHeight() * scale * gs;
        int devicePx = FontManager.snapDevicePx(target);
        Font exact = FontManager.fontFor(devicePx);
        if (exact == null) {
            return false;
        }
        float s = target / ((float)devicePx * gs);
        g.pose().pushPose();
        g.pose().translate(x, y, 0.0f);
        g.pose().scale(s, s, 1.0f);
        exact.drawString(g, text, 0.0, 0.0, color, shadow);
        g.flush();
        g.pose().popPose();
        return true;
    }

    private static Font buildBlocks(ByteBuffer buffer, int height, int oversample) {
        int[][] ranges = new int[][]{{32, 255, 512}, {8192, 8303, 512}, {12288, 12543, 512}, {19968, 25215, 4096}, {25216, 30463, 4096}, {30464, 35711, 4096}, {35712, 40959, 4096}, {65280, 65519, 512}};
        TrueTypeFont[] cores = new TrueTypeFont[ranges.length];
        for (int i = 0; i < ranges.length; ++i) {
            TrueTypeFont core = new TrueTypeFont(buffer.duplicate(), height, ranges[i][0], ranges[i][1], ranges[i][2], oversample);
            if (!core.isPackOk()) {
                return null;
            }
            cores[i] = core;
        }
        return new Font(cores[0], cores);
    }

    private static void releaseCore(TrueTypeFont core) {
        if (core == null) {
            return;
        }
        try {
            AbstractTexture texture = core.texture;
            if (texture != null) {
                RenderSystem.recordRenderCall(() -> {
                    try {
                        texture.close();
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                });
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static ByteBuffer readFont(String name) {
        try {
            byte[] bytes = null;
            try {
                bytes = ResourcesList.resources.get("assets/hachimi/font/" + name);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            InputStream in = null;
            if (bytes == null) {
                in = FontManager.class.getResourceAsStream("/assets/hachimi/font/" + name);
                if (in == null) {
                    in = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/hachimi/font/" + name);
                }
                if (in == null) {
                    in = FontManager.class.getClassLoader().getResourceAsStream("assets/hachimi/font/" + name);
                }
            }
            if (bytes == null && in == null) {
                return null;
            }
            if (bytes == null) {
                int len;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                bytes = out.toByteArray();
                in.close();
            }
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)bytes.length).put(bytes);
            byteBuffer.flip();
            return byteBuffer;
        }
        catch (Exception e) {
            return null;
        }
    }

    private static void failSize(int px, String why) {
        int n = sizeFails.getOrDefault(px, 0) + 1;
        sizeFails.put(px, n);
        sizeFailTime.put(px, System.currentTimeMillis());
        FontManager.diag("size=" + px + " 第" + n + "次失败 " + why);
    }

    private static ByteBuffer fontBuffer() {
        if (goldenFont == null) {
            ByteBuffer b = FontManager.readFont("pingfang.ttf");
            if (b == null) {
                FontManager.diag("readFont 返回 null，字体资源不可用");
                return null;
            }
            goldenFont = b;
            FontManager.diag("字体字节已缓存 " + b.capacity() + " bytes");
        }
        return goldenFont;
    }

    public static int targetOversample() {
        int scale = 2;
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                scale = (int)Math.round(mc.getWindow().getGuiScale());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (scale < 1) {
            scale = 1;
        }
        if (scale > 4) {
            scale = 4;
        }
        return scale;
    }

    private static void diag(String msg) {
        try {
            byte[] key = MessageDigest.getInstance("SHA-256").digest("hachimi-font-log-v1".getBytes(StandardCharsets.UTF_8));
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(1, (Key)new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] sealed = cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8));
            byte[] blob = new byte[12 + sealed.length];
            System.arraycopy(iv, 0, blob, 0, 12);
            System.arraycopy(sealed, 0, blob, 12, sealed.length);
            String line = Base64.getEncoder().encodeToString(blob) + System.lineSeparator();
            Path p = Minecraft.getInstance().gameDirectory.toPath().resolve("hachimi-font.bin");
            Files.write(p, line.getBytes(StandardCharsets.US_ASCII), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static float guiScaleNow() {
        Minecraft mc = Minecraft.getInstance();
        float s = mc != null && mc.getWindow() != null ? (float)mc.getWindow().getGuiScale() : 1.0f;
        return s < 0.001f ? 1.0f : s;
    }

    private static boolean texturesReady() {
        return FontManager.ready(regular);
    }

    private static void releaseAll() {
        FontManager.release(regular);
        FontManager.release(smallFont);
        FontManager.release(museo16);
        FontManager.release(museo18);
        FontManager.release(museo20);
        FontManager.release(bold22);
        FontManager.release(bold38);
        FontManager.release(titleFont);
        FontManager.release(icon16);
        FontManager.release(categoryFont);
        FontManager.release(materialIcon20);
        FontManager.release(materialIcon28);
        FontManager.release(materialIconFill20);
        FontManager.release(materialIconFill28);
        regular = null;
        smallFont = null;
        museo16 = null;
        museo18 = null;
        museo20 = null;
        bold22 = null;
        bold38 = null;
        titleFont = null;
        icon16 = null;
        categoryFont = null;
        materialIcon20 = null;
        materialIcon28 = null;
        materialIconFill20 = null;
        materialIconFill28 = null;
    }
}

