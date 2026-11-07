/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;

public class JumpresetModule
extends Module {
    private static final byte[] S_1F8AC5A1 = new byte[]{8, 55, 47, 50, 16, 39, 49, 39, 54};  // XOR0x42 "JumpReset"
    private static final byte[] S_783BFF54 = new byte[]{1, 42, 35, 44, 33, 39};  // XOR0x42 "Chance"
    private static final byte[] S_4164A2AB = new byte[]{3, 33, 33, 55, 48, 35, 33, 59, 98, 15, 43, 44};  // XOR0x42 "Accuracy Min"
    private static final byte[] S_4164A1BD = new byte[]{3, 33, 33, 55, 48, 35, 33, 59, 98, 15, 35, 58};  // XOR0x42 "Accuracy Max"
    private static final byte[] S_E8C221F = new byte[]{13, 44, 46, 59, 98, 53, 42, 39, 44, 98, 54, 35, 48, 37, 39, 54, 43, 44, 37};  // XOR0x42 "Only when targeting"
    private static final byte[] S_1F2AF89F = new byte[]{21, 35, 54, 39, 48, 98, 33, 42, 39, 33, 41};  // XOR0x42 "Water check"
    private final ListSetting chance = new ListSetting("Chance", 40, 0, 100);
    private final ListSetting accuracyMin = new ListSetting("Accuracy Min", 40, 0, 100);
    private final ListSetting accuracyMax = new ListSetting("Accuracy Max", 60, 0, 100);
    private final BooleanSetting onlyWhenTargeting = new BooleanSetting("Only when targeting", false);
    private final BooleanSetting waterCheck = new BooleanSetting("Water check", false);
    private final Random random = new Random();
    private double motionX;
    private double motionY;
    private double motionZ;
    private boolean waitingForReset;
    private boolean shouldJump;
    private boolean jumping;

    public JumpresetModule() {
        super("JumpReset", Category.COMBAT);
        this.values.add(this.chance);
        this.values.add(this.accuracyMin);
        this.values.add(this.accuracyMax);
        this.values.add(this.onlyWhenTargeting);
        this.values.add(this.waterCheck);
    }

    private static double roundToScale(double value) {
        return (double)Math.round(value * 1000.0) / 1000.0;
    }

    private boolean shouldReduce(Minecraft mc) {
        if (this.jumping) {
            return false;
        }
        if (mc.player.hasEffect(MobEffects.JUMP)) {
            return false;
        }
        if (((Boolean)this.waterCheck.get()).booleanValue() && mc.player.isInWater()) {
            return false;
        }
        if (!(!((Boolean)this.onlyWhenTargeting.get()).booleanValue() || mc.crosshairPickEntity instanceof LivingEntity && (double)mc.crosshairPickEntity.distanceTo((Entity)mc.player) <= 5.0)) {
            return false;
        }
        int roll = this.random.nextInt(100);
        return (double)roll >= 100.0 - (double)((Integer)this.chance.get()).intValue();
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (this.jumping && mc.player != null) {
            mc.options.keyJump.setDown(false);
        }
        this.jumping = false;
        this.shouldJump = false;
        this.waitingForReset = false;
        this.motionX = 0.0;
        this.motionY = 0.0;
        this.motionZ = 0.0;
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.player != mc.player) {
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            if (!this.shouldJump) {
                return;
            }
            double accuracy = (double)((Integer)this.accuracyMin.get()).intValue() + ((double)((Integer)this.accuracyMax.get()).intValue() - (double)((Integer)this.accuracyMin.get()).intValue()) * this.random.nextDouble();
            float roll = this.random.nextInt(100);
            if ((double)roll < 100.0 - accuracy) {
                return;
            }
            mc.options.keyJump.setDown(true);
            this.jumping = true;
            this.shouldJump = false;
        } else if (event.phase == TickEvent.Phase.END && this.jumping) {
            mc.options.keyJump.setDown(false);
            this.jumping = false;
        }
    }

    @Override
    public void onPreTick() {
        if (!this.waitingForReset) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            this.waitingForReset = false;
            return;
        }
        double posX = JumpresetModule.roundToScale(mc.player.getDeltaMovement().x);
        double posY = JumpresetModule.roundToScale(mc.player.getDeltaMovement().y);
        double posZ = JumpresetModule.roundToScale(mc.player.getDeltaMovement().z);
        double expectedX = JumpresetModule.roundToScale(this.motionX);
        double expectedY = JumpresetModule.roundToScale(this.motionY);
        double expectedZ = JumpresetModule.roundToScale(this.motionZ);
        if (posX == expectedX && posY == expectedY && posZ == expectedZ) {
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
            this.waitingForReset = false;
            if (!mc.options.keyJump.isDown()) {
                this.shouldJump = true;
            }
        }
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        boolean unsuitableVelocity;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (!(packet instanceof ClientboundSetEntityMotionPacket)) {
            return false;
        }
        ClientboundSetEntityMotionPacket velocityPacket = (ClientboundSetEntityMotionPacket)packet;
        if (velocityPacket.getId() != mc.player.getId()) {
            return false;
        }
        double motionX = (double)velocityPacket.getXa() / 8000.0;
        double motionY = (double)velocityPacket.getYa() / 8000.0;
        double motionZ = (double)velocityPacket.getZa() / 8000.0;
        boolean bl = unsuitableVelocity = motionX == 0.0 && motionZ == 0.0 || motionY < 0.0;
        if (!unsuitableVelocity && this.shouldReduce(mc)) {
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            this.waitingForReset = true;
        }
        return false;
    }
}

