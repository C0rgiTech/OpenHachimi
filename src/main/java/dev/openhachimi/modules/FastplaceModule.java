/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;

public class FastplaceModule
extends Module {
    private static final byte[] S_750109EB = new byte[]{4, 35, 49, 54, 18, 46, 35, 33, 39};  // XOR0x42 "FastPlace"
    private static final byte[] S_10586 = new byte[]{1, 18, 17};  // XOR0x42 "CPS"
    private static final byte[] S_6E8B63E = new byte[]{13, 36, 36, 42, 35, 44, 38};  // XOR0x42 "Offhand"
    private static final byte[] S_74FC021 = new byte[]{13, 44, 46, 59, 0, 46, 45, 33, 41};  // XOR0x42 "OnlyBlock"
    private static final byte[] S_69353CBE = new byte[]{36, 29, 123, 115, 114, 116, 122, 29};  // XOR0x42 "f_91068_"
    private static final byte[] S_7D30E417 = new byte[]{48, 43, 37, 42, 54, 1, 46, 43, 33, 41, 6, 39, 46, 35, 59};  // XOR0x42 "rightClickDelay"
    private static final byte[] S_692AB570 = new byte[]{36, 29, 123, 114, 122, 115, 122, 29};  // XOR0x42 "f_90818_"
    private static final byte[] S_5024D067 = new byte[]{33, 46, 43, 33, 41, 1, 45, 55, 44, 54};  // XOR0x42 "clickCount"
    public ListSetting cps = new ListSetting("CPS", 20, 1, 40);
    private final BooleanSetting offhand = new BooleanSetting("Offhand", false);
    private final BooleanSetting onlyBlock = new BooleanSetting("OnlyBlock", true);
    private float rightCounter = 0.0f;
    private static Field rightClickDelayField = null;
    private static Field clickCountField = null;

    public FastplaceModule() {
        super("FastPlace", Category.PLAYER);
        this.onlyBlock.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)FastplaceModule.this.offhand.get();
            }
        });
        this.values.add(this.cps);
        this.values.add(this.offhand);
        this.values.add(this.onlyBlock);
    }

    static {
        try {
            rightClickDelayField = Minecraft.class.getDeclaredField("f_91068_");
            rightClickDelayField.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            try {
                rightClickDelayField = Minecraft.class.getDeclaredField("rightClickDelay");
                rightClickDelayField.setAccessible(true);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            clickCountField = KeyMapping.class.getDeclaredField("f_90818_");
            clickCountField.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            try {
                clickCountField = KeyMapping.class.getDeclaredField("clickCount");
                clickCountField.setAccessible(true);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private boolean isBlockStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof BlockItem;
    }

    private boolean shouldFastPlace(Minecraft mc) {
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        if (((Boolean)this.offhand.get()).booleanValue()) {
            if (((Boolean)this.onlyBlock.get()).booleanValue()) {
                return this.isBlockStack(main) || this.isBlockStack(off);
            }
            return !main.isEmpty() || !off.isEmpty();
        }
        return this.isBlockStack(main);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.screen != null) {
            return;
        }
        if (mc.options.keyUse.isDown()) {
            if (!this.shouldFastPlace(mc)) {
                this.rightCounter = 0.0f;
                return;
            }
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
                if (rightClickDelayField != null) {
                    try {
                        rightClickDelayField.setInt(mc, 0);
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                this.rightCounter += (float)((Integer)this.cps.get()).intValue() / 20.0f;
                if (this.rightCounter >= 1.0f) {
                    KeyMapping.click((InputConstants.Key)mc.options.keyUse.getKey());
                    this.rightCounter -= 1.0f;
                } else if (clickCountField != null) {
                    try {
                        clickCountField.setInt(mc.options.keyUse, 0);
                    }
                    catch (Exception exception) {}
                }
            } else {
                this.rightCounter = 0.0f;
            }
        } else {
            this.rightCounter = 0.0f;
        }
    }
}

