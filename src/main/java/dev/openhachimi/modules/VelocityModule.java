/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.player.RaytraceUtil2;
import dev.openhachimi.utils.rotation.RotationUtil;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;

public class VelocityModule
extends Module {
    private static final byte[] S_310A0FD = new byte[]{20, 39, 46, 45, 33, 43, 54, 59};  // XOR0x42 "Velocity"
    private static final byte[] S_20D759FC = new byte[]{3, 54, 54, 35, 33, 41, 98, 33, 45, 55, 44, 54, 49};  // XOR0x42 "Attack counts"
    public static VelocityModule INSTANCE;
    public static float targetYaw;
    public static float targetPitch;
    public static boolean hasTargetRotation;
    public static int attackQueue;
    public static boolean delay;
    public static boolean jump;
    public static boolean receiveDamage;
    private final ListSetting counts = new ListSetting("Attack counts", 3, 1, 5);
    private final Queue<Packet<?>> packets = new ConcurrentLinkedQueue();
    private long lag;
    private float yaw;

    public VelocityModule() {
        super("Velocity", Category.COMBAT);
        INSTANCE = this;
        this.values.add(this.counts);
    }

    private void flush() {
        Minecraft mc = Minecraft.getInstance();
        try {
            if (!delay) {
                return;
            }
            while (!this.packets.isEmpty()) {
                Packet<?> raw = this.packets.poll();
                if (raw == null || mc.getConnection() == null) continue;
                ((Packet)raw).handle(mc.getConnection());
            }
            delay = false;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (KillauraModule.INSTANCE != null && KillauraModule.INSTANCE.enabled) {
            return KillauraModule.INSTANCE.isValidTarget(entity);
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (entity == Minecraft.getInstance().player) {
            return false;
        }
        if (AntibotModule.isBot(entity)) {
            return false;
        }
        if (TeamsModule.isSameTeam(entity)) {
            return false;
        }
        return entity instanceof Player;
    }

    private static float yawFromMotion(int motionX, int motionZ) {
        float yaw = (float)(Math.toDegrees(Math.atan2(motionZ, motionX)) + 90.0);
        return Mth.wrapDegrees((float)yaw);
    }

    @Override
    public void onDisable() {
        delay = false;
        this.packets.clear();
        attackQueue = 0;
        jump = false;
        hasTargetRotation = false;
        receiveDamage = false;
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (attackQueue >= 1 && mc.player.getAttackStrengthScale(0.5f) >= 0.9f) {
            LivingEntity living;
            EntityHitResult entityHitResult;
            Entity entity;
            HitResult hitResult;
            Rotation rot = new Rotation(mc.player.getYRot(), mc.player.getXRot());
            if (hasTargetRotation) {
                rot = new Rotation(targetYaw, targetPitch);
            }
            if ((hitResult = RaytraceUtil2.rayCast(rot, 3.0)) instanceof EntityHitResult && (entity = (entityHitResult = (EntityHitResult)hitResult).getEntity()) instanceof LivingEntity && this.isValidTarget((Entity)(living = (LivingEntity)entity))) {
                mc.player.setSprinting(EventRegistrar.lastSprint && mc.player.isSprinting());
                mc.gameMode.attack((Player)mc.player, (Entity)living);
            }
            --attackQueue;
        }
    }

    @Override
    public void onPreTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        hasTargetRotation = false;
        if (delay && mc.player.onGround() && mc.player.isSprinting()) {
            this.flush();
            if (receiveDamage) {
                receiveDamage = false;
                attackQueue = (Integer)this.counts.get();
                jump = true;
            }
        }
        if (jump) {
            Rotation rot = new Rotation(this.yaw, mc.player.getXRot());
            rot = RotationUtil.smooth(rot, 180.0);
            targetYaw = rot.getYaw();
            targetPitch = rot.getPitch();
            hasTargetRotation = true;
        }
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        ClientboundSetEntityMotionPacket motionPacket;
        boolean selfMotion;
        ClientboundDamageEventPacket damagePacket;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        if (packet instanceof ClientboundDamageEventPacket && (damagePacket = (ClientboundDamageEventPacket)packet).entityId() == mc.player.getId()) {
            receiveDamage = true;
        }
        boolean cancelled = false;
        boolean bl = selfMotion = packet instanceof ClientboundSetEntityMotionPacket && ((ClientboundSetEntityMotionPacket)packet).getId() == mc.player.getId();
        if (delay && (selfMotion || packet instanceof ClientboundPingPacket || packet instanceof ClientboundPlayerLookAtPacket)) {
            this.packets.add(packet);
            cancelled = true;
        }
        if (packet instanceof ClientboundPlayerPositionPacket) {
            this.lag = System.currentTimeMillis();
            if (delay) {
                delay = false;
                this.packets.clear();
            }
        }
        if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
            this.onDisable();
        }
        if (packet instanceof ClientboundSetEntityMotionPacket && (motionPacket = (ClientboundSetEntityMotionPacket)packet).getId() == mc.player.getId() && System.currentTimeMillis() - this.lag >= 100L) {
            this.yaw = VelocityModule.yawFromMotion(motionPacket.getXa(), motionPacket.getZa());
            if (!mc.player.onGround() || !mc.player.isSprinting()) {
                if (!delay) {
                    delay = true;
                    if (!cancelled) {
                        this.packets.add(packet);
                        cancelled = true;
                    }
                }
            } else if (receiveDamage) {
                receiveDamage = false;
                attackQueue = (Integer)this.counts.get();
                jump = true;
            }
        }
        return cancelled;
    }

    @Override
    public void onMovementInput(Input input) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (jump) {
            jump = false;
            if (mc.player.onGround()) {
                input.forwardImpulse = 1.0f;
                input.leftImpulse = 0.0f;
                input.jumping = true;
            }
        } else if (delay && mc.player.onGround()) {
            input.forwardImpulse = 1.0f;
            input.leftImpulse = 0.0f;
            input.jumping = false;
            if (!AutosoupModule.SPRINT_LOCK) {
                mc.player.setSprinting(true);
            }
        }
    }
}

