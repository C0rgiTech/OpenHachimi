package dev.openhachimi.security;

import java.lang.instrument.Instrumentation;

/**
 * Java-agent entry point used by the native injector to obtain a JVMTI environment
 * in an already-running Minecraft process. It does not transform game classes.
 */
public final class InjectAgent {
    private InjectAgent() {
    }

    public static void agentmain(String arguments, Instrumentation instrumentation) {
        System.getProperties().put("openhachimi.inject.instrumentation", instrumentation);
    }
}
