/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.command;

import dev.openhachimi.hud.Notifications;
import dev.openhachimi.modules.ClickguiModule;
import dev.openhachimi.modules.FriendModule;
import dev.openhachimi.modules.Module;
import dev.openhachimi.modules.NotificationModule;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.settings.Setting;
import dev.openhachimi.utils.ModuleManager;
import dev.openhachimi.config.ConfigLoadManager;
import dev.openhachimi.utils.ClickGUI;

import com.mojang.blaze3d.platform.InputConstants;
import java.awt.Desktop;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class CommandManager {
    private static final byte[] S_3553F3A5 = new byte[]{50, 43, 44, 41, 98, 109, 98, 48, 45, 49, 39, 98, 109, 98, 37, 46, 35, 49, 49, 98, 109, 98, 38, 39, 36, 35, 55, 46, 54};  // XOR0x42 "pink / rose / glass / default"
    private static final byte[] S_35E4B04C = new byte[]{108, 36, 48, 43, 39, 44, 38};  // XOR0x42 ".friend"
    private static final byte[] S_6B15954 = new byte[]{108, 36, 48, 43, 39, 44, 38, 98};  // XOR0x42 ".friend "
    private static final byte[] S_20 = new byte[]{98};
    private static final byte[] S_0 = new byte[0];
    private static final byte[] S_16774 = new byte[]{30, 49, 105};  // XOR0x42 "\\s+"
    private static final byte[] S_30CF41 = new byte[]{42, 39, 46, 50};  // XOR0x42 "help"
    private static final byte[] S_35C17D = new byte[]{49, 35, 52, 39};  // XOR0x42 "save"
    private static final byte[] S_32C4E6 = new byte[]{46, 45, 35, 38};  // XOR0x42 "load"
    private static final byte[] S_32B09E = new byte[]{46, 43, 49, 54};  // XOR0x42 "list"
    private static final byte[] S_306685AB = new byte[]{38, 39, 46, 39, 54, 39};  // XOR0x42 "delete"
    private static final byte[] S_1840B = new byte[]{38, 39, 46};  // XOR0x42 "del"
    private static final byte[] S_484AF884 = new byte[]{48, 39, 47, 45, 52, 39};  // XOR0x42 "remove"
    private static final byte[] S_E3B = new byte[]{48, 47};  // XOR0x42 "rm"
    private static final byte[] S_3164AE = new byte[]{43, 44, 36, 45};  // XOR0x42 "info"
    private static final byte[] S_4ACDCFF2 = new byte[]{49, 54, 35, 54, 55, 49};  // XOR0x42 "status"
    private static final byte[] S_484A8199 = new byte[]{48, 39, 46, 45, 35, 38};  // XOR0x42 "reload"
    private static final byte[] S_69375C9 = new byte[]{54, 42, 39, 47, 39};  // XOR0x42 "theme"
    private static final byte[] S_68B1DB1 = new byte[]{49, 54, 59, 46, 39};  // XOR0x42 "style"
    private static final byte[] S_1BC62 = new byte[]{49, 39, 54};  // XOR0x42 "set"
    private static final byte[] S_1B30F = new byte[]{50, 55, 54};  // XOR0x42 "put"
    private static final byte[] S_4594E2DF = new byte[]{50, 48, 39, 49, 39, 54};  // XOR0x42 "preset"
    private static final byte[] S_4AD8989A = new byte[]{49, 54, 59, 46, 39, 105};  // XOR0x42 "style+"
    private static final byte[] S_1848D = new byte[]{38, 43, 48};  // XOR0x42 "dir"
    private static final byte[] S_346425 = new byte[]{50, 35, 54, 42};  // XOR0x42 "path"
    private static final byte[] S_34264A = new byte[]{45, 50, 39, 44};  // XOR0x42 "open"
    private static final byte[] S_345D1C6E = new byte[]{36, 45, 46, 38, 39, 48};  // XOR0x42 "folder"
    private static final byte[] S_2E243D = new byte[]{32, 43, 44, 38};  // XOR0x42 "bind"
    private static final byte[] S_19E5F = new byte[]{41, 39, 59};  // XOR0x42 "key"
    private static final byte[] S_44110163 = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 33, 45, 44, 36, 43, 37, 98, 49, 35, 52, 39, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "Usage: .config save <name>"
    private static final byte[] S_2C1E7636 = new byte[]{11, 44, 52, 35, 46, 43, 38, 98, 44, 35, 47, 39, 98, 96};  // XOR0x42 "Invalid name \""
    private static final byte[] S_3CB1B53A = new byte[]{96, 98, 111, 98, 115, 111, 113, 112, 98, 33, 42, 35, 48, 49, 110, 98, 44, 45, 98, 49, 50, 35, 33, 39, 49, 98, 45, 48, 98, 30, 98, 109, 98, 120, 98, 104, 98, 125, 98, 96, 98, 126, 98, 124, 98, 62};  // XOR0x42 "\" - 1-32 chars, no spaces or \\ / : * ? \" < > |"
    private static final byte[] S_255827FC = new byte[]{13, 52, 39, 48, 53, 48, 45, 54, 39, 98, 50, 48, 45, 36, 43, 46, 39, 98, 96};  // XOR0x42 "Overwrote profile \""
    private static final byte[] S_273ED4F2 = new byte[]{17, 35, 52, 39, 38, 98, 50, 48, 45, 36, 43, 46, 39, 98, 96};  // XOR0x42 "Saved profile \""
    private static final byte[] S_22 = new byte[]{96};
    private static final byte[] S_7C5605DA = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 33, 45, 44, 36, 43, 37, 98, 46, 45, 35, 38, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "Usage: .config load <name>"
    private static final byte[] S_3B84E74B = new byte[]{18, 48, 45, 36, 43, 46, 39, 98, 96};  // XOR0x42 "Profile \""
    private static final byte[] S_74110CAA = new byte[]{96, 98, 44, 45, 54, 98, 36, 45, 55, 44, 38, 98, 111, 98, 108, 33, 45, 44, 36, 43, 37, 98, 46, 43, 49, 54};  // XOR0x42 "\" not found - .config list"
    private static final byte[] S_2D55DD30 = new byte[]{14, 45, 35, 38, 39, 38, 98, 50, 48, 45, 36, 43, 46, 39, 98, 96};  // XOR0x42 "Loaded profile \""
    private static final byte[] S_299FEF68 = new byte[]{12, 45, 98, 50, 48, 45, 36, 43, 46, 39, 49, 98, 59, 39, 54, 98, 111, 98, 108, 33, 45, 44, 36, 43, 37, 98, 49, 35, 52, 39, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "No profiles yet - .config save <name>"
    private static final byte[] S_35193992 = new byte[]{18, 48, 45, 36, 43, 46, 39, 49, 98, 106};  // XOR0x42 "Profiles ("
    private static final byte[] S_A10F = new byte[]{107, 120, 98};  // XOR0x42 "): "
    private static final byte[] S_574 = new byte[]{110, 98};  // XOR0x42 ", "
    private static final byte[] S_40B = new byte[]{98, 105};  // XOR0x42 " +"
    private static final byte[] S_1F62B75 = new byte[]{98, 47, 45, 48, 39};  // XOR0x42 " more"
    private static final byte[] S_4448AF35 = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 33, 45, 44, 36, 43, 37, 98, 38, 39, 46, 39, 54, 39, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "Usage: .config delete <name>"
    private static final byte[] S_10573C91 = new byte[]{96, 98, 43, 49, 98, 43, 44, 98, 55, 49, 39, 98, 111, 98, 46, 45, 35, 38, 98, 35, 44, 45, 54, 42, 39, 48, 98, 50, 48, 45, 36, 43, 46, 39, 98, 36, 43, 48, 49, 54};  // XOR0x42 "\" is in use - load another profile first"
    private static final byte[] S_44578D77 = new byte[]{96, 98, 44, 45, 54, 98, 36, 45, 55, 44, 38};  // XOR0x42 "\" not found"
    private static final byte[] S_3C8D004 = new byte[]{6, 39, 46, 39, 54, 39, 38, 98, 50, 48, 45, 36, 43, 46, 39, 98, 96};  // XOR0x42 "Deleted profile \""
    private static final byte[] S_3F43BAB9 = new byte[]{1, 45, 55, 46, 38, 98, 44, 45, 54, 98, 38, 39, 46, 39, 54, 39, 98, 96};  // XOR0x42 "Could not delete \""
    private static final byte[] S_7DB3 = new byte[]{98, 111, 98};  // XOR0x42 " - "
    private static final byte[] S_20EDCE92 = new byte[]{44, 45, 54, 98, 53, 48, 43, 54, 54, 39, 44, 98, 59, 39, 54};  // XOR0x42 "not written yet"
    private static final byte[] S_2F = new byte[]{109};
    private static final byte[] S_85FF = new byte[]{98, 45, 44};  // XOR0x42 " on"
    private static final byte[] S_2C818A5D = new byte[]{96, 98, 42, 35, 49, 98, 44, 45, 98, 36, 43, 46, 39, 98, 45, 44, 98, 38, 43, 49, 41, 98, 59, 39, 54};  // XOR0x42 "\" has no file on disk yet"
    private static final byte[] S_29509D63 = new byte[]{16, 39, 46, 45, 35, 38, 39, 38, 98, 50, 48, 45, 36, 43, 46, 39, 98, 96};  // XOR0x42 "Reloaded profile \""
    private static final byte[] S_39E16AB3 = new byte[]{1, 46, 43, 33, 41, 5, 55, 43};  // XOR0x42 "ClickGui"
    private static final byte[] S_62445E55 = new byte[]{1, 46, 43, 33, 41, 5, 55, 43, 98, 47, 45, 38, 55, 46, 39, 98, 44, 45, 54, 98, 35, 52, 35, 43, 46, 35, 32, 46, 39};  // XOR0x42 "ClickGui module not available"
    private static final byte[] S_60A74195 = new byte[]{23, 44, 41, 44, 45, 53, 44, 98, 54, 42, 39, 47, 39, 98, 96};  // XOR0x42 "Unknown theme \""
    private static final byte[] S_FF251 = new byte[]{96, 98, 111, 98};  // XOR0x42 "\" - "
    private static final byte[] S_7663D242 = new byte[]{1, 46, 43, 33, 41, 5, 55, 43, 98, 54, 42, 39, 47, 39, 120, 98};  // XOR0x42 "ClickGui theme: "
    private static final byte[] S_65CDBD6F = new byte[]{13, 50, 39, 44, 39, 38, 120, 98};  // XOR0x42 "Opened: "
    private static final byte[] S_3D3371AF = new byte[]{13, 50, 39, 44, 98, 47, 35, 44, 55, 35, 46, 46, 59, 120, 98};  // XOR0x42 "Open manually: "
    private static final byte[] S_47705FEB = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 33, 45, 44, 36, 43, 37, 98, 49, 39, 54, 98, 126, 47, 45, 38, 55, 46, 39, 124, 98, 126, 50, 48, 45, 50, 39, 48, 54, 59, 124, 98, 126, 52, 35, 46, 55, 39, 124};  // XOR0x42 "Usage: .config set <module> <property> <value>"
    private static final byte[] S_1603FBF7 = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 49, 39, 54, 98, 126, 47, 45, 38, 55, 46, 39, 124, 98, 126, 50, 48, 45, 50, 39, 48, 54, 59, 124, 98, 126, 52, 35, 46, 55, 39, 124, 98, 98, 106, 45, 48, 98, 108, 49, 39, 54, 98, 126, 50, 48, 45, 50, 39, 48, 54, 59, 124, 98, 126, 52, 35, 46, 55, 39, 124, 107};  // XOR0x42 "Usage: .set <module> <property> <value>  (or .set <property> <value>)"
    private static final byte[] S_38A4EA01 = new byte[]{96, 98, 43, 49, 98, 35, 47, 32, 43, 37, 55, 45, 55, 49, 110, 98, 50, 48, 39, 36, 43, 58, 98, 54, 42, 39, 98, 47, 45, 38, 55, 46, 39};  // XOR0x42 "\" is ambiguous, prefix the module"
    private static final byte[] S_2B16726C = new byte[]{12, 45, 98, 49, 55, 33, 42, 98, 49, 39, 54, 54, 43, 44, 37, 121, 98, 54, 48, 59, 98, 108, 46, 43, 49, 54};  // XOR0x42 "No such setting; try .list"
    private static final byte[] S_1E1A55D8 = new byte[]{23, 44, 41, 44, 45, 53, 44, 98, 52, 35, 46, 55, 39, 98, 111, 98};  // XOR0x42 "Unknown value - "
    private static final byte[] S_454535A8 = new byte[]{7, 58, 50, 39, 33, 54, 39, 38, 98, 54, 48, 55, 39, 98, 109, 98, 36, 35, 46, 49, 39};  // XOR0x42 "Expected true / false"
    private static final byte[] S_6B419EE6 = new byte[]{0, 35, 38, 98, 44, 55, 47, 32, 39, 48, 98, 96};  // XOR0x42 "Bad number \""
    private static final byte[] S_7FA3 = new byte[]{98, 127, 98};  // XOR0x42 " = "
    private static final byte[] S_178A1 = new byte[]{35, 38, 38};  // XOR0x42 "add"
    private static final byte[] S_103111B1 = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 36, 48, 43, 39, 44, 38, 98, 35, 38, 38, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "Usage: .friend add <name>"
    private static final byte[] S_79169CC4 = new byte[]{4, 48, 43, 39, 44, 38, 98, 35, 38, 38, 39, 38, 120, 98};  // XOR0x42 "Friend added: "
    private static final byte[] S_2BA1A333 = new byte[]{3, 46, 48, 39, 35, 38, 59, 98, 43, 44, 98, 36, 48, 43, 39, 44, 38, 98, 46, 43, 49, 54, 120, 98};  // XOR0x42 "Already in friend list: "
    private static final byte[] S_6A677C98 = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 36, 48, 43, 39, 44, 38, 98, 48, 39, 47, 45, 52, 39, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "Usage: .friend remove <name>"
    private static final byte[] S_3C89CCA4 = new byte[]{4, 48, 43, 39, 44, 38, 98, 48, 39, 47, 45, 52, 39, 38, 120, 98};  // XOR0x42 "Friend removed: "
    private static final byte[] S_50DF9E18 = new byte[]{12, 45, 54, 98, 43, 44, 98, 36, 48, 43, 39, 44, 38, 98, 46, 43, 49, 54, 120, 98};  // XOR0x42 "Not in friend list: "
    private static final byte[] S_1FF12637 = new byte[]{4, 48, 43, 39, 44, 38, 98, 46, 43, 49, 54, 98, 43, 49, 98, 39, 47, 50, 54, 59};  // XOR0x42 "Friend list is empty"
    private static final byte[] S_32015F5B = new byte[]{4, 48, 43, 39, 44, 38, 49, 120, 98};  // XOR0x42 "Friends: "
    private static final byte[] S_3CFD257 = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 36, 48, 43, 39, 44, 38, 98, 35, 38, 38, 62, 48, 39, 47, 45, 52, 39, 62, 46, 43, 49, 54, 98, 126, 44, 35, 47, 39, 124};  // XOR0x42 "Usage: .friend add|remove|list <name>"
    private static final byte[] S_36758E = new byte[]{54, 48, 55, 39};  // XOR0x42 "true"
    private static final byte[] S_DDF = new byte[]{45, 44};  // XOR0x42 "on"
    private static final byte[] S_1D2E7 = new byte[]{59, 39, 49};  // XOR0x42 "yes"
    private static final byte[] S_31 = new byte[]{115};
    private static final byte[] S_5CB1923 = new byte[]{36, 35, 46, 49, 39};  // XOR0x42 "false"
    private static final byte[] S_1AD6F = new byte[]{45, 36, 36};  // XOR0x42 "off"
    private static final byte[] S_DC1 = new byte[]{44, 45};  // XOR0x42 "no"
    private static final byte[] S_30 = new byte[]{114};
    private static final byte[] S_23 = new byte[]{97};
    private static final byte[] S_648 = new byte[]{114, 58};  // XOR0x42 "0x"
    private static final byte[] S_628 = new byte[]{114, 26};  // XOR0x42 "0X"
    private static final byte[] S_2D45DD0B = new byte[]{12, 45, 54, 43, 36, 43, 33, 35, 54, 43, 45, 44};  // XOR0x42 "Notification"
    private static final byte[] S_7352FD = new byte[]{12, 45, 54, 43, 36, 43, 33, 35, 54, 43, 45, 44, 98, 47, 45, 38, 55, 46, 39, 98, 44, 45, 54, 98, 35, 52, 35, 43, 46, 35, 32, 46, 39};  // XOR0x42 "Notification module not available"
    private static final byte[] S_13798A6B = new byte[]{18, 48, 39, 49, 39, 54, 49, 120, 98, 50, 43, 44, 41, 98, 109, 98, 48, 45, 49, 39, 98, 109, 98, 37, 46, 35, 49, 49, 98, 109, 98, 38, 39, 36, 35, 55, 46, 54};  // XOR0x42 "Presets: pink / rose / glass / default"
    private static final byte[] S_348176 = new byte[]{50, 43, 44, 41};  // XOR0x42 "pink"
    private static final byte[] S_35814F = new byte[]{48, 45, 49, 39};  // XOR0x42 "rose"
    private static final byte[] S_5DE077C = new byte[]{37, 46, 35, 49, 49};  // XOR0x42 "glass"
    private static final byte[] S_5C13D641 = new byte[]{38, 39, 36, 35, 55, 46, 54};  // XOR0x42 "default"
    private static final byte[] S_26105657 = new byte[]{23, 44, 41, 44, 45, 53, 44, 98, 50, 48, 39, 49, 39, 54, 98, 96};  // XOR0x42 "Unknown preset \""
    private static final byte[] S_37C21265 = new byte[]{18, 48, 39, 49, 39, 54, 120, 98};  // XOR0x42 "Preset: "
    private static final byte[] S_4C44E62A = new byte[]{49, 35, 52, 39, 98, 109, 98, 46, 45, 35, 38, 98, 109, 98, 46, 43, 49, 54, 98, 109, 98, 38, 39, 46, 39, 54, 39, 98, 109, 98, 43, 44, 36, 45, 98, 109, 98, 48, 39, 46, 45, 35, 38, 98, 109, 98, 54, 42, 39, 47, 39, 98, 109, 98, 49, 39, 54, 98, 109, 98, 50, 48, 39, 49, 39, 54, 98, 109, 98, 38, 43, 48, 98, 109, 98, 45, 50, 39, 44};  // XOR0x42 "save / load / list / delete / info / reload / theme / set / preset / dir / open"
    private static final byte[] S_42 = new byte[]{0};
    private static final byte[] S_41C35695 = new byte[]{103, 108, 115, 36, 9, 0};  // XOR0x42 "%.1fKB"
    private static final byte[] S_5A0 = new byte[]{111, 111};  // XOR0x42 "--"
    private static final byte[] S_7D29815E = new byte[]{23, 49, 35, 37, 39, 120, 98, 108, 32, 43, 44, 38, 98, 126, 47, 45, 38, 55, 46, 39, 124, 98, 126, 41, 39, 59, 124, 98, 98, 98, 39, 108, 37, 108, 98, 108, 32, 43, 44, 38, 98, 33, 46, 43, 33, 41, 37, 55, 43, 98, 27};  // XOR0x42 "Usage: .bind <module> <key>   e.g. .bind clickgui Y"
    private static final byte[] S_A505647 = new byte[]{15, 45, 38, 55, 46, 39, 98, 44, 45, 54, 98, 36, 45, 55, 44, 38, 120, 98};  // XOR0x42 "Module not found: "
    private static final byte[] S_3DEF9A4F = new byte[]{23, 44, 41, 44, 45, 53, 44, 98, 41, 39, 59, 120, 98};  // XOR0x42 "Unknown key: "
    private static final byte[] S_4482BB43 = new byte[]{98, 32, 45, 55, 44, 38, 98, 54, 45, 98};  // XOR0x42 " bound to "
    private static final byte[] S_197EE = new byte[]{43, 44, 49};  // XOR0x42 "ins"
    private static final byte[] S_347AD2 = new byte[]{50, 37, 55, 50};  // XOR0x42 "pgup"
    private static final byte[] S_3478C1 = new byte[]{50, 37, 38, 44};  // XOR0x42 "pgdn"
    private static final byte[] S_18975 = new byte[]{39, 49, 33};  // XOR0x42 "esc"
    private static final byte[] S_1B8A1 = new byte[]{48, 39, 54};  // XOR0x42 "ret"
    private static final byte[] S_3ED0CF96 = new byte[]{46, 49, 42, 43, 36, 54};  // XOR0x42 "lshift"
    private static final byte[] S_490DE350 = new byte[]{48, 49, 42, 43, 36, 54};  // XOR0x42 "rshift"
    private static final byte[] S_620ACD7 = new byte[]{46, 33, 54, 48, 46};  // XOR0x42 "lctrl"
    private static final byte[] S_67539DD = new byte[]{48, 33, 54, 48, 46};  // XOR0x42 "rctrl"
    private static final byte[] S_3291BD = new byte[]{46, 35, 46, 54};  // XOR0x42 "lalt"
    private static final byte[] S_354BF7 = new byte[]{48, 35, 46, 54};  // XOR0x42 "ralt"
    private static final byte[] S_3970C2B9 = new byte[]{43, 44, 49, 39, 48, 54};  // XOR0x42 "insert"
    private static final byte[] S_501A1A5A = new byte[]{50, 35, 37, 39, 108, 55, 50};  // XOR0x42 "page.up"
    private static final byte[] S_31F53CA1 = new byte[]{50, 35, 37, 39, 108, 38, 45, 53, 44};  // XOR0x42 "page.down"
    private static final byte[] S_32DC8081 = new byte[]{39, 49, 33, 35, 50, 39};  // XOR0x42 "escape"
    private static final byte[] S_5C306D8 = new byte[]{39, 44, 54, 39, 48};  // XOR0x42 "enter"
    private static final byte[] S_3F0A97DB = new byte[]{46, 39, 36, 54, 108, 49, 42, 43, 36, 54};  // XOR0x42 "left.shift"
    private static final byte[] S_28511630 = new byte[]{48, 43, 37, 42, 54, 108, 49, 42, 43, 36, 54};  // XOR0x42 "right.shift"
    private static final byte[] S_649F1E76 = new byte[]{46, 39, 36, 54, 108, 33, 45, 44, 54, 48, 45, 46};  // XOR0x42 "left.control"
    private static final byte[] S_163F5B8B = new byte[]{48, 43, 37, 42, 54, 108, 33, 45, 44, 54, 48, 45, 46};  // XOR0x42 "right.control"
    private static final byte[] S_665BA742 = new byte[]{46, 39, 36, 54, 108, 35, 46, 54};  // XOR0x42 "left.alt"
    private static final byte[] S_2267B6D7 = new byte[]{48, 43, 37, 42, 54, 108, 35, 46, 54};  // XOR0x42 "right.alt"
    private static final byte[] S_21ECD058 = new byte[]{41, 39, 59, 108, 41, 39, 59, 32, 45, 35, 48, 38, 108};  // XOR0x42 "key.keyboard."
    private static final byte[] S_309E28F0 = new byte[]{108, 33, 45, 44, 36, 43, 37};  // XOR0x42 ".config"
    private static final byte[] S_166976 = new byte[]{108, 33, 36, 37};  // XOR0x42 ".cfg"
    private static final byte[] S_4B804C47 = new byte[]{15, 15, 111, 38, 38, 98, 10, 10, 120, 47, 47};  // XOR0x42 "MM-dd HH:mm"
    private static final byte[] S_16A574 = new byte[]{108, 49, 39, 54};  // XOR0x42 ".set"
    private static final byte[] S_169C21 = new byte[]{108, 50, 55, 54};  // XOR0x42 ".put"
    private static final byte[] S_5512625B = new byte[]{108, 54, 42, 39, 47, 39};  // XOR0x42 ".theme"
    private static final byte[] S_2BDFAAB = new byte[]{108, 49, 35, 52, 39};  // XOR0x42 ".save"
    private static final byte[] S_2BAFE14 = new byte[]{108, 46, 45, 35, 38};  // XOR0x42 ".load"
    private static final byte[] S_2BAE9CC = new byte[]{108, 46, 43, 49, 54};  // XOR0x42 ".list"
    private static final String[] ROOTS = new String[]{".config", ".cfg"};
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final int LIST_SHOWN = 8;
    private static final String[] SHORTHANDS = new String[]{".set", ".put", ".cfg", ".theme", ".save", ".load", ".list"};

    private static int parseKey(String name) {
        if (name == null) {
            return -1;
        }
        String raw = name.trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) {
            return -1;
        }
        switch (raw) {
            case "ins": {
                raw = "insert";
                break;
            }
            case "del": {
                raw = "delete";
                break;
            }
            case "pgup": {
                raw = "page.up";
                break;
            }
            case "pgdn": {
                raw = "page.down";
                break;
            }
            case "esc": {
                raw = "escape";
                break;
            }
            case "ret": {
                raw = "enter";
                break;
            }
            case "lshift": {
                raw = "left.shift";
                break;
            }
            case "rshift": {
                raw = "right.shift";
                break;
            }
            case "lctrl": {
                raw = "left.control";
                break;
            }
            case "rctrl": {
                raw = "right.control";
                break;
            }
            case "lalt": {
                raw = "left.alt";
                break;
            }
            case "ralt": {
                raw = "right.alt";
                break;
            }
        }
        try {
            InputConstants.Key key = InputConstants.getKey((String)("key.keyboard." + raw));
            if (key == null) {
                return -1;
            }
            int value = key.getValue();
            return value <= 0 ? -1 : value;
        }
        catch (Throwable t) {
            return -1;
        }
    }

    private CommandManager() {
    }

    private static void notify(String text, Notifications.NotificationType type) {
        try {
            Notifications.showForced(text, type);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void notify(String text) {
        CommandManager.notify(text, Notifications.NotificationType.NOTIFY);
    }

    private static void load(String name) {
        if (name == null) {
            CommandManager.warn("Usage: .config load <name>");
            return;
        }
        if (!dev.openhachimi.config.ConfigManager.isValidName(name)) {
            CommandManager.warn("Invalid name \"" + name + "\"");
            return;
        }
        if (!dev.openhachimi.config.ConfigManager.fileFor(name).exists()) {
            CommandManager.warn("Profile \"" + name + "\" not found - .config list");
            return;
        }
        ConfigLoadManager.getInstance().loadConfig(name);
        CommandManager.notify("Loaded profile \"" + name + "\"");
    }

    public static boolean matches(String message) {
        if (message == null) {
            return false;
        }
        String trimmed = message.trim();
        if (trimmed.equals(".friend") || trimmed.startsWith(".friend ")) {
            return true;
        }
        for (String root : ROOTS) {
            if (!trimmed.equals(root) && !trimmed.startsWith(root + " ")) continue;
            return true;
        }
        for (String prefix : SHORTHANDS) {
            if (!trimmed.equals(prefix) && !trimmed.startsWith(prefix + " ")) continue;
            return true;
        }
        return false;
    }

    private static String size(long bytes) {
        if (bytes < 1024L) {
            return bytes + "B";
        }
        return String.format(Locale.ROOT, "%.1fKB", (double)bytes / 1024.0);
    }

    private static String join(String[] parts, int from, int toExclusive) {
        StringBuilder sb = new StringBuilder(32);
        for (int i = from; i < toExclusive; ++i) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private static boolean apply(Setting<?> property, String raw) {
        if (property instanceof BooleanSetting) {
            Boolean parsed = CommandManager.parseBool(raw);
            if (parsed == null) {
                return false;
            }
            ((BooleanSetting)property).set(parsed);
            return true;
        }
        if (property instanceof ModeSetting) {
            return ((ModeSetting)property).setIgnoreCase(raw);
        }
        if (property instanceof ListSetting) {
            Integer parsed = CommandManager.parseNumber(raw);
            if (parsed == null) {
                return false;
            }
            ((ListSetting)property).setUnchecked(parsed);
            return true;
        }
        if (property instanceof NumberSetting) {
            try {
                float parsed = Float.parseFloat(raw.trim());
                ((NumberSetting)property).setUnchecked(Float.valueOf(parsed));
                return true;
            }
            catch (NumberFormatException ignored) {
                return false;
            }
        }
        return false;
    }

    private static void list() {
        List<String> names = dev.openhachimi.config.ConfigManager.listNames();
        if (names.isEmpty()) {
            CommandManager.notify("No profiles yet - .config save <name>");
            return;
        }
        String current = ConfigLoadManager.getInstance().getCurrentConfig();
        StringBuilder sb = new StringBuilder(64);
        sb.append("Profiles (").append(names.size()).append("): ");
        int shown = Math.min(names.size(), 8);
        for (int i = 0; i < shown; ++i) {
            String n;
            if (i > 0) {
                sb.append(", ");
            }
            if ((n = names.get(i)).equalsIgnoreCase(current)) {
                sb.append('*');
            }
            sb.append(n);
        }
        if (names.size() > shown) {
            sb.append(" +").append(names.size() - shown).append(" more");
        }
        CommandManager.notify(sb.toString());
    }

    private static void info() {
        ConfigLoadManager registry = ConfigLoadManager.getInstance();
        String current = registry.getCurrentConfig();
        File file = dev.openhachimi.config.ConfigManager.fileFor(current);
        int enabled = 0;
        for (Module module : ModuleManager.modules) {
            if (!module.enabled) continue;
            ++enabled;
        }
        String tail = file.exists() ? CommandManager.size(file.length()) + " - " + CommandManager.stamp(file.lastModified()) : "not written yet";
        CommandManager.notify(current + " - " + tail + " - " + enabled + "/" + ModuleManager.modules.size() + " on");
    }

    private static void set(String message) {
        String prop;
        Setting<?> found;
        Module candidate;
        int first;
        String[] parts = message.trim().split("\\s+");
        for (first = 1; first < parts.length && !parts[first].equalsIgnoreCase("set") && !parts[first].equalsIgnoreCase("put"); ++first) {
        }
        if (first >= parts.length) {
            CommandManager.warn("Usage: .config set <module> <property> <value>");
            return;
        }
        String[] rest = new String[parts.length - first - 1];
        System.arraycopy(parts, first + 1, rest, 0, rest.length);
        if (rest.length < 2) {
            CommandManager.warn("Usage: .set <module> <property> <value>  (or .set <property> <value>)");
            return;
        }
        String value = rest[rest.length - 1];
        Module module = null;
        Setting<?> property = null;
        if (rest.length >= 3 && (candidate = ModuleManager.getModule(rest[0])) != null && (found = CommandManager.findSetting(candidate, prop = CommandManager.join(rest, 1, rest.length - 1))) != null) {
            module = candidate;
            property = found;
        }
        if (property == null) {
            String prop2 = CommandManager.join(rest, 0, rest.length - 1);
            for (Module candidate2 : ModuleManager.modules) {
                Setting<?> found2 = CommandManager.findSetting(candidate2, prop2);
                if (found2 == null) continue;
                if (property != null) {
                    CommandManager.warn("\"" + prop2 + "\" is ambiguous, prefix the module");
                    return;
                }
                module = candidate2;
                property = found2;
            }
        }
        if (module == null || property == null) {
            CommandManager.warn("No such setting; try .list");
            return;
        }
        if (!CommandManager.apply(property, value)) {
            if (property instanceof ModeSetting) {
                CommandManager.warn("Unknown value - " + ((ModeSetting)property).getModeList());
            } else if (property instanceof BooleanSetting) {
                CommandManager.warn("Expected true / false");
            } else {
                CommandManager.warn("Bad number \"" + value + "\"");
            }
            return;
        }
        ConfigLoadManager.getInstance().saveConfig();
        CommandManager.notify(property.name + " = " + property.getDisplay());
    }

    public static void execute(String message) {
        String[] parts;
        String trimmed = message == null ? "" : message.trim();
        String[] stringArray = parts = trimmed.isEmpty() ? new String[]{} : trimmed.split("\\s+");
        if (parts.length >= 1 && ".friend".equalsIgnoreCase(parts[0])) {
            CommandManager.friend(parts);
            return;
        }
        String sub = parts.length >= 2 ? parts[1].toLowerCase(Locale.ROOT) : "help";
        String arg = parts.length >= 3 ? parts[2] : null;
        switch (sub) {
            case "save": {
                CommandManager.save(arg);
                break;
            }
            case "load": {
                CommandManager.load(arg);
                break;
            }
            case "list": {
                CommandManager.list();
                break;
            }
            case "delete": 
            case "del": 
            case "remove": 
            case "rm": {
                CommandManager.delete(arg);
                break;
            }
            case "info": 
            case "status": {
                CommandManager.info();
                break;
            }
            case "reload": {
                CommandManager.reload();
                break;
            }
            case "theme": 
            case "style": {
                CommandManager.theme(arg);
                break;
            }
            case "set": 
            case "put": {
                CommandManager.set(trimmed);
                break;
            }
            case "preset": 
            case "style+": {
                CommandManager.preset(arg);
                break;
            }
            case "dir": 
            case "path": {
                CommandManager.dir();
                break;
            }
            case "open": 
            case "folder": {
                CommandManager.open();
                break;
            }
            case "bind": 
            case "key": {
                CommandManager.bind(parts.length >= 3 ? parts[2] : null, parts.length >= 4 ? parts[3] : null);
                break;
            }
            default: {
                CommandManager.help();
            }
        }
    }

    private static void delete(String name) {
        if (name == null) {
            CommandManager.warn("Usage: .config delete <name>");
            return;
        }
        if (!dev.openhachimi.config.ConfigManager.isValidName(name)) {
            CommandManager.warn("Invalid name \"" + name + "\"");
            return;
        }
        if (name.equalsIgnoreCase(ConfigLoadManager.getInstance().getCurrentConfig())) {
            CommandManager.warn("\"" + name + "\" is in use - load another profile first");
            return;
        }
        File file = dev.openhachimi.config.ConfigManager.fileFor(name);
        if (!file.exists()) {
            CommandManager.warn("Profile \"" + name + "\" not found");
            return;
        }
        if (file.delete()) {
            CommandManager.notify("Deleted profile \"" + name + "\"");
        } else {
            CommandManager.warn("Could not delete \"" + name + "\"");
        }
    }

    private static void save(String name) {
        if (name == null) {
            CommandManager.warn("Usage: .config save <name>");
            return;
        }
        if (!dev.openhachimi.config.ConfigManager.isValidName(name)) {
            CommandManager.warn("Invalid name \"" + name + "\" - 1-32 chars, no spaces or \\ / : * ? \" < > |");
            return;
        }
        boolean existed = dev.openhachimi.config.ConfigManager.fileFor(name).exists();
        ConfigLoadManager.getInstance().saveConfig(name);
        CommandManager.notify((existed ? "Overwrote profile \"" : "Saved profile \"") + name + "\"");
    }

    private static void open() {
        File dir = dev.openhachimi.config.ConfigManager.resolveConfigDir();
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
                CommandManager.notify("Opened: " + dir.getAbsolutePath());
                return;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        CommandManager.warn("Open manually: " + dir.getAbsolutePath());
    }

    private static void bind(String moduleName, String keyName) {
        if (moduleName == null || keyName == null) {
            CommandManager.warn("Usage: .bind <module> <key>   e.g. .bind clickgui Y");
            return;
        }
        String needle = moduleName.replace(" ", "").toLowerCase(Locale.ROOT);
        Module target = null;
        for (Module module : ModuleManager.modules) {
            if (module.name == null || !module.name.replace(" ", "").toLowerCase(Locale.ROOT).equals(needle)) continue;
            target = module;
            break;
        }
        if (target == null) {
            CommandManager.warn("Module not found: " + moduleName);
            return;
        }
        int code = CommandManager.parseKey(keyName);
        if (code < 0) {
            CommandManager.warn("Unknown key: " + keyName);
            return;
        }
        target.key = code;
        try {
            ConfigLoadManager.getInstance().saveConfig();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        CommandManager.warn(target.name + " bound to " + keyName.toUpperCase(Locale.ROOT));
    }

    private static void dir() {
        CommandManager.notify(dev.openhachimi.config.ConfigManager.resolveConfigDir().getAbsolutePath());
    }

    private static void warn(String text) {
        CommandManager.notify(text, Notifications.NotificationType.WARNING);
    }

    private static Integer parseNumber(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        try {
            if (v.startsWith("#")) {
                return Integer.parseInt(v.substring(1), 16);
            }
            if (v.length() > 2 && (v.startsWith("0x") || v.startsWith("0X"))) {
                return (int)Long.parseLong(v.substring(2), 16);
            }
            return Integer.parseInt(v);
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String stamp(long millis) {
        if (millis <= 0L) {
            return "--";
        }
        return STAMP.format(Instant.ofEpochMilli(millis));
    }

    private static void reload() {
        ConfigLoadManager registry = ConfigLoadManager.getInstance();
        String current = registry.getCurrentConfig();
        if (!dev.openhachimi.config.ConfigManager.fileFor(current).exists()) {
            CommandManager.warn("Profile \"" + current + "\" has no file on disk yet");
            return;
        }
        registry.loadConfig(current);
        CommandManager.notify("Reloaded profile \"" + current + "\"");
    }

    private static void help() {
        CommandManager.notify("save / load / list / delete / info / reload / theme / set / preset / dir / open");
    }

    private static void preset(String name) {
        Module module = ModuleManager.getModule("Notification");
        if (!(module instanceof NotificationModule)) {
            CommandManager.warn("Notification module not available");
            return;
        }
        NotificationModule cfg = (NotificationModule)module;
        if (name == null) {
            CommandManager.notify("Presets: pink / rose / glass / default");
            return;
        }
        switch (name.trim().toLowerCase(Locale.ROOT)) {
            case "pink": {
                CommandManager.applyPink(cfg);
                break;
            }
            case "rose": {
                CommandManager.applyRose(cfg);
                break;
            }
            case "glass": {
                CommandManager.applyGlass(cfg);
                break;
            }
            case "default": {
                for (Setting value : cfg.values) {
                    value.reset();
                }
                break;
            }
            default: {
                CommandManager.warn("Unknown preset \"" + name + "\" - " + "pink / rose / glass / default");
                return;
            }
        }
        ConfigLoadManager.getInstance().saveConfig();
        CommandManager.notify("Preset: " + name.trim().toLowerCase(Locale.ROOT));
    }

    private static void theme(String arg) {
        Module module = ModuleManager.getModule("ClickGui");
        if (!(module instanceof ClickguiModule)) {
            CommandManager.warn("ClickGui module not available");
            return;
        }
        ClickguiModule clickGui = (ClickguiModule)module;
        if (arg == null) {
            clickGui.theme.next();
        } else {
            String canonical = CommandManager.canonicalTheme(arg);
            if (canonical == null) {
                CommandManager.warn("Unknown theme \"" + arg + "\" - " + CommandManager.themeList());
                return;
            }
            clickGui.theme.set(canonical);
        }
        ConfigLoadManager.getInstance().saveConfig();
        CommandManager.notify("ClickGui theme: " + clickGui.theme.getDisplay());
    }

    private static String themeList() {
        StringBuilder sb = new StringBuilder(48);
        for (int i = 0; i < ClickGUI.THEMES.length; ++i) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ClickGUI.THEMES[i]);
        }
        return sb.toString();
    }

    private static void friend(String[] parts) {
        String name;
        String sub = parts.length >= 2 ? parts[1].toLowerCase(Locale.ROOT) : "";
        String string = name = parts.length >= 3 ? parts[2] : "";
        if ("add".equals(sub)) {
            if (name.isEmpty()) {
                CommandManager.warn("Usage: .friend add <name>");
                return;
            }
            if (FriendModule.addName(name)) {
                ConfigLoadManager.getInstance().saveConfig();
                CommandManager.notify("Friend added: " + name);
            } else {
                CommandManager.warn("Already in friend list: " + name);
            }
            return;
        }
        if ("remove".equals(sub) || "rm".equals(sub) || "del".equals(sub)) {
            if (name.isEmpty()) {
                CommandManager.warn("Usage: .friend remove <name>");
                return;
            }
            if (FriendModule.removeName(name)) {
                ConfigLoadManager.getInstance().saveConfig();
                CommandManager.notify("Friend removed: " + name);
            } else {
                CommandManager.warn("Not in friend list: " + name);
            }
            return;
        }
        if ("list".equals(sub)) {
            String names = FriendModule.namesJoined();
            CommandManager.notify(names.isEmpty() ? "Friend list is empty" : "Friends: " + names);
            return;
        }
        CommandManager.warn("Usage: .friend add|remove|list <name>");
    }

    private static String canonicalTheme(String wanted) {
        if (wanted == null) {
            return null;
        }
        String trimmed = wanted.trim();
        for (String theme : ClickGUI.THEMES) {
            if (!theme.equalsIgnoreCase(trimmed)) continue;
            return theme;
        }
        return null;
    }

    private static Boolean parseBool(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim().toLowerCase(Locale.ROOT);
        if (v.equals("true") || v.equals("on") || v.equals("yes") || v.equals("1")) {
            return Boolean.TRUE;
        }
        if (v.equals("false") || v.equals("off") || v.equals("no") || v.equals("0")) {
            return Boolean.FALSE;
        }
        return null;
    }

    private static Setting<?> findSetting(Module module, String name) {
        if (module == null || name == null) {
            return null;
        }
        for (Setting<?> candidate : module.values) {
            if (!candidate.name.equalsIgnoreCase(name)) continue;
            return candidate;
        }
        String squeezed = name.replace(" ", "");
        for (Setting<?> candidate : module.values) {
            if (!candidate.name.replace(" ", "").equalsIgnoreCase(squeezed)) continue;
            return candidate;
        }
        return null;
    }

    private static void applyPink(NotificationModule cfg) {
        cfg.backgroundTop.set(3873331);
        cfg.backgroundBottom.set(1838618);
        cfg.backgroundAlpha.set(150);
        cfg.rectangleCustom.set(true);
        cfg.rectangleColor.set(16732099);
        cfg.borderColor.set(16743129);
        cfg.borderAlpha.set(70);
        cfg.shadowAlpha.set(45);
        cfg.titleColor.set(16743129);
        cfg.textColor.set(15972314);
        cfg.textAlpha.set(235);
        cfg.toggleNotify.set(true);
    }

    private static void applyGlass(NotificationModule cfg) {
        cfg.backgroundTop.set(3947586);
        cfg.backgroundBottom.set(0x1E1E22);
        cfg.backgroundAlpha.set(165);
        cfg.rectangleCustom.set(false);
        cfg.borderColor.set(0xFFFFFF);
        cfg.borderAlpha.set(80);
        cfg.shadowAlpha.set(30);
        cfg.titleColor.set(0xF7F7FA);
        cfg.textColor.set(13224402);
        cfg.textAlpha.set(210);
        cfg.toggleNotify.set(true);
    }

    private static void applyRose(NotificationModule cfg) {
        cfg.backgroundTop.set(4857662);
        cfg.backgroundBottom.set(2231836);
        cfg.backgroundAlpha.set(185);
        cfg.rectangleCustom.set(true);
        cfg.rectangleColor.set(16732099);
        cfg.borderColor.set(16751584);
        cfg.borderAlpha.set(70);
        cfg.shadowAlpha.set(35);
        cfg.titleColor.set(16769523);
        cfg.textColor.set(15774685);
        cfg.textAlpha.set(215);
        cfg.toggleNotify.set(true);
    }
}

