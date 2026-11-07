/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.rotation.RotationUtil2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;

public class CrystalauraModule
extends Module {
    private static final byte[] S_22F8F199 = new byte[]{1, 48, 59, 49, 54, 35, 46, 3, 55, 48, 35};  // XOR0x42 "CrystalAura"
    private static final byte[] S_4B1491D = new byte[]{16, 35, 44, 37, 39};  // XOR0x42 "Range"
    private static final byte[] S_123FC97B = new byte[]{16, 45, 54, 35, 54, 39};  // XOR0x42 "Rotate"
    private static final byte[] S_4C94EDE = new byte[]{17, 53, 43, 44, 37};  // XOR0x42 "Swing"
    private static final Minecraft mc = Minecraft.getInstance();
    private final NumberSetting range = new NumberSetting("Range", 3.0f, 1.0f, 6.0f);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", true);
    private final BooleanSetting swing = new BooleanSetting("Swing", true);
    public static volatile boolean hasTargetRotation;
    public static volatile float targetYaw;
    public static volatile float targetPitch;
    private Entity target;

    public CrystalauraModule() {
        super("CrystalAura", Category.COMBAT);
        this.values.add(this.range);
        this.values.add(this.rotate);
        this.values.add(this.swing);
    }

    public void attack() {
        if (this.target == null || CrystalauraModule.mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.getConnection().send((Packet)ServerboundInteractPacket.createAttackPacket((Entity)this.target, (boolean)false));
        if (((Boolean)this.swing.get()).booleanValue()) {
            CrystalauraModule.mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void onDisable() {
        this.target = null;
        hasTargetRotation = false;
    }

    @Override
    public void onEnable() {
        this.target = null;
        hasTargetRotation = false;
    }

    @Override
    public void onTick() {
        if (this.target == null || !hasTargetRotation) {
            return;
        }
        if (!this.target.isAlive() || CrystalauraModule.mc.player.distanceTo(this.target) > ((Float)this.range.get()).floatValue()) {
            this.target = null;
            hasTargetRotation = false;
            return;
        }
        this.attack();
        this.target = null;
        hasTargetRotation = false;
    }

    @Override
    public void onPreTick() {
        if (CrystalauraModule.mc.player == null || CrystalauraModule.mc.level == null || CrystalauraModule.mc.screen != null) {
            this.target = null;
            return;
        }
        this.target = null;
        float best = Float.MAX_VALUE;
        float limit = ((Float)this.range.get()).floatValue();
        boolean anyCrystal = false;
        for (Entity e : CrystalauraModule.mc.level.entitiesForRendering()) {
            if (!(e instanceof EndCrystal) || !e.isAlive()) continue;
            anyCrystal = true;
            break;
        }
        if (!anyCrystal) {
            hasTargetRotation = false;
            this.target = null;
            return;
        }
        for (Entity crystal : CrystalauraModule.mc.level.entitiesForRendering()) {
            float pitchDiff;
            if (!(crystal instanceof EndCrystal) || !crystal.isAlive() || CrystalauraModule.mc.player.distanceTo(crystal) > limit) continue;
            float[] rot = RotationUtil2.toEntity(crystal);
            float yawDiff = Math.abs(RotationUtil2.angleDiff(CrystalauraModule.mc.player.getYRot(), rot[0]));
            float score = (float)Math.sqrt(yawDiff * yawDiff + (pitchDiff = Math.abs(RotationUtil2.angleDiff(CrystalauraModule.mc.player.getXRot(), rot[1]))) * pitchDiff);
            if (!(score < best)) continue;
            best = score;
            this.target = crystal;
            targetYaw = rot[0];
            targetPitch = rot[1];
        }
        hasTargetRotation = this.target != null;
    }

    @Override
    public void onWorldChange() {
        this.target = null;
        hasTargetRotation = false;
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
    }
}

