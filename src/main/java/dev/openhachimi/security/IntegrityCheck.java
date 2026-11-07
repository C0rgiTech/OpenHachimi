/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.security;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;

public final class IntegrityCheck {
    private static final byte[] S_1784F7D = new byte[]{44, 35, 54, 43, 52, 39, 29, 49, 39, 46, 36, 29, 33, 42, 39, 33, 41};  // XOR0x42 "native_self_check"
    private static final byte[] S_3EBD8096 = new byte[]{111, 40, 35, 52, 35, 35, 37, 39, 44, 54};  // XOR0x42 "-javaagent"
    private static final byte[] S_6799488D = new byte[]{111, 35, 37, 39, 44, 54, 46, 43, 32};  // XOR0x42 "-agentlib"
    private static final byte[] S_B917F1D = new byte[]{111, 35, 37, 39, 44, 54, 50, 35, 54, 42};  // XOR0x42 "-agentpath"
    private static final byte[] S_1A8A278E = new byte[]{35, 37, 39, 44, 54, 111, 35, 48, 37};  // XOR0x42 "agent-arg"
    private static final byte[] S_18F29264 = new byte[]{111, 40, 35, 52, 35, 35, 37, 39, 44, 54, 120};  // XOR0x42 "-javaagent:"
    private static final byte[] S_669E64BD = new byte[]{111, 35, 37, 39, 44, 54, 50, 35, 54, 42, 120};  // XOR0x42 "-agentpath:"
    private static final byte[] S_48D8E9B8 = new byte[]{35, 37, 39, 44, 54, 111, 35, 48, 37, 111, 43, 44, 46, 43, 44, 39};  // XOR0x42 "agent-arg-inline"
    private static final byte[] S_2C02D957 = new byte[]{35, 48, 54, 42, 35, 49};  // XOR0x42 "arthas"
    private static final byte[] S_4ED7C948 = new byte[]{40, 35, 52, 35, 49, 49, 43, 49, 54};  // XOR0x42 "javassist"
    private static final byte[] S_15E883F9 = new byte[]{32, 59, 54, 39, 111, 32, 55, 38, 38, 59, 111, 35, 37, 39, 44, 54};  // XOR0x42 "byte-buddy-agent"
    private static final byte[] S_4DD49E23 = new byte[]{40, 35, 52, 35, 35, 37, 39, 44, 54};  // XOR0x42 "javaagent"
    private static final byte[] S_2DD2DE23 = new byte[]{32, 54, 48, 35, 33, 39};  // XOR0x42 "btrace"
    private static final byte[] S_5E0D174 = new byte[]{37, 48, 39, 59, 49};  // XOR0x42 "greys"
    private static final byte[] S_13740D9B = new byte[]{40, 52, 47, 111, 49, 35, 44, 38, 32, 45, 58};  // XOR0x42 "jvm-sandbox"
    private static final byte[] S_23D2381A = new byte[]{35, 49, 59, 44, 33, 111, 50, 48, 45, 36, 43, 46, 39, 48};  // XOR0x42 "async-profiler"
    private static final byte[] S_51AE4A27 = new byte[]{40, 35, 52, 35, 108, 33, 46, 35, 49, 49, 108, 50, 35, 54, 42};  // XOR0x42 "java.class.path"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_32EF7A45 = new byte[]{49, 55, 49, 50, 43, 33, 43, 45, 55, 49, 111, 35, 37, 39, 44, 54, 120};  // XOR0x42 "suspicious-agent:"
    private static final byte[] S_3389437B = new byte[]{40, 35, 52, 35, 108, 46, 35, 44, 37, 108, 43, 44, 49, 54, 48, 55, 47, 39, 44, 54, 108, 11, 44, 49, 54, 48, 55, 47, 39, 44, 54, 35, 54, 43, 45, 44};  // XOR0x42 "java.lang.instrument.Instrumentation"
    private static final byte[] S_66F6D344 = new byte[]{43, 44, 49, 54, 48, 55, 47, 39, 44, 54, 35, 54, 43, 45, 44, 111, 46, 45, 35, 38, 39, 38};  // XOR0x42 "instrumentation-loaded"
    private static final byte[] S_234FC9E9 = new byte[]{49, 55, 44, 108, 43, 44, 49, 54, 48, 55, 47, 39, 44, 54, 108, 11, 44, 49, 54, 48, 55, 47, 39, 44, 54, 35, 54, 43, 45, 44, 11, 47, 50, 46};  // XOR0x42 "sun.instrument.InstrumentationImpl"
    private static final byte[] S_4EBD2073 = new byte[]{43, 44, 49, 54, 48, 55, 47, 39, 44, 54, 35, 54, 43, 45, 44, 111, 43, 47, 50, 46, 111, 46, 45, 35, 38, 39, 38};  // XOR0x42 "instrumentation-impl-loaded"
    private static final byte[] S_69707512 = new byte[]{40, 35, 52, 35, 108, 43, 45, 108, 54, 47, 50, 38, 43, 48};  // XOR0x42 "java.io.tmpdir"
    private static final byte[] S_11876ECE = new byte[]{55, 49, 39, 48, 108, 44, 35, 47, 39};  // XOR0x42 "user.name"
    private static final byte[] S_4C09433F = new byte[]{108, 35, 54, 54, 35, 33, 42, 29, 50, 43, 38};  // XOR0x42 ".attach_pid"
    private static final byte[] S_16E07706 = new byte[]{35, 54, 54, 35, 33, 42, 111, 50, 43, 38, 111, 36, 43, 46, 39};  // XOR0x42 "attach-pid-file"
    private static final byte[] S_55F16C01 = new byte[]{42, 49, 50, 39, 48, 36, 38, 35, 54, 35, 29};  // XOR0x42 "hsperfdata_"
    private static final byte[] S_35644513 = new byte[]{50, 39, 48, 36, 38, 35, 54, 35, 111, 39, 47, 50, 54, 59};  // XOR0x42 "perfdata-empty"
    private static volatile boolean tripped = false;
    private static volatile String reason = "";

