/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.config;

import dev.openhachimi.modules.Module;
import dev.openhachimi.security.NativeStringUtil;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.EnumSetting;
import dev.openhachimi.settings.IntSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.settings.Setting;
import dev.openhachimi.modules.ModuleManager;
import dev.openhachimi.utils.EventDispatcherImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;

public class ConfigManager {
    private static final byte[] S_6725D0EA = new byte[]{42, 39, 59, 50, 43, 58, 39, 46};  // XOR0x42 "heypixel"
    private static final byte[] S_2F3F8342 = new byte[]{33, 45, 44, 36, 43, 37};  // XOR0x42 "config"
    private static final byte[] S_166619 = new byte[]{108, 32, 43, 44};  // XOR0x42 ".bin"
    private static final byte[] S_5B0FA5B0 = new byte[]{14, 35, 26, 47, 37, 123, 17, 47, 45, 99, 18, 19, 49, 32, 99, 48, 45, 18, 32, 56, 2, 13, 47, 49, 3, 42, 33, 56, 58, 35, 3, 38, 45, 32, 39, 7, 32, 44, 47, 3, 24, 36, 37, 105, 2, 127, 10, 58, 99};  // XOR0x42 "LaXmg9Smo!PQsb!roPbz@OmsAhczxaAdobeEbnmAZfg+@=Hx!"
    private static final byte[] S_2E = new byte[]{108};
    private static final byte[] S_5C0 = new byte[]{108, 108};  // XOR0x42 ".."
    private static final byte[] S_224C3EB4 = new byte[]{30, 109, 120, 104, 125, 96, 126, 124, 62};  // XOR0x42 "\\/:*?\"<>|"
    private static final byte[] S_27C6ED = new byte[]{22, 43, 47, 39};  // XOR0x42 "Time"
    private static final byte[] S_4C7D471 = new byte[]{17, 54, 35, 54, 39};  // XOR0x42 "State"
    private static final byte[] S_1263F = new byte[]{9, 39, 59};  // XOR0x42 "Key"
    private static final byte[] S_184A8942 = new byte[]{20, 35, 46, 55, 39, 49};  // XOR0x42 "Values"
    private static final byte[] S_2C61BF67 = new byte[]{15, 45, 38, 55, 46, 39, 49};  // XOR0x42 "Modules"
    private static final byte[] S_16AA29 = new byte[]{108, 54, 47, 50};  // XOR0x42 ".tmp"
    private static final byte[] S_72D71A9 = new byte[]{4, 35, 43, 46, 39, 38, 98, 54, 45, 98, 49, 35, 52, 39, 98, 33, 45, 44, 36, 43, 37, 120, 98};  // XOR0x42 "Failed to save config: "
    private static final byte[] S_7DB3 = new byte[]{98, 111, 98};  // XOR0x42 " - "
    private static final byte[] S_66F8B205 = new byte[]{3, 44, 54, 43, 12, 43, 33, 41};  // XOR0x42 "AntiNick"
    private static final byte[] S_1D445469 = new byte[]{12, 43, 33, 41, 6, 39, 54, 39, 33, 54, 45, 48};  // XOR0x42 "NickDetector"
    private static final byte[] S_42433460 = new byte[]{4, 35, 43, 46, 39, 38, 98, 54, 45, 98, 46, 45, 35, 38, 98, 33, 45, 44, 36, 43, 37, 120, 98};  // XOR0x42 "Failed to load config: "
    public static final String DIR_NAME = "hachimi"; // initialiser lost by CFR
    public static final String CONFIG_DIR_NAME = "config"; // initialiser lost by CFR
    public static final String EXTENSION = ".bin"; // initialiser lost by CFR
    private final String name;
    private final File file;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String CRYPT_KEY = "LaXmg9Smo!PQsb!roPbz@OmsAhczxaAdobeEbnmAZfg+@=Hx!"; // initialiser lost by CFR

    public String getName() {
        return this.name;
    }

