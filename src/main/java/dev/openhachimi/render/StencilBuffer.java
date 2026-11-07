/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render;

import dev.openhachimi.modules.AutopearlModule;
import dev.openhachimi.modules.AutosoupModule;
import dev.openhachimi.modules.AutowebModule;
import dev.openhachimi.modules.CrystalauraModule;
import dev.openhachimi.modules.KillauraModule;
import dev.openhachimi.modules.MidpearlModule;
import dev.openhachimi.modules.ScaffoldModule;
import dev.openhachimi.modules.VelocityModule;
import dev.openhachimi.utils.ModuleManager;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.player.MoveUtil;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;

public final class StencilBuffer {
    public static float targetYaw;
    public static float targetPitch;
    public static boolean isRotating;
    private static float lastSentYaw;
    private static float lastSentPitch;
    private static boolean hasLastSent;
    private static boolean sentLookThisTick;
    private static boolean sentMoveThisTick;
    private static float cameraYaw;
    private static float cameraYawO;
    private static boolean spoofActive;
    private static boolean renderInit;
    private static int renderHoldTicks;
    private static float lastCamYaw;
    private static Field yBobField;
    private static Field yBobOField;
    private static Field xBobField;
    private static Field xBobOField;
    private static boolean bobFieldsResolved;

    private StencilBuffer() {
    }

    public static void clear() {
        isRotating = false;
        renderInit = false;
        renderHoldTicks = 0;
        StencilBuffer.restoreCameraIfNeeded();
    }

    public static void setTarget(float yaw, float pitch) {
        targetYaw = yaw;
        targetPitch = Mth.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        isRotating = true;
    }

    public static void afterModules() {
        StencilBuffer.pullTargets();
        StencilBuffer.applyRenderOnly();
        StencilBuffer.disableInteractionIfNeeded();
    }

    private static void applyRenderOnly() {
        Minecraft mc = Minecraft.getInstance();
        renderInit = false;
    }

    private static boolean anglesClose(float a, float b) {
        return Math.abs(Mth.wrapDegrees((float)(a - b))) < 0.05f;
    }

