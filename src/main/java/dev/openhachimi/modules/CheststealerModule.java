/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.utils.player.InventoryUtil;
import dev.openhachimi.utils.MSTimer;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;

public class CheststealerModule
extends Module {
    private static final byte[] S_7B181B = new byte[]{1, 42, 39, 49, 54, 17, 54, 39, 35, 46, 39, 48};  // XOR0x42 "ChestStealer"
    private static final byte[] S_29671DF5 = new byte[]{15, 43, 44, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Min Delay"
    private static final byte[] S_3EDC963 = new byte[]{6, 39, 46, 35, 59};  // XOR0x42 "Delay"
    private static final byte[] S_1408091B = new byte[]{1, 46, 45, 49, 39, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Close Delay"
    private static final byte[] S_5AD0C20E = new byte[]{1, 46, 45, 49, 39, 98, 6, 39, 46, 35, 59, 98, 12, 55, 47, 32, 39, 48};  // XOR0x42 "Close Delay Number"
    private static final byte[] S_48C74769 = new byte[]{7, 44, 38, 39, 48, 98, 1, 42, 39, 49, 54};  // XOR0x42 "Ender Chest"
    private static final byte[] S_31E7802D = new byte[]{13, 50, 39, 44, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Open Delay"
    private static final byte[] S_587EFB54 = new byte[]{33, 45, 44, 54, 35, 43, 44, 39, 48, 108, 33, 42, 39, 49, 54};  // XOR0x42 "container.chest"
    private static final byte[] S_13FE9445 = new byte[]{33, 45, 44, 54, 35, 43, 44, 39, 48, 108, 33, 42, 39, 49, 54, 6, 45, 55, 32, 46, 39};  // XOR0x42 "container.chestDouble"
    private static final byte[] S_3DEC40C6 = new byte[]{33, 45, 44, 54, 35, 43, 44, 39, 48, 108, 39, 44, 38, 39, 48, 33, 42, 39, 49, 54};  // XOR0x42 "container.enderchest"
    private static final byte[] S_3E0F6E1 = new byte[]{1, 42, 39, 49, 54};  // XOR0x42 "Chest"
    private static final MSTimer timer = new MSTimer();
    private static final Random random = new Random();
    private final ListSetting minDelay = new ListSetting("Min Delay", 125, 0, 1000);
    private final ListSetting delay = new ListSetting("Delay", 150, 0, 1000);
    private final BooleanSetting closeDelay = new BooleanSetting("Close Delay", true);
    private final ListSetting cDelay = new ListSetting("Close Delay Number", 150, 0, 1000);
    private final BooleanSetting pickEnderChest = new BooleanSetting("Ender Chest", false);
    private final ListSetting openDelay = new ListSetting("Open Delay", 200, 0, 2000);
    private long chestOpenedAt = 0L;
    private Screen lastTickScreen;

    public CheststealerModule() {
        super("ChestStealer", Category.PLAYER);
        this.values.add(this.minDelay);
        this.values.add(this.delay);
        this.values.add(this.closeDelay);
        this.values.add(this.cDelay);
        this.values.add(this.pickEnderChest);
    }

    public static boolean isWorking() {
        return !timer.delay(3L);
    }

    public static boolean isItemUseful(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (InventoryUtil.isGodItem(stack) || InventoryUtil.isSharpnessAxe(stack)) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof ArmorItem) {
            float bestArmor;
            ArmorItem item2 = (ArmorItem)item;
            float protection = InventoryUtil.getProtection(stack);
            return !(protection <= (bestArmor = InventoryUtil.getBestArmorScore(item2.getEquipmentSlot())));
        }
        if (stack.getItem() instanceof SwordItem) {
            float bestDamage;
            float damage = InventoryUtil.getSwordDamage(stack);
            return !(damage <= (bestDamage = InventoryUtil.getBestSwordDamage()));
        }
        if (stack.getItem() instanceof PickaxeItem) {
            float bestScore;
            float score = InventoryUtil.getToolScore(stack);
            return !(score <= (bestScore = InventoryUtil.getBestPickaxeScore()));
        }
        if (stack.getItem() instanceof AxeItem) {
            float bestScore;
            float score = InventoryUtil.getToolScore(stack);
            return !(score <= (bestScore = InventoryUtil.getBestAxeScore()));
        }
        if (stack.getItem() instanceof ShovelItem) {
            float bestScore;
            float score = InventoryUtil.getToolScore(stack);
            return !(score <= (bestScore = InventoryUtil.getBestShovelScore()));
        }
        if (stack.getItem() instanceof CrossbowItem) {
            float bestScore;
            float score = InventoryUtil.getCrossbowScore(stack);
            return !(score <= (bestScore = InventoryUtil.getBestCrossbowScore()));
        }
        if (stack.getItem() instanceof BowItem && InventoryUtil.isPunchBow(stack)) {
            float bestScore;
            float score = InventoryUtil.getPunchBowScore(stack);
            return !(score <= (bestScore = InventoryUtil.getBestPunchBowScore()));
        }
        if (stack.getItem() instanceof BowItem && InventoryUtil.isPowerBow(stack)) {
            float bestScore;
            float score = InventoryUtil.getPowerBowScore(stack);
            return !(score <= (bestScore = InventoryUtil.getBestPowerBowScore()));
        }
        if (stack.getItem() == Items.COMPASS) {
            return !InventoryUtil.hasItem(stack.getItem());
        }
        if (stack.getItem() == Items.WATER_BUCKET && InventoryUtil.getItemCount(Items.WATER_BUCKET) >= InvmanagerModule.getWaterBucketCount()) {
            return false;
        }
        if (stack.getItem() == Items.LAVA_BUCKET && InventoryUtil.getItemCount(Items.LAVA_BUCKET) >= InvmanagerModule.getLavaBucketCount()) {
            return false;
        }
        if (stack.getItem() instanceof BlockItem && InventoryUtil.isValidStack(stack) && InventoryUtil.getBlockCountInInventory() + stack.getCount() >= InvmanagerModule.getMaxBlockSize()) {
            return false;
        }
        if (stack.getItem() == Items.ARROW && InventoryUtil.getItemCount(Items.ARROW) + stack.getCount() >= InvmanagerModule.getMaxArrowSize()) {
            return false;
        }
        if (stack.getItem() instanceof FishingRodItem && InventoryUtil.getItemCount(Items.FISHING_ROD) >= 1) {
            return false;
        }
        if (stack.getItem() != Items.SNOWBALL && stack.getItem() != Items.EGG || InventoryUtil.getItemCount(Items.SNOWBALL) + InventoryUtil.getItemCount(Items.EGG) + stack.getCount() < InvmanagerModule.getMaxProjectileSize() && InvmanagerModule.shouldKeepProjectile()) {
            return !(stack.getItem() instanceof ItemNameBlockItem) && InventoryUtil.isCommonItemUseful(stack);
        }
        return false;
    }

    private static boolean isBestItemInChest(ChestMenu menu, ItemStack stack) {
        if (!InventoryUtil.isGodItem(stack) && !InventoryUtil.isSharpnessAxe(stack)) {
            for (int i = 0; i < menu.getRowCount() * 9; ++i) {
                ItemStack checkStack = menu.getSlot(i).getItem();
                Item item = stack.getItem();
                if (item instanceof ArmorItem) {
                    ArmorItem item2 = (ArmorItem)item;
                    item = checkStack.getItem();
                    if (item instanceof ArmorItem) {
                        ArmorItem checkItem = (ArmorItem)item;
                        if (item2.getEquipmentSlot() != checkItem.getEquipmentSlot() || !(InventoryUtil.getProtection(checkStack) > InventoryUtil.getProtection(stack))) continue;
                        return false;
                    }
                }
                if (stack.getItem() instanceof SwordItem && checkStack.getItem() instanceof SwordItem) {
                    if (!(InventoryUtil.getSwordDamage(checkStack) > InventoryUtil.getSwordDamage(stack))) continue;
                } else if (stack.getItem() instanceof PickaxeItem && checkStack.getItem() instanceof PickaxeItem) {
                    if (!(InventoryUtil.getToolScore(checkStack) > InventoryUtil.getToolScore(stack))) continue;
                } else if (stack.getItem() instanceof AxeItem && checkStack.getItem() instanceof AxeItem) {
                    if (!(InventoryUtil.getToolScore(checkStack) > InventoryUtil.getToolScore(stack))) continue;
                } else if (!(stack.getItem() instanceof ShovelItem) || !(checkStack.getItem() instanceof ShovelItem) || !(InventoryUtil.getToolScore(checkStack) > InventoryUtil.getToolScore(stack))) continue;
                return false;
            }
            return true;
        }
        return true;
    }

    private boolean isChestEmpty(ChestMenu menu) {
        for (int i = 0; i < menu.getRowCount() * 9; ++i) {
            ItemStack item = menu.getSlot(i).getItem();
            if (item.isEmpty() || !CheststealerModule.isItemUseful(item) || !CheststealerModule.isBestItemInChest(menu, item)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void onTick() {
        Screen currentScreen;
        block3: {
            int nextDelay;
            ChestMenu menu;
            Minecraft mc;
            block5: {
                ContainerScreen container;
                block4: {
                    mc = Minecraft.getInstance();
                    currentScreen = mc.screen;
                    if (!(currentScreen instanceof ContainerScreen)) break block3;
                    container = (ContainerScreen)currentScreen;
                    if (!(container.getMenu() instanceof ChestMenu)) {
                        return;
                    }
                    menu = (ChestMenu)container.getMenu();
                    if (currentScreen == this.lastTickScreen) break block4;
                    timer.reset();
                    this.chestOpenedAt = System.currentTimeMillis();
                    break block3;
                }
                String chestTitle = container.getTitle().getString();
                String chest = Component.translatable((String)"container.chest").getString();
                String largeChest = Component.translatable((String)"container.chestDouble").getString();
                String enderChest = Component.translatable((String)"container.enderchest").getString();
                if (!chestTitle.equals(chest) && !chestTitle.equals(largeChest) && !chestTitle.equals("Chest") && (!((Boolean)this.pickEnderChest.get()).booleanValue() || !chestTitle.equals(enderChest))) break block3;
                nextDelay = Math.max((Integer)this.minDelay.get(), (int)((double)((Integer)this.delay.get()).intValue() + random.nextGaussian() * 50.0));
                if (!this.isChestEmpty(menu) || !timer.delay(nextDelay)) break block5;
                if (mc.player == null || !((Boolean)this.closeDelay.get()).booleanValue() || !timer.delay(((Integer)this.cDelay.get()).intValue())) break block3;
                mc.player.closeContainer();
                timer.reset();
                break block3;
            }
            if (System.currentTimeMillis() - this.chestOpenedAt >= (long)((Integer)this.openDelay.get()).intValue()) {
                List<Integer> slots = IntStream.range(0, menu.getRowCount() * 9).boxed().collect(Collectors.toList());
                Collections.shuffle(slots);
                for (Integer pSlotId : slots) {
                    ItemStack stack = menu.getSlot(pSlotId.intValue()).getItem();
                    if (!CheststealerModule.isItemUseful(stack) || !CheststealerModule.isBestItemInChest(menu, stack) || !timer.delay(nextDelay)) continue;
                    mc.gameMode.handleInventoryMouseClick(menu.containerId, pSlotId.intValue(), 0, ClickType.QUICK_MOVE, (Player)mc.player);
                    timer.reset();
                    break;
                }
            }
        }
        this.lastTickScreen = currentScreen;
    }
}

