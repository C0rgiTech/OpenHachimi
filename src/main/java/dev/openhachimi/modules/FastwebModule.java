/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ListSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class FastwebModule
extends Module {
    private static final byte[] S_2304E538 = new byte[]{4, 35, 49, 54, 21, 39, 32};  // XOR0x42 "FastWeb"
    private static final byte[] S_5F12ADFA = new byte[]{5, 48, 45, 55, 44, 38, 98, 15, 55, 46, 54, 43, 50, 46, 43, 39, 48};  // XOR0x42 "Ground Multiplier"
    private final ListSetting groundMultiplier = new ListSetting("Ground Multiplier", 660, 0, 1000){

        @Override
        public String getDisplay() {
            return String.valueOf((double)((Integer)this.get()).intValue() / 1000.0);
        }
    };
    private int playerInWebTick = -1;
    private int lastGroundSneakTick = -100;
    private int inputTick = -1;
    private float inputForward;
    private float inputStrafe;
    private boolean inputJump;
    private boolean inputSneak;

    public FastwebModule() {
        super("FastWeb", Category.MOVEMENT);
        this.values.add(this.groundMultiplier);
    }

    private BlockState findWebState(Minecraft mc) {
        AABB box = mc.player.getBoundingBox().deflate(1.0E-7);
        int minX = Mth.floor((double)box.minX);
        int maxX = Mth.floor((double)box.maxX);
        int minY = Mth.floor((double)box.minY);
        int maxY = Mth.floor((double)box.maxY);
        int minZ = Mth.floor((double)box.minZ);
        int maxZ = Mth.floor((double)box.maxZ);
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockState state = mc.level.getBlockState(new BlockPos(x, y, z));
                    if (!state.is(Blocks.COBWEB)) continue;
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        this.playerInWebTick = -1;
        this.inputTick = -1;
        this.lastGroundSneakTick = -100;
    }

    @Override
    public void onTick() {
        double motionZ;
        double motionX;
        int tick;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        BlockState webState = this.findWebState(mc);
        if (webState == null) {
            return;
        }
        this.playerInWebTick = tick = mc.player.tickCount;
        Vec3 baseMovement = mc.player.getDeltaMovement();
        boolean onGround = mc.player.onGround();
        if (onGround) {
            double multiplier = (double)((Integer)this.groundMultiplier.get()).intValue() / 1000.0;
            double horizontal = 0.25 + 0.75 * multiplier;
            mc.player.makeStuckInBlock(webState, new Vec3(horizontal, 0.05, horizontal));
        } else {
            mc.player.makeStuckInBlock(webState, new Vec3(1.0, 1.0, 1.0));
        }
        float forward = 0.0f;
        float strafe = 0.0f;
        boolean jump = false;
        boolean sneak = false;
        if (mc.player.input != null) {
            forward = mc.player.input.forwardImpulse;
            strafe = mc.player.input.leftImpulse;
            jump = mc.player.input.jumping;
            sneak = mc.player.input.shiftKeyDown;
        } else if (this.inputTick == tick && this.playerInWebTick == tick) {
            forward = this.inputForward;
            strafe = this.inputStrafe;
            jump = this.inputJump;
            sneak = this.inputSneak;
        }
        if (forward != 0.0f && strafe != 0.0f) {
            forward *= 0.707f;
            strafe *= 0.707f;
        }
        if (onGround) {
            motionX = baseMovement.x;
            motionZ = baseMovement.z;
        } else {
            motionX = 0.0;
            motionZ = 0.0;
            if (forward != 0.0f || strafe != 0.0f) {
                double speed = 0.14122;
                double yaw = Math.toRadians(mc.player.getYRot());
                motionX = (-Math.sin(yaw) * (double)forward + Math.cos(yaw) * (double)strafe) * speed;
                motionZ = (Math.cos(yaw) * (double)forward + Math.sin(yaw) * (double)strafe) * speed;
            }
        }
        double motionY = onGround ? baseMovement.y : (jump && sneak ? 0.0 : (jump ? 0.06222 : (sneak ? -0.18777 : 0.0)));
        mc.player.setDeltaMovement(motionX, motionY, motionZ);
        mc.player.fallDistance = 0.0f;
    }

    @Override
    public void onMovementInput(Input input) {
        int tick;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || this.findWebState(mc) == null) {
            return;
        }
        this.playerInWebTick = tick = mc.player.tickCount;
        this.inputTick = tick;
        this.inputForward = input.forwardImpulse;
        this.inputStrafe = input.leftImpulse;
        this.inputJump = input.jumping;
        this.inputSneak = input.shiftKeyDown;
        if (!mc.player.onGround()) {
            return;
        }
        if (input.shiftKeyDown) {
            this.lastGroundSneakTick = tick;
        }
        if (input.shiftKeyDown || tick - this.lastGroundSneakTick <= 1) {
            input.jumping = false;
            this.inputJump = false;
        }
    }
}

