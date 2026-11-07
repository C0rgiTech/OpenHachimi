/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import java.io.File;

public final class InstallHelper {
    private static volatile boolean installed = false;

    public static void configure(File gameDir) {
    }

    private InstallHelper() {
    }

    public static void line(String text) {
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){

                @Override
                public void uncaughtException(Thread t, Throwable e) {
                }
            });
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void swallow(Throwable e) {
    }
}