    public ConfigManager(String name) {
        this.name = name;
        this.file = ConfigManager.fileFor(name);
        File parent = this.file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    public void load() {
        if (!this.file.exists()) {
            this.save();
            return;
        }
        try {
            String encryptedContent = Files.readString(this.file.toPath()).trim();
            if (encryptedContent.isEmpty()) {
                return;
            }
            String decryptedJson = NativeStringUtil.decryptFromBase64(encryptedContent, "LaXmg9Smo!PQsb!roPbz@OmsAhczxaAdobeEbnmAZfg+@=Hx!");
            JsonObject jsonObject = (JsonObject)this.gson.fromJson(decryptedJson, JsonObject.class);
            if (jsonObject == null || !jsonObject.has("Modules")) {
                return;
            }
            JsonObject modulesObject = jsonObject.getAsJsonObject("Modules");
            for (Module m : ModuleManager.modules) {
                boolean state;
                String moduleKey = m.name;
                if (!modulesObject.has(moduleKey) && "AntiNick".equals(moduleKey) && modulesObject.has("NickDetector")) {
                    moduleKey = "NickDetector";
                }
                if (!modulesObject.has(moduleKey)) continue;
                JsonObject moduleObject = modulesObject.getAsJsonObject(moduleKey);
                if (moduleObject.has("State") && m.enabled != (state = moduleObject.get("State").getAsBoolean())) {
                    m.toggle();
                }
                if (moduleObject.has("Key")) {
                    m.key = moduleObject.get("Key").getAsInt();
                }
                if (!moduleObject.has("Values")) continue;
                JsonObject valueObject = moduleObject.getAsJsonObject("Values");
                m.prepareSettings(valueObject);
                for (Setting<?> v : m.values) {
                    if (!valueObject.has(v.name)) continue;
                    try {
                        if (v instanceof BooleanSetting) {
                            BooleanSetting bv = (BooleanSetting)v;
                            bv.set(valueObject.get(v.name).getAsBoolean());
                            continue;
                        }
                        if (v instanceof ModeSetting) {
                            ModeSetting mv = (ModeSetting)v;
                            mv.set(valueObject.get(v.name).getAsString());
                            continue;
                        }
                        if (v instanceof IntSetting) {
                            IntSetting sv = (IntSetting)v;
                            sv.set(valueObject.get(v.name).getAsString());
                            continue;
                        }
                        if (v instanceof EnumSetting) {
                            EnumSetting kv = (EnumSetting)v;
                            kv.setUnchecked(valueObject.get(v.name).getAsInt());
                            continue;
                        }
                        if (v instanceof ListSetting) {
                            ListSetting nv = (ListSetting)v;
                            nv.setUnchecked(valueObject.get(v.name).getAsInt());
                            continue;
                        }
                        if (!(v instanceof NumberSetting)) continue;
                        NumberSetting fv = (NumberSetting)v;
                        fv.setUnchecked(Float.valueOf(valueObject.get(v.name).getAsFloat()));
                    }
                    catch (Exception exception) {}
                }
            }
        }
        catch (Exception e) {
            EventDispatcherImpl.log("Failed to load config: " + this.name + " - " + e.getMessage());
        }
    }

    public void save() {
        block22: {
            try {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("Time", (Number)System.currentTimeMillis());
                JsonObject modulesObject = new JsonObject();
                for (Module m : ModuleManager.modules) {
                    JsonObject moduleObject = new JsonObject();
                    moduleObject.addProperty("State", Boolean.valueOf(m.enabled));
                    moduleObject.addProperty("Key", (Number)m.key);
                    JsonObject valueObject = new JsonObject();
                    for (Setting<?> v : m.values) {
                        if (v instanceof BooleanSetting) {
                            valueObject.addProperty(v.name, (Boolean)v.get());
                            continue;
                        }
                        if (v instanceof ModeSetting) {
                            valueObject.addProperty(v.name, (String)v.get());
                            continue;
                        }
                        if (v instanceof IntSetting) {
                            valueObject.addProperty(v.name, (String)v.get());
                            continue;
                        }
                        if (v instanceof EnumSetting) {
                            valueObject.addProperty(v.name, (Number)((Integer)v.get()));
                            continue;
                        }
                        if (v instanceof ListSetting) {
                            valueObject.addProperty(v.name, (Number)((Integer)v.get()));
                            continue;
                        }
                        if (!(v instanceof NumberSetting)) continue;
                        valueObject.addProperty(v.name, (Number)((Float)v.get()));
                    }
                    moduleObject.add("Values", (JsonElement)valueObject);
                    modulesObject.add(m.name, (JsonElement)moduleObject);
                }
                jsonObject.add("Modules", (JsonElement)modulesObject);
                String jsonStr = this.gson.toJson((JsonElement)jsonObject);
                String sealed = NativeStringUtil.encryptToBase64(jsonStr, "LaXmg9Smo!PQsb!roPbz@OmsAhczxaAdobeEbnmAZfg+@=Hx!");
                if (sealed == null || sealed.isEmpty()) {
                    return;
                }
                File parent = this.file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                File tmp = new File(parent, this.file.getName() + ".tmp");
                try (PrintWriter writer = new PrintWriter(tmp)) {
                    writer.println(sealed);
                }
                if (!tmp.renameTo(this.file)) {
                    try (PrintWriter writer = new PrintWriter(this.file)) {
                        writer.println(sealed);
                    }
                    tmp.delete();
                }
            }
            catch (Exception e) {
                EventDispatcherImpl.log("Failed to save config: " + this.name + " - " + e.getMessage());
            }
        }
    }

    public File getFile() {
        return this.file;
    }

    public boolean exists() {
        return this.file.exists();
    }

    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty() || trimmed.length() > 32) {
            return false;
        }
        if (trimmed.startsWith(".") || trimmed.contains("..")) {
            return false;
        }
        for (char c : "\\/:*?\"<>|".toCharArray()) {
            if (trimmed.indexOf(c) < 0) continue;
            return false;
        }
        for (int i = 0; i < trimmed.length(); ++i) {
            if (!Character.isWhitespace(trimmed.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static File fileFor(String name) {
        return new File(ConfigManager.resolveConfigDir(), name + ".bin");
    }

    public static List<String> listNames() {
        ArrayList<String> names = new ArrayList<String>();
        File[] files = ConfigManager.resolveConfigDir().listFiles();
        if (files == null) {
            return names;
        }
        for (File candidate : files) {
            String fileName;
            if (!candidate.isFile() || !(fileName = candidate.getName()).toLowerCase(Locale.ROOT).endsWith(".bin")) continue;
            names.add(fileName.substring(0, fileName.length() - ".bin".length()));
        }
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public static File resolveConfigDir() {
        File adamDir = new File(Minecraft.getInstance().gameDirectory, "heypixel");
        return new File(adamDir, "config");
    }
}

