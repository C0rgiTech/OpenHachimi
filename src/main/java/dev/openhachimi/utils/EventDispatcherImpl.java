/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import dev.openhachimi.config.ConfigLoadManager;
import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.security.AntiDebug;

import java.io.File;

import dev.openhachimi.utils.render.font.FontManager;
import net.minecraftforge.eventbus.api.IEventBus;

public class EventDispatcherImpl {
    public static IEventBus originalBus;
    public static String version;
    private static volatile boolean initialized;

    static {
        version = "v1.0";
    }

    public static void log(String msg) {
    }

    public static synchronized void init() {
        if (initialized) {
            EventDispatcherImpl.log("EventDispatcherImpl.init() already done, skipping.");
            return;
        }
        initialized = true;
        EventDispatcherImpl.log("EventDispatcherImpl.init() called.");
        try {
            System.setProperty("java.awt.headless", "false");
            System.setProperty("sun.java2d.opengl", "false");
            System.setProperty("sun.java2d.d3d", "false");
            System.setProperty("sun.java2d.noddraw", "true");
            EventDispatcherImpl.log("Registering InternalEventProcessor...");
            try {
                EventRegistrar.register();
                EventDispatcherImpl.log("InternalEventProcessor registered.");
            }
            catch (Throwable handlerEx) {
                EventDispatcherImpl.log("Failed to register InternalEventProcessor: " + handlerEx.toString());
            }
            try {
                InstallHelper.configure(new File("."));
                InstallHelper.install();
            }
            catch (Throwable handlerEx) {
                // empty catch block
            }
            try {
                AntiDebug.start();
            }
            catch (Throwable handlerEx) {
                // empty catch block
            }
            EventDispatcherImpl.log("Initializing ConfigRegistry...");
            ModuleManager.init();
            EventDispatcherImpl.log("Initializing ProfileRegistry...");
            ConfigLoadManager.init();
            EventDispatcherImpl.log("Initializing GlyphRendererAdapter (Pre-load for Tier 0 in-memory resources)...");
            FontManager.init();
            try {
                EventDispatcherImpl.log("Auto handlers skipped.");
            }
            catch (Throwable handlerEx) {
                EventDispatcherImpl.log("Failed to register auto handlers: " + handlerEx.toString());
            }
            try {
                dev.openhachimi.config.FontManager.init();
                EventDispatcherImpl.log("Custom fonts initialized.");
            }
            catch (Throwable fontEx) {
                EventDispatcherImpl.log("Failed to initialize custom fonts: " + fontEx.toString());
            }
        }
        catch (Throwable t) {
            EventDispatcherImpl.log("EventDispatcherImpl Error: " + t.toString());
        }
    }
}

