package dev.openhachimi.security;

import dev.openhachimi.config.ConfigLoadManager;
import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.modules.ModuleManager;
import dev.openhachimi.utils.render.font.FontManager;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Starts the Forge event based client from the native injector path, after Forge
 * has finished mod discovery and Minecraft has created its main client instance.
 */
public final class InjectBootstrap {
    private static final String STARTED_KEY = "openhachimi.inject.started";
    private static final String LOADER_KEY = "openhachimi.inject.classloader";
    private static final AtomicBoolean STARTED = new AtomicBoolean();

    private InjectBootstrap() {
    }

    public static void inject(ClassLoader gameLoader) throws Exception {
        if (gameLoader == null) {
            throw new IllegalArgumentException("Minecraft game class loader is null");
        }
        if (isAlreadyLoadedByForge(gameLoader)) {
            return;
        }

        synchronized (System.getProperties()) {
            if (System.getProperties().containsKey(STARTED_KEY) || !STARTED.compareAndSet(false, true)) {
                return;
            }
            System.getProperties().put(STARTED_KEY, Boolean.TRUE);
            System.getProperties().put(LOADER_KEY, InjectBootstrap.class.getClassLoader());
        }

        try {
            Minecraft minecraft = awaitMinecraft();
            CompletableFuture<Void> initialized = new CompletableFuture<>();
            minecraft.execute(() -> {
                try {
                    initializeClient();
                    initialized.complete(null);
                } catch (Throwable failure) {
                    initialized.completeExceptionally(failure);
                }
            });
            initialized.get(60, TimeUnit.SECONDS);
        } catch (Throwable failure) {
            synchronized (System.getProperties()) {
                System.getProperties().remove(STARTED_KEY);
                System.getProperties().remove(LOADER_KEY);
            }
            STARTED.set(false);
            if (failure instanceof Exception) {
                throw (Exception) failure;
            }
            if (failure instanceof Error) {
                throw (Error) failure;
            }
            throw new RuntimeException(failure);
        }
    }

    private static boolean isAlreadyLoadedByForge(ClassLoader gameLoader) {
        try {
            Class<?> entry = Class.forName("dev.openhachimi.HachimiClient", false, gameLoader);
            Method get = entry.getMethod("get");
            return get.invoke(null) != null;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Minecraft awaitMinecraft() throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
        while (System.nanoTime() < deadline) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                return minecraft;
            }
            Thread.sleep(250L);
        }
        throw new IllegalStateException("Minecraft client instance did not become available");
    }

    private static void initializeClient() {
        dev.openhachimi.config.FontManager.init();
        FontManager.init();
        try {
            ModuleManager.init();
        } catch (Throwable failure) {
            failure.printStackTrace();
        }
        try {
            ConfigLoadManager.init();
        } catch (Throwable failure) {
            failure.printStackTrace();
        }
        EventRegistrar.register();
        System.err.println("[OpenHachimi] InjectBootstrap initialized on Minecraft's client thread.");
    }
}
