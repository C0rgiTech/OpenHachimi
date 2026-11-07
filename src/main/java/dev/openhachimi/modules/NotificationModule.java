/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.hud.Notifications;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.utils.ColorValue;

import java.util.function.Supplier;

public class NotificationModule
extends Module {
    private static final byte[] S_2D45DD0B = new byte[]{12, 45, 54, 43, 36, 43, 33, 35, 54, 43, 45, 44};  // XOR0x42 "Notification"
    private static final byte[] S_7B7EB995 = new byte[]{22, 45, 37, 37, 46, 39, 98, 12, 45, 54, 43, 36, 59};  // XOR0x42 "Toggle Notify"
    private static final byte[] S_668C1BAB = new byte[]{6, 43, 49, 50, 46, 35, 59, 98, 22, 43, 47, 39};  // XOR0x42 "Display Time"
    private static final byte[] S_1944AF63 = new byte[]{0, 35, 33, 41, 37, 48, 45, 55, 44, 38, 98, 22, 45, 50};  // XOR0x42 "Background Top"
    private static final byte[] S_6142495D = new byte[]{0, 35, 33, 41, 37, 48, 45, 55, 44, 38, 98, 0, 45, 54, 54, 45, 47};  // XOR0x42 "Background Bottom"
    private static final byte[] S_59C953EC = new byte[]{0, 35, 33, 41, 37, 48, 45, 55, 44, 38, 98, 3, 46, 50, 42, 35};  // XOR0x42 "Background Alpha"
    private static final byte[] S_6D074B82 = new byte[]{16, 39, 33, 54, 35, 44, 37, 46, 39, 98, 1, 55, 49, 54, 45, 47};  // XOR0x42 "Rectangle Custom"
    private static final byte[] S_1405A932 = new byte[]{16, 39, 33, 54, 35, 44, 37, 46, 39, 98, 1, 45, 46, 45, 48};  // XOR0x42 "Rectangle Color"
    private static final byte[] S_3424A6F = new byte[]{0, 45, 48, 38, 39, 48, 98, 1, 45, 46, 45, 48};  // XOR0x42 "Border Color"
    private static final byte[] S_324CC6A = new byte[]{0, 45, 48, 38, 39, 48, 98, 3, 46, 50, 42, 35};  // XOR0x42 "Border Alpha"
    private static final byte[] S_521A4A5E = new byte[]{17, 42, 35, 38, 45, 53, 98, 3, 46, 50, 42, 35};  // XOR0x42 "Shadow Alpha"
    private static final byte[] S_287FFB5B = new byte[]{22, 43, 54, 46, 39, 98, 1, 45, 46, 45, 48};  // XOR0x42 "Title Color"
    private static final byte[] S_B781EB0 = new byte[]{22, 39, 58, 54, 98, 1, 45, 46, 45, 48};  // XOR0x42 "Text Color"
    private static final byte[] S_B5AA0AB = new byte[]{22, 39, 58, 54, 98, 3, 46, 50, 42, 35};  // XOR0x42 "Text Alpha"
    public final BooleanSetting toggleNotify = new BooleanSetting("Toggle Notify", true);
    public final ListSetting infoDuration = new ListSetting("Display Time", 2000, 500, 10000);
    public final ColorValue backgroundTop = new ColorValue("Background Top", 0x343438);
    public final ColorValue backgroundBottom = new ColorValue("Background Bottom", 0x1C1C1F);
    public final ListSetting backgroundAlpha = new ListSetting("Background Alpha", 218, 0, 255);
    public final BooleanSetting rectangleCustom = new BooleanSetting("Rectangle Custom", false);
    public final ColorValue rectangleColor = new ColorValue("Rectangle Color", 3906559);
    public final ColorValue borderColor = new ColorValue("Border Color", 0xFFFFFF);
    public final ListSetting borderAlpha = new ListSetting("Border Alpha", 54, 0, 255);
    public final ListSetting shadowAlpha = new ListSetting("Shadow Alpha", 24, 0, 255);
    public final ColorValue titleColor = new ColorValue("Title Color", 0xF5F5F7);
    public final ColorValue textColor = new ColorValue("Text Color", 11447986);
    public final ListSetting textAlpha = new ListSetting("Text Alpha", 205, 0, 255);

    public NotificationModule() {
        super("Notification", Category.RENDER);
        this.values.add(this.toggleNotify);
        this.values.add(this.infoDuration);
        this.values.add(this.backgroundTop);
        this.values.add(this.backgroundBottom);
        this.values.add(this.backgroundAlpha);
        this.values.add(this.rectangleCustom);
        this.values.add(this.rectangleColor);
        this.values.add(this.borderColor);
        this.values.add(this.borderAlpha);
        this.values.add(this.shadowAlpha);
        this.values.add(this.titleColor);
        this.values.add(this.textColor);
        this.values.add(this.textAlpha);
        this.rectangleColor.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)NotificationModule.this.rectangleCustom.get();
            }
        });
    }

    public static NotificationModule active() {
        Module module = ModuleManager.getModule("Notification");
        if (module instanceof NotificationModule && module.enabled) {
            return (NotificationModule)module;
        }
        return null;
    }

    public int durationFor(Notifications.NotificationType type) {
        int duration = (Integer)this.infoDuration.get();
        if (type == Notifications.NotificationType.WARNING) {
            duration = 5000;
        } else if (type == Notifications.NotificationType.ERROR) {
            duration = 10000;
        }
        return duration;
    }
}

