/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.utils.rotation.RotationManager;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.player.InventoryUtil;
import dev.openhachimi.utils.player.PlayerData;
import dev.openhachimi.render.BlockCounterDisplay;
import dev.openhachimi.utils.player.MoveUtil;
import dev.openhachimi.utils.player.RaytraceUtil2;
import dev.openhachimi.utils.rotation.RotationUtil;
import dev.openhachimi.utils.MathUtil;

import java.lang.reflect.Field;
import java.util.function.Supplier;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

public class ScaffoldModule
extends Module {
    private static final byte[] S_4E5DF416 = new byte[]{17, 33, 35, 36, 36, 45, 46, 38};  // XOR0x42 "Scaffold"
    private static final byte[] S_12F8D41 = new byte[]{3, 55, 54, 45, 98, 113, 48, 38, 98, 18, 39, 48, 49, 45, 44};  // XOR0x42 "Auto 3rd Person"
    private static final byte[] S_4CF42C8 = new byte[]{22, 39, 46, 46, 59};  // XOR0x42 "Telly"
    private static final byte[] S_2763EA = new byte[]{17, 44, 35, 50};  // XOR0x42 "Snap"
    private static final byte[] S_6D049B16 = new byte[]{17, 35, 36, 39, 21, 35, 46, 41};  // XOR0x42 "SafeWalk"
    private static final byte[] S_2D4AE925 = new byte[]{16, 45, 54, 35, 54, 43, 45, 44, 98, 17, 50, 39, 39, 38};  // XOR0x42 "Rotation Speed"
    private static final byte[] S_7EBE9190 = new byte[]{16, 45, 54, 35, 54, 43, 45, 44, 98, 0, 35, 33, 41, 98, 17, 50, 39, 39, 38};  // XOR0x42 "Rotation Back Speed"
    private static final byte[] S_582012FE = new byte[]{22, 39, 46, 46, 59, 98, 22, 43, 33, 41, 49};  // XOR0x42 "Telly Ticks"
    private static final byte[] S_59CE950D = new byte[]{3, 55, 54, 45, 17, 54, 55, 33, 41};  // XOR0x42 "AutoStuck"
    private static final byte[] S_A428DE7 = new byte[]{17, 54, 55, 33, 41, 98, 15, 45, 38, 39};  // XOR0x42 "Stuck Mode"
    private static final byte[] S_5512692F = new byte[]{17, 54, 45, 50, 98, 15, 45, 52, 39};  // XOR0x42 "Stop Move"
    private static final byte[] S_27EB6C95 = new byte[]{17, 41, 43, 50, 98, 22, 43, 33, 41, 49};  // XOR0x42 "Skip Ticks"
    private static final byte[] S_3D4D46D = new byte[]{0, 46, 45, 33, 41};  // XOR0x42 "Block"
    private static final byte[] S_14D02CCB = new byte[]{36, 29, 115, 114, 119, 112, 115, 114, 29};  // XOR0x42 "f_105210_"
    private static final byte[] S_58723A72 = new byte[]{43, 49, 6, 39, 49, 54, 48, 45, 59, 43, 44, 37};  // XOR0x42 "isDestroying"
    public static ScaffoldModule INSTANCE;
    public static float targetYaw;
    public static float targetPitch;
    public static boolean hasTargetRotation;
    public static boolean stopMoveActive;
    public static boolean stopMoveRestore;
    public static double freezeX;
    public static double freezeY;
    public static double freezeZ;
    public static double freezeMX;
    public static double freezeMY;
    public static double freezeMZ;
    public static float freezeFall;
    private final BooleanSetting f5 = new BooleanSetting("Auto 3rd Person", true);
    private final BooleanSetting telly = new BooleanSetting("Telly", true);
    private final BooleanSetting snap = new BooleanSetting("Snap", false);
    private final BooleanSetting safeWalk = new BooleanSetting("SafeWalk", false);
    private final NumberSetting rotateSpeed = new NumberSetting("Rotation Speed", 90.0f, 1.0f, 180.0f);
    private final NumberSetting rotateBackSpeed = new NumberSetting("Rotation Back Speed", 160.0f, 1.0f, 180.0f);
    private final NumberSetting tellyTick = new NumberSetting("Telly Ticks", 0.0f, 0.0f, 6.0f);
    private final BooleanSetting autoStuck = new BooleanSetting("AutoStuck", false);
    private final ModeSetting stuckMode = new ModeSetting("Stuck Mode", new String[]{"Stop Move", "Skip Ticks"}, "Stop Move");
    public final BooleanSetting block = new BooleanSetting("Block", false);
    private final BlockCounterDisplay blockCounterDisplay = new BlockCounterDisplay();
    private double baseY = -1.0;
    private int bigVelocityTick;
    private int morekbStuckTick;
    private int stuckCooldown;
    private boolean shouldCancelMove;
    private int airTick;
    private int yLevel;
    private BlockPos blockPos;
    private Direction enumFacing;
    private int oldSlot = -1;
    private int rotateCount;
    private boolean checkedBlock;
    private Rotation rotation;
    private boolean sentSwingThisTick;
    private boolean sentPlaceThisTick;
    private Field isDestroyingField;
    private BlockPos candidateBlockPos;
    private Direction candidateFacing;

    public ScaffoldModule() {
        super("Scaffold", Category.PLAYER);
        INSTANCE = this;
        this.values.add(this.f5);
        this.values.add(this.telly);
        this.values.add(this.snap);
        this.values.add(this.safeWalk);
        this.values.add(this.rotateSpeed);
        this.values.add(this.rotateBackSpeed);
        this.values.add(this.tellyTick);
        this.values.add(this.autoStuck);
        this.values.add(this.stuckMode);
        this.values.add(this.block);
        this.stuckMode.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)ScaffoldModule.this.autoStuck.get();
            }
        });
        this.snap.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)ScaffoldModule.this.telly.get() == false;
            }
        });
        this.safeWalk.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)ScaffoldModule.this.telly.get() == false;
            }
        });
        this.tellyTick.setVisible(new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)ScaffoldModule.this.telly.get();
            }
        });
        try {
            this.isDestroyingField = MultiPlayerGameMode.class.getDeclaredField("f_105210_");
        }
        catch (Exception e) {
            try {
                this.isDestroyingField = MultiPlayerGameMode.class.getDeclaredField("isDestroying");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.isDestroyingField != null) {
            this.isDestroyingField.setAccessible(true);
        }
    }

    private void place() {
        this.place(true);
    }

    private void place(boolean checkRotation) {
        Minecraft mc = Minecraft.getInstance();
        if (!this.onAir() || this.blockPos == null || this.enumFacing == null) {
            return;
        }
        Rotation currentRot = new Rotation(targetYaw, targetPitch);
        boolean hasRotated = RaytraceUtil2.overBlock(currentRot, this.blockPos);
        if (hasRotated || !checkRotation) {
            boolean wasDestroying = false;
            if (this.isDestroyingField != null) {
                try {
                    wasDestroying = this.isDestroyingField.getBoolean(mc.gameMode);
                    if (wasDestroying) {
                        this.isDestroyingField.setBoolean(mc.gameMode, false);
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(this.getPlaceVector(this.blockPos, this.enumFacing), this.enumFacing, this.blockPos, false));
            if (this.isDestroyingField != null && wasDestroying) {
                try {
                    this.isDestroyingField.setBoolean(mc.gameMode, true);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (result == InteractionResult.SUCCESS || result == InteractionResult.CONSUME) {
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    private boolean onAir() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 baseVec = mc.player.getEyePosition();
        BlockPos base = BlockPos.containing((double)baseVec.x, (double)this.getYLevel(), (double)baseVec.z);
        BlockState state = mc.level.getBlockState(base);
        return state.getBlock() instanceof AirBlock || state.getBlock() instanceof WaterlilyBlock;
    }

    private double evaluateBlock(Vec3 baseVec, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return -1.0;
        }
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof AirBlock) && !(state.getBlock() instanceof WaterlilyBlock)) {
            return -1.0;
        }
        if (pos.getY() > this.getYLevel()) {
            return -1.0;
        }
        Vec3 center = new Vec3((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5);
        double bestDistSq = Double.MAX_VALUE;
        Direction bestDir = null;
        for (Direction dir : Direction.values()) {
            Vec3 vec3;
            Vec3 relevant;
            double distSq;
            Vec3 hit = center.add(new Vec3((double)dir.getNormal().getX(), (double)dir.getNormal().getY(), (double)dir.getNormal().getZ()).scale(0.5));
            BlockPos baseBlockPos = pos.relative(dir);
            if (!this.isSolidAndNonInteractive(mc.level.getBlockState(baseBlockPos), baseBlockPos) || !((distSq = (relevant = hit.subtract(baseVec)).lengthSqr()) <= 20.25) || !(relevant.dot(vec3 = new Vec3((double)dir.getNormal().getX(), (double)dir.getNormal().getY(), (double)dir.getNormal().getZ())) >= 0.0) || dir.getOpposite() == Direction.UP && MoveUtil.isMoving() && this.behavesAsIfJumpReleased() || !(distSq < bestDistSq)) continue;
            bestDistSq = distSq;
            bestDir = dir.getOpposite();
            this.candidateBlockPos = baseBlockPos;
        }
        if (bestDir != null) {
            this.candidateFacing = bestDir;
            return bestDistSq;
        }
        return -1.0;
    }

    private void getBlockInfo() {
        Minecraft mc = Minecraft.getInstance();
        Vec3 baseVec = mc.player.getEyePosition();
        BlockPos base = BlockPos.containing((double)baseVec.x, (double)this.getYLevel(), (double)baseVec.z);
        int baseX = base.getX();
        int baseZ = base.getZ();
        if (this.isSolidAndNonInteractive(mc.level.getBlockState(base), base)) {
            this.checkedBlock = false;
            return;
        }
        BlockPos bestBlockPos = null;
        Direction bestFacing = null;
        double bestDistSq = Double.MAX_VALUE;
        for (int d = 0; d <= 6; ++d) {
            for (int x = -d; x <= d; ++x) {
                for (int z = -d; z <= d; ++z) {
                    if (d != 0 && Math.abs(x) != d && Math.abs(z) != d) continue;
                    for (int y = 0; y <= d; ++y) {
                        BlockPos pos = new BlockPos(baseX + x, this.getYLevel() - y, baseZ + z);
                        double distSq = this.evaluateBlock(baseVec, pos);
                        if (!(distSq >= 0.0) || !(distSq < bestDistSq)) continue;
                        bestDistSq = distSq;
                        bestBlockPos = this.candidateBlockPos;
                        bestFacing = this.candidateFacing;
                    }
                }
            }
            if (bestBlockPos != null) break;
        }
        if (bestBlockPos != null) {
            this.blockPos = bestBlockPos;
            this.enumFacing = bestFacing;
            this.checkedBlock = true;
        } else {
            this.checkedBlock = false;
        }
    }

    private boolean rescueEnabled() {
        return (Boolean)this.autoStuck.get();
    }

    public int getYLevel() {
        Minecraft mc = Minecraft.getInstance();
        if (MoveUtil.isMoving() && (double)mc.player.fallDistance <= 0.25 && ((Boolean)this.telly.get()).booleanValue()) {
            return this.yLevel;
        }
        return (int)Math.floor(mc.player.getY()) - 1;
    }

    private int findValidBlockSlot() {
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!InventoryUtil.isValidStack(stack)) continue;
            return i;
        }
        return -1;
    }

    private boolean useStopMove() {
        return (Boolean)this.autoStuck.get() != false && this.stuckMode.isMode("Stop Move");
    }

    private void refreshStop() {
        stopMoveActive = true;
    }

    private Vec3 getPlaceVector(BlockPos pos, Direction face) {
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (face != Direction.UP && face != Direction.DOWN) {
            y += 0.08;
        } else {
            x += (double) MathUtil.getRandomInRange(-0.3f, 0.3f);
            z += (double) MathUtil.getRandomInRange(-0.3f, 0.3f);
        }
        if (face == Direction.WEST || face == Direction.EAST) {
            z += (double) MathUtil.getRandomInRange(-0.3f, 0.3f);
        }
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += (double) MathUtil.getRandomInRange(-0.3f, 0.3f);
        }
        return new Vec3(x, y, z);
    }

    private void beginStop() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        freezeX = mc.player.getX();
        freezeY = mc.player.getY();
        freezeZ = mc.player.getZ();
        freezeMX = mc.player.getDeltaMovement().x;
        freezeMY = mc.player.getDeltaMovement().y;
        freezeMZ = mc.player.getDeltaMovement().z;
        freezeFall = mc.player.fallDistance;
        stopMoveActive = true;
        this.shouldCancelMove = true;
        this.morekbStuckTick = 0;
    }

    private void releaseStop() {
        if (stopMoveActive) {
            stopMoveRestore = true;
        }
        stopMoveActive = false;
        this.shouldCancelMove = false;
        this.morekbStuckTick = 0;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (this.oldSlot != -1) {
                mc.player.getInventory().selected = this.oldSlot;
            }
            if (((Boolean)this.f5.get()).booleanValue()) {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
        }
        RotationManager.clear();
        hasTargetRotation = false;
        if (mc.player != null && stopMoveActive) {
            mc.player.setNoGravity(false);
        }
        stopMoveActive = false;
        stopMoveRestore = false;
        this.shouldCancelMove = false;
        this.baseY = -1.0;
        this.bigVelocityTick = 0;
        this.morekbStuckTick = 0;
        this.stuckCooldown = 0;
    }

    @Override
    public void onEnable() {
        if (!this.blockCounterDisplay.isAnimating()) {
            this.blockCounterDisplay.reset();
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.oldSlot = mc.player.getInventory().selected;
            if (((Boolean)this.f5.get()).booleanValue()) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
        }
        this.airTick = 0;
        if (mc.player != null) {
            this.yLevel = (int)Math.floor(mc.player.getY()) - 1;
        }
        this.blockPos = null;
        this.enumFacing = null;
        this.rotateCount = 0;
        this.checkedBlock = false;
        this.rotation = null;
        hasTargetRotation = false;
        this.baseY = -1.0;
        this.bigVelocityTick = 0;
        this.morekbStuckTick = 0;
        this.stuckCooldown = 0;
        this.shouldCancelMove = false;
        stopMoveActive = false;
        stopMoveRestore = false;
    }

    public Rotation getRotation(BlockPos pos, Direction direction) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return new Rotation(0.0f, 0.0f);
        }
        if (pos == null) {
            return new Rotation(Mth.wrapDegrees((float)(mc.player.getYRot() - 180.0f)), 89.64f);
        }
        if (direction == null) {
            direction = Direction.UP;
        }
        Rotation rotations = this.onAir() ? RotationUtil.calculate(pos, direction) : RotationUtil.calculate(pos.getCenter());
        float[] yawArray = new float[]{Mth.wrapDegrees((float)(mc.player.getYRot() - 135.0f)) + MathUtil.getRandomInRange(-0.3f, 0.3f), Mth.wrapDegrees((float)(mc.player.getYRot() - 144.0f)) + MathUtil.getRandomInRange(-0.3f, 0.3f), Mth.wrapDegrees((float)(mc.player.getYRot() - 180.0f)) + MathUtil.getRandomInRange(-0.3f, 0.3f), Mth.wrapDegrees((float)(mc.player.getYRot() + 144.0f)) + MathUtil.getRandomInRange(-0.3f, 0.3f), Mth.wrapDegrees((float)(mc.player.getYRot() + 135.0f)) + MathUtil.getRandomInRange(-0.3f, 0.3f), rotations.getYaw() + MathUtil.getRandomInRange(-0.3f, 0.3f)};
        float[] pitchArray = new float[]{82.0f + MathUtil.getRandomInRange(-0.3f, 0.3f), rotations.getPitch() + MathUtil.getRandomInRange(-0.3f, 0.3f)};
        for (float pitch : pitchArray) {
            for (float yaw : yawArray) {
                Rotation rotationb = new Rotation(yaw, pitch);
                boolean hasRotated = RaytraceUtil2.overBlock(rotationb, pos);
                if (!hasRotated) continue;
                return rotationb;
            }
        }
        return rotations;
    }

    @Override
    public boolean shouldRender2DWhenDisabled() {
        return this.blockCounterDisplay.isAnimating();
    }

    boolean isBlockDisplayAnimating() {
        return this.blockCounterDisplay.isAnimating();
    }

    private boolean behavesAsIfJumpReleased() {
        return (Boolean)this.telly.get() != false || !Minecraft.getInstance().options.keyJump.isDown();
    }

    private boolean isSolidAndNonInteractive(BlockState state, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        boolean hasCollision = !state.getCollisionShape((BlockGetter)mc.level, pos).isEmpty();
        boolean hasNoMenu = state.getMenuProvider((Level)mc.level, pos) == null;
        return hasCollision && hasNoMenu;
    }

    private boolean hasSupportBelowPlayer(int maxDepth) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        AABB bb = mc.player.getBoundingBox();
        int minX = Mth.floor((double)bb.minX);
        int maxX = Mth.floor((double)bb.maxX);
        int minZ = Mth.floor((double)bb.minZ);
        int maxZ = Mth.floor((double)bb.maxZ);
        for (int i = 0; i <= maxDepth; ++i) {
            int y = Mth.floor((double)mc.player.getY()) - i;
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
                    if (state.getBlock() instanceof AirBlock || state.getBlock() instanceof LiquidBlock) continue;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        double velocity = Math.hypot(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z);
        if (velocity > 0.4 && !mc.player.onGround()) {
            this.bigVelocityTick = 3;
        } else if (this.bigVelocityTick > 0) {
            --this.bigVelocityTick;
        }
        if (!((Boolean)this.autoStuck.get()).booleanValue() || !this.stuckMode.isMode("Stop Move")) {
            this.releaseStop();
            this.stuckCooldown = 0;
            return;
        }
        if (mc.player.onGround()) {
            this.baseY = (double)((int)mc.player.getY()) - 0.2;
            this.releaseStop();
            this.bigVelocityTick = 0;
            this.stuckCooldown = 0;
            return;
        }
        if (this.baseY == -1.0) {
            this.baseY = (double)((int)mc.player.getY()) - 0.2;
        }
        boolean isBelowGroundY = mc.player.getY() < this.baseY;
        boolean falling = mc.player.getDeltaMovement().y < -0.08 && mc.player.fallDistance > 1.0f;
        boolean velocityDanger = this.bigVelocityTick > 0 && !mc.player.onGround();
        boolean hasNearSupport = this.hasSupportBelowPlayer(1);
        boolean canPlace = false;
        if (this.blockPos != null) {
            double dz;
            double dy;
            double dx = (double)this.blockPos.getX() + 0.5 - mc.player.getX();
            double distSq = dx * dx + (dy = (double)this.blockPos.getY() + 0.5 - (mc.player.getY() + (double)mc.player.getEyeHeight())) * dy + (dz = (double)this.blockPos.getZ() + 0.5 - mc.player.getZ()) * dz;
            canPlace = distSq <= 20.160100000000003;
            boolean bl = canPlace;
        }
        if (this.shouldCancelMove) {
            ++this.morekbStuckTick;
            if (hasNearSupport) {
                this.releaseStop();
                return;
            }
            if (this.morekbStuckTick >= 5 || !canPlace) {
                this.releaseStop();
                this.stuckCooldown = 6;
            } else {
                this.refreshStop();
                return;
            }
        }
        if (this.stuckCooldown > 0) {
            --this.stuckCooldown;
            return;
        }
        if (this.blockPos == null || !canPlace || hasNearSupport || !this.onAir()) {
            return;
        }
        if (velocityDanger || isBelowGroundY && falling) {
            this.beginStop();
        }
    }

    @Override
    public void onPreTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        hasTargetRotation = false;
        this.sentSwingThisTick = false;
        this.sentPlaceThisTick = false;
        int slotID = this.findValidBlockSlot();
        if (slotID != -1 && mc.player.getInventory().selected != slotID) {
            mc.player.getInventory().selected = slotID;
        }
        if (mc.player.onGround()) {
            this.yLevel = (int)Math.floor(mc.player.getY()) - 1;
        }
        this.getBlockInfo();
        if (this.rotation == null || this.onAir()) {
            this.rotation = this.getRotation(this.blockPos, this.enumFacing);
        }
        if (((Boolean)this.telly.get()).booleanValue()) {
            if (mc.player.onGround()) {
                this.airTick = 0;
                this.blockPos = null;
                this.enumFacing = null;
                Rotation targetRot = new Rotation(mc.player.getYRot(), mc.player.getXRot());
                Rotation smoothed = RotationUtil.smooth(targetRot, ((Float)this.rotateBackSpeed.get()).floatValue());
                targetYaw = smoothed.getYaw();
                targetPitch = smoothed.getPitch();
                if (Math.abs(Mth.wrapDegrees((float)(targetYaw - targetRot.getYaw()))) > 1.0f || Math.abs(targetPitch - targetRot.getPitch()) > 1.0f) {
                    hasTargetRotation = true;
                }
            } else {
                if ((float)this.airTick >= ((Float)this.tellyTick.get()).floatValue()) {
                    if (this.blockPos == null || this.enumFacing == null) {
                        ++this.airTick;
                        return;
                    }
                    boolean reachable = true;
                    if (mc.player.getDeltaMovement().y < -0.1) {
                        PlayerData fallingPlayer = new PlayerData((Player)mc.player);
                        fallingPlayer.calculate(2);
                        if ((double)this.blockPos.getY() > fallingPlayer.y) {
                            reachable = false;
                        }
                    }
                    if (this.checkedBlock && !reachable && this.rotateCount < 8 && !this.useStopMove() && this.rescueEnabled()) {
                        ++this.rotateCount;
                        ++Module.skipTicks;
                        if (mc.getConnection() != null) {
                            mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(this.rotation.getYaw(), this.rotation.getPitch(), mc.player.onGround()));
                        }
                        this.place(false);
                    } else {
                        Rotation smoothed = RotationUtil.smooth(this.rotation, ((Float)this.rotateSpeed.get()).floatValue());
                        targetYaw = smoothed.getYaw();
                        targetPitch = smoothed.getPitch();
                        hasTargetRotation = true;
                        this.place();
                        this.rotateCount = Math.max(0, this.rotateCount - 1);
                    }
                }
                ++this.airTick;
            }
        } else {
            Rotation smoothed;
            if (this.blockPos == null && this.rotation == null) {
                Rotation targetRot = new Rotation(Mth.wrapDegrees((float)(mc.player.getYRot() - 180.0f)), 89.64f);
                smoothed = RotationUtil.smooth(targetRot, ((Float)this.rotateSpeed.get()).floatValue());
                targetYaw = smoothed.getYaw();
                targetPitch = smoothed.getPitch();
                hasTargetRotation = true;
            }
            if (this.onAir() || !((Boolean)this.snap.get()).booleanValue()) {
                boolean reachable = this.blockPos != null;
                boolean bl = reachable;
                if (mc.player.getDeltaMovement().y < -0.1) {
                    PlayerData fallingPlayer = new PlayerData((Player)mc.player);
                    fallingPlayer.calculate(2);
                    if (this.blockPos != null && (double)this.blockPos.getY() > fallingPlayer.y) {
                        reachable = false;
                    }
                }
                if (this.checkedBlock && !reachable && this.rotateCount < 8 && !this.useStopMove() && this.rescueEnabled()) {
                    ++this.rotateCount;
                    ++Module.skipTicks;
                    if (mc.getConnection() != null) {
                        mc.getConnection().send((Packet)new ServerboundMovePlayerPacket.Rot(this.rotation.getYaw(), this.rotation.getPitch(), mc.player.onGround()));
                    }
                    this.place(false);
                } else if (this.rotation != null) {
                    smoothed = RotationUtil.smooth(this.rotation, ((Float)this.rotateSpeed.get()).floatValue());
                    targetYaw = smoothed.getYaw();
                    targetPitch = smoothed.getPitch();
                    hasTargetRotation = true;
                    this.place();
                    this.rotateCount = Math.max(0, this.rotateCount - 1);
                }
            }
        }
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        ClientboundSetEntityMotionPacket motion;
        if (!stopMoveActive) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        return packet instanceof ClientboundSetEntityMotionPacket && (motion = (ClientboundSetEntityMotionPacket)packet).getId() == mc.player.getId();
    }

    @Override
    public boolean onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof ServerboundSwingPacket) {
            if (this.sentSwingThisTick) {
                return true;
            }
            this.sentSwingThisTick = true;
        } else if (packet instanceof ServerboundUseItemOnPacket) {
            if (this.sentPlaceThisTick) {
                return true;
            }
            this.sentPlaceThisTick = true;
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
        int count = InventoryUtil.getBlockCountInInventory();
        ItemStack held = mc.player.getMainHandItem();
        ItemStack displayStack = InventoryUtil.isValidStack(held) ? held : InventoryUtil.getBestBlock();
        EagleModule eagle = ModuleManager.getModule(EagleModule.class);
        boolean paired = eagle != null && (eagle.enabled && (Boolean)eagle.block.get() != false || eagle.isBlockDisplayAnimating());
        this.blockCounterDisplay.render(graphics, displayStack, count, 256, paired ? 54.0f : 30.0f, this.enabled && (Boolean)this.block.get() != false);
    }

    @Override
    public void onMovementInput(Input input) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (stopMoveActive) {
            input.forwardImpulse = 0.0f;
            input.leftImpulse = 0.0f;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
            return;
        }
        if (mc.player.onGround() && !mc.options.keyJump.isDown() && MoveUtil.isMoving() && ((Boolean)this.telly.get()).booleanValue()) {
            input.jumping = true;
        }
        if (((Boolean)this.safeWalk.get()).booleanValue() && !((Boolean)this.telly.get()).booleanValue() && mc.player.onGround() && EagleModule.isOnBlockEdge(0.3f, true)) {
            input.shiftKeyDown = true;
        }
    }
}