    private IntegrityCheck() {
    }

    public static void check() {
        IntegrityCheck.checkAgentArgument();
        IntegrityCheck.checkClassPath();
        IntegrityCheck.checkInstrumentationClass();
        IntegrityCheck.checkAttachFile();
        IntegrityCheck.checkNativeSelf();
    }

    private static String currentPid() {
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            if (name == null) {
                return "";
            }
            int at = name.indexOf(64);
            return at > 0 ? name.substring(0, at) : "";
        }
        catch (Throwable ignored) {
            return "";
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean isLoaded(String name) {
        try {
            ClassLoader ctx;
            ClassLoader sys = ClassLoader.getSystemClassLoader();
            if (sys != null) {
                try {
                    Class<?> c = Class.forName(name, false, sys);
                    if (c != null) {
                        return true;
                    }
                }
                catch (ClassNotFoundException c) {
                }
                catch (Throwable c) {
                    // empty catch block
                }
            }
            if ((ctx = Thread.currentThread().getContextClassLoader()) == null || ctx == sys) return false;
            try {
                Class<?> c = Class.forName(name, false, ctx);
                if (c == null) return false;
                return true;
            }
            catch (ClassNotFoundException classNotFoundException) {}
            finally {
                return false;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static void trip(String why) {
        if (!tripped) {
            reason = why;
            tripped = true;
        }
    }

    public static String reason() {
        return reason;
    }

    private static void checkInstrumentationClass() {
        if (IntegrityCheck.isLoaded("java.lang.instrument.Instrumentation")) {
            IntegrityCheck.trip("instrumentation-loaded");
            return;
        }
        if (IntegrityCheck.isLoaded("sun.instrument.InstrumentationImpl")) {
            IntegrityCheck.trip("instrumentation-impl-loaded");
        }
    }

    public static boolean tripped() {
        return tripped;
    }

    private static void checkAgentArgument() {
        try {
            List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String a : args) {
                if (a == null) continue;
                String low = a.toLowerCase();
                if (low.startsWith("-javaagent") || low.startsWith("-agentlib") || low.startsWith("-agentpath")) {
                    IntegrityCheck.trip("agent-arg");
                    return;
                }
                if (!low.contains("-javaagent:") && !low.contains("-agentpath:")) continue;
                IntegrityCheck.trip("agent-arg-inline");
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void checkNativeSelf() {
        if (!NativeLoader.ready()) {
            return;
        }
        try {
            if (!IntegrityCheck.selfCheck()) {
                IntegrityCheck.trip("native_self_check");
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void checkClassPath() {
        String[] markers = new String[]{"arthas", "javassist", "byte-buddy-agent", "javaagent", "btrace", "greys", "jvm-sandbox", "async-profiler"};
        try {
            String cp = System.getProperty("java.class.path", "").toLowerCase();
            if (cp.isEmpty()) {
                return;
            }
            for (String m : markers) {
                if (!cp.contains(m)) continue;
                IntegrityCheck.trip("suspicious-agent:" + m);
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void checkAttachFile() {
        try {
            File attach;
            String tmp = System.getProperty("java.io.tmpdir", "");
            String user = System.getProperty("user.name", "");
            if (tmp.isEmpty()) {
                return;
            }
            String pid = IntegrityCheck.currentPid();
            if (pid != null && !pid.isEmpty() && (attach = new File(tmp, ".attach_pid" + pid)).exists()) {
                IntegrityCheck.trip("attach-pid-file");
                return;
            }
            if (!user.isEmpty()) {
                File perf = new File(new File(tmp, "hsperfdata_" + user), pid == null ? "" : pid);
                if (pid != null && perf.exists() && perf.length() == 0L) {
                    IntegrityCheck.trip("perfdata-empty");
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static native boolean selfCheck();
}

