/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.utils.player.InventoryUtil;
import dev.openhachimi.utils.player.MoveUtil;
import dev.openhachimi.utils.MSTimer;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
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

public class InvmanagerModule
extends Module {
    private static final byte[] S_55254A9C = new byte[]{11, 44, 52, 15, 35, 44, 35, 37, 39, 48};  // XOR0x42 "InvManager"
    private static final byte[] S_29671DF5 = new byte[]{15, 43, 44, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Min Delay"
    private static final byte[] S_3EDC963 = new byte[]{6, 39, 46, 35, 59};  // XOR0x42 "Delay"
    private static final byte[] S_71A09B3E = new byte[]{13, 36, 36, 42, 35, 44, 38, 98, 11, 54, 39, 47, 49};  // XOR0x42 "Offhand Items"
    private static final byte[] S_252358 = new byte[]{12, 45, 44, 39};  // XOR0x42 "None"
    private static final byte[] S_1C4D4583 = new byte[]{5, 45, 46, 38, 39, 44, 98, 3, 50, 50, 46, 39};  // XOR0x42 "Golden Apple"
    private static final byte[] S_166A609 = new byte[]{18, 48, 45, 40, 39, 33, 54, 43, 46, 39};  // XOR0x42 "Projectile"
    private static final byte[] S_2ABF02F1 = new byte[]{4, 43, 49, 42, 43, 44, 37, 98, 16, 45, 38};  // XOR0x42 "Fishing Rod"
    private static final byte[] S_3D4D46D = new byte[]{0, 46, 45, 33, 41};  // XOR0x42 "Block"
    private static final byte[] S_62CFF8E = new byte[]{3, 55, 54, 45, 98, 3, 48, 47, 45, 48};  // XOR0x42 "Auto Armor"
    private static final byte[] S_3EC4B030 = new byte[]{11, 44, 52, 39, 44, 54, 45, 48, 59, 98, 13, 44, 46, 59};  // XOR0x42 "Inventory Only"
    private static final byte[] S_7B0A4E11 = new byte[]{17, 53, 43, 54, 33, 42, 98, 17, 53, 45, 48, 38};  // XOR0x42 "Switch Sword"
    private static final byte[] S_65570A81 = new byte[]{17, 53, 45, 48, 38, 98, 17, 46, 45, 54};  // XOR0x42 "Sword Slot"
    private static final byte[] S_7A15BCA1 = new byte[]{17, 53, 43, 54, 33, 42, 98, 0, 46, 45, 33, 41};  // XOR0x42 "Switch Block"
    private static final byte[] S_3FFA35F1 = new byte[]{0, 46, 45, 33, 41, 98, 17, 46, 45, 54};  // XOR0x42 "Block Slot"
    private static final byte[] S_47312450 = new byte[]{15, 35, 58, 98, 0, 46, 45, 33, 41, 98, 17, 43, 56, 39};  // XOR0x42 "Max Block Size"
    private static final byte[] S_2A6C3E61 = new byte[]{17, 53, 43, 54, 33, 42, 98, 18, 43, 33, 41, 35, 58, 39};  // XOR0x42 "Switch Pickaxe"
    private static final byte[] S_40D3AF31 = new byte[]{18, 43, 33, 41, 35, 58, 39, 98, 17, 46, 45, 54};  // XOR0x42 "Pickaxe Slot"
    private static final byte[] S_62066822 = new byte[]{17, 53, 43, 54, 33, 42, 98, 3, 58, 39};  // XOR0x42 "Switch Axe"
    private static final byte[] S_5A5BF010 = new byte[]{3, 58, 39, 98, 17, 46, 45, 54};  // XOR0x42 "Axe Slot"
    private static final byte[] S_7860DD05 = new byte[]{17, 53, 43, 54, 33, 42, 98, 0, 45, 53, 98, 45, 48, 98, 1, 48, 45, 49, 49, 32, 45, 53};  // XOR0x42 "Switch Bow or Crossbow"
    private static final byte[] S_4DC42D4 = new byte[]{0, 45, 53, 98, 17, 46, 45, 54};  // XOR0x42 "Bow Slot"
    private static final byte[] S_600A777A = new byte[]{0, 45, 53, 98, 18, 48, 43, 45, 48, 43, 54, 59};  // XOR0x42 "Bow Priority"
    private static final byte[] S_26961AA = new byte[]{1, 48, 45, 49, 49, 32, 45, 53};  // XOR0x42 "Crossbow"
    private static final byte[] S_29BA2FEF = new byte[]{18, 45, 53, 39, 48, 98, 0, 45, 53};  // XOR0x42 "Power Bow"
    private static final byte[] S_38F28B98 = new byte[]{18, 55, 44, 33, 42, 98, 0, 45, 53};  // XOR0x42 "Punch Bow"
    private static final byte[] S_136AC254 = new byte[]{15, 35, 58, 98, 3, 48, 48, 45, 53, 98, 17, 43, 56, 39};  // XOR0x42 "Max Arrow Size"
    private static final byte[] S_3AC1CA9F = new byte[]{17, 53, 43, 54, 33, 42, 98, 21, 35, 54, 39, 48, 98, 0, 55, 33, 41, 39, 54};  // XOR0x42 "Switch Water Bucket"
    private static final byte[] S_534714CB = new byte[]{21, 35, 54, 39, 48, 98, 0, 55, 33, 41, 39, 54, 98, 17, 46, 45, 54};  // XOR0x42 "Water Bucket Slot"
    private static final byte[] S_7433FDE2 = new byte[]{17, 53, 43, 54, 33, 42, 98, 7, 44, 38, 39, 48, 98, 18, 39, 35, 48, 46};  // XOR0x42 "Switch Ender Pearl"
    private static final byte[] S_419FFE50 = new byte[]{7, 44, 38, 39, 48, 98, 18, 39, 35, 48, 46, 98, 17, 46, 45, 54};  // XOR0x42 "Ender Pearl Slot"
    private static final byte[] S_2D9410A1 = new byte[]{17, 53, 43, 54, 33, 42, 98, 4, 43, 48, 39, 32, 35, 46, 46};  // XOR0x42 "Switch Fireball"
    private static final byte[] S_385D0609 = new byte[]{4, 43, 48, 39, 32, 35, 46, 46, 98, 17, 46, 45, 54};  // XOR0x42 "Fireball Slot"
    private static final byte[] S_48747ACF = new byte[]{17, 53, 43, 54, 33, 42, 98, 5, 45, 46, 38, 39, 44, 98, 3, 50, 50, 46, 39};  // XOR0x42 "Switch Golden Apple"
    private static final byte[] S_2BAB029B = new byte[]{5, 45, 46, 38, 39, 44, 98, 3, 50, 50, 46, 39, 98, 17, 46, 45, 54};  // XOR0x42 "Golden Apple Slot"
    private static final byte[] S_7BD815C6 = new byte[]{22, 42, 48, 45, 53, 98, 11, 54, 39, 47, 49};  // XOR0x42 "Throw Items"
    private static final byte[] S_38BE2445 = new byte[]{9, 39, 39, 50, 98, 21, 35, 54, 39, 48, 98, 0, 55, 33, 41, 39, 54, 49};  // XOR0x42 "Keep Water Buckets"
    private static final byte[] S_1D44B324 = new byte[]{9, 39, 39, 50, 98, 14, 35, 52, 35, 98, 0, 55, 33, 41, 39, 54, 49};  // XOR0x42 "Keep Lava Buckets"
    private static final byte[] S_545F1240 = new byte[]{9, 39, 39, 50, 98, 7, 37, 37, 49, 98, 100, 98, 17, 44, 45, 53, 32, 35, 46, 46, 49};  // XOR0x42 "Keep Eggs & Snowballs"
    private static final byte[] S_37220271 = new byte[]{17, 53, 43, 54, 33, 42, 98, 7, 37, 37, 49, 98, 100, 98, 17, 44, 45, 53, 32, 35, 46, 46, 49};  // XOR0x42 "Switch Eggs & Snowballs"
    private static final byte[] S_509B2A39 = new byte[]{7, 37, 37, 49, 98, 100, 98, 17, 44, 45, 53, 32, 35, 46, 46, 49, 98, 17, 46, 45, 54};  // XOR0x42 "Eggs & Snowballs Slot"
    private static final byte[] S_27B09660 = new byte[]{15, 35, 58, 98, 7, 37, 37, 49, 98, 100, 98, 17, 44, 45, 53, 32, 35, 46, 46, 49, 98, 17, 43, 56, 39};  // XOR0x42 "Max Eggs & Snowballs Size"
    private static final byte[] S_6206A6DB = new byte[]{17, 53, 43, 54, 33, 42, 98, 16, 45, 38};  // XOR0x42 "Switch Rod"
    private static final byte[] S_6282FCF7 = new byte[]{16, 45, 38, 98, 17, 46, 45, 54};  // XOR0x42 "Rod Slot"
    private static final byte[] S_347B7D6B = new byte[]{(byte)165, (byte)192, (byte)251, (byte)167, (byte)197, (byte)249, (byte)166, (byte)255, (byte)253, (byte)165, (byte)214, (byte)234};
    private static final MSTimer timer = new MSTimer();
    private static final Random random = new Random();
    private final ListSetting minDelay = new ListSetting("Min Delay", 125, 0, 1000);
    private final ListSetting delay = new ListSetting("Delay", 150, 0, 1000);
    private final ModeSetting offhandItems = new ModeSetting("Offhand Items", new String[]{"None", "Golden Apple", "Projectile", "Fishing Rod", "Block"}, "None");
    private final BooleanSetting autoArmor = new BooleanSetting("Auto Armor", true);
    private final BooleanSetting inventoryOnly = new BooleanSetting("Inventory Only", true);
    private final BooleanSetting switchSword = new BooleanSetting("Switch Sword", true);
    private final ListSetting swordSlot = new ListSetting("Sword Slot", 1, 1, 9);
    private final BooleanSetting switchBlock = new BooleanSetting("Switch Block", true);
    private final ListSetting blockSlot = new ListSetting("Block Slot", 2, 1, 9);
    private final ListSetting maxBlockSize = new ListSetting("Max Block Size", 256, 64, 512);
    private final BooleanSetting switchPickaxe = new BooleanSetting("Switch Pickaxe", true);
    private final ListSetting pickaxeSlot = new ListSetting("Pickaxe Slot", 3, 1, 9);
    private final BooleanSetting switchAxe = new BooleanSetting("Switch Axe", true);
    private final ListSetting axeSlot = new ListSetting("Axe Slot", 4, 1, 9);
    private final BooleanSetting switchBow = new BooleanSetting("Switch Bow or Crossbow", true);
    private final ListSetting bowSlot = new ListSetting("Bow Slot", 5, 1, 9);
    private final ModeSetting preferBow = new ModeSetting("Bow Priority", new String[]{"Crossbow", "Power Bow", "Punch Bow"}, "Crossbow");
    private final ListSetting maxArrowSize = new ListSetting("Max Arrow Size", 256, 64, 512);
    private final BooleanSetting switchWaterBucket = new BooleanSetting("Switch Water Bucket", true);
    private final ListSetting waterBucketSlot = new ListSetting("Water Bucket Slot", 6, 1, 9);
    private final BooleanSetting switchEnderPearl = new BooleanSetting("Switch Ender Pearl", true);
    private final ListSetting enderPearlSlot = new ListSetting("Ender Pearl Slot", 7, 1, 9);
    private final BooleanSetting switchFireball = new BooleanSetting("Switch Fireball", true);
    private final ListSetting fireballSlot = new ListSetting("Fireball Slot", 8, 1, 9);
    private final BooleanSetting switchGoldenApple = new BooleanSetting("Switch Golden Apple", true);
    private final ListSetting goldenAppleSlot = new ListSetting("Golden Apple Slot", 9, 1, 9);
    private final BooleanSetting throwItems = new BooleanSetting("Throw Items", true);
    private final ListSetting waterBucketCount = new ListSetting("Keep Water Buckets", 1, 0, 5);
    private final ListSetting lavaBucketCount = new ListSetting("Keep Lava Buckets", 1, 0, 5);
    private final BooleanSetting keepProjectile = new BooleanSetting("Keep Eggs & Snowballs", true);
    private final BooleanSetting switchProjectile = new BooleanSetting("Switch Eggs & Snowballs", false);
    private final ListSetting projectileSlot = new ListSetting("Eggs & Snowballs Slot", 9, 1, 9);
    private final ListSetting maxProjectileSize = new ListSetting("Max Eggs & Snowballs Size", 64, 16, 256);
    private final BooleanSetting switchRod = new BooleanSetting("Switch Rod", false);
    private final ListSetting rodSlot = new ListSetting("Rod Slot", 9, 1, 9);
    int noMoveTicks = 0;
    private boolean clickOffHand = false;
    private boolean inventoryOpen = false;

    public InvmanagerModule() {
        super("InvManager", Category.PLAYER);
        this.values.add(this.minDelay);
        this.values.add(this.delay);
        this.values.add(this.offhandItems);
        this.values.add(this.autoArmor);
        this.values.add(this.inventoryOnly);
        this.values.add(this.switchSword);
        this.values.add(this.swordSlot);
        this.values.add(this.switchBlock);
        this.values.add(this.blockSlot);
        this.values.add(this.maxBlockSize);
        this.values.add(this.switchPickaxe);
        this.values.add(this.pickaxeSlot);
        this.values.add(this.switchAxe);
        this.values.add(this.axeSlot);
        this.values.add(this.switchBow);
        this.values.add(this.bowSlot);
        this.values.add(this.preferBow);
        this.values.add(this.maxArrowSize);
        this.values.add(this.switchWaterBucket);
        this.values.add(this.waterBucketSlot);
        this.values.add(this.switchEnderPearl);
        this.values.add(this.enderPearlSlot);
        this.values.add(this.switchFireball);
        this.values.add(this.fireballSlot);
        this.values.add(this.switchGoldenApple);
        this.values.add(this.goldenAppleSlot);
        this.values.add(this.throwItems);
        this.values.add(this.waterBucketCount);
        this.values.add(this.lavaBucketCount);
        this.values.add(this.keepProjectile);
        this.values.add(this.switchProjectile);
        this.values.add(this.projectileSlot);
        this.values.add(this.maxProjectileSize);
        this.values.add(this.switchRod);
        this.values.add(this.rodSlot);
    }

    public boolean isItemUseful(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (InventoryUtil.isGodItem(stack)) {
            return true;
        }
        if (stack.getDisplayName().getString().contains("点击使用")) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof ArmorItem) {
            ArmorItem item2 = (ArmorItem)item;
            float protection = InventoryUtil.getProtection(stack);
            if (InventoryUtil.getCurrentArmorScore(item2.getEquipmentSlot()) >= protection) {
                return false;
            }
            float bestArmor = InventoryUtil.getBestArmorScore(item2.getEquipmentSlot());
            return !(protection < bestArmor);
        }
        if (stack.getItem() instanceof SwordItem) {
            return InventoryUtil.getBestSword() == stack;
        }
        if (stack.getItem() instanceof PickaxeItem) {
            return InventoryUtil.getBestPickaxe() == stack;
        }
        if (stack.getItem() instanceof AxeItem && !InventoryUtil.isSharpnessAxe(stack)) {
            return InventoryUtil.getBestAxe() == stack;
        }
        if (stack.getItem() instanceof ShovelItem) {
            return InventoryUtil.getBestShovel() == stack;
        }
        if (stack.getItem() instanceof CrossbowItem) {
            return InventoryUtil.getBestCrossbow() == stack;
        }
        if (stack.getItem() instanceof BowItem && InventoryUtil.isPunchBow(stack)) {
            return InventoryUtil.getBestPunchBow() == stack;
        }
        if (stack.getItem() instanceof BowItem && InventoryUtil.isPowerBow(stack)) {
            return InventoryUtil.getBestPowerBow() == stack;
        }
        if (stack.getItem() instanceof BowItem && InventoryUtil.getItemCount(Items.BOW) > 1) {
            return false;
        }
        if (stack.getItem() == Items.WATER_BUCKET && InventoryUtil.getItemCount(Items.WATER_BUCKET) > InvmanagerModule.getWaterBucketCount()) {
            return false;
        }
        if (stack.getItem() == Items.LAVA_BUCKET && InventoryUtil.getItemCount(Items.LAVA_BUCKET) > InvmanagerModule.getLavaBucketCount()) {
            return false;
        }
        if (stack.getItem() instanceof FishingRodItem && InventoryUtil.getItemCount(Items.FISHING_ROD) > 1) {
            return false;
        }
        if (!(stack.getItem() != Items.SNOWBALL && stack.getItem() != Items.EGG || InvmanagerModule.shouldKeepProjectile())) {
            return false;
        }
        if (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return true;
        }
        return !(stack.getItem() instanceof ItemNameBlockItem) && InventoryUtil.isCommonItemUseful(stack);
    }

    public static int getLavaBucketCount() {
        InvmanagerModule m = ModuleManager.getModule(InvmanagerModule.class);
        if (m != null) {
            return (Integer)m.lavaBucketCount.get();
        }
        return 1;
    }

    public static int getMaxArrowSize() {
        InvmanagerModule m = ModuleManager.getModule(InvmanagerModule.class);
        if (m != null) {
            return (Integer)m.maxArrowSize.get();
        }
        return 256;
    }

    public static int getMaxBlockSize() {
        InvmanagerModule m = ModuleManager.getModule(InvmanagerModule.class);
        if (m != null) {
            return (Integer)m.maxBlockSize.get();
        }
        return 256;
    }

    private void swapItem(int targetSlot, Item item) {
        int bestItemSlot;
        Minecraft mc = Minecraft.getInstance();
        ItemStack currentSlot = (ItemStack)mc.player.getInventory().items.get(targetSlot);
        int nextDelay = Math.max((Integer)this.minDelay.get(), (int)((double)((Integer)this.delay.get()).intValue() + random.nextGaussian() * 50.0));
        if (InventoryUtil.isItemValid(currentSlot) && timer.delay(nextDelay) && (bestItemSlot = InventoryUtil.getItemSlot(item)) != -1) {
            ItemStack bestItemStack = (ItemStack)mc.player.getInventory().items.get(bestItemSlot);
            if (currentSlot.getItem() != item || currentSlot.getItem() == item && currentSlot.getCount() < bestItemStack.getCount()) {
                if (bestItemSlot < 9) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestItemSlot + 36, targetSlot, ClickType.SWAP, (Player)mc.player);
                } else {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestItemSlot, targetSlot, ClickType.SWAP, (Player)mc.player);
                }
                this.inventoryOpen = true;
                timer.reset();
            }
        }
    }

    private void swapItem(int targetSlot, ItemStack bestItem) {
        int bestItemSlot;
        Minecraft mc = Minecraft.getInstance();
        ItemStack currentSlot = (ItemStack)mc.player.getInventory().items.get(targetSlot);
        int nextDelay = Math.max((Integer)this.minDelay.get(), (int)((double)((Integer)this.delay.get()).intValue() + random.nextGaussian() * 50.0));
        if (InventoryUtil.isItemValid(currentSlot) && bestItem != currentSlot && timer.delay(nextDelay) && (bestItemSlot = InventoryUtil.getItemStackSlot(bestItem)) != -1) {
            if (bestItemSlot < 9) {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestItemSlot + 36, targetSlot, ClickType.SWAP, (Player)mc.player);
            } else {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, bestItemSlot, targetSlot, ClickType.SWAP, (Player)mc.player);
            }
            this.inventoryOpen = true;
            timer.reset();
        }
    }

    private ItemStack stackAt(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || slot < 0 || slot >= mc.player.getInventory().items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = (ItemStack)mc.player.getInventory().items.get(slot);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private ItemStack offhandStack() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.getInventory().offhand.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = (ItemStack)mc.player.getInventory().offhand.get(0);
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private void swapOffHand(int slot) {
        Minecraft mc = Minecraft.getInstance();
        if (slot < 9) {
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot + 36, 40, ClickType.SWAP, (Player)mc.player);
        } else {
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 40, ClickType.SWAP, (Player)mc.player);
        }
        this.inventoryOpen = true;
        timer.reset();
    }

    private void throwItem(ItemStack item) {
        int itemSlot;
        Minecraft mc = Minecraft.getInstance();
        int nextDelay = Math.max((Integer)this.minDelay.get(), (int)((double)((Integer)this.delay.get()).intValue() + random.nextGaussian() * 50.0));
        if (InventoryUtil.isItemValid(item) && timer.delay(nextDelay) && (itemSlot = InventoryUtil.getItemStackSlot(item)) != -1) {
            if (itemSlot < 9) {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, itemSlot + 36, 1, ClickType.THROW, (Player)mc.player);
            } else {
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, itemSlot, 1, ClickType.THROW, (Player)mc.player);
            }
            this.inventoryOpen = true;
            timer.reset();
        }
    }

    public static int getWaterBucketCount() {
        InvmanagerModule m = ModuleManager.getModule(InvmanagerModule.class);
        if (m != null) {
            return (Integer)m.waterBucketCount.get();
        }
        return 1;
    }

    public static boolean shouldKeepProjectile() {
        InvmanagerModule m = ModuleManager.getModule(InvmanagerModule.class);
        if (m != null) {
            return (Boolean)m.keepProjectile.get();
        }
        return true;
    }

    public static int getMaxProjectileSize() {
        InvmanagerModule m = ModuleManager.getModule(InvmanagerModule.class);
        if (m != null) {
            return (Integer)m.maxProjectileSize.get();
        }
        return 64;
    }

    @Override
    public void onTick() {
        ItemStack offHand; // declarations lost by CFR
        int targetSlot;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (InventoryUtil.shouldDisableFeatures()) {
            return;
        }
        this.noMoveTicks = MoveUtil.isMoving() ? 0 : (this.noMoveTicks = this.noMoveTicks + 1);
        boolean allowMove = (Boolean)this.inventoryOnly.get() == false;
        boolean bl = allowMove;
        if (CheststealerModule.isWorking() || ((Boolean)this.inventoryOnly.get() != false ? !(mc.screen instanceof InventoryScreen) : !allowMove && this.noMoveTicks <= 1)) {
            this.clickOffHand = false;
            return;
        }
        Screen screen = mc.screen;
        if (screen instanceof AbstractContainerScreen) {
            AbstractContainerScreen container = (AbstractContainerScreen)screen;
            if (container.getMenu().containerId != mc.player.inventoryMenu.containerId) {
                return;
            }
        }
        int nextDelay = Math.max((Integer)this.minDelay.get(), (int)((double)((Integer)this.delay.get()).intValue() + random.nextGaussian() * 50.0));
        if (((Boolean)this.autoArmor.get()).booleanValue()) {
            ArmorItem item;
            ItemStack stack;
            for (int i = 0; i < mc.player.getInventory().armor.size(); ++i) {
                stack = (ItemStack)mc.player.getInventory().armor.get(i);
                Item item2 = stack.getItem();
                if (!(item2 instanceof ArmorItem)) continue;
                item = (ArmorItem)item2;
                if (stack.isEmpty() || !timer.delay(nextDelay) || !(InventoryUtil.getBestArmorScore(item.getEquipmentSlot()) > InventoryUtil.getProtection(stack))) continue;
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, 4 + (4 - i), 1, ClickType.THROW, (Player)mc.player);
                this.inventoryOpen = true;
                timer.reset();
            }
            for (int ix = 0; ix < mc.player.getInventory().items.size(); ++ix) {
                Item item3;
                stack = (ItemStack)mc.player.getInventory().items.get(ix);
                if (stack.isEmpty() || !((item3 = stack.getItem()) instanceof ArmorItem)) continue;
                item = (ArmorItem)item3;
                float currentItemScore = InventoryUtil.getProtection(stack);
                boolean isBestItem = InventoryUtil.getBestArmorScore(item.getEquipmentSlot()) == currentItemScore;
                boolean isBetterItem = InventoryUtil.getCurrentArmorScore(item.getEquipmentSlot()) < currentItemScore;
                boolean bl2 = isBetterItem;
                if (!isBestItem || !isBetterItem || !timer.delay(nextDelay)) continue;
                if (ix < 9) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, ix + 36, 0, ClickType.QUICK_MOVE, (Player)mc.player);
                } else {
                    mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, ix, 0, ClickType.QUICK_MOVE, (Player)mc.player);
                }
                this.inventoryOpen = true;
                timer.reset();
            }
        }
        if (this.clickOffHand && timer.delay(nextDelay)) {
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, 45, 0, ClickType.PICKUP, (Player)mc.player);
            this.inventoryOpen = true;
            this.clickOffHand = false;
            timer.reset();
        }
        if (this.offhandItems.isMode("Golden Apple")) {
            offHand = this.offhandStack();
            Item offHandItem = offHand.getItem();
            int egapSlot = InventoryUtil.getItemSlot(Items.ENCHANTED_GOLDEN_APPLE);
            int gapSlot = InventoryUtil.getItemSlot(Items.GOLDEN_APPLE);
            if (offHandItem == Items.ENCHANTED_GOLDEN_APPLE) {
                if (offHand.getCount() < offHand.getMaxStackSize() && egapSlot != -1 && timer.delay(nextDelay)) {
                    targetSlot = egapSlot;
                    if (targetSlot < 9) {
                        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, targetSlot + 36, 0, ClickType.PICKUP, (Player)mc.player);
                    } else {
                        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, targetSlot, 0, ClickType.PICKUP, (Player)mc.player);
                    }
                    this.inventoryOpen = true;
                    this.clickOffHand = true;
                    timer.reset();
                }
            } else if (offHandItem == Items.GOLDEN_APPLE) {
                if (egapSlot != -1) {
                    if (timer.delay(nextDelay)) {
                        this.swapOffHand(egapSlot);
                        timer.reset();
                    }
                } else if (offHand.getCount() < offHand.getMaxStackSize() && gapSlot != -1 && timer.delay(nextDelay)) {
                    targetSlot = gapSlot;
                    if (targetSlot < 9) {
                        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, targetSlot + 36, 0, ClickType.PICKUP, (Player)mc.player);
                    } else {
                        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, targetSlot, 0, ClickType.PICKUP, (Player)mc.player);
                    }
                    this.inventoryOpen = true;
                    this.clickOffHand = true;
                    timer.reset();
                }
            } else if (egapSlot != -1) {
                if (timer.delay(nextDelay)) {
                    this.swapOffHand(egapSlot);
                    timer.reset();
                }
            } else if (gapSlot != -1 && timer.delay(nextDelay)) {
                this.swapOffHand(gapSlot);
                timer.reset();
            }
        } else if (this.offhandItems.isMode("Projectile")) {
            offHand = this.offhandStack();
            ItemStack bestProjectile = InventoryUtil.getBestProjectile();
            if (bestProjectile != null) {
                int slot = InventoryUtil.getItemStackSlot(bestProjectile);
                boolean shouldSwap = offHand.getItem() != Items.EGG && offHand.getItem() != Items.SNOWBALL || offHand.getCount() < bestProjectile.getCount();
                boolean bl3 = shouldSwap;
                if (shouldSwap && slot != -1 && timer.delay(nextDelay)) {
                    this.swapOffHand(slot);
                }
            }
        } else if (this.offhandItems.isMode("Fishing Rod")) {
            offHand = this.offhandStack();
            int slotx = InventoryUtil.getItemSlot(Items.FISHING_ROD);
            if (slotx != -1 && timer.delay(nextDelay) && offHand.getItem() != Items.FISHING_ROD) {
                this.swapOffHand(slotx);
            }
        } else if (this.offhandItems.isMode("Block")) {
            offHand = this.offhandStack();
            ItemStack bestBlock = InventoryUtil.getBestBlock();
            if (bestBlock != null) {
                int slotx = InventoryUtil.getItemStackSlot(bestBlock);
                boolean shouldSwapx = !InventoryUtil.isValidStack(offHand) || offHand.getCount() < bestBlock.getCount();
                boolean bl4 = shouldSwapx;
                if (shouldSwapx && slotx != -1 && timer.delay(nextDelay)) {
                    this.swapOffHand(slotx);
                }
            }
        }
        if (((Boolean)this.switchGoldenApple.get()).booleanValue() && !this.offhandItems.isMode("Golden Apple")) {
            int targetSlotIdx = (Integer)this.goldenAppleSlot.get() - 1;
            int egapSlot = InventoryUtil.getItemSlot(Items.ENCHANTED_GOLDEN_APPLE);
            int gapSlot = InventoryUtil.getItemSlot(Items.GOLDEN_APPLE);
            int bestGapSlot = egapSlot != -1 ? egapSlot : gapSlot;
            int n = bestGapSlot;
            if (bestGapSlot != -1) {
                ItemStack currentInSlot = this.stackAt(targetSlotIdx);
                ItemStack bestGapItem = this.stackAt(bestGapSlot);
                if (currentInSlot.getItem() != bestGapItem.getItem() || currentInSlot.getCount() < bestGapItem.getCount() && currentInSlot.getItem() == bestGapItem.getItem()) {
                    this.swapItem(targetSlotIdx, bestGapItem);
                }
            }
        }
        if (((Boolean)this.switchBlock.get()).booleanValue()) {
            int blockSlotIndex = (Integer)this.blockSlot.get() - 1;
            ItemStack currentBlock = this.stackAt(blockSlotIndex);
            ItemStack bestBlock = InventoryUtil.getBestBlock();
            if (!(bestBlock == null || bestBlock.getCount() <= currentBlock.getCount() && InventoryUtil.isValidStack(currentBlock) || this.offhandItems.isMode("Block"))) {
                this.swapItem(blockSlotIndex, bestBlock);
            }
            if ((float) InventoryUtil.getBlockCountInInventory() > (float)((Integer)this.maxBlockSize.get()).intValue()) {
                this.throwItem(InventoryUtil.getWorstBlock());
            }
        }
        if (((Boolean)this.switchSword.get()).booleanValue()) {
            int slotIndex = (Integer)this.swordSlot.get() - 1;
            ItemStack currentSword = (ItemStack)mc.player.getInventory().items.get(slotIndex);
            ItemStack bestSword = InventoryUtil.getBestSword();
            ItemStack bestShapeAxe = InventoryUtil.getBestShapeAxe();
            if (InventoryUtil.getAxeDamage(bestShapeAxe) > InventoryUtil.getSwordDamage(bestSword)) {
                bestSword = bestShapeAxe;
            }
            if (bestSword != null) {
                float currentDamage = currentSword.getItem() instanceof SwordItem ? InventoryUtil.getSwordDamage(currentSword) : InventoryUtil.getAxeDamage(currentSword);
                float bestWeaponDamage = bestSword.getItem() instanceof SwordItem ? InventoryUtil.getSwordDamage(bestSword) : InventoryUtil.getAxeDamage(bestSword);
                float f = bestWeaponDamage;
                if (bestWeaponDamage > currentDamage) {
                    this.swapItem(slotIndex, bestSword);
                }
            }
        }
        if (((Boolean)this.switchPickaxe.get()).booleanValue()) {
            int slotIndex = (Integer)this.pickaxeSlot.get() - 1;
            ItemStack bestPickaxe = InventoryUtil.getBestPickaxe();
            ItemStack currentPickaxe = (ItemStack)mc.player.getInventory().items.get(slotIndex);
            if (bestPickaxe != null && bestPickaxe.getItem() instanceof PickaxeItem && (InventoryUtil.getToolScore(bestPickaxe) > InventoryUtil.getToolScore(currentPickaxe) || !(currentPickaxe.getItem() instanceof PickaxeItem))) {
                this.swapItem(slotIndex, bestPickaxe);
            }
        }
        if (((Boolean)this.switchAxe.get()).booleanValue()) {
            int slotIndex = (Integer)this.axeSlot.get() - 1;
            ItemStack bestAxe = InventoryUtil.getBestAxe();
            ItemStack currentAxe = (ItemStack)mc.player.getInventory().items.get(slotIndex);
            if (bestAxe != null && bestAxe.getItem() instanceof AxeItem && (InventoryUtil.getToolScore(bestAxe) > InventoryUtil.getToolScore(currentAxe) || !(currentAxe.getItem() instanceof AxeItem))) {
                this.swapItem(slotIndex, bestAxe);
            }
        }
        if (((Boolean)this.switchRod.get()).booleanValue() && !this.offhandItems.isMode("Fishing Rod")) {
            int slotIndex = (Integer)this.rodSlot.get() - 1;
            ItemStack bestRod = InventoryUtil.getFishingRod();
            ItemStack currentRod = (ItemStack)mc.player.getInventory().items.get(slotIndex);
            if (!(currentRod.getItem() instanceof FishingRodItem)) {
                this.swapItem(slotIndex, bestRod);
            }
        }
        if (((Boolean)this.switchBow.get()).booleanValue()) {
            float currentScore;
            float bestScore;
            ItemStack bestBow;
            int slotIndex = (Integer)this.bowSlot.get() - 1;
            ItemStack currentBow = (ItemStack)mc.player.getInventory().items.get(slotIndex);
            if (this.preferBow.isMode("Crossbow")) {
                bestBow = InventoryUtil.getBestCrossbow();
                bestScore = InventoryUtil.getCrossbowScore(bestBow);
                currentScore = InventoryUtil.getCrossbowScore(currentBow);
            } else if (this.preferBow.isMode("Power Bow")) {
                bestBow = InventoryUtil.getBestPowerBow();
                bestScore = InventoryUtil.getPowerBowScore(bestBow);
                currentScore = InventoryUtil.getPowerBowScore(currentBow);
            } else {
                bestBow = InventoryUtil.getBestPunchBow();
                bestScore = InventoryUtil.getPunchBowScore(bestBow);
                currentScore = InventoryUtil.getPunchBowScore(currentBow);
            }
            if (bestBow == null) {
                bestBow = InventoryUtil.getBestCrossbow();
                bestScore = InventoryUtil.getCrossbowScore(bestBow);
                currentScore = InventoryUtil.getCrossbowScore(currentBow);
            }
            if (bestBow == null) {
                bestBow = InventoryUtil.getBestPowerBow();
                bestScore = InventoryUtil.getPowerBowScore(bestBow);
                currentScore = InventoryUtil.getPowerBowScore(currentBow);
            }
            if (bestBow == null) {
                bestBow = InventoryUtil.getBestPunchBow();
                bestScore = InventoryUtil.getPunchBowScore(bestBow);
                currentScore = InventoryUtil.getPunchBowScore(currentBow);
            }
            if (bestBow != null && bestScore > currentScore) {
                this.swapItem(slotIndex, bestBow);
            }
            if ((float) InventoryUtil.getItemCount(Items.ARROW) > (float)((Integer)this.maxArrowSize.get()).intValue()) {
                this.throwItem(InventoryUtil.getWorstArrow());
            }
        }
        if (((Boolean)this.switchEnderPearl.get()).booleanValue()) {
            this.swapItem((Integer)this.enderPearlSlot.get() - 1, Items.ENDER_PEARL);
        }
        if (((Boolean)this.switchWaterBucket.get()).booleanValue()) {
            this.swapItem((Integer)this.waterBucketSlot.get() - 1, Items.WATER_BUCKET);
        }
        if (((Boolean)this.switchFireball.get()).booleanValue()) {
            this.swapItem((Integer)this.fireballSlot.get() - 1, Items.FIRE_CHARGE);
        }
        if (((Boolean)this.keepProjectile.get()).booleanValue()) {
            if ((float)(InventoryUtil.getItemCount(Items.EGG) + InventoryUtil.getItemCount(Items.SNOWBALL)) > (float)((Integer)this.maxProjectileSize.get()).intValue()) {
                this.throwItem(InventoryUtil.getWorstProjectile());
            }
            if (((Boolean)this.switchProjectile.get()).booleanValue() && !this.offhandItems.isMode("Projectile")) {
                int pSlot = (Integer)this.projectileSlot.get() - 1;
                if (InventoryUtil.getItemCount(Items.EGG) > 0) {
                    this.swapItem(pSlot, Items.EGG);
                } else if (InventoryUtil.getItemCount(Items.SNOWBALL) > 0) {
                    this.swapItem(pSlot, Items.SNOWBALL);
                }
            }
        }
        if (((Boolean)this.throwItems.get()).booleanValue()) {
            List<Integer> slots = IntStream.range(0, mc.player.getInventory().items.size()).boxed().collect(Collectors.toList());
            Collections.shuffle(slots);
            for (Integer slotIdx : slots) {
                ItemStack stack = (ItemStack)mc.player.getInventory().items.get(slotIdx.intValue());
                if (stack.isEmpty() || this.isItemUseful(stack)) continue;
                this.throwItem(stack);
            }
        }
    }

    @Override
    public boolean onOutgoingPacket(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (packet instanceof ServerboundContainerClosePacket) {
            this.inventoryOpen = false;
        }
        if (this.inventoryOpen && !((Boolean)this.inventoryOnly.get()).booleanValue()) {
            if (packet instanceof ServerboundMovePlayerPacket) {
                if (MoveUtil.isMoving()) {
                    mc.getConnection().send((Packet)new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
                }
            } else if (packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundUseItemPacket || packet instanceof ServerboundInteractPacket || packet instanceof ServerboundPlayerActionPacket) {
                mc.getConnection().send((Packet)new ServerboundContainerClosePacket(mc.player.inventoryMenu.containerId));
            }
        }
        return false;
    }
}