    public static float getCameraYaw() {
        if (spoofActive) {
            return cameraYaw;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null ? mc.player.getYRot() : 0.0f;
    }

    private static void applySpoof() {
        if (!isRotating) {
            StencilBuffer.restoreCameraIfNeeded();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!spoofActive) {
            cameraYaw = player.getYRot();
            cameraYawO = player.yRotO;
            lastCamYaw = cameraYaw;
            spoofActive = true;
        } else if (Math.abs(Mth.wrapDegrees((float)(player.getYRot() - targetYaw))) > 0.01f) {
            cameraYaw = player.getYRot();
            cameraYawO = player.yRotO;
        }
        player.setYRot(targetYaw);
    }

    public static boolean hasServerLook() {
        return hasLastSent && StencilBuffer.anglesClose(lastSentYaw, targetYaw) && StencilBuffer.anglesClose(lastSentPitch, targetPitch);
    }

    private static void pullTargets() {
        isRotating = false;
        AutosoupModule autoSoup = ModuleManager.getModule(AutosoupModule.class);
        if (autoSoup != null && autoSoup.enabled && AutosoupModule.hasTargetRotation) {
            StencilBuffer.setTarget(AutosoupModule.targetYaw, AutosoupModule.targetPitch);
            return;
        }
        AutopearlModule autoPearl = AutopearlModule.INSTANCE;
        if (autoPearl != null && autoPearl.enabled && AutopearlModule.hasTargetRotation) {
            StencilBuffer.setTarget(AutopearlModule.targetYaw, AutopearlModule.targetPitch);
            return;
        }
        MidpearlModule midPearl = MidpearlModule.INSTANCE;
        if (midPearl != null && midPearl.enabled && MidpearlModule.hasTargetRotation) {
            StencilBuffer.setTarget(MidpearlModule.targetYaw, MidpearlModule.targetPitch);
            return;
        }
        VelocityModule velocity = VelocityModule.INSTANCE;
        if (velocity != null && velocity.enabled && VelocityModule.hasTargetRotation) {
            StencilBuffer.setTarget(VelocityModule.targetYaw, VelocityModule.targetPitch);
            return;
        }
        ScaffoldModule scaffold = ModuleManager.getModule(ScaffoldModule.class);
        if (scaffold != null && scaffold.enabled && ScaffoldModule.hasTargetRotation) {
            StencilBuffer.setTarget(ScaffoldModule.targetYaw, ScaffoldModule.targetPitch);
            return;
        }
        CrystalauraModule crystalAura = ModuleManager.getModule(CrystalauraModule.class);
        if (crystalAura != null && crystalAura.enabled && CrystalauraModule.hasTargetRotation) {
            StencilBuffer.setTarget(CrystalauraModule.targetYaw, CrystalauraModule.targetPitch);
            return;
        }
        KillauraModule killAura = KillauraModule.INSTANCE;
        if (killAura != null && killAura.enabled && KillauraModule.hasTargetRotation) {
            StencilBuffer.setTarget(KillauraModule.targetYaw, KillauraModule.targetPitch);
            return;
        }
        AutowebModule autoWebPlace = AutowebModule.INSTANCE;
        if (autoWebPlace != null && autoWebPlace.enabled && AutowebModule.hasTargetRotation) {
            StencilBuffer.setTarget(AutowebModule.targetYaw, AutowebModule.targetPitch);
            return;
        }
    }

    public static boolean isSpoofActive() {
        return spoofActive;
    }

    public static Rotation getLastRotation() {
        if (hasLastSent) {
            return new Rotation(lastSentYaw, lastSentPitch);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return new Rotation(mc.player.getYRot(), mc.player.getXRot());
        }
        return new Rotation(0.0f, 0.0f);
    }

    public static Rotation getRotation() {
        if (isRotating) {
            return new Rotation(targetYaw, targetPitch);
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return new Rotation(mc.player.getYRot(), mc.player.getXRot());
        }
        return new Rotation(0.0f, 0.0f);
    }

    public static void ensureLookBeforeAction() {
    }

    public static boolean hasSentMoveThisTick() {
        return sentMoveThisTick;
    }

    public static boolean hasSentLookThisTick() {
        return sentLookThisTick;
    }

    private static void disableInteractionIfNeeded() {
        if (!isRotating) {
            return;
        }
        ScaffoldModule scaffold = ModuleManager.getModule(ScaffoldModule.class);
        if (scaffold != null && scaffold.enabled) {
            return;
        }
        if (AutosoupModule.ownsInteractionKeys()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        while (mc.options.keyUse.consumeClick()) {
        }
        mc.options.keyUse.setDown(false);
        while (mc.options.keyAttack.consumeClick()) {
        }
        mc.options.keyAttack.setDown(false);
    }

    private static void restoreCameraIfNeeded() {
        if (!spoofActive) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null) {
            player.setYRot(cameraYaw);
            player.yRotO = lastCamYaw;
            lastCamYaw = cameraYaw;
        }
        spoofActive = false;
    }

    public static void beginTick() {
        sentLookThisTick = false;
        sentMoveThisTick = false;
        StencilBuffer.restoreCameraIfNeeded();
        StencilBuffer.pullTargets();
        StencilBuffer.applyRenderOnly();
    }

    public static void onPostTick() {
        StencilBuffer.restoreCameraIfNeeded();
        StencilBuffer.applyRenderOnly();
    }

    public static Object transformOutgoing(Object packet) {
        if (!(packet instanceof ServerboundMovePlayerPacket)) {
            return packet;
        }
        ServerboundMovePlayerPacket move = (ServerboundMovePlayerPacket)packet;
        sentMoveThisTick = true;
        if (isRotating || spoofActive) {
            float yaw = targetYaw;
            float pitch = targetPitch;
            if (move.hasRotation()) {
                if (sentLookThisTick && !move.hasPosition()) {
                    return null;
                }
                if (hasLastSent && StencilBuffer.anglesClose(yaw, lastSentYaw) && StencilBuffer.anglesClose(pitch, lastSentPitch)) {
                    if (move.hasPosition()) {
                        return new ServerboundMovePlayerPacket.Pos(move.getX(0.0), move.getY(0.0), move.getZ(0.0), move.isOnGround());
                    }
                    return null;
                }
                ServerboundMovePlayerPacket replaced = move.hasPosition() ? new ServerboundMovePlayerPacket.PosRot(move.getX(0.0), move.getY(0.0), move.getZ(0.0), yaw, pitch, move.isOnGround()) : new ServerboundMovePlayerPacket.Rot(yaw, pitch, move.isOnGround());
                lastSentYaw = yaw;
                lastSentPitch = pitch;
                hasLastSent = true;
                sentLookThisTick = true;
                return replaced;
            }
            if (move.hasPosition() && !StencilBuffer.hasServerLook()) {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null) {
                    return packet;
                }
                ServerboundMovePlayerPacket.PosRot replaced = new ServerboundMovePlayerPacket.PosRot(move.getX(player.getX()), move.getY(player.getY()), move.getZ(player.getZ()), yaw, pitch, move.isOnGround());
                lastSentYaw = yaw;
                lastSentPitch = pitch;
                hasLastSent = true;
                sentLookThisTick = true;
                return replaced;
            }
            return packet;
        }
        if (move.hasRotation()) {
            float moveYaw = move.getYRot(lastSentYaw);
            float movePitch = move.getXRot(lastSentPitch);
            ServerboundMovePlayerPacket replaced = move.hasPosition() ? new ServerboundMovePlayerPacket.PosRot(move.getX(0.0), move.getY(0.0), move.getZ(0.0), moveYaw, movePitch, move.isOnGround()) : new ServerboundMovePlayerPacket.Rot(moveYaw, movePitch, move.isOnGround());
            lastSentYaw = moveYaw;
            lastSentPitch = movePitch;
            hasLastSent = true;
            return replaced;
        }
        return packet;
    }

    public static void onPlayerTickStart() {
        StencilBuffer.pullTargets();
        StencilBuffer.applySpoof();
    }

    public static void fixInput(Input input) {
        if (!isRotating || input == null) {
            return;
        }
        MoveUtil.fixMovement(input, targetYaw);
    }

    public static void onPlayerTickEnd() {
        StencilBuffer.applyRenderOnly();
    }

    public static void onIncomingPacket(Object packet) {
    }

    public static boolean onOutgoingPacket(Object packet) {
        return false;
    }
}

