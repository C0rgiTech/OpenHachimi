/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.NumberSetting;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;

public class ProjaimModule
extends Module {
    private static final byte[] S_50C85C88 = new byte[]{18, 48, 45, 40, 3, 43, 47};  // XOR0x42 "ProjAim"
    private static final byte[] S_4C61027 = new byte[]{17, 50, 39, 39, 38};  // XOR0x42 "Speed"
    private static final byte[] S_110AD = new byte[]{4, 13, 20};  // XOR0x42 "FOV"
    private static final byte[] S_4B1491D = new byte[]{16, 35, 44, 37, 39};  // XOR0x42 "Range"
    private static final double DRAG = 0.99;
    private static final double GRAVITY = 0.05;
    private static final double DRAG_INV = 99.99999999999991;
    private static final double GRAVITY_TERM = 4.999999999999996;
    private static final double BASE_VELOCITY = 3.0;
    private static final float MIN_POWER = 0.35f;
    private static final int LEAD_ITERATIONS = 8;
    private static final double LEAD_TOLERANCE = 0.03;
    private final ListSetting speed = new ListSetting("Speed", 45, 10, 100){

        @Override
        public String getDisplay() {
            return String.valueOf((double)((Integer)this.get()).intValue() / 10.0);
        }
    };
    private final ListSetting fov = new ListSetting("FOV", 60, 10, 180);
    private final NumberSetting range = new NumberSetting("Range", 20.0f, 5.0f, 40.0f);
    private LivingEntity target;

    public ProjaimModule() {
        super("ProjAim", Category.COMBAT);
        this.values.add(this.speed);
        this.values.add(this.fov);
        this.values.add(this.range);
    }

    private LivingEntity findTarget(Minecraft mc) {
        LivingEntity bestTarget = null;
        double bestAngle = Double.MAX_VALUE;
        double range = ((Float)this.range.get()).floatValue();
        Iterable<Entity> entities = mc.level.entitiesForRendering();
        if (entities == null) {
            return null;
        }
        for (Entity entity : entities) {
            float pitchDiff;
            double angleDistance;
            double[] rotation;
            float yawDiff;
            LivingEntity living;
            if (!(entity instanceof LivingEntity) || !this.isValidTarget(mc, living = (LivingEntity)entity) || (double)mc.player.distanceTo(entity) > range || (yawDiff = Math.abs(Mth.wrapDegrees((float)((float)((rotation = ProjaimModule.angleTo(mc, (Entity)living))[0] - (double)mc.player.getYRot()))))) > (float)((Integer)this.fov.get()).intValue() || !((angleDistance = Math.sqrt((double)(yawDiff * yawDiff) + (double)((pitchDiff = Math.abs((float)rotation[1] - mc.player.getXRot())) * pitchDiff))) < bestAngle)) continue;
            bestAngle = angleDistance;
            bestTarget = living;
        }
        return bestTarget;
    }

    private static double[] angleTo(Minecraft mc, Entity entity) {
        double targetHeight = Math.max(0.0, Math.min(mc.player.getY() - entity.getY() + (double)mc.player.getEyeHeight(), (entity.getBoundingBox().maxY - entity.getBoundingBox().minY) * 0.9));
        Vec3 difference = entity.position().add(0.0, targetHeight, 0.0).subtract(mc.player.getEyePosition());
        double horizontal = Math.hypot(difference.x, difference.z);
        float yaw = (float)(Mth.atan2((double)difference.z, (double)difference.x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Mth.atan2((double)difference.y, (double)horizontal) * 180.0 / Math.PI));
        return new double[]{yaw, pitch};
    }

    private boolean isValidTarget(Minecraft mc, LivingEntity entity) {
        if (entity == null || entity == mc.player) {
            return false;
        }
        if (!(entity instanceof Player)) {
            return false;
        }
        if (!entity.isAlive() || entity.isRemoved()) {
            return false;
        }
        if (AntibotModule.isBot((Entity)entity)) {
            return false;
        }
        if (TeamsModule.isSameTeam((Entity)entity)) {
            return false;
        }
        return mc.player.hasLineOfSight((Entity)entity);
    }

    private static double ticksToReach(double horiz, double pitchDeg, double speed) {
        double cos = Math.cos(Math.toRadians(pitchDeg));
        if (cos <= 1.0E-4) {
            return Double.NaN;
        }
        double k = horiz / (99.99999999999991 * speed * cos);
        if (!(k > 0.0) || k >= 1.0) {
            return Double.NaN;
        }
        return Math.log(1.0 - k) / Math.log(0.99);
    }

    private double[] solveLead(Minecraft mc, LivingEntity target, double speed) {
        if (!(speed > 0.5) || !Double.isFinite(speed)) {
            return null;
        }
        double ox = mc.player.getX();
        double oy = mc.player.getEyeY() - 0.1;
        double oz = mc.player.getZ();
        double tvx = target.getX() - target.xOld;
        double tvy = target.getY() - target.yOld;
        double tvz = target.getZ() - target.zOld;
        if (!Double.isFinite(tvx) || !Double.isFinite(tvy) || !Double.isFinite(tvz) || tvx * tvx + tvy * tvy + tvz * tvz > 4.0) {
            tvx = 0.0;
            tvy = 0.0;
            tvz = 0.0;
        }
        double tx = target.getX();
        double ty = target.getY();
        double tz = target.getZ();
        double aimY = ty + (double)target.getBbHeight() * 0.5;
        double flight = Math.max(Math.hypot(tx - ox, tz - oz) / speed, 1.0);
        double yaw = 0.0;
        double pitch = 0.0;
        for (int iter = 0; iter < 8; ++iter) {
            double ax = tx + tvx * flight;
            double ay = aimY + tvy * flight;
            double dx = ax - ox;
            double az = tz + tvz * flight;
            double dz = az - oz;
            double horiz = Math.hypot(dx, dz);
            if (!(horiz > 0.4) || !Double.isFinite(horiz)) {
                return null;
            }
            yaw = Math.toDegrees(Math.atan2(-dx, dz));
            double solved = ProjaimModule.solvePitch(horiz, ay - oy, speed);
            if (Double.isNaN(solved)) {
                return null;
            }
            pitch = solved;
            double ticks = ProjaimModule.ticksToReach(horiz, pitch, speed);
            if (Double.isNaN(ticks)) {
                return null;
            }
            if (Math.abs(ticks - flight) < 0.03) break;
            flight = ticks;
        }
        if (!Double.isFinite(yaw) || !Double.isFinite(pitch)) {
            return null;
        }
        return new double[]{yaw, pitch};
    }

    private static double solvePitch(double horiz, double height, double speed) {
        double ratio = horiz / (99.99999999999991 * speed);
        if (!(ratio > 0.0) || ratio >= 0.999) {
            return Double.NaN;
        }
        double limit = Math.toDegrees(Math.acos(ratio)) - 0.35;
        if (!(limit > 0.5)) {
            return Double.NaN;
        }
        double prevTheta = limit;
        double prevF = ProjaimModule.yAtDistance(horiz, limit, speed) - height;
        double theta = limit - 1.0;
        while (theta >= -limit) {
            double f = ProjaimModule.yAtDistance(horiz, theta, speed) - height;
            if (Double.isNaN(f)) {
                prevTheta = theta;
                prevF = Double.NaN;
                theta -= 1.0;
                continue;
            }
            if (prevF <= 0.0 && f > 0.0) {
                double lo = prevTheta;
                double hi = theta;
                for (int i = 0; i < 50; ++i) {
                    double mid = (lo + hi) * 0.5;
                    double fm = ProjaimModule.yAtDistance(horiz, mid, speed) - height;
                    if (Double.isNaN(fm)) {
                        return Double.NaN;
                    }
                    if (fm > 0.0) {
                        hi = mid;
                        continue;
                    }
                    lo = mid;
                }
                return (lo + hi) * 0.5;
            }
            prevTheta = theta;
            prevF = f;
            theta -= 1.0;
        }
        return Double.NaN;
    }

    private static float quantizeToward(float step, float remaining, double sensitivityStep) {
        if (step == 0.0f || remaining == 0.0f) {
            return 0.0f;
        }
        double rounded = (float)((double)Math.round((double)step / sensitivityStep) * sensitivityStep);
        if (Math.abs(rounded) < sensitivityStep * 0.5) {
            if ((double)Math.abs(remaining) >= sensitivityStep) {
                rounded = Math.copySign(sensitivityStep, (double)remaining);
            } else {
                return 0.0f;
            }
        }
        if (Math.signum(rounded) != (double)Math.signum(remaining)) {
            return 0.0f;
        }
        if (Math.abs(rounded) > (double)Math.abs(remaining)) {
            double floored = Math.floor((double)Math.abs(remaining) / sensitivityStep) * sensitivityStep;
            if (floored < sensitivityStep * 0.5) {
                return 0.0f;
            }
            return (float)Math.copySign(floored, (double)remaining);
        }
        return (float)rounded;
    }

    private static float clampAbs(float value, float maxAbs) {
        if (maxAbs <= 0.0f) {
            return 0.0f;
        }
        if (value > maxAbs) {
            return maxAbs;
        }
        if (value < -maxAbs) {
            return -maxAbs;
        }
        return value;
    }

    private static boolean isChargingBow(Player player) {
        try {
            return player.isUsingItem() && player.getUseItem() != null && player.getUseItem().getItem() instanceof BowItem;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private static double yAtDistance(double horiz, double pitchDeg, double speed) {
        double rad = Math.toRadians(pitchDeg);
        double cos = Math.cos(rad);
        if (cos <= 1.0E-4) {
            return Double.NaN;
        }
        double k = horiz / (99.99999999999991 * speed * cos);
        if (!(k > 0.0) || k >= 1.0) {
            return Double.NaN;
        }
        double ticks = Math.log(1.0 - k) / Math.log(0.99);
        return 99.99999999999991 * (4.999999999999996 - speed * Math.sin(rad)) * k - 4.999999999999996 * ticks;
    }

    @Override
    public void onDisable() {
        this.target = null;
    }

    @Override
    public void onTick() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null || !ProjaimModule.isChargingBow((Player)mc.player)) {
                this.target = null;
                return;
            }
            this.target = this.findTarget(mc);
        }
        catch (Throwable throwable) {
            this.target = null;
        }
    }

    @Override
    public void onWorldChange() {
        this.target = null;
    }

    @Override
    public void onRender3D() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || mc.level == null || this.target == null) {
                return;
            }
            if (!ProjaimModule.isChargingBow((Player)mc.player) || !this.target.isAlive() || this.target.isRemoved() || !this.isValidTarget(mc, this.target) || (double)mc.player.distanceTo((Entity)this.target) > (double)((Float)this.range.get()).floatValue()) {
                this.target = null;
                return;
            }
            int charge = mc.player.getUseItem().getUseDuration() - mc.player.getUseItemRemainingTicks();
            float power = BowItem.getPowerForTime((int)charge);
            if (power < 0.35f) {
                return;
            }
            double[] rotation = this.solveLead(mc, this.target, 3.0 * (double)power);
            if (rotation == null) {
                return;
            }
            float remainingYaw = Mth.wrapDegrees((float)((float)(rotation[0] - (double)mc.player.getYRot())));
            float remainingPitch = (float)rotation[1] - mc.player.getXRot();
            if (Math.abs(remainingYaw) > (float)((Integer)this.fov.get()).intValue()) {
                return;
            }
            float speedValue = (float)((Integer)this.speed.get()).intValue() / 10.0f;
            float maxStep = speedValue * (100.0f / (float)Math.max(mc.getFps(), 1));
            float moveYaw = ProjaimModule.clampAbs(remainingYaw, maxStep);
            float movePitch = ProjaimModule.clampAbs(remainingPitch, maxStep);
            double sensitivity = (Double)mc.options.sensitivity().get() * 0.6 + 0.2;
            double sensitivityStep = sensitivity * sensitivity * sensitivity * 8.0 * 0.15;
            if (sensitivityStep < 1.0E-4) {
                sensitivityStep = 1.0E-4;
            }
            float fixedX = ProjaimModule.quantizeToward(moveYaw, remainingYaw, sensitivityStep);
            float fixedY = ProjaimModule.quantizeToward(movePitch, remainingPitch, sensitivityStep);
            if (fixedX == 0.0f && fixedY == 0.0f) {
                return;
            }
            mc.player.turn((double)fixedX, (double)fixedY);
        }
        catch (Throwable throwable) {
            this.target = null;
        }
    }
}

