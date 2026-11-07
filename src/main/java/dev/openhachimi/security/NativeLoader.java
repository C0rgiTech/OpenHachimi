/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.security;

import java.io.*;

public final class NativeLoader {
    private static final byte[] S_278F6F4A = new byte[]{109, 35, 49, 49, 39, 54, 49, 109, 42, 35, 33, 42, 43, 47, 43, 109, 44, 35, 54, 43, 52, 39, 109, 42, 35, 33, 42, 43, 47, 43, 108, 38, 46, 46};  // XOR0x42 "/assets/hachimi/native/hachimi.dll"
    private static final byte[] S_5DB3FE35 = new byte[]{48, 39, 49, 45, 55, 48, 33, 39, 29, 47, 43, 49, 49, 43, 44, 37};  // XOR0x42 "resource_missing"
    private static final byte[] S_35036F28 = new byte[]{54, 39, 47, 50, 29, 53, 48, 43, 54, 39, 29, 36, 35, 43, 46, 39, 38};  // XOR0x42 "temp_write_failed"
    private static final byte[] S_3A = new byte[]{120};
    private static final byte[] S_66B9497F = new byte[]{38, 39, 52, 108, 45, 50, 39, 44, 42, 35, 33, 42, 43, 47, 43, 108, 48, 39, 44, 38, 39, 48, 108, 16, 39, 44, 38, 39, 48, 112, 6};  // XOR0x42 "dev.openhachimi.render.Render2D"
    private static final byte[] S_6963689A = new byte[]{38, 39, 52, 108, 45, 50, 39, 44, 42, 35, 33, 42, 43, 47, 43, 108, 49, 39, 33, 55, 48, 43, 54, 59, 108, 11, 44, 54, 39, 37, 48, 43, 54, 59, 1, 42, 39, 33, 41};  // XOR0x42 "dev.openhachimi.security.IntegrityCheck"
    private static final byte[] S_69707512 = new byte[]{40, 35, 52, 35, 108, 43, 45, 108, 54, 47, 50, 38, 43, 48};  // XOR0x42 "java.io.tmpdir"
    private static final byte[] S_2E = new byte[]{108};
    private static final byte[] S_1955E = new byte[]{42, 58, 44};  // XOR0x42 "hxn"
    private static final byte[] S_16AA29 = new byte[]{108, 54, 47, 50};  // XOR0x42 ".tmp"
    private static final byte[] S_0 = new byte[0];
    private static volatile boolean ready = false;
    private static volatile String loadError = "";
    private static File tempFile = null;

    public static boolean ready() {
        return ready;
    }

    private NativeLoader() {
    }

    public static synchronized boolean load() {
        if (ready) {
            return true;
        }
        try {
            NativeLoader.registerNative(NativeLoader.class);
            NativeLoader.bindAll();
            ready = true;
            return true;
        }
        catch (Throwable throwable) {
            try {
                byte[] image = NativeLoader.readResource("/assets/hachimi/native/hachimi.dll");
                if (image == null || image.length < 64) {
                    loadError = "resource_missing";
                    return false;
                }
                File tmp = NativeLoader.writeTemp(image);
                if (tmp == null) {
                    loadError = "temp_write_failed";
                    return false;
                }
                System.load(tmp.getAbsolutePath());
                tempFile = tmp;
                NativeLoader.bindAll();
                ready = true;
                return true;
            }
            catch (Throwable t) {
                loadError = t.getClass().getSimpleName() + ":" + t.getMessage();
                return false;
            }
        }
    }

    public static synchronized void unload() {
        if (tempFile != null && tempFile.exists()) {
            try {
                tempFile.delete();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            tempFile = null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static byte[] readResource(String path) throws IOException {
        byte[] byArray;
        InputStream in = NativeLoader.class.getResourceAsStream(path);
        if (in == null) {
            return null;
        }
        try {
            int n;
            ByteArrayOutputStream out = new ByteArrayOutputStream(131072);
            byte[] buf = new byte[65536];
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            byArray = out.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                in.close();
                throw throwable;
            }
            catch (Throwable t) {
                return null;
            }
        }
        in.close();
        return byArray;
    }

    public static native int guardCheck();

    private static native void registerNative(Class<?> var0);

    public static int guardScore() {
        if (!ready) {
            return -1;
        }
        try {
            return NativeLoader.guardCheck();
        }
        catch (Throwable t) {
            return -1;
        }
    }

    private static void bindAll() {
        try {
            NativeLoader.registerNative(NativeLoader.class);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            NativeLoader.registerCrypt(Class.forName("dev.openhachimi.security.NativeStringUtil"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            NativeLoader.registerGuard(Class.forName("dev.openhachimi.security.IntegrityCheck"));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static native void registerGuard(Class<?> var0);

    private static native void registerCrypt(Class<?> var0);

    public static String loadError() {
        return loadError;
    }

    public static native long hash64(byte[] var0, long var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static File writeTemp(byte[] image) {
        try {
            File dir = new File(System.getProperty("java.io.tmpdir", "."));
            File f = File.createTempFile("hxn", ".tmp", dir);
            f.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(f);){
                out.write(image);
            }
            return f;
        }
        catch (Throwable t) {
            return null;
        }
    }
}

