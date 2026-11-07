/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.config;

import dev.openhachimi.utils.render.font.FontRender;
import dev.openhachimi.utils.ResourcesList;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;

public class FontManager {
    private static final byte[] S_1CBF22A = new byte[]{35, 49, 49, 39, 54, 49, 109, 42, 35, 33, 42, 43, 47, 43, 109, 36, 45, 44, 54, 109, 50, 43, 44, 37, 36, 35, 44, 37, 108, 54, 54, 36};  // XOR0x42 "assets/hachimi/font/pingfang.ttf"
    private static final byte[] S_20347B09 = new byte[]{35, 49, 49, 39, 54, 49, 109, 42, 35, 33, 42, 43, 47, 43, 109, 36, 45, 44, 54, 109, 32, 45, 46, 38, 108, 54, 54, 36};  // XOR0x42 "assets/hachimi/font/bold.ttf"
    private static final byte[] S_2F = new byte[]{109};
    public static FontRender regular;
    public static FontRender bold;

    public static boolean ready() {
        return regular != null;
    }

    private FontManager() {
    }

    public static void init() {
        if (regular != null) {
            return;
        }
        try {
            ByteBuffer boldBuf;
            ByteBuffer pingfang = FontManager.readFont("assets/hachimi/font/pingfang.ttf");
            if (pingfang != null) {
                regular = new FontRender(pingfang, 20, 0, 65535, 8192);
            }
            if ((boldBuf = FontManager.readFont("assets/hachimi/font/bold.ttf")) != null) {
                bold = new FontRender(boldBuf, 22, 0, 255, 2048);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static ByteBuffer readFont(String name) {
        byte[] data = null;
        try {
            data = ResourcesList.resources.get(name);
        }
        catch (Throwable t) {
            data = null;
        }
        if (data == null && (data = FontManager.readClasspath("/" + name)) == null) {
            data = FontManager.readClasspath(name);
        }
        if (data == null) {
            return null;
        }
        ByteBuffer buf = BufferUtils.createByteBuffer((int)data.length);
        buf.put(data);
        buf.flip();
        return buf;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private static byte[] readClasspath(String path) {
        InputStream in = null;
        try {
            in = FontRender.class.getResourceAsStream(path);
            if (in == null) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            }
            if (in == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            return out.toByteArray();
        }
        catch (Throwable t) {
            return null;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (Throwable ignored) {}
            }
        }
    }
}

