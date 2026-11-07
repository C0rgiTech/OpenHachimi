/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.DoubleSetting;
import dev.openhachimi.settings.IntSetting;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.world.entity.Entity;

public class FriendModule
extends Module {
    private static final byte[] S_7DEAF6BE = new byte[]{4, 48, 43, 39, 44, 38};  // XOR0x42 "Friend"
    private static final byte[] S_2008FB3D = new byte[]{3, 38, 38, 98, 4, 48, 43, 39, 44, 38};  // XOR0x42 "Add Friend"
    private static final byte[] S_7761A52 = new byte[]{105, 98, 3, 38, 38, 98, 4, 48, 43, 39, 44, 38};  // XOR0x42 "+ Add Friend"
    private static final byte[] S_2E5AB9BA = new byte[]{16, 39, 47, 45, 52, 39, 98, 4, 48, 43, 39, 44, 38};  // XOR0x42 "Remove Friend"
    private static final byte[] S_35A59B07 = new byte[]{111, 98, 16, 39, 47, 45, 52, 39, 98, 4, 48, 43, 39, 44, 38};  // XOR0x42 "- Remove Friend"
    private static final byte[] S_3F73E122 = new byte[]{4, 48, 43, 39, 44, 38, 98};  // XOR0x42 "Friend "
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_574 = new byte[]{110, 98};  // XOR0x42 ", "
    public static FriendModule instance;
    private static final int MAX_FRIENDS = 64;
    private final List<IntSetting> rows = new ArrayList<IntSetting>();
    private final DoubleSetting add;
    private final DoubleSetting remove;

    public FriendModule() {
        super("Friend", Category.MISC);
        instance = this;
        this.add = new DoubleSetting("Add Friend", "+ Add Friend", new Runnable(){

            @Override
            public void run() {
                FriendModule.this.appendRow();
            }
        });
        this.remove = new DoubleSetting("Remove Friend", "- Remove Friend", new Runnable(){

            @Override
            public void run() {
                FriendModule.this.dropRow();
            }
        });
        this.appendRow();
    }

    private List<String> names() {
        ArrayList<String> out = new ArrayList<String>();
        for (IntSetting row : this.rows) {
            String name = FriendModule.normalize((String)row.get());
            if (name.isEmpty() || out.contains(name)) continue;
            out.add(name);
        }
        return out;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean addName(String value) {
        FriendModule self = instance;
        if (self == null) {
            return false;
        }
        String name = FriendModule.normalize(value);
        if (name.isEmpty()) {
            return false;
        }
        for (IntSetting row : self.rows) {
            if (!FriendModule.normalize((String)row.get()).equals(name)) continue;
            return false;
        }
        for (IntSetting row : self.rows) {
            if (!FriendModule.normalize((String)row.get()).isEmpty()) continue;
            row.set(name);
            return true;
        }
        if (self.rows.size() >= 64) {
            return false;
        }
        self.appendRow();
        self.rows.get(self.rows.size() - 1).set(name);
        return true;
    }

    public static boolean isFriend(Entity entity) {
        if (entity == null) {
            return false;
        }
        return FriendModule.isFriendName(entity.getName().getString());
    }

    private void rebuild() {
        this.values.clear();
        for (IntSetting row : this.rows) {
            this.values.add(row);
        }
        this.values.add(this.add);
        this.values.add(this.remove);
        this.values.add(this.invisible);
    }

    private void appendRow() {
        if (this.rows.size() >= 64) {
            return;
        }
        this.rows.add(new IntSetting("Friend " + (this.rows.size() + 1), "", 16));
        this.rebuild();
    }

    public static boolean isFriendName(String value) {
        FriendModule self = instance;
        if (self == null || !self.enabled) {
            return false;
        }
        String target = FriendModule.normalize(value);
        if (target.isEmpty()) {
            return false;
        }
        for (String name : self.names()) {
            if (!name.equals(target)) continue;
            return true;
        }
        return false;
    }

    private void dropRow() {
        if (this.rows.size() <= 1) {
            this.rows.get(0).set("");
            this.rebuild();
            return;
        }
        this.rows.remove(this.rows.size() - 1);
        this.rebuild();
    }

    @Override
    public void prepareSettings(JsonObject values) {
        int count = 1;
        if (values != null) {
            for (int i = 1; i <= 64; ++i) {
                if (!values.has("Friend " + i)) continue;
                count = i;
            }
        }
        while (this.rows.size() < count) {
            this.rows.add(new IntSetting("Friend " + (this.rows.size() + 1), "", 16));
        }
        while (this.rows.size() > count) {
            this.rows.remove(this.rows.size() - 1);
        }
        this.rebuild();
    }

    public static boolean removeName(String value) {
        FriendModule self = instance;
        if (self == null) {
            return false;
        }
        String name = FriendModule.normalize(value);
        if (name.isEmpty()) {
            return false;
        }
        for (IntSetting row : self.rows) {
            if (!FriendModule.normalize((String)row.get()).equals(name)) continue;
            row.set("");
            return true;
        }
        return false;
    }

    public static String namesJoined() {
        FriendModule self = instance;
        if (self == null) {
            return "";
        }
        return String.join((CharSequence)", ", self.names());
    }
}

