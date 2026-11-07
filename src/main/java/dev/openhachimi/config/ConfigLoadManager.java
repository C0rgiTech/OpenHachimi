/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.config;


public class ConfigLoadManager {
    private static final byte[] S_5C13D641 = new byte[]{38, 39, 36, 35, 55, 46, 54};  // XOR0x42 "default"
    private static ConfigLoadManager instance;
    private String currentConfig = "default";

    public ConfigLoadManager() {
        new dev.openhachimi.config.ConfigManager(this.currentConfig).load();
    }

    public static ConfigLoadManager getInstance() {
        if (instance == null) {
            ConfigLoadManager.init();
        }
        return instance;
    }

    public static void init() {
        if (instance == null) {
            instance = new ConfigLoadManager();
        }
    }

    public void saveConfig(String name) {
        this.currentConfig = name;
        new dev.openhachimi.config.ConfigManager(this.currentConfig).save();
    }

    public void saveConfig() {
        new dev.openhachimi.config.ConfigManager(this.currentConfig).save();
    }

    public void loadConfig(String name) {
        this.currentConfig = name;
        new dev.openhachimi.config.ConfigManager(this.currentConfig).load();
    }

    public void loadConfig() {
        new dev.openhachimi.config.ConfigManager(this.currentConfig).load();
    }

    public String getCurrentConfig() {
        return this.currentConfig;
    }
}

