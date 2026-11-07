/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.render.ClickGuiScreen;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.ClickGUIRenderHelper;
import dev.openhachimi.utils.ColorValue;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClickguiModule
extends Module {
    private static final byte[] S_39E16AB3 = new byte[]{1, 46, 43, 33, 41, 5, 55, 43};  // XOR0x42 "ClickGui"
    private static final byte[] S_4D085A9 = new byte[]{22, 42, 39, 47, 39};  // XOR0x42 "Theme"
    private static final byte[] S_21806F = new byte[]{4, 45, 44, 54};  // XOR0x42 "Font"
    private static final byte[] S_78FB7791 = new byte[]{1, 55, 49, 54, 45, 47};  // XOR0x42 "Custom"
    private static final byte[] S_711D735B = new byte[]{20, 35, 44, 43, 46, 46, 35};  // XOR0x42 "Vanilla"
    private static final byte[] S_1FA487 = new byte[]{0, 46, 55, 48};  // XOR0x42 "Blur"
    private static final byte[] S_17B08FEB = new byte[]{13, 50, 35, 33, 43, 54, 59};  // XOR0x42 "Opacity"
    private static final byte[] S_1D740871 = new byte[]{0, 48, 43, 37, 42, 54, 44, 39, 49, 49};  // XOR0x42 "Brightness"
    private static final byte[] S_5A421BC = new byte[]{18, 43, 46, 46, 98, 1, 45, 46, 45, 48};  // XOR0x42 "Pill Color"
    private static final byte[] S_586A3B7 = new byte[]{18, 43, 46, 46, 98, 3, 46, 50, 42, 35};  // XOR0x42 "Pill Alpha"
    private static final byte[] S_43EA8EA3 = new byte[]{22, 45, 37, 37, 46, 39, 98, 13, 36, 36};  // XOR0x42 "Toggle Off"
    private static final byte[] S_2316A9E6 = new byte[]{14, 45, 37, 45, 98, 0, 48, 43, 37, 42, 54, 44, 39, 49, 49};  // XOR0x42 "Logo Brightness"
    private static final byte[] S_16E1963C = new byte[]{14, 45, 37, 45, 98, 0, 35, 33, 41};  // XOR0x42 "Logo Back"
    private static final byte[] S_3D8E045C = new byte[]{1, 35, 54, 39, 37, 45, 48, 59, 98, 0, 48, 43, 37, 42, 54};  // XOR0x42 "Category Bright"
    private static final byte[] S_C24E94E = new byte[]{1, 42, 43, 50, 98, 0, 48, 43, 37, 42, 54};  // XOR0x42 "Chip Bright"
    private static final byte[] S_5F42D523 = new byte[]{0, 35, 48, 98, 10, 45, 45, 41, 49};  // XOR0x42 "Bar Hooks"
    public final ModeSetting theme = new ModeSetting("Theme", ClickGUIRenderHelper.THEMES, ClickGUIRenderHelper.THEMES[0]);
    public final ModeSetting fontMode = new ModeSetting("Font", new String[]{"Custom", "Vanilla"}, "Custom");
    public final NumberSetting blur = new NumberSetting("Blur", 3.0f, 0.1f, 8.0f);
    public final NumberSetting opacity = new NumberSetting("Opacity", 1.0f, 0.0f, 1.0f);
    public final NumberSetting brightness = new NumberSetting("Brightness", 1.0f, 0.2f, 2.0f);
    public final ColorValue pillColor = new ColorValue("Pill Color", 0xF2FBFF);
    public final NumberSetting pillAlpha = new NumberSetting("Pill Alpha", 1.0f, 0.0f, 1.0f);
    public final ColorValue toggleOffColor = new ColorValue("Toggle Off", 10134445);
    public final NumberSetting logoBrightness = new NumberSetting("Logo Brightness", 1.0f, 0.2f, 2.5f);
    public final NumberSetting logoBackBrightness = new NumberSetting("Logo Back", 1.0f, 0.2f, 2.5f);
    public final NumberSetting categoryBrightness = new NumberSetting("Category Bright", 1.0f, 0.2f, 2.5f);
    public final NumberSetting pillBrightness = new NumberSetting("Chip Bright", 1.0f, 0.2f, 2.5f);
    public final BooleanSetting barHook = new BooleanSetting("Bar Hooks", true);

    public ClickguiModule() {
        super("ClickGui", Category.RENDER);
        this.key = 294;
        this.values.add(this.theme);
        this.values.add(this.blur);
        this.values.add(this.opacity);
        this.values.add(this.brightness);
        this.values.add(this.pillColor);
        this.values.add(this.pillAlpha);
        this.values.add(this.toggleOffColor);
        this.values.add(this.logoBrightness);
        this.values.add(this.logoBackBrightness);
        this.values.add(this.categoryBrightness);
        this.values.add(this.pillBrightness);
        this.values.add(this.barHook);
    }

    private void openScreen(final Minecraft mc) {
        Runnable task = new Runnable(){

            @Override
            public void run() {
                try {
                    if (mc.screen instanceof ClickGuiScreen) {
                        return;
                    }
                    mc.setScreen((Screen)new ClickGuiScreen());
                }
                catch (Throwable error) {
                    ClickGUIRenderHelper.isOpen = false;
                    ClickguiModule.this.enabled = false;
                }
            }
        };
        if (mc.isSameThread()) {
            task.run();
        } else {
            mc.execute(task);
        }
    }

    @Override
    public void onDisable() {
        final Minecraft mc = Minecraft.getInstance();
        ClickGUIRenderHelper.isOpen = false;
        if (mc == null) {
            return;
        }
        Runnable task = new Runnable(){

            @Override
            public void run() {
                try {
                    if (mc.screen instanceof ClickGuiScreen) {
                        mc.setScreen((Screen)null);
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        };
        if (mc.isSameThread()) {
            task.run();
        } else {
            mc.execute(task);
        }
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            this.enabled = false;
            return;
        }
        ClickGUIRenderHelper.isOpen = true;
        if (mc.screen instanceof ClickGuiScreen) {
            return;
        }
        this.openScreen(mc);
    }
}

