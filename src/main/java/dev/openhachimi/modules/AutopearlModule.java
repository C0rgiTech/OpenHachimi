/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.EnumSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.rotation.RotationUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class AutopearlModule
extends Module {
    private static final byte[] S_599D33B7 = new byte[]{3, 55, 54, 45, 18, 39, 35, 48, 46};  // XOR0x42 "AutoPearl"
    private static final byte[] S_3E62DDC4 = new byte[]{18, 48, 43, 45, 48, 43, 54, 59};  // XOR0x42 "Priority"
    private static final byte[] S_99EE7C = new byte[]{10, 39, 35, 46, 54, 42};  // XOR0x42 "Health"
    private static final byte[] S_1148D = new byte[]{4, 45, 20};  // XOR0x42 "FoV"
    private static final byte[] S_4B1491D = new byte[]{16, 35, 44, 37, 39};  // XOR0x42 "Range"
    private static final byte[] S_3ABB13B7 = new byte[]{22, 48, 43, 37, 37, 39, 48, 98, 9, 39, 59};  // XOR0x42 "Trigger Key"
    private static final byte[] S_76F374D8 = new byte[]{15, 35, 58, 98, 10, 39, 35, 46, 54, 42};  // XOR0x42 "Max Health"
    private static final byte[] S_110AD = new byte[]{4, 13, 20};  // XOR0x42 "FOV"
    private static final byte[] S_1F94AB5 = new byte[]{3, 43, 47, 98, 18, 45, 43, 44, 54};  // XOR0x42 "Aim Point"
    private static final byte[] S_40FEE09 = new byte[]{4, 48, 45, 44, 54};  // XOR0x42 "Front"
    private static final byte[] S_1F7907 = new byte[]{0, 35, 33, 41};  // XOR0x42 "Back"
    private static final byte[] S_500BC83E = new byte[]{12, 39, 35, 48, 39, 49, 54};  // XOR0x42 "Nearest"
    private static final byte[] S_223F56C = new byte[]{3, 43, 47, 98, 17, 50, 39, 39, 38};  // XOR0x42 "Aim Speed"
    private static final byte[] S_139C8075 = new byte[]{17, 43, 46, 39, 44, 54};  // XOR0x42 "Silent"
    private static final byte[] S_28197B54 = new byte[]{17, 53, 35, 50, 98, 0, 35, 33, 41};  // XOR0x42 "Swap Back"
    private static final byte[] S_3EDC963 = new byte[]{6, 39, 46, 35, 59};  // XOR0x42 "Delay"
    private static final byte[] S_4DC949AB = new byte[]{3, 54, 54, 35, 33, 41, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Attack Delay"
    private static final byte[] S_1D1DE017 = new byte[]{32, 46, 45, 33, 41, 17, 54, 35, 54, 39, 18, 48, 39, 38, 43, 33, 54, 43, 45, 44, 10, 35, 44, 38, 46, 39, 48};  // XOR0x42 "blockStatePredictionHandler"
    private static final byte[] S_4EBA27A5 = new byte[]{36, 29, 112, 113, 113, 119, 113, 115, 29};  // XOR0x42 "f_233531_"
    private static final byte[] S_62675549 = new byte[]{39, 44, 49, 55, 48, 39, 10, 35, 49, 17, 39, 44, 54, 1, 35, 48, 48, 43, 39, 38, 11, 54, 39, 47};  // XOR0x42 "ensureHasSentCarriedItem"
    private static final byte[] S_22AFBD34 = new byte[]{47, 29, 115, 114, 119, 113, 116, 112, 29};  // XOR0x42 "m_105362_"
    public static AutopearlModule INSTANCE;
    public static float targetYaw;
    public static float targetPitch;
    public static boolean hasTargetRotation;
    private static final double PEARL_SPEED = 1.5;
    private static final double PEARL_DRAG = 0.99;
    private static final double PEARL_GRAVITY = 0.03;
    private static final float TOLERANCE = 0.6f;
    private static final double AIM_OFFSET = 0.45;
    private final ModeSetting priority = new ModeSetting("Priority", new String[]{"Health", "FoV", "Range"}, "Health");
    private final EnumSetting triggerKey = new EnumSetting("Trigger Key", 0);
    private final NumberSetting maxHealth = new NumberSetting("Max Health", 20.0f, 1.0f, 20.0f);
    private final ListSetting fov = new ListSetting("FOV", 180, 10, 360);
    private final NumberSetting range = new NumberSetting("Range", 24.0f, 3.0f, 64.0f);
    private final ModeSetting aimPoint = new ModeSetting("Aim Point", new String[]{"Front", "Back", "Nearest"}, "Nearest");
    private final NumberSetting aimSpeed = new NumberSetting("Aim Speed", 6.0f, 1.0f, 10.0f);
    private final BooleanSetting silent = new BooleanSetting("Silent", true);
    private final BooleanSetting swapBack = new BooleanSetting("Swap Back", true);
    private final ListSetting delay = new ListSetting("Delay", 10, 0, 60);
    private final ListSetting attackDelay = new ListSetting("Attack Delay", 3, 0, 10);
    private int stage;
    private int ticks;
    private int lastAttackTick;
    private int reactionTicks;
    private int cooldown;
    private int waitTicks;
    private int pearlSlot = -1;
    private int previousSlot = -1;
    private LivingEntity target;
    private float solveYaw;
    private float solvePitch;
    private static Method ensureHasSentCarriedItemMethod;
    private static boolean ensureHasSentCarriedItemResolved;
    private static Field predictionHandlerField;
    private static boolean predictionHandlerResolved;

    public AutopearlModule() {
        super("AutoPearl", Category.MISC);
        INSTANCE = this;
        this.values.add(this.triggerKey);
        this.values.add(this.priority);
        this.values.add(this.maxHealth);
        this.values.add(this.fov);
        this.values.add(this.range);
        this.values.add(this.aimPoint);
        this.values.add(this.aimSpeed);
        this.values.add(this.silent);
        this.values.add(this.swapBack);
        this.values.add(this.delay);
        this.values.add(this.attackDelay);
    }

    private void trigger() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return;
        }
        if (this.stage != 0 || this.cooldown > 0) {
            return;
        }
        LivingEntity found = this.findTarget(mc);
        if (found == null) {
            return;
        }
        Vec3 point = this.resolveAimPoint(mc, found);
        if (point == null || !this.solve(mc, point)) {
            return;
        }
        this.target = found;
        this.stage = 1;
        this.reactionTicks = 2 + (int)(Math.random() * 4.0);
    }

    private void reset() {
        this.stage = 0;
        this.reactionTicks = 0;
        this.waitTicks = 0;
        this.pearlSlot = -1;
        this.previousSlot = -1;
        this.target = null;
        hasTargetRotation = false;
    }

    private boolean valid(Minecraft mc) {
        LivingEntity living = this.target;
        if (living == null || !living.isAlive() || living.isRemoved()) {
            return false;
        }
        if (mc.player.distanceTo((Entity)living) > ((Float)this.range.get()).floatValue() + 8.0f) {
            return false;
        }
        return this.findPearlSlot(mc) != -1;
    }

    private void cancel(Minecraft mc) {
        this.restoreSlot(mc);
        this.reset();
        this.cooldown = 4;
    }

    private LivingEntity findTarget(Minecraft mc) {
        double maxRange = ((Float)this.range.get()).floatValue();
        float fovValue = ((Integer)this.fov.get()).intValue();
        float healthCap = ((Float)this.maxHealth.get()).floatValue();
        ArrayList<Player> candidates = new ArrayList<Player>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            Rotation angles;
            Vec3 point;
            double distance;
            Player player;
            if (!(entity instanceof Player) || (player = (Player)entity) == mc.player || !player.isAlive() || player.isRemoved() || AntibotModule.isBot((Entity)player) || TeamsModule.isSameTeam((Entity)player) || (distance = (double)mc.player.distanceTo((Entity)player)) > maxRange || distance < 1.5 || player.getHealth() > healthCap || (point = this.resolveAimPoint(mc, (LivingEntity)player)) == null || Math.abs(Mth.wrapDegrees((float)((angles = RotationUtil.calculate(point)).getYaw() - mc.player.getYRot()))) > fovValue) continue;
            candidates.add(player);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (this.priority.isMode("Health")) {
            candidates.sort((first, second) -> Double.compare(first.getHealth(), second.getHealth()));
        } else if (this.priority.isMode("Range")) {
            candidates.sort((first, second) -> Double.compare(mc.player.distanceTo((Entity)first), mc.player.distanceTo((Entity)second)));
        } else {
            candidates.sort((first, second) -> Double.compare(this.angleScore(mc, (LivingEntity)first), this.angleScore(mc, (LivingEntity)second)));
        }
        return (LivingEntity)candidates.get(0);
    }

    private void applyRotation(Minecraft mc, float yaw, float pitch) {
        float clamped = Mth.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        if (((Boolean)this.silent.get()).booleanValue()) {
            Rotation fixed = RotationUtil.applySensitivityPatch(new Rotation(yaw, clamped));
            targetYaw = fixed.getYaw();
            targetPitch = Mth.clamp((float)fixed.getPitch(), (float)-90.0f, (float)90.0f);
            hasTargetRotation = true;
            return;
        }
        float stepYaw = Mth.wrapDegrees((float)(yaw - mc.player.getYRot()));
        float stepPitch = clamped - mc.player.getXRot();
        if (stepYaw != 0.0f) {
            mc.player.setYRot(mc.player.getYRot() + stepYaw);
            mc.player.setYHeadRot(mc.player.getYRot());
            mc.player.yRotO += stepYaw;
        }
        if (stepPitch != 0.0f) {
            mc.player.setXRot(Mth.clamp((float)(mc.player.getXRot() + stepPitch), (float)-90.0f, (float)90.0f));
            mc.player.xRotO += stepPitch;
        }
    }

    private float solvePitch(double distance, double heightDifference) {
        double[] tolerances;
        for (double tolerance : tolerances = new double[]{0.08, 0.2, 0.45}) {
            for (double pitch = 89.0; pitch >= -89.0; pitch -= 0.25) {
                if (!(Math.abs(this.heightAt(distance, pitch) - heightDifference) < tolerance)) continue;
                return (float)pitch;
            }
        }
        return Float.NaN;
    }

    private double angleScore(Minecraft mc, LivingEntity entity) {
        Vec3 point = this.resolveAimPoint(mc, entity);
        if (point == null) {
            return Double.MAX_VALUE;
        }
        Rotation angles = RotationUtil.calculate(point);
        double yaw = Mth.wrapDegrees((float)(angles.getYaw() - mc.player.getYRot()));
        double pitch = angles.getPitch() - mc.player.getXRot();
        return Math.sqrt(yaw * yaw + pitch * pitch);
    }

    private void restoreSlot(Minecraft mc) {
        if (mc.player == null) {
            this.previousSlot = -1;
            return;
        }
        int previous = this.previousSlot;
        this.previousSlot = -1;
        if (previous == -1 || !((Boolean)this.swapBack.get()).booleanValue()) {
            return;
        }
        if (mc.player.getInventory().selected != this.pearlSlot) {
            return;
        }
        mc.player.getInventory().selected = previous;
        this.sendCarriedItem(mc);
    }

    private Vec3 resolveAimPoint(Minecraft mc, LivingEntity living) {
        AABB box = living.getBoundingBox();
        double middle = living.getY() + (double)living.getBbHeight() * 0.55;
        if (this.aimPoint.isMode("Front") || this.aimPoint.isMode("Back")) {
            Vec3 look = living.getLookAngle();
            double length = Math.hypot(look.x, look.z);
            if (length < 0.001) {
                return null;
            }
            double sign = this.aimPoint.isMode("Front") ? 1.0 : -1.0;
            double offsetX = look.x / length * 0.45 * sign;
            double offsetZ = look.z / length * 0.45 * sign;
            return new Vec3(living.getX() + offsetX, middle, living.getZ() + offsetZ);
        }
        Vec3 eye = mc.player.getEyePosition();
        double x = Mth.clamp((double)eye.x, (double)box.minX, (double)box.maxX);
        double y = Mth.clamp((double)eye.y, (double)box.minY, (double)box.maxY);
        double z = Mth.clamp((double)eye.z, (double)box.minZ, (double)box.maxZ);
        return new Vec3(x, y, z);
    }

    private void tickWait(Minecraft mc) {
        if (--this.waitTicks > 0) {
            return;
        }
        this.restoreSlot(mc);
        this.cooldown = (Integer)this.delay.get();
        this.reset();
    }

    private boolean trajectoryClear(Minecraft mc, Vec3 eye, float yaw, float pitch, Vec3 point, double targetDistance) {
        double yawRadians = Math.toRadians(yaw);
        double pitchRadians = Math.toRadians(pitch);
        double velocityX = -Math.sin(yawRadians) * Math.cos(pitchRadians) * 1.5;
        double velocityY = -Math.sin(pitchRadians) * 1.5;
        double velocityZ = Math.cos(yawRadians) * Math.cos(pitchRadians) * 1.5;
        double x = eye.x;
        double y = eye.y;
        double z = eye.z;
        for (int i = 0; i < 200; ++i) {
            Vec3 start = new Vec3(x, y, z);
            Vec3 end = new Vec3(x + velocityX, y + velocityY, z + velocityZ);
            BlockHitResult hit = mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player));
            if (hit.getType() != HitResult.Type.MISS) {
                double hitDistance = Math.hypot(hit.getLocation().x - eye.x, hit.getLocation().z - eye.z);
                return hitDistance >= targetDistance - 0.6;
            }
            x = end.x;
            y = end.y;
            z = end.z;
            if (y < (double)mc.level.getMinBuildHeight()) {
                return false;
            }
            velocityX *= 0.99;
            velocityY = velocityY * 0.99 - 0.03;
            velocityZ *= 0.99;
            if (!(Math.hypot(x - eye.x, z - eye.z) >= targetDistance)) continue;
            return true;
        }
        return false;
    }

    private boolean solve(Minecraft mc, Vec3 point) {
        Vec3 eye = mc.player.getEyePosition();
        double deltaX = point.x - eye.x;
        double deltaZ = point.z - eye.z;
        double distance = Math.hypot(deltaX, deltaZ);
        if (distance < 0.4) {
            return false;
        }
        float yaw = (float)(Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float pitch = this.solvePitch(distance, point.y - eye.y);
        if (Float.isNaN(pitch)) {
            return false;
        }
        if (!this.trajectoryClear(mc, eye, yaw, pitch, point, distance)) {
            return false;
        }
        this.solveYaw = Mth.wrapDegrees((float)yaw);
        this.solvePitch = Mth.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        return true;
    }

    private void sendCarriedItem(Minecraft mc) {
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (gameMode == null) {
            return;
        }
        Method method = AutopearlModule.resolveEnsureHasSentCarriedItem(gameMode);
        if (method == null) {
            return;
        }
        try {
            method.invoke((Object)gameMode, new Object[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void tickAim(Minecraft mc) {
        if (!this.valid(mc)) {
            this.cancel(mc);
            return;
        }
        Vec3 point = this.resolveAimPoint(mc, this.target);
        if (point == null || !this.solve(mc, point)) {
            this.cancel(mc);
            return;
        }
        if (this.reactionTicks > 0) {
            --this.reactionTicks;
            return;
        }
        if (!this.rotateToward(mc, this.solveYaw, this.solvePitch)) {
            return;
        }
        if (this.combatBusy(mc)) {
            return;
        }
        int slot = this.findPearlSlot(mc);
        if (slot == -1) {
            this.cancel(mc);
            return;
        }
        this.pearlSlot = slot;
        this.previousSlot = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;
        this.sendCarriedItem(mc);
        this.stage = 2;
        this.waitTicks = 1;
    }

    private int findPearlSlot(Minecraft mc) {
        if (mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (mc.player.getInventory().getItem(i).getItem() != Items.ENDER_PEARL) continue;
            return i;
        }
        return -1;
    }

    private double heightAt(double distance, double pitch) {
        double radians = Math.toRadians(pitch);
        double cosine = Math.cos(radians);
        if (cosine <= 1.0E-4) {
            return Double.NaN;
        }
        double velocityX = 1.5 * cosine;
        double velocityY = -1.5 * Math.sin(radians);
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < 300; ++i) {
            double nextX = x + velocityX;
            double nextY = y + velocityY;
            if (nextX >= distance) {
                double ratio = (distance - x) / velocityX;
                return y + (nextY - y) * ratio;
            }
            x = nextX;
            y = nextY;
            velocityX *= 0.99;
            velocityY = velocityY * 0.99 - 0.03;
            if (!(y < -128.0)) continue;
            return -128.0;
        }
        return -128.0;
    }

    private boolean combatBusy(Minecraft mc) {
        int window = (Integer)this.attackDelay.get();
        if (window > 0 && this.ticks - this.lastAttackTick < window) {
            return true;
        }
        return mc.options.keyAttack.isDown() && mc.player.getAttackStrengthScale(0.5f) > 0.85f;
    }

    private boolean rotateToward(Minecraft mc, float yaw, float pitch) {
        float deltaPitch;
        Rotation last = this.currentRotation(mc);
        float deltaYaw = Mth.wrapDegrees((float)(yaw - last.getYaw()));
        double distance = Math.hypot(deltaYaw, deltaPitch = pitch - last.getPitch());
        if (distance <= (double)0.6f) {
            this.applyRotation(mc, yaw, pitch);
            return true;
        }
        double speed = ((Float)this.aimSpeed.get()).floatValue();
        double normalized = Mth.clamp((double)(distance / 90.0), (double)0.0, (double)1.0);
        double factor = speed * (0.28 + 0.72 * Math.sin(normalized * Math.PI * 0.5));
        double maxStep = Math.max(0.3, factor *= 0.82 + Math.random() * 0.36);
        float stepYaw = (float)Mth.clamp((double)deltaYaw, (double)(-maxStep), (double)maxStep);
        float stepPitch = (float)Mth.clamp((double)deltaPitch, (double)(-maxStep * 0.6), (double)(maxStep * 0.6));
        float nextYaw = last.getYaw() + stepYaw + (float)((Math.random() - 0.5) * 0.12);
        float nextPitch = last.getPitch() + stepPitch - (float)(Math.random() * 0.08);
        this.applyRotation(mc, nextYaw, nextPitch);
        return false;
    }

    private void tickThrow(Minecraft mc) {
        if (--this.waitTicks > 0) {
            return;
        }
        if (mc.player.getInventory().selected != this.pearlSlot || mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) {
            this.cancel(mc);
            return;
        }
        this.applyRotation(mc, this.solveYaw, this.solvePitch);
        this.sendPredictiveUseItem(mc);
        mc.player.swing(InteractionHand.MAIN_HAND);
        this.stage = 3;
        this.waitTicks = 2;
    }

    private Rotation currentRotation(Minecraft mc) {
        if (((Boolean)this.silent.get()).booleanValue() && hasTargetRotation) {
            return new Rotation(targetYaw, targetPitch);
        }
        return new Rotation(mc.player.getYRot(), mc.player.getXRot());
    }

    private static Method findNoArgMethod(Class<?> clazz, String name) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(name, new Class[0]);
                method.setAccessible(true);
                return method;
            }
            catch (NoSuchMethodException noSuchMethodException) {
                continue;
            }
        }
        return null;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        this.restoreSlot(mc);
        this.reset();
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    private static Method resolveEnsureHasSentCarriedItem(MultiPlayerGameMode gameMode) {
        if (ensureHasSentCarriedItemResolved) {
            return ensureHasSentCarriedItemMethod;
        }
        ensureHasSentCarriedItemResolved = true;
        Class<?> clazz = gameMode.getClass();
        Method found = AutopearlModule.findNoArgMethod(clazz, "ensureHasSentCarriedItem");
        if (found == null) {
            found = AutopearlModule.findNoArgMethod(clazz, "m_105362_");
        }
        ensureHasSentCarriedItemMethod = found;
        return found;
    }

    private static Field resolvePredictionHandlerField() {
        if (predictionHandlerResolved) {
            return predictionHandlerField;
        }
        predictionHandlerResolved = true;
        try {
            Field field = ClientLevel.class.getDeclaredField("blockStatePredictionHandler");
            field.setAccessible(true);
            predictionHandlerField = field;
            return field;
        }
        catch (Exception exception) {
            try {
                Field field = ClientLevel.class.getDeclaredField("f_233531_");
                field.setAccessible(true);
                predictionHandlerField = field;
                return field;
            }
            catch (Exception exception2) {
                Field[] fields = ClientLevel.class.getDeclaredFields();
                for (int i = 0; i < fields.length; ++i) {
                    Field candidate = fields[i];
                    if (!BlockStatePredictionHandler.class.isAssignableFrom(candidate.getType())) continue;
                    candidate.setAccessible(true);
                    predictionHandlerField = candidate;
                    return candidate;
                }
                return null;
            }
        }
    }

    private void sendPredictiveUseItem(Minecraft mc) {
        if (mc.getConnection() == null || mc.level == null || mc.player == null) {
            return;
        }
        try {
            Field field = AutopearlModule.resolvePredictionHandlerField();
            if (field != null) {
                BlockStatePredictionHandler predictionHandler = (BlockStatePredictionHandler)field.get(mc.level);
                try (BlockStatePredictionHandler predicting = predictionHandler.startPredicting();){
                    int sequence = predicting.currentSequence();
                    mc.getConnection().send((Packet)new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence));
                    return;
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (mc.gameMode != null) {
            mc.gameMode.useItem((Player)mc.player, InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void onPreTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            this.reset();
            return;
        }
        ++this.ticks;
        if (this.cooldown > 0) {
            --this.cooldown;
        }
        if (this.stage == 0) {
            return;
        }
        if (this.stage == 1) {
            this.tickAim(mc);
            return;
        }
        if (this.stage == 2) {
            this.tickThrow(mc);
            return;
        }
        this.tickWait(mc);
    }

    @Override
    public void onWorldChange() {
        Minecraft mc = Minecraft.getInstance();
        this.restoreSlot(mc);
        this.reset();
    }

    @Override
    public void onKeyPressed(int keyCode) {
        int bound = (Integer)this.triggerKey.get();
        if (bound == 0 || keyCode != bound) {
            return;
        }
        this.trigger();
    }

    @Override
    public void onAttackEntity(AttackEntityEvent event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        this.lastAttackTick = this.ticks;
    }
}

