/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render;

import dev.openhachimi.security.NativeLoader;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class Render2D {
    private static final SecureRandom RNG = new SecureRandom();
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private Render2D() {
    }

    public static byte[] e(String text) {
        if (NativeLoader.ready()) {
            try {
                byte[] b = Render2D.nEncode(text);
                if (b != null) {
                    return b;
                }
            }
            catch (Throwable b) {
                // empty catch block
            }
        }
        byte[] raw = text.getBytes(StandardCharsets.UTF_8);
        byte[] out = new byte[raw.length];
        for (int i = 0; i < raw.length; ++i) {
            out[i] = (byte)(raw[i] ^ 0x42);
        }
        return out;
    }

    public static String d(byte[] data) {
        if (NativeLoader.ready()) {
            try {
                String s = Render2D.nDecode(data);
                if (s != null) {
                    return s;
                }
            }
            catch (Throwable s) {
                // empty catch block
            }
        }
        byte[] copy = new byte[data.length];
        for (int i = 0; i < data.length; ++i) {
            copy[i] = (byte)(data[i] ^ 0x42);
        }
        return new String(copy, StandardCharsets.UTF_8);
    }

    private static SecretKeySpec deriveKey(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static byte[] sha256(byte[] data) {
        if (NativeLoader.ready()) {
            try {
                byte[] b = Render2D.nSha256(data);
                if (b != null && b.length == 32) {
                    return b;
                }
            }
            catch (Throwable b) {
                // empty catch block
            }
        }
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        }
        catch (Exception e) {
            return new byte[32];
        }
    }

    public static String encryptToBase64(String plainText, String password) throws Exception {
        SecretKeySpec keySpec = Render2D.deriveKey(password);
        byte[] iv = new byte[12];
        RNG.nextBytes(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(1, (Key)keySpec, gcmSpec);
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    private static native byte[] nSha256(byte[] var0);

    public static String decryptFromBase64(String base64CipherText, String password) throws Exception {
        SecretKeySpec keySpec = Render2D.deriveKey(password);
        byte[] cipherMessage = Base64.getDecoder().decode(base64CipherText);
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);
        byte[] iv = new byte[12];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(2, (Key)keySpec, gcmSpec);
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }

    private static native String nDecode(byte[] var0);

    private static native byte[] nEncode(String var0);
}

