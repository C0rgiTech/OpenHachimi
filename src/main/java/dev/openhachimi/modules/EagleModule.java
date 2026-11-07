/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.render.BlockCounterDisplay;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EagleModule
extends Module {
    private static final byte[] S_3F9FDE4 = new byte[]{7, 35, 37, 46, 39};  // XOR0x42 "Eagle"
    private static final byte[] S_2C7E8D7C = new byte[]{15, 43, 44, 98, 49, 44, 39, 35, 41, 98, 54, 43, 33, 41, 49};  // XOR0x42 "Min sneak ticks"
    private static final byte[] S_6EAE818E = new byte[]{15, 35, 58, 98, 49, 44, 39, 35, 41, 98, 54, 43, 33, 41, 49};  // XOR0x42 "Max sneak ticks"
    private static final byte[] S_36FE9DB7 = new byte[]{13, 44, 46, 59, 98, 32, 35, 33, 41, 53, 35, 48, 38};  // XOR0x42 "Only backward"
    private static final byte[] S_50E7A0E5 = new byte[]{11, 37, 44, 45, 48, 39, 98, 42, 35, 46, 36, 98, 32, 46, 45, 33, 41, 49};  // XOR0x42 "Ignore half blocks"
    private static final byte[] S_48F53E7B = new byte[]{17, 44, 39, 35, 41, 98, 43, 44, 98, 35, 43, 48};  // XOR0x42 "Sneak in air"
    private static final byte[] S_3D4D46D = new byte[]{0, 46, 45, 33, 41};  // XOR0x42 "Block"
    private final ListSetting minTicks = new ListSetting("Min sneak ticks", 1, 0, 10);
    private final ListSetting maxTicks = new ListSetting("Max sneak ticks", 3, 0, 10);
    private final BooleanSetting onlyBack = new BooleanSetting("Only backward", true);
    private final BooleanSetting ignoreHalfBlocks = new BooleanSetting("Ignore half blocks", true);
    private final BooleanSetting sneakInAir = new BooleanSetting("Sneak in air", true);
    public final BooleanSetting block = new BooleanSetting("Block", false);
    private final BlockCounterDisplay blockCounterDisplay = new BlockCounterDisplay();
    private int ticks;

    public EagleModule() {
        super("Eagle", Category.MOVEMENT);
        this.values.add(this.minTicks);
        this.values.add(this.maxTicks);
        this.values.add(this.onlyBack);
        this.values.add(this.ignoreHalfBlocks);
        this.values.add(this.sneakInAir);
        this.values.add(this.block);
    }

    public static boolean isOnBlockEdge(float sensitivity, boolean ignoreHalfBlocks) {
        double lookZ;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        AABB checkBox = mc.player.getBoundingBox().move(0.0, -0.5, 0.0).inflate((double)(-sensitivity), 0.0, (double)(-sensitivity));
        boolean noCollision = !mc.level.getCollisions((Entity)mc.player, checkBox).iterator().hasNext();
        boolean bl = noCollision;
        if (!noCollision) {
            return false;
        }
        if (!ignoreHalfBlocks) {
            return true;
        }
        AABB looseBox = mc.player.getBoundingBox().move(0.0, -0.5, 0.0).inflate(-0.05, 0.0, -0.05);
        if (EagleModule.hasHalfBlockSupport(mc, looseBox)) {
            return false;
        }
        float yaw = mc.player.getYRot() * ((float)Math.PI / 180);
        double lookX = (double)(-Mth.sin((float)yaw)) * 0.35;
        AABB frontBox = looseBox.move(lookX, 0.0, lookZ = (double)Mth.cos((float)yaw) * 0.35);
        return !EagleModule.hasHalfBlockSupport(mc, frontBox);
    }

    private int getRandomInRange(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Override
    public void onEnable() {
        if (!this.blockCounterDisplay.isAnimating()) {
            this.blockCounterDisplay.reset();
        }
    }

    @Override
    public boolean shouldRender2DWhenDisabled() {
        return this.blockCounterDisplay.isAnimating();
    }

    boolean isBlockDisplayAnimating() {
        return this.blockCounterDisplay.isAnimating();
    }

    private static boolean hasHalfBlockSupport(Minecraft mc, AABB box) {
        int minX = Mth.floor((double)box.minX);
        int maxX = Mth.floor((double)box.maxX);
        int minY = Mth.floor((double)box.minY);
        int maxY = Mth.floor((double)box.maxY) + 1;
        int minZ = Mth.floor((double)box.minZ);
        int maxZ = Mth.floor((double)box.maxZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    VoxelShape shape;
                    Block block;
                    pos.set(x, y, z);
                    BlockState state = mc.level.getBlockState((BlockPos)pos);
                    if (state.isAir() || !((block = state.getBlock()) instanceof SlabBlock) && !(block instanceof StairBlock) && !(block instanceof SnowLayerBlock) && !(block instanceof CarpetBlock) && !(block instanceof TrapDoorBlock) || (shape = state.getCollisionShape((BlockGetter)mc.level, (BlockPos)pos)).isEmpty()) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            this.blockCounterDisplay.reset();
            return;
        }
        ItemStack held = mc.player.getMainHandItem();
        int count = held.getItem() instanceof BlockItem ? held.getCount() : 0;
        ItemStack displayStack = count > 0 ? held : ItemStack.EMPTY;
        ScaffoldModule scaffold = ModuleManager.getModule(ScaffoldModule.class);
        boolean paired = scaffold != null && scaffold.enabled;
        int reference = held.getItem() instanceof BlockItem ? Math.max(1, held.getMaxStackSize()) : 64;
        this.blockCounterDisplay.render(graphics, displayStack, count, reference, paired ? 19.0f : 30.0f, this.enabled && (Boolean)this.block.get() != false);
    }

    @Override
    public void onMovementInput(Input input) {
        float forward;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (this.ticks > 0) {
            --this.ticks;
        }
        if (((forward = input.forwardImpulse) <= 0.0f || !((Boolean)this.onlyBack.get()).booleanValue()) && EagleModule.isOnBlockEdge(0.3f, (Boolean)this.ignoreHalfBlocks.get()) && mc.player.onGround()) {
            this.ticks = this.getRandomInRange((Integer)this.minTicks.get(), (Integer)this.maxTicks.get());
        }
        if (this.ticks > 0) {
            input.shiftKeyDown = true;
        }
        if (!(!((Boolean)this.sneakInAir.get()).booleanValue() || mc.player.onGround() || mc.player.isInWater() || mc.player.isInLava() || mc.player.onClimbable() || mc.player.isPassenger() || mc.player.isFallFlying())) {
            input.shiftKeyDown = true;
        }
    }
}

