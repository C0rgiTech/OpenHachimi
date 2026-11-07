/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class AutoclickerModule
extends Module {
    private static final byte[] S_43C9FF66 = new byte[]{3, 55, 54, 45, 1, 46, 43, 33, 41, 39, 48};  // XOR0x42 "AutoClicker"
    private static final byte[] S_10586 = new byte[]{1, 18, 17};  // XOR0x42 "CPS"
    private static final byte[] S_61666534 = new byte[]{13, 44, 46, 59, 3, 54, 54, 35, 33, 41};  // XOR0x42 "OnlyAttack"
    private static final byte[] S_6935407F = new byte[]{36, 29, 123, 115, 114, 117, 122, 29};  // XOR0x42 "f_91078_"
    private static final byte[] S_38080DE9 = new byte[]{47, 43, 49, 49, 22, 43, 47, 39};  // XOR0x42 "missTime"
    private static final Minecraft mc = Minecraft.getInstance();
    private static Field missTimeField;
    private static long lastClickTime;
    private final ListSetting cps = new ListSetting("CPS", 20, 1, 30);
    private final BooleanSetting onlyAttack = new BooleanSetting("OnlyAttack", true);

    public AutoclickerModule() {
        super("AutoClicker", Category.COMBAT);
        this.values.add(this.cps);
        this.values.add(this.onlyAttack);
    }

    static {
        try {
            missTimeField = Minecraft.class.getDeclaredField("f_91078_");
            missTimeField.setAccessible(true);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            try {
                missTimeField = Minecraft.class.getDeclaredField("missTime");
                missTimeField.setAccessible(true);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private boolean attackKeyDown() {
        if (AutoclickerModule.mc.options.keyAttack.isDown()) {
            return true;
        }
        InputConstants.Key bound = AutoclickerModule.mc.options.keyAttack.getKey();
        long window = mc.getWindow().getWindow();
        if (bound.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton((long)window, (int)bound.getValue()) == 1;
        }
        return InputConstants.isKeyDown((long)window, (int)bound.getValue());
    }

    private void clearMissTime() {
        if (missTimeField == null) {
            return;
        }
        try {
            missTimeField.setInt(mc, 0);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Override
    public void onDisable() {
        lastClickTime = 0L;
    }

    @Override
    public void onEnable() {
        lastClickTime = 0L;
    }

    @Override
    public void onTick() {
        int clicksPerSecond;
        if (AutoclickerModule.mc.player == null || AutoclickerModule.mc.level == null) {
            return;
        }
        if (AutoclickerModule.mc.screen != null) {
            return;
        }
        if (!this.attackKeyDown()) {
            return;
        }
        if (((Boolean)this.onlyAttack.get()).booleanValue()) {
            if (AutoclickerModule.mc.gameMode == null || AutoclickerModule.mc.gameMode.isDestroying()) {
                return;
            }
            HitResult hitResult = AutoclickerModule.mc.hitResult;
            if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
                return;
            }
        }
        if ((clicksPerSecond = ((Integer)this.cps.get()).intValue()) <= 0) {
            return;
        }
        long interval = 1000L / (long)clicksPerSecond;
        long now = System.currentTimeMillis();
        if (now - lastClickTime < interval) {
            return;
        }
        this.clearMissTime();
        KeyMapping.click((InputConstants.Key)AutoclickerModule.mc.options.keyAttack.getKey());
        lastClickTime = now;
    }
}

