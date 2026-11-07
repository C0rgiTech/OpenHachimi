/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.hud;

import dev.openhachimi.modules.NotificationModule;
import dev.openhachimi.modules.WatermarkModule;


public final class Notifications {
    private static final byte[] S_3482321 = new byte[]{7, 44, 35, 32, 46, 39, 38};  // XOR0x42 "Enabled"
    private static final byte[] S_14009E9C = new byte[]{6, 43, 49, 35, 32, 46, 39, 38};  // XOR0x42 "Disabled"
    private static final byte[] S_263E477C = new byte[]{21, 35, 48, 44, 43, 44, 37};  // XOR0x42 "Warning"
    private static final byte[] S_401E1E8 = new byte[]{7, 48, 48, 45, 48};  // XOR0x42 "Error"
    private static final byte[] S_BEAB3C7 = new byte[]{10, 35, 33, 42, 43, 47, 43};  // XOR0x42 "Hachimi"

    private static void dispatch(String title, String message, NotificationType type, int duration) {
        WatermarkModule.postNotification(title, message, type, duration);
    }

    private static String getTopText(NotificationType type) {
        if (type == NotificationType.WARNING) {
            return "Warning";
        }
        if (type == NotificationType.ERROR) {
            return "Error";
        }
        return "Hachimi";
    }

    public static void show(String message, NotificationType type, int duration) {
        Notifications.dispatch(Notifications.getTopText(type), message, type, duration);
    }

    public static void show(String message, NotificationType type) {
        Notifications.dispatch(Notifications.getTopText(type), message, type, -1);
    }

    public static void showToggle(String moduleName, boolean enabled) {
        NotificationModule config = NotificationModule.active();
        if (config == null) {
            return;
        }
        if (!((Boolean)config.toggleNotify.get()).booleanValue()) {
            return;
        }
        Notifications.dispatch(moduleName, enabled ? "Enabled" : "Disabled", enabled ? NotificationType.ENABLED : NotificationType.DISABLED, -1);
    }

    public static void showForced(String message, NotificationType type) {
        int duration = -1;
        NotificationModule config = NotificationModule.active();
        if (config != null) {
            duration = config.durationFor(type);
        }
        if (duration > 0 && duration < 1500) {
            duration = 1500;
        }
        Notifications.dispatch(Notifications.getTopText(type), message, type, duration);
    }

    public enum NotificationType {
        INFO, WARNING, ERROR, NOTIFY, ENABLED, DISABLED;
    }
}

