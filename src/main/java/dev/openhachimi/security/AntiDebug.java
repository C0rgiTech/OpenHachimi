/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.security;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AntiDebug {
    private static final byte[] S_3EBD8096 = new byte[]{111, 40, 35, 52, 35, 35, 37, 39, 44, 54};  // XOR0x42 "-javaagent"
    private static final byte[] S_B917F1D = new byte[]{111, 35, 37, 39, 44, 54, 50, 35, 54, 42};  // XOR0x42 "-agentpath"
    private static final byte[] S_31B593 = new byte[]{40, 38, 53, 50};  // XOR0x42 "jdwp"
    private static final byte[] S_36C6532A = new byte[]{40, 52, 47, 111, 35, 48, 37};  // XOR0x42 "jvm-arg"
    private static final byte[] S_696C9A2 = new byte[]{54, 45, 45, 46, 120};  // XOR0x42 "tool:"
    private static final byte[] S_2C02D957 = new byte[]{35, 48, 54, 42, 35, 49};  // XOR0x42 "arthas"
    private static final byte[] S_3040B370 = new byte[]{54, 42, 48, 39, 35, 38, 120};  // XOR0x42 "thread:"
    private static final byte[] S_1BC38AE7 = new byte[]{33, 45, 47, 108, 49, 55, 44, 108, 54, 45, 45, 46, 49, 108, 35, 54, 54, 35, 33, 42};  // XOR0x42 "com.sun.tools.attach"
    private static final byte[] S_4F1DF46A = new byte[]{33, 45, 47, 108, 54, 35, 45, 32, 35, 45, 108, 35, 48, 54, 42, 35, 49};  // XOR0x42 "com.taobao.arthas"
    private static final byte[] S_3EE10D84 = new byte[]{44, 39, 54, 108, 32, 59, 54, 39, 32, 55, 38, 38, 59, 108, 35, 37, 39, 44, 54};  // XOR0x42 "net.bytebuddy.agent"
    private static final byte[] S_3482846D = new byte[]{36, 48, 35, 47, 39, 120};  // XOR0x42 "frame:"
    private static final byte[] S_50344500 = new byte[]{42, 111, 37, 55, 35, 48, 38};  // XOR0x42 "h-guard"
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_5E82C89A = new byte[]{33, 45, 47, 108, 54, 35, 45, 32, 35, 45, 108, 35, 48, 54, 42, 35, 49, 108, 33, 45, 48, 39, 108, 49, 39, 48, 52, 39, 48, 108, 3, 48, 54, 42, 35, 49, 17, 39, 48, 52, 39, 48};  // XOR0x42 "com.taobao.arthas.core.server.ArthasServer"
    private static final byte[] S_60159CD1 = new byte[]{44, 39, 54, 108, 32, 59, 54, 39, 32, 55, 38, 38, 59, 108, 35, 37, 39, 44, 54, 108, 0, 59, 54, 39, 0, 55, 38, 38, 59, 3, 37, 39, 44, 54};  // XOR0x42 "net.bytebuddy.agent.ByteBuddyAgent"
    private static final byte[] S_D62F42F = new byte[]{35, 54, 54, 35, 33, 42, 98, 46, 43, 49, 54, 39, 44, 39, 48};  // XOR0x42 "attach listener"
    private static final byte[] S_689ECB1F = new byte[]{49, 43, 37, 44, 35, 46, 98, 38, 43, 49, 50, 35, 54, 33, 42, 39, 48};  // XOR0x42 "signal dispatcher"
    private static final byte[] S_2AA9B775 = new byte[]{48, 39, 36, 39, 48, 39, 44, 33, 39, 98, 42, 35, 44, 38, 46, 39, 48};  // XOR0x42 "reference handler"
    private static volatile boolean tripped = false;
    private static volatile String reason = "";
    private static volatile boolean started = false;
    private static final String[] TOOL_CLASSES = new String[]{"com.taobao.arthas.core.server.ArthasServer", "net.bytebuddy.agent.ByteBuddyAgent"};
    private static final String[] TOOL_THREADS = new String[]{"attach listener", "arthas", "jdwp", "signal dispatcher", "reference handler"};
    private static volatile boolean shut = false;

    private AntiDebug() {
    }

    public static void start() {
        if (started) {
            return;
        }
        started = true;
        AntiDebug.checkOnce();
        Thread t = new Thread(new Runnable(){

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(4000L);
                    }
                    catch (InterruptedException e) {
                        return;
                    }
                    if (tripped) {
                        AntiDebug.disableAll();
                        continue;
                    }
                    AntiDebug.checkOnce();
                    if (!tripped) continue;
                    AntiDebug.disableAll();
                }
            }
        }, "h-guard");
        t.setDaemon(true);
        t.start();
    }

    private static void trip(String why) {
        if (!tripped) {
            tripped = true;
            reason = why;
        }
    }

    public static String reason() {
        return reason;
    }

    public static void checkOnce() {
        if (tripped) {
            return;
        }
        AntiDebug.checkJvmArgs();
        if (tripped) {
            return;
        }
        AntiDebug.checkLoadedClasses();
        if (tripped) {
            return;
        }
        AntiDebug.checkThreads();
    }

    private static void disableAll() {
        if (shut) {
            return;
        }
        shut = true;
        Runtime.getRuntime().halt(1);
    }

    private static void checkThreads() {
        try {
            Set<Thread> threads = Thread.getAllStackTraces().keySet();
            for (Thread thread : threads) {
                String n = thread.getName();
                if (n == null || !n.toLowerCase().contains("arthas")) continue;
                AntiDebug.trip("thread:" + n);
                return;
            }
        }
        catch (Throwable threads) {
            // empty catch block
        }
        try {
            Map<Thread, StackTraceElement[]> all = Thread.getAllStackTraces();
            for (Map.Entry entry : all.entrySet()) {
                for (StackTraceElement f : (StackTraceElement[])entry.getValue()) {
                    String cn = f.getClassName();
                    if (!cn.startsWith("com.sun.tools.attach") && !cn.startsWith("com.taobao.arthas") && !cn.startsWith("net.bytebuddy.agent")) continue;
                    AntiDebug.trip("frame:" + cn);
                    return;
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void checkLoadedClasses() {
        for (String name : TOOL_CLASSES) {
            try {
                Class.forName(name, false, ClassLoader.getSystemClassLoader());
                AntiDebug.trip("tool:" + name);
                return;
            }
            catch (Throwable throwable) {
            }
        }
    }

    private static void checkJvmArgs() {
        try {
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
            List<String> args = bean.getInputArguments();
            for (String a : args) {
                String s;
                if (a == null || !(s = a.toLowerCase()).contains("-javaagent") && !s.contains("-agentpath") && !s.contains("jdwp")) continue;
                AntiDebug.trip("jvm-arg");
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static boolean tripped() {
        return tripped;
    }
}

