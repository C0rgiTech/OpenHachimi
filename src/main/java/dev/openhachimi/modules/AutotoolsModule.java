/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AutotoolsModule
extends Module {
    private static final byte[] S_59DA514C = new byte[]{3, 55, 54, 45, 22, 45, 45, 46, 49};  // XOR0x42 "AutoTools"
    private static final byte[] S_5EC6BA53 = new byte[]{17, 53, 43, 54, 33, 42, 98, 0, 35, 33, 41};  // XOR0x42 "Switch Back"
    private static final byte[] S_79E04E45 = new byte[]{18, 48, 39, 49, 49, 98, 49, 42, 43, 36, 54};  // XOR0x42 "Press shift"
    public static AutotoolsModule INSTANCE;
    private final BooleanSetting switchBack = new BooleanSetting("Switch Back", true);
    private final BooleanSetting sneakTrigger = new BooleanSetting("Press shift", true);
    private int originSlot = -1;

    public AutotoolsModule() {
        super("AutoTools", Category.PLAYER);
        INSTANCE = this;
        this.values.add(this.switchBack);
        this.values.add(this.sneakTrigger);
    }

    private int getBestTool(Minecraft mc, BlockPos pos) {
        BlockState blockState = mc.level.getBlockState(pos);
        Block block = blockState.getBlock();
        int slot = 0;
        float damage = 1.0f;
        for (int index = 0; index < 9; ++index) {
            int efficiency;
            ItemStack itemStack = mc.player.getInventory().getItem(index);
            if (this.isGodItem(itemStack) || itemStack.isEmpty() || blockState.isAir() || itemStack.getItem() instanceof SwordItem && !(block instanceof WebBlock)) continue;
            float destroySpeed = itemStack.getDestroySpeed(blockState);
            if (destroySpeed > 1.0f && !(block instanceof DropExperienceBlock) && !(block instanceof RedStoneOreBlock) && (efficiency = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.BLOCK_EFFICIENCY, (ItemStack)itemStack)) > 0) {
                destroySpeed += (float)(efficiency * efficiency + 1);
            }
            if (!(destroySpeed > damage)) continue;
            slot = index;
            damage = destroySpeed;
        }
        return damage > 1.0f ? slot : -1;
    }

    private boolean isGodItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        if (itemStack.getItem() instanceof AxeItem && itemStack.getItem() == Items.GOLDEN_AXE && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.SHARPNESS, (ItemStack)itemStack) > 100) {
            return true;
        }
        if (itemStack.getItem() == Items.SLIME_BALL && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.KNOCKBACK, (ItemStack)itemStack) > 1) {
            return true;
        }
        return itemStack.getItem() == Items.TOTEM_OF_UNDYING || itemStack.getItem() == Items.END_CRYSTAL;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (((Boolean)this.switchBack.get()).booleanValue()) {
            this.restoreOriginalSlot(mc);
        } else {
            this.originSlot = -1;
        }
    }

    public static boolean shouldBlockAimAssist() {
        AutotoolsModule config = INSTANCE;
        Minecraft mc = Minecraft.getInstance();
        return config != null && config.enabled && (Boolean)config.sneakTrigger.get() != false && mc.player != null && mc.player.isShiftKeyDown();
    }

    private void restoreOriginalSlot(Minecraft mc) {
        if (this.originSlot == -1) {
            return;
        }
        if (mc.player != null) {
            mc.player.getInventory().selected = this.originSlot;
        }
        this.originSlot = -1;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        boolean canTrigger = (Boolean)this.sneakTrigger.get() == false || mc.player.isShiftKeyDown();
        boolean bl = canTrigger;
        if (canTrigger && mc.gameMode.isDestroying()) {
            BlockHitResult hitResult;
            int bestTool;
            if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK && (bestTool = this.getBestTool(mc, (hitResult = (BlockHitResult)mc.hitResult).getBlockPos())) != -1 && bestTool != mc.player.getInventory().selected) {
                this.originSlot = mc.player.getInventory().selected;
                mc.player.getInventory().selected = bestTool;
            }
            return;
        }
        if (((Boolean)this.switchBack.get()).booleanValue()) {
            this.restoreOriginalSlot(mc);
        }
    }
}

