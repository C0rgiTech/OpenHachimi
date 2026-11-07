/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.utils.rotation.RotationManager;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.rotation.RotationUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class MidpearlModule
extends Module {
    private static final byte[] S_206426FE = new byte[]{15, 43, 38, 18, 39, 35, 48, 46};  // XOR0x42 "MidPearl"
    private static final byte[] S_77471352 = new byte[]{0, 55, 54, 54, 45, 44};  // XOR0x42 "Button"
    private static final byte[] S_95BC5B5 = new byte[]{15, 43, 38, 38, 46, 39};  // XOR0x42 "Middle"
    private static final byte[] S_2D504359 = new byte[]{15, 45, 55, 49, 39, 98, 118};  // XOR0x42 "Mouse 4"
    private static final byte[] S_2D50435A = new byte[]{15, 45, 55, 49, 39, 98, 119};  // XOR0x42 "Mouse 5"
    private static final byte[] S_156640A2 = new byte[]{11, 44, 54, 39, 48, 33, 39, 50, 54};  // XOR0x42 "Intercept"
    private static final byte[] S_479EA090 = new byte[]{11, 44, 54, 39, 48, 33, 39, 50, 54, 98, 0, 55, 54, 54, 45, 44};  // XOR0x42 "Intercept Button"
    private static final byte[] S_3870F6C6 = new byte[]{13, 44, 98, 16, 39, 46, 39, 35, 49, 39};  // XOR0x42 "On Release"
    private static final byte[] S_3E691775 = new byte[]{0, 46, 45, 33, 41, 98, 1, 42, 39, 33, 41};  // XOR0x42 "Block Check"
    private static final byte[] S_1D1DE017 = new byte[]{32, 46, 45, 33, 41, 17, 54, 35, 54, 39, 18, 48, 39, 38, 43, 33, 54, 43, 45, 44, 10, 35, 44, 38, 46, 39, 48};  // XOR0x42 "blockStatePredictionHandler"
    private static final byte[] S_4EBA27A5 = new byte[]{36, 29, 112, 113, 113, 119, 113, 115, 29};  // XOR0x42 "f_233531_"
    private static final byte[] S_62675549 = new byte[]{39, 44, 49, 55, 48, 39, 10, 35, 49, 17, 39, 44, 54, 1, 35, 48, 48, 43, 39, 38, 11, 54, 39, 47};  // XOR0x42 "ensureHasSentCarriedItem"
    private static final byte[] S_22AFBD34 = new byte[]{47, 29, 115, 114, 119, 113, 116, 112, 29};  // XOR0x42 "m_105362_"
    public static MidpearlModule INSTANCE;
    public static float targetYaw;
    public static float targetPitch;
    public static boolean hasTargetRotation;
    private final ModeSetting mainButtonSetting = new ModeSetting("Button", new String[]{"Middle", "Mouse 4", "Mouse 5"}, "Middle");
    private final BooleanSetting interceptSetting = new BooleanSetting("Intercept", false);
    private final ModeSetting interceptButtonSetting = new ModeSetting("Intercept Button", new String[]{"Mouse 4", "Mouse 5"}, "Mouse 4");
    private final BooleanSetting holdModeSetting = new BooleanSetting("On Release", false);
    private final BooleanSetting losCheckSetting = new BooleanSetting("Block Check", true);
    private boolean isHoldingIntercept;
    private boolean isHoldingButton;
    private boolean pearlSlotSelected;
    private int throwCountdown;
    private int disableDelay;
    private int pearlSavedSlot = -1;
    private boolean throwPhaseActive;
    private int preThrowTicks;
    private int rodSavedSlot = -1;
    private int postThrowTicks;
    private boolean pendingClearRotation;
    private static Method ensureHasSentCarriedItemMethod;
    private static boolean ensureHasSentCarriedItemResolved;
    private static Field predictionHandlerField;
    private static boolean predictionHandlerResolved;

    public MidpearlModule() {
        super("MidPearl", Category.MISC);
        INSTANCE = this;
        this.values.add(this.mainButtonSetting);
        this.values.add(this.interceptSetting);
        this.values.add(this.interceptButtonSetting);
        this.values.add(this.holdModeSetting);
        this.values.add(this.losCheckSetting);
    }

    private void reset() {
        this.pearlSlotSelected = false;
        this.throwCountdown = 0;
        this.disableDelay = 0;
        this.pearlSavedSlot = -1;
        this.throwPhaseActive = false;
        this.pendingClearRotation = false;
        this.clearTargetRotation();
        this.preThrowTicks = 0;
        this.isHoldingIntercept = false;
        this.isHoldingButton = false;
        this.rodSavedSlot = -1;
        this.postThrowTicks = 0;
    }

    private void sendCarriedItem(Minecraft mc) {
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (gameMode == null) {
            return;
        }
        Method method = MidpearlModule.resolveEnsureHasSentCarriedItem(gameMode);
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

    public boolean isInterceptButton(int button) {
        return (Boolean)this.interceptSetting.get() != false && this.getMouseButton(this.interceptButtonSetting) == button && this.hasEnemyPearlTarget();
    }

    private void cancelThrow() {
        this.throwPhaseActive = false;
        this.pendingClearRotation = false;
        this.clearTargetRotation();
        this.preThrowTicks = 0;
        this.isHoldingIntercept = false;
    }

    private boolean isAutoRodActive() {
        AutorodModule autoRod = AutorodModule.INSTANCE;
        if (autoRod == null) {
            autoRod = ModuleManager.getModule(AutorodModule.class);
        }
        return autoRod != null && autoRod.enabled && autoRod.shouldInterceptButton(this.getMouseButton(this.mainButtonSetting));
    }

    private boolean isButtonDown(Minecraft mc, int button) {
        return GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)button) == 1;
    }

    private void tickNormalMode(Minecraft mc) {
        if (this.throwCountdown > 0 || this.throwPhaseActive) {
            return;
        }
        boolean pressed = this.isButtonDown(mc, this.getMouseButton(this.mainButtonSetting));
        if (pressed) {
            int slot;
            if (!this.pearlSlotSelected && (slot = this.findPearlSlot(mc)) != -1 && slot < 9) {
                this.pearlSlotSelected = true;
                this.pearlSavedSlot = mc.player.getInventory().selected;
                mc.player.getInventory().selected = slot;
                this.sendCarriedItem(mc);
            }
        } else if (this.pearlSlotSelected) {
            this.pearlSlotSelected = false;
            this.throwCountdown = 1;
        }
    }

    private void selectPearlSlot(Minecraft mc, int slot) {
        if (slot == -1 || slot > 8) {
            return;
        }
        if (this.throwPhaseActive) {
            return;
        }
        this.throwPhaseActive = true;
        this.preThrowTicks = 2;
        this.rodSavedSlot = mc.player.getInventory().selected;
        mc.player.getInventory().selected = slot;
        this.sendCarriedItem(mc);
    }

    private void throwPearl(Minecraft mc) {
        if (mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL) {
            this.sendPredictiveUseItem(mc);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        this.postThrowTicks = 2;
        this.throwPhaseActive = false;
        this.preThrowTicks = -1;
        this.isHoldingButton = true;
        this.pendingClearRotation = true;
    }

    private float[] getBallisticAngles(Minecraft mc, Vec3 target) {
        Vec3 eyePos = mc.player.getEyePosition();
        double velocity = 1.5;
        double gravity = 0.03;
        double drag = 0.99;
        double dx = target.x - eyePos.x;
        double dy = target.y - eyePos.y;
        double dz = target.z - eyePos.z;
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        if (horizontalDist == 0.0) {
            return new float[]{yaw, dy > 0.0 ? -90.0f : 90.0f};
        }
        for (float pitch = 90.0f; pitch >= -90.0f; pitch -= 0.5f) {
            double pitchRad = Math.toRadians(pitch);
            double velX = -Math.sin(Math.toRadians(yaw)) * Math.cos(pitchRad);
            double velY = -Math.sin(pitchRad);
            double velZ = Math.cos(Math.toRadians(yaw)) * Math.cos(pitchRad);
            double mag = Math.sqrt(velX * velX + velY * velY + velZ * velZ);
            velX = velX / mag * velocity;
            velY = velY / mag * velocity;
            velZ = velZ / mag * velocity;
            Vec3 projectilePos = new Vec3(eyePos.x, eyePos.y, eyePos.z);
            Vec3 projectileVel = new Vec3(velX, velY, velZ);
            boolean abandon = false;
            for (int i = 0; i < 300; ++i) {
                projectilePos = projectilePos.add(projectileVel);
                projectileVel = new Vec3(projectileVel.x * drag, projectileVel.y * drag - gravity, projectileVel.z * drag);
                if (projectilePos.y < (double)(mc.level.getMinBuildHeight() - 10)) {
                    abandon = true;
                    break;
                }
                double traveledDist = Math.sqrt(Math.pow(projectilePos.x - eyePos.x, 2.0) + Math.pow(projectilePos.z - eyePos.z, 2.0));
                if (!(traveledDist >= horizontalDist)) continue;
                if (Math.abs(projectilePos.y - target.y) < 1.0) {
                    return new float[]{Mth.wrapDegrees((float)yaw), Mth.wrapDegrees((float)pitch)};
                }
                if (!(projectileVel.y < 0.0) || !(projectilePos.y < target.y)) break;
                abandon = true;
                break;
            }
            if (abandon) continue;
        }
        return null;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            if (this.pearlSavedSlot != -1) {
                mc.player.getInventory().selected = this.pearlSavedSlot;
                this.sendCarriedItem(mc);
            }
            if (this.rodSavedSlot != -1) {
                mc.player.getInventory().selected = this.rodSavedSlot;
                this.sendCarriedItem(mc);
            }
        }
        this.reset();
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    private int getMouseButton(ModeSetting setting) {
        String mode = (String)setting.get();
        if ("Mouse 4".equals(mode)) {
            return 3;
        }
        if ("Mouse 5".equals(mode)) {
            return 4;
        }
        return 2;
    }

    private Vec3 simulatePearlLanding(Minecraft mc, final ThrownEnderpearl pearl) {
        double x = pearl.getX();
        double y = pearl.getY();
        double z = pearl.getZ();
        double dx = pearl.getDeltaMovement().x;
        double dy = pearl.getDeltaMovement().y;
        double dz = pearl.getDeltaMovement().z;
        double gravity = 0.03;
        for (int step = 0; step < 1000; ++step) {
            double d = 0.0; // initialised: assignment path not guaranteed
            BlockHitResult blockHit;
            Vec3 start = new Vec3(x, y, z);
            Vec3 end = new Vec3(x + dx, y + dy, z + dz);
            HitResult bestHit = blockHit = mc.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)pearl));
            double bestDistSq = start.distanceToSqr(blockHit.getLocation());
            List nearby = mc.level.getEntities((Entity)pearl, pearl.getBoundingBox().expandTowards(dx, dy, dz).inflate(1.0), (Predicate)new Predicate<Entity>(){

                @Override
                public boolean test(Entity e) {
                    return e.canBeCollidedWith() && e != pearl.getOwner();
                }
            });
            for (int i = 0; i < nearby.size(); ++i) {
                Vec3 hit;
                double dsq;
                AABB box;
                Optional clip;
                Entity other = (Entity)nearby.get(i);
                if (!(other instanceof LivingEntity) || !(clip = (box = other.getBoundingBox().inflate(0.3)).clip(start, end)).isPresent() || !((dsq = start.distanceToSqr(hit = (Vec3)clip.get())) < bestDistSq)) continue;
                bestDistSq = dsq;
                bestHit = new EntityHitResult(other, hit);
            }
            if (bestHit != null && bestHit.getType() != HitResult.Type.MISS) {
                return bestHit.getLocation();
            }
            x += dx;
            z += dz;
            y += dy;
            if (d < (double)(mc.level.getMinBuildHeight() - 10)) break;
            dx *= 0.99;
            dy *= 0.99;
            dz *= 0.99;
            dy -= gravity;
        }
        return null;
    }

    private static Method resolveEnsureHasSentCarriedItem(MultiPlayerGameMode gameMode) {
        if (ensureHasSentCarriedItemResolved) {
            return ensureHasSentCarriedItemMethod;
        }
        ensureHasSentCarriedItemResolved = true;
        Class<?> clazz = gameMode.getClass();
        Method found = MidpearlModule.findNoArgMethod(clazz, "ensureHasSentCarriedItem");
        if (found == null) {
            found = MidpearlModule.findNoArgMethod(clazz, "m_105362_");
        }
        ensureHasSentCarriedItemMethod = found;
        return found;
    }

    private Vec3 findNearestEnemyPearlLanding(Minecraft mc) {
        Vec3 bestLanding = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : mc.level.entitiesForRendering()) {
            double dist;
            Vec3 landing;
            ThrownEnderpearl pearl;
            Entity owner;
            if (!(entity instanceof ThrownEnderpearl) || !entity.isAlive() || (owner = (pearl = (ThrownEnderpearl)entity).getOwner()) == null || owner.equals((Object)mc.player) || (landing = this.simulatePearlLanding(mc, pearl)) == null || !((dist = landing.distanceTo(mc.player.position())) < bestDist)) continue;
            bestDist = dist;
            bestLanding = landing;
        }
        return bestLanding;
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
        catch (Exception field) {
            try {
                Field field2 = ClientLevel.class.getDeclaredField("f_233531_");
                field2.setAccessible(true);
                predictionHandlerField = field2;
                return field2;
            }
            catch (Exception field2) {
                Field[] fields = ClientLevel.class.getDeclaredFields();
                for (int i = 0; i < fields.length; ++i) {
                    Field field3 = fields[i];
                    if (!BlockStatePredictionHandler.class.isAssignableFrom(field3.getType())) continue;
                    field3.setAccessible(true);
                    predictionHandlerField = field3;
                    break;
                }
                return predictionHandlerField;
            }
        }
    }

    private void clearTargetRotation() {
        hasTargetRotation = false;
    }

    private void updateTargetRotation(Minecraft mc) {
        float[] angles = this.calculateTargetRotation(mc);
        if (angles == null) {
            this.clearTargetRotation();
            return;
        }
        Rotation lastRot = RotationManager.getLastRotation();
        float prevYaw = lastRot.getYaw();
        float unwrappedYaw = prevYaw + Mth.wrapDegrees((float)(angles[0] - prevYaw));
        Rotation utilRot = new Rotation(unwrappedYaw, angles[1]);
        Rotation fixedRotation = RotationUtil.applySensitivityPatch(utilRot);
        targetYaw = fixedRotation.getYaw();
        targetPitch = Mth.clamp((float)fixedRotation.getPitch(), (float)-90.0f, (float)90.0f);
        hasTargetRotation = true;
    }

    private void sendPredictiveUseItem(Minecraft mc) {
        if (mc.getConnection() == null || mc.level == null || mc.player == null) {
            return;
        }
        try {
            Field field = MidpearlModule.resolvePredictionHandlerField();
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

    private boolean hasEnemyPearlTarget() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        return this.calculateTargetRotation(mc) != null;
    }

    private float[] calculateTargetRotation(Minecraft mc) {
        BlockHitResult clip;
        Vec3 landing = this.findNearestEnemyPearlLanding(mc);
        if (landing == null) {
            return null;
        }
        int slot = this.findPearlSlot(mc);
        if (slot == -1 || slot >= 9) {
            return null;
        }
        if (((Boolean)this.losCheckSetting.get()).booleanValue() && (clip = mc.level.clip(new ClipContext(mc.player.getEyePosition(), landing, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player))).getType() != HitResult.Type.MISS) {
            return null;
        }
        return this.getBallisticAngles(mc, landing);
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onPreTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            if (this.throwPhaseActive) {
                this.reset();
            }
            return;
        }
        if (mc.screen != null) {
            return;
        }
        if (this.postThrowTicks > 0) {
            --this.postThrowTicks;
            if (this.postThrowTicks == 0 && this.rodSavedSlot != -1) {
                mc.player.getInventory().selected = this.rodSavedSlot;
                this.sendCarriedItem(mc);
                this.rodSavedSlot = -1;
            }
        }
        if (this.pendingClearRotation && this.postThrowTicks <= 0) {
            this.pendingClearRotation = false;
            this.clearTargetRotation();
        }
        if (this.disableDelay > 0) {
            --this.disableDelay;
            if (this.disableDelay == 0) {
                if (this.pearlSavedSlot != -1) {
                    mc.player.getInventory().selected = this.pearlSavedSlot;
                    this.sendCarriedItem(mc);
                }
                this.reset();
            }
            return;
        }
        if (this.throwPhaseActive) {
            this.updateTargetRotation(mc);
            if (this.preThrowTicks > 0) {
                --this.preThrowTicks;
            }
            if (this.preThrowTicks == 0 && hasTargetRotation) {
                this.throwPearl(mc);
            }
            return;
        }
        if (this.throwCountdown > 0) {
            --this.throwCountdown;
            if (this.throwCountdown == 0) {
                if (mc.player.getMainHandItem().getItem() == Items.ENDER_PEARL) {
                    this.sendPredictiveUseItem(mc);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
                this.disableDelay = 2;
            }
            return;
        }
        if (((Boolean)this.interceptSetting.get()).booleanValue()) {
            boolean pressed = this.isButtonDown(mc, this.getMouseButton(this.interceptButtonSetting));
            if (pressed) {
                if (this.isHoldingButton) {
                    return;
                }
            } else {
                this.isHoldingButton = false;
            }
            if (((Boolean)this.holdModeSetting.get()).booleanValue()) {
                if (pressed) {
                    this.updateTargetRotation(mc);
                    this.isHoldingIntercept = true;
                } else {
                    if (this.isHoldingIntercept) {
                        if (hasTargetRotation) {
                            this.selectPearlSlot(mc, this.findPearlSlot(mc));
                        } else {
                            this.cancelThrow();
                        }
                    }
                    this.isHoldingIntercept = false;
                }
            } else if (pressed) {
                this.updateTargetRotation(mc);
                if (hasTargetRotation) {
                    this.selectPearlSlot(mc, this.findPearlSlot(mc));
                }
            } else {
                this.cancelThrow();
            }
            boolean sameButton = this.getMouseButton(this.interceptButtonSetting) == this.getMouseButton(this.mainButtonSetting);
            boolean bl = sameButton;
            if (sameButton && (pressed || this.isHoldingIntercept)) {
                return;
            }
        }
        if (this.isAutoRodActive()) {
            return;
        }
        this.tickNormalMode(mc);
    }
}

