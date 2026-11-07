/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.player.RaytraceUtil2;
import dev.openhachimi.utils.MSTimer;
import dev.openhachimi.utils.rotation.RotationUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;

public class KillauraModule
extends Module {
    private static final byte[] S_5FC31201 = new byte[]{9, 43, 46, 46, 3, 55, 48, 35};  // XOR0x42 "KillAura"
    private static final byte[] S_45CD5992 = new byte[]{18, 46, 35, 59, 39, 48, 49};  // XOR0x42 "Players"
    private static final byte[] S_24AD93 = new byte[]{15, 45, 32, 49};  // XOR0x42 "Mobs"
    private static final byte[] S_3024C9D7 = new byte[]{3, 44, 43, 47, 35, 46, 49};  // XOR0x42 "Animals"
    private static final byte[] S_794C57CD = new byte[]{20, 43, 46, 46, 35, 37, 39, 48, 49};  // XOR0x42 "Villagers"
    private static final byte[] S_7D884A49 = new byte[]{10, 43, 54, 98, 49, 39, 46, 39, 33, 54};  // XOR0x42 "Hit select"
    private static final byte[] S_10586 = new byte[]{1, 18, 17};  // XOR0x42 "CPS"
    private static final byte[] S_3D21E82 = new byte[]{3, 43, 47, 98, 48, 35, 44, 37, 39};  // XOR0x42 "Aim range"
    private static final byte[] S_110AD = new byte[]{4, 13, 20};  // XOR0x42 "FOV"
    private static final byte[] S_2F0DD945 = new byte[]{16, 45, 54, 35, 54, 43, 45, 44, 98, 49, 50, 39, 39, 38};  // XOR0x42 "Rotation speed"
    private static final byte[] S_24ADC3 = new byte[]{15, 45, 38, 39};  // XOR0x42 "Mode"
    private static final byte[] S_139D7068 = new byte[]{17, 43, 44, 37, 46, 39};  // XOR0x42 "Single"
    private static final byte[] S_1460A354 = new byte[]{17, 53, 43, 54, 33, 42};  // XOR0x42 "Switch"
    private static final byte[] S_7BF1A1B7 = new byte[]{17, 53, 43, 54, 33, 42, 98, 38, 39, 46, 35, 59};  // XOR0x42 "Switch delay"
    private static final byte[] S_3E62DDC4 = new byte[]{18, 48, 43, 45, 48, 43, 54, 59};  // XOR0x42 "Priority"
    private static final byte[] S_99EE7C = new byte[]{10, 39, 35, 46, 54, 42};  // XOR0x42 "Health"
    private static final byte[] S_1148D = new byte[]{4, 45, 20};  // XOR0x42 "FoV"
    private static final byte[] S_4B1491D = new byte[]{16, 35, 44, 37, 39};  // XOR0x42 "Range"
    private static final byte[] S_252358 = new byte[]{12, 45, 44, 39};  // XOR0x42 "None"
    public static KillauraModule INSTANCE;
    public static boolean hasTargetRotation;
    public static float targetYaw;
    public static float targetPitch;
    public static Entity currentTarget;
    public static List<Entity> targets;
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting villagers = new BooleanSetting("Villagers", false);
    private final BooleanSetting hitSel = new BooleanSetting("Hit select", true);
    private final ListSetting cps = new ListSetting("CPS", 12, 1, 20);
    private final NumberSetting aimRange = new NumberSetting("Aim range", 4.0f, 0.01f, 6.0f);
    private final ListSetting fov = new ListSetting("FOV", 180, 0, 360);
    private final ListSetting rotationSpeed = new ListSetting("Rotation speed", 180, 0, 180);
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Single", "Switch"}, "Single");
    private final ListSetting switchDelay = new ListSetting("Switch delay", 100, 0, 500);
    private final ModeSetting priority = new ModeSetting("Priority", new String[]{"Health", "FoV", "Range", "None"}, "Range");
    private final MSTimer switchTimer = new MSTimer();
    private int targetNumber;
    private long lastAttackTime;
    public int attacks;

    public KillauraModule() {
        super("KillAura", Category.COMBAT);
        INSTANCE = this;
        this.values.add(this.players);
        this.values.add(this.mobs);
        this.values.add(this.animals);
        this.values.add(this.villagers);
        this.values.add(this.hitSel);
        this.values.add(this.cps);
        this.values.add(this.aimRange);
        this.values.add(this.fov);
        this.values.add(this.rotationSpeed);
        this.values.add(this.mode);
        this.values.add(this.switchDelay);
        this.values.add(this.priority);
    }

    static {
        hasTargetRotation = false;
        targetYaw = 0.0f;
        targetPitch = 0.0f;
    }

    public boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return false;
        }
        if (AntibotModule.isBot(entity)) {
            return false;
        }
        if (TeamsModule.isSameTeam(entity)) {
            return false;
        }
        if (FriendModule.isFriend(entity)) {
            return false;
        }
        if (entity instanceof Player) {
            return (Boolean)this.players.get();
        }
        if (entity instanceof Monster) {
            return (Boolean)this.mobs.get();
        }
        if (entity instanceof Animal) {
            return (Boolean)this.animals.get();
        }
        if (entity instanceof Villager) {
            return (Boolean)this.villagers.get();
        }
        return (Boolean)this.mobs.get();
    }

    @Override
    public void onDisable() {
        targets = null;
        currentTarget = null;
        this.attacks = 0;
        hasTargetRotation = false;
        super.onDisable();
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
        while (this.attacks > 0) {
            Entity hit;
            HitResult hitResult;
            --this.attacks;
            if (mc.player.isUsingItem()) {
                return;
            }
            Rotation rot = new Rotation(mc.player.getYRot(), mc.player.getXRot());
            if (hasTargetRotation) {
                rot = new Rotation(targetYaw, targetPitch);
            }
            if ((hitResult = RaytraceUtil2.rayCast(rot, 3.0)) instanceof EntityHitResult && (hit = ((EntityHitResult)hitResult).getEntity()) != null && this.isValidTarget(hit)) {
                mc.player.setSprinting(EventRegistrar.lastSprint && mc.player.isSprinting());
                mc.gameMode.attack((Player)mc.player, hit);
            }
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.resetAttackStrengthTicker();
        }
    }

    @Override
    public void onTick() {
        Entity target;
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (ScaffoldModule.INSTANCE != null && ScaffoldModule.INSTANCE.enabled) {
            hasTargetRotation = false;
            currentTarget = null;
            targets = new ArrayList<Entity>();
            return;
        }
        targets = new ArrayList<Entity>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            boolean b;
            if (entity == mc.player || !(entity instanceof LivingEntity) || !this.isValidTarget(entity) || !(b = RotationUtil.getDistance(entity) <= (double)((Float)this.aimRange.get()).floatValue() && Math.abs(Mth.wrapDegrees((float)(mc.player.getYRot() - RotationUtil.calculate(entity).getYaw()))) <= (float)((Integer)this.fov.get()).intValue())) continue;
            targets.add(entity);
        }
        if (this.priority.isMode("Range")) {
            targets.sort(new Comparator<Entity>(){

                @Override
                public int compare(Entity o1, Entity o2) {
                    return Double.compare(o1.distanceTo((Entity)mc.player), o2.distanceTo((Entity)mc.player));
                }
            });
        } else if (this.priority.isMode("FoV")) {
            targets.sort(new Comparator<Entity>(){

                @Override
                public int compare(Entity o1, Entity o2) {
                    double fov1 = Math.abs(Mth.wrapDegrees((float)(mc.player.getYRot() - RotationUtil.calculate(o1).getYaw())));
                    double fov2 = Math.abs(Mth.wrapDegrees((float)(mc.player.getYRot() - RotationUtil.calculate(o2).getYaw())));
                    return Double.compare(fov1, fov2);
                }
            });
        } else if (this.priority.isMode("Health")) {
            targets.sort(new Comparator<Entity>(){

                @Override
                public int compare(Entity o1, Entity o2) {
                    double h1 = o1 instanceof LivingEntity ? (double)((LivingEntity)o1).getHealth() : 0.0;
                    double h2 = o2 instanceof LivingEntity ? (double)((LivingEntity)o2).getHealth() : 0.0;
                    return Double.compare(h1, h2);
                }
            });
        }
        if (targets.isEmpty()) {
            hasTargetRotation = false;
            currentTarget = null;
            return;
        }
        String currentMode = (String)this.mode.get();
        if ("Single".equals(currentMode)) {
            this.targetNumber = 0;
        } else if ("Switch".equals(currentMode) && this.switchTimer.delay(((Integer)this.switchDelay.get()).intValue())) {
            this.switchTimer.reset();
            if (++this.targetNumber >= targets.size()) {
                this.targetNumber = 0;
            }
        }
        if (this.targetNumber >= targets.size()) {
            this.targetNumber = 0;
        }
        currentTarget = target = targets.get(this.targetNumber);
        if (VelocityModule.INSTANCE != null && VelocityModule.INSTANCE.enabled && VelocityModule.jump) {
            hasTargetRotation = false;
        } else {
            Rotation calculated = RotationUtil.calculate(target);
            Rotation smoothed = RotationUtil.smooth(calculated, ((Integer)this.rotationSpeed.get()).floatValue());
            targetYaw = smoothed.getYaw();
            targetPitch = smoothed.getPitch();
            hasTargetRotation = true;
        }
        if (((Boolean)this.hitSel.get()).booleanValue()) {
            long delay;
            long time;
            LivingEntity living;
            EntityHitResult entityHitResult;
            Entity entity;
            HitResult hitResult;
            Rotation rot = new Rotation(mc.player.getYRot(), mc.player.getXRot());
            if (hasTargetRotation) {
                rot = new Rotation(targetYaw, targetPitch);
            }
            if ((hitResult = RaytraceUtil2.rayCast(rot, 3.0)) instanceof EntityHitResult && (entity = (entityHitResult = (EntityHitResult)hitResult).getEntity()) instanceof LivingEntity && this.isValidTarget((Entity)(living = (LivingEntity)entity)) && (time = System.currentTimeMillis()) - this.lastAttackTime >= (delay = (long)(1000.0 / Math.max(0.01, ThreadLocalRandom.current().nextGaussian(((Integer)this.cps.get()).intValue(), 5.0)))) && (living.hurtTime <= 1 || mc.player.hurtTime >= 6)) {
                ++this.attacks;
                this.lastAttackTime = time;
            }
        }
    }

    @Override
    public void onRender2D(GuiGraphics g, float partialTick) {
        long delay;
        long time;
        Minecraft mc = Minecraft.getInstance();
        if (targets != null && !targets.isEmpty() && !((Boolean)this.hitSel.get()).booleanValue() && (time = System.currentTimeMillis()) - this.lastAttackTime >= (delay = (long)(1000.0 / Math.max(0.01, ThreadLocalRandom.current().nextGaussian(((Integer)this.cps.get()).intValue(), 5.0))))) {
            ++this.attacks;
            this.lastAttackTime = time;
        }
    }
}

