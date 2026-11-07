/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.hud.RenderableHud;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.utils.TargetHUDRenderHelper;
import dev.openhachimi.utils.MetricHelper;
import dev.openhachimi.utils.PathHelper;
import dev.openhachimi.utils.TargetHUD;

import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;

public class TargethudModule
extends Module {
    private static final byte[] S_46D51186 = new byte[]{22, 35, 48, 37, 39, 54, 10, 23, 6};  // XOR0x42 "TargetHUD"
    private static final byte[] S_24ADC3 = new byte[]{15, 45, 38, 39};  // XOR0x42 "Mode"
    private static final byte[] S_9B0573F = new byte[]{15, 45, 38, 39, 48, 44};  // XOR0x42 "Modern"
    private static final byte[] S_3F4C6E21 = new byte[]{6, 39, 36, 35, 55, 46, 54};  // XOR0x42 "Default"
    private static final byte[] S_139D1DD2 = new byte[]{17, 43, 47, 50, 46, 39};  // XOR0x42 "Simple"
    private static final byte[] S_2599E9 = new byte[]{13, 50, 35, 43};  // XOR0x42 "Opai"
    private static final byte[] S_66890281 = new byte[]{6, 43, 49, 50, 46, 35, 59, 98, 15, 45, 38, 39};  // XOR0x42 "Display Mode"
    private static final byte[] S_10125 = new byte[]{3, 43, 47};  // XOR0x42 "Aim"
    private static final byte[] S_7583DA68 = new byte[]{3, 54, 54, 35, 33, 41};  // XOR0x42 "Attack"
    private static final byte[] S_252358 = new byte[]{12, 45, 44, 39};  // XOR0x42 "None"
    private static final byte[] S_4BA94A5C = new byte[]{17, 42, 45, 53, 98, 3, 48, 47, 45, 48};  // XOR0x42 "Show Armor"
    private static final byte[] S_260CA4 = new byte[]{18, 45, 49, 26};  // XOR0x42 "PosX"
    private static final byte[] S_260CA5 = new byte[]{18, 45, 49, 27};  // XOR0x42 "PosY"
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Modern", "Default", "Simple", "Opai"}, "Simple");
    public final ModeSetting displayMode = new ModeSetting("Display Mode", new String[]{"Aim", "Attack", "None"}, "Aim");
    public final BooleanSetting showArmor = new BooleanSetting("Show Armor", true);
    public final ListSetting posX = new ListSetting("PosX", 100, 0, 2000);
    public final ListSetting posY = new ListSetting("PosY", 100, 0, 1000);
    private final RenderableHud modernHUD = new MetricHelper();
    private final RenderableHud defaultHUD = new TargetHUD();
    private final RenderableHud simpleHUD = new TargetHUDRenderHelper();
    private final RenderableHud opaiHUD = new PathHelper();

    public TargethudModule() {
        super("TargetHUD", Category.RENDER);
        this.posX.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return false;
            }
        });
        this.posY.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return false;
            }
        });
        this.showArmor.setVisible(new Supplier<Boolean>(){
            private static final byte[] S_2599E9 = new byte[]{13, 50, 35, 43};  // XOR0x42 "Opai"

            @Override
            public Boolean get() {
                return TargethudModule.this.mode.isMode("Opai");
            }
        });
        this.values.add(this.mode);
        this.values.add(this.displayMode);
        this.values.add(this.showArmor);
        this.values.add(this.posX);
        this.values.add(this.posY);
    }

    @Override
    public void onRender2D(GuiGraphics g, float partialTick) {
        if (this.mode.isMode("Modern")) {
            this.modernHUD.render(g, partialTick, this);
        } else if (this.mode.isMode("Default")) {
            this.defaultHUD.render(g, partialTick, this);
        } else if (this.mode.isMode("Opai")) {
            this.opaiHUD.render(g, partialTick, this);
        } else {
            this.simpleHUD.render(g, partialTick, this);
        }
    }
}

