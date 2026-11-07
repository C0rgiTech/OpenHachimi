/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ExperienceBottleItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;

public class InventoryUtil {
    public static final List<Block> blacklistedBlocks = Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.LAVA, Blocks.ENCHANTING_TABLE, Blocks.GLASS_PANE, Blocks.IRON_BARS, Blocks.SNOW, Blocks.COAL_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.TORCH, Blocks.ANVIL, Blocks.NOTE_BLOCK, Blocks.JUKEBOX, Blocks.TNT, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.LAPIS_ORE, Blocks.STONE_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.STONE_BUTTON, Blocks.LEVER, Blocks.TALL_GRASS, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.RAIL, Blocks.CORNFLOWER, Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM, Blocks.VINE, Blocks.SUNFLOWER, Blocks.LADDER, Blocks.FURNACE, Blocks.SAND, Blocks.CACTUS, Blocks.DISPENSER, Blocks.DROPPER, Blocks.CRAFTING_TABLE, Blocks.COBWEB, Blocks.PUMPKIN, Blocks.COBBLESTONE_WALL, Blocks.OAK_FENCE, Blocks.REDSTONE_TORCH, Blocks.FLOWER_POT);
    private static final Minecraft mc = Minecraft.getInstance();

    public static int getItemCount(Item checkItem) {
        int count = 0;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || item.getItem() != checkItem) continue;
            count += item.getCount();
        }
        return count;
    }

    public static boolean hasItem(Item checkItem) {
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || item.getItem() != checkItem) continue;
            return true;
        }
        return false;
    }

    public static float getSwordDamage(ItemStack stack) {
        int sharpness;
        if (stack == null || stack.isEmpty()) {
            return 0.0f;
        }
        float valence = 0.0f;
        Item item = stack.getItem();
        if (item instanceof SwordItem) {
            SwordItem sword = (SwordItem)item;
            valence += sword.getDamage() + 1.0f;
        }
        if ((sharpness = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.SHARPNESS, (ItemStack)stack)) > 0) {
            valence += Enchantments.SHARPNESS.getDamageBonus(sharpness, MobType.UNDEFINED);
        }
        return valence;
    }

    public static float getBestSwordDamage() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof SwordItem) || !((score = InventoryUtil.getSwordDamage(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static boolean isSharpnessAxe(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof AxeItem)) {
            return false;
        }
        int itemEnchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.SHARPNESS, (ItemStack)stack);
        return itemEnchantmentLevel >= 8 && itemEnchantmentLevel < 50;
    }

    public static float getBestArmorScore(EquipmentSlot slot) {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof ArmorItem) || ((ArmorItem)item.getItem()).getEquipmentSlot() != slot || !((score = InventoryUtil.getProtection(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static float getProtection(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0.0f;
        }
        int valence = 0;
        Item item = itemStack.getItem();
        if (item instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem)item;
            ArmorMaterial material = armor.getMaterial();
            if (material == ArmorMaterials.LEATHER) {
                valence += 100;
            } else if (material == ArmorMaterials.CHAIN) {
                valence += 200;
            } else if (material == ArmorMaterials.IRON) {
                valence += 400;
            } else if (material == ArmorMaterials.GOLD) {
                valence += 300;
            } else if (material == ArmorMaterials.DIAMOND) {
                valence += 500;
            } else if (material == ArmorMaterials.NETHERITE) {
                valence += 600;
            }
        }
        return valence += EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.ALL_DAMAGE_PROTECTION, (ItemStack)itemStack);
    }

    public static float getToolScore(ItemStack stack) {
        if (stack == null || stack.isEmpty() || InventoryUtil.isGodItem(stack) || InventoryUtil.isSharpnessAxe(stack)) {
            return 0.0f;
        }
        float valence = 0.0f;
        if (stack.getItem() instanceof PickaxeItem) {
            valence += stack.getDestroySpeed(Blocks.STONE.defaultBlockState());
        } else if (stack.getItem() instanceof AxeItem) {
            valence += stack.getDestroySpeed(Blocks.OAK_LOG.defaultBlockState());
        } else if (stack.getItem() instanceof ShovelItem) {
            valence += stack.getDestroySpeed(Blocks.DIRT.defaultBlockState());
        } else {
            return 0.0f;
        }
        int efficiency = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.BLOCK_EFFICIENCY, (ItemStack)stack);
        if (efficiency > 0) {
            valence += (float)efficiency * 0.0075f;
        }
        return valence;
    }

    public static boolean isCommonItemUseful(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem block = (BlockItem)item;
            if (block.getBlock() == Blocks.ENCHANTING_TABLE) {
                return false;
            }
            return block.getBlock() != Blocks.COBWEB;
        }
        if (item instanceof BookItem || item instanceof ExperienceBottleItem || item instanceof FireworkRocketItem) {
            return false;
        }
        if (item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS || item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS) {
            return false;
        }
        return item != Items.FLINT_AND_STEEL;
    }

    public static float getPowerBowScore(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BowItem)) {
            return 0.0f;
        }
        float valence = 10.0f;
        valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.PUNCH_ARROWS, (ItemStack)stack) / 10.0f;
        valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.INFINITY_ARROWS, (ItemStack)stack);
        valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.FLAMING_ARROWS, (ItemStack)stack);
        return (valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.POWER_ARROWS, (ItemStack)stack)) + (float)stack.getDamageValue() / (float)stack.getMaxDamage();
    }

    public static boolean isValidStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof BlockItem) || stack.getCount() <= 1 || !InventoryUtil.isItemValid(stack)) {
            return false;
        }
        String string = stack.getDisplayName().getString();
        if (string.contains("Click") || string.contains("点击") || stack.getItem() instanceof ItemNameBlockItem) {
            return false;
        }
        Block block = ((BlockItem)stack.getItem()).getBlock();
        if (block instanceof FlowerBlock || block instanceof BushBlock || block instanceof FungusBlock || block instanceof CropBlock) {
            return false;
        }
        return !(block instanceof SlabBlock) && !blacklistedBlocks.contains(block);
    }

    public static boolean isPunchBow(ItemStack stack) {
        return InventoryUtil.getPunchBowScore(stack) > 10.0f && InventoryUtil.isItemValid(stack);
    }

    public static float getPunchBowScore(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BowItem)) {
            return 0.0f;
        }
        float valence = 10.0f;
        valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.PUNCH_ARROWS, (ItemStack)stack);
        valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.INFINITY_ARROWS, (ItemStack)stack);
        valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.FLAMING_ARROWS, (ItemStack)stack);
        return (valence += (float)EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.POWER_ARROWS, (ItemStack)stack) / 10.0f) + (float)stack.getDamageValue() / (float)stack.getMaxDamage();
    }

    public static boolean isPowerBow(ItemStack stack) {
        return InventoryUtil.getPowerBowScore(stack) > 10.0f && InventoryUtil.isItemValid(stack);
    }

    public static float getBestAxeScore() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof AxeItem) || InventoryUtil.isSharpnessAxe(item) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getToolScore(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static float getBestShovelScore() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof ShovelItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getToolScore(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static float getCrossbowScore(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof CrossbowItem)) {
            return 0.0f;
        }
        int valence = 0;
        valence += EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.QUICK_CHARGE, (ItemStack)stack);
        valence += EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.MULTISHOT, (ItemStack)stack);
        return valence += EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.PIERCING, (ItemStack)stack);
    }

    public static ItemStack getBestSword() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof SwordItem) || !((score = InventoryUtil.getSwordDamage(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestCrossbow() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof CrossbowItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getCrossbowScore(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestPickaxe() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof PickaxeItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getToolScore(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestPunchBow() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof BowItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getPunchBowScore(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestShovel() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof ShovelItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getToolScore(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestPowerBow() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof BowItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getPowerBowScore(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestAxe() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof AxeItem) || InventoryUtil.isSharpnessAxe(item) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getToolScore(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getBestBlock() {
        ItemStack best = null;
        int maxCount = -1;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || !(item.getItem() instanceof BlockItem) || !InventoryUtil.isValidStack(item) || !InventoryUtil.isItemValid(item) || item.getCount() <= maxCount) continue;
            maxCount = item.getCount();
            best = item;
        }
        return best;
    }

    public static ItemStack getWorstBlock() {
        ItemStack worst = null;
        int minCount = Integer.MAX_VALUE;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || !(item.getItem() instanceof BlockItem) || !InventoryUtil.isValidStack(item) || !InventoryUtil.isItemValid(item) || item.getCount() >= minCount) continue;
            minCount = item.getCount();
            worst = item;
        }
        return worst;
    }

    public static int getItemSlot(Item item) {
        for (int i = 0; i < InventoryUtil.mc.player.getInventory().items.size(); ++i) {
            if (((ItemStack) InventoryUtil.mc.player.getInventory().items.get(i)).getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static ItemStack getWorstProjectile() {
        ItemStack worst = null;
        int minCount = Integer.MAX_VALUE;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || item.getItem() != Items.EGG && item.getItem() != Items.SNOWBALL || item.getCount() >= minCount) continue;
            minCount = item.getCount();
            worst = item;
        }
        return worst;
    }

    public static ItemStack getWorstArrow() {
        ItemStack worst = null;
        int minCount = Integer.MAX_VALUE;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || !(item.getItem() instanceof ArrowItem) || !InventoryUtil.isItemValid(item) || item.getCount() >= minCount) continue;
            minCount = item.getCount();
            worst = item;
        }
        return worst;
    }

    public static ItemStack getBestShapeAxe() {
        ItemStack best = null;
        float maxScore = -1.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof AxeItem) || !InventoryUtil.isSharpnessAxe(item) || !InventoryUtil.isItemValid(item) || InventoryUtil.isGodAxe(item) || !((score = InventoryUtil.getAxeDamage(item)) > maxScore)) continue;
            maxScore = score;
            best = item;
        }
        return best;
    }

    public static ItemStack getFishingRod() {
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || !(item.getItem() instanceof FishingRodItem) || !InventoryUtil.isItemValid(item)) continue;
            return item;
        }
        return null;
    }

    public static int getItemStackSlot(ItemStack stack) {
        if (stack == null) {
            return -1;
        }
        for (int i = 0; i < InventoryUtil.mc.player.getInventory().items.size(); ++i) {
            if (InventoryUtil.mc.player.getInventory().items.get(i) != stack) continue;
            return i;
        }
        return -1;
    }

    public static ItemStack getBestProjectile() {
        ItemStack best = null;
        int maxCount = -1;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || item.getItem() != Items.EGG && item.getItem() != Items.SNOWBALL || !InventoryUtil.isItemValid(item) || item.getCount() <= maxCount) continue;
            maxCount = item.getCount();
            best = item;
        }
        return best;
    }

    public static float getAxeDamage(ItemStack stack) {
        int sharpness;
        if (stack == null || stack.isEmpty()) {
            return 0.0f;
        }
        float valence = 0.0f;
        Item item = stack.getItem();
        if (item instanceof AxeItem) {
            AxeItem axe = (AxeItem)item;
            if (InventoryUtil.isSharpnessAxe(stack)) {
                if (axe == Items.WOODEN_AXE || axe == Items.GOLDEN_AXE) {
                    valence += 4.0f;
                } else if (axe == Items.STONE_AXE) {
                    valence += 5.0f;
                } else if (axe == Items.IRON_AXE) {
                    valence += 6.0f;
                } else if (axe == Items.DIAMOND_AXE) {
                    valence += 7.0f;
                }
            }
        }
        if ((sharpness = EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.SHARPNESS, (ItemStack)stack)) > 0) {
            valence += Enchantments.SHARPNESS.getDamageBonus(sharpness, MobType.UNDEFINED);
        }
        return valence;
    }

    public static boolean isGodItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() == Items.GOLDEN_AXE && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.SHARPNESS, (ItemStack)stack) > 100) {
            return true;
        }
        if (stack.getItem() == Items.SLIME_BALL && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.KNOCKBACK, (ItemStack)stack) > 1) {
            return true;
        }
        return stack.getItem() == Items.TOTEM_OF_UNDYING || stack.getItem() == Items.END_CRYSTAL;
    }

    public static boolean isItemValid(ItemStack s) {
        if (!s.isEmpty()) {
            if (s.getItem() instanceof PlayerHeadItem) {
                return false;
            }
            String string = s.getDisplayName().getString();
            if (string.contains("Click") || string.contains("Right") || string.contains("点击") || string.contains("Teleport") || string.contains("使用") || string.contains("传送") || string.contains("再来")) {
                return false;
            }
        }
        return true;
    }

    public static boolean isKBStick(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == Items.STICK && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.KNOCKBACK, (ItemStack)stack) > 1;
    }

    public static boolean isGodAxe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == Items.GOLDEN_AXE && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.SHARPNESS, (ItemStack)stack) > 100;
    }

    public static boolean isGoldenHead(ItemStack e) {
        if (e.isEmpty()) {
            return false;
        }
        Item item = e.getItem();
        if (item instanceof BlockItem) {
            BlockItem item2 = (BlockItem)item;
            return item2.getBlock() instanceof SkullBlock;
        }
        return false;
    }

    public static boolean isKBBall(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == Items.SLIME_BALL && EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.KNOCKBACK, (ItemStack)stack) > 1;
    }

    public static int getPunchLevel(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.PUNCH_ARROWS, (ItemStack)stack);
    }

    public static List<ItemStack> getAllItems() {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>(40);
        list.addAll((Collection<ItemStack>) InventoryUtil.mc.player.getInventory().items);
        list.addAll((Collection<ItemStack>) InventoryUtil.mc.player.getInventory().armor);
        return list;
    }

    public static boolean isEnchantedGApple(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
    }

    public static int findEmptySlot() {
        for (int i = 0; i < 9; ++i) {
            if (!((ItemStack) InventoryUtil.mc.player.getInventory().items.get(i)).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public static boolean isEndCrystal(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL;
    }

    public static int findEmptyInventory() {
        for (int i = 9; i < InventoryUtil.mc.player.getInventory().items.size(); ++i) {
            if (!((ItemStack) InventoryUtil.mc.player.getInventory().items.get(i)).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    public static int getPowerLevel(ItemStack stack) {
        return EnchantmentHelper.getItemEnchantmentLevel((Enchantment)Enchantments.POWER_ARROWS, (ItemStack)stack);
    }

    public static Integer findItemHotbar(Item item) {
        for (int i = 0; i < 9; ++i) {
            if (((ItemStack) InventoryUtil.mc.player.getInventory().items.get(i)).getItem() != item) continue;
            return i;
        }
        return null;
    }

    public static float getBestPunchBowScore() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof BowItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getPunchBowScore(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static float getBestPickaxeScore() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof PickaxeItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getToolScore(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static float getCurrentArmorScore(EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD) {
            return InventoryUtil.getProtection((ItemStack) InventoryUtil.mc.player.getInventory().armor.get(3));
        }
        if (slot == EquipmentSlot.CHEST) {
            return InventoryUtil.getProtection((ItemStack) InventoryUtil.mc.player.getInventory().armor.get(2));
        }
        if (slot == EquipmentSlot.LEGS) {
            return InventoryUtil.getProtection((ItemStack) InventoryUtil.mc.player.getInventory().armor.get(1));
        }
        if (slot == EquipmentSlot.FEET) {
            return InventoryUtil.getProtection((ItemStack) InventoryUtil.mc.player.getInventory().armor.get(0));
        }
        return 0.0f;
    }

    public static float getBestCrossbowScore() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof CrossbowItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getCrossbowScore(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static float getBestPowerBowScore() {
        float maxScore = 0.0f;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            float score;
            if (item.isEmpty() || !(item.getItem() instanceof BowItem) || !InventoryUtil.isItemValid(item) || !((score = InventoryUtil.getPowerBowScore(item)) > maxScore)) continue;
            maxScore = score;
        }
        return maxScore;
    }

    public static int getBlockCountInInventory() {
        int sum = 0;
        for (ItemStack item : InventoryUtil.getAllItems()) {
            if (item.isEmpty() || !(item.getItem() instanceof BlockItem) || !InventoryUtil.isValidStack(item) || !InventoryUtil.isItemValid(item)) continue;
            sum += item.getCount();
        }
        return sum;
    }

    public static boolean shouldDisableFeatures() {
        for (ItemStack item : InventoryUtil.getAllItems()) {
            String string;
            if (item.isEmpty() || !(string = item.getDisplayName().getString()).contains("长按点击") && !string.contains("点击使用") && !string.contains("离开游戏") && !string.contains("选择一个队伍") && !string.contains("再来一局")) continue;
            return true;
        }
        return false;
    }
}

