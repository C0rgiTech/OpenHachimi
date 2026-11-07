/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

import dev.openhachimi.utils.rotation.RotationManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import dev.openhachimi.utils.rotation.Rotation;
import dev.openhachimi.utils.rotation.RotationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class RaytraceUtil2 {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean inView(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || entity == null) {
            return false;
        }
        double renderDistance = (double)((Integer)mc.options.renderDistance().get()).intValue() * 16.0;
        Rotation rotations = RotationUtil.calculate(entity);
        if (Math.abs(Mth.wrapDegrees((float)(mc.player.getYRot() - rotations.getYaw()))) > (float)((Integer)mc.options.fov().get()).intValue()) {
            return false;
        }
        double dist = mc.player.distanceTo(entity);
        if (dist > 100.0 || !(entity instanceof Player)) {
            HitResult result = RaytraceUtil2.rayCast(rotations, renderDistance, 0.2f);
            return result != null && result.getType() == HitResult.Type.ENTITY;
        }
        AABB bb = entity.getBoundingBox();
        double width = bb.getXsize();
        double height = bb.getYsize();
        double depth = bb.getZsize();
        Vec3 basePos = entity.position();
        for (double yPercent = 1.0; yPercent >= -1.0; yPercent -= 0.5) {
            for (double xPercent = 1.0; xPercent >= -1.0; xPercent -= 1.0) {
                for (double zPercent = 1.0; zPercent >= -1.0; zPercent -= 1.0) {
                    Vec3 targetPoint = basePos.add(width * xPercent, height * yPercent, depth * zPercent);
                    Rotation subRotations = RotationUtil.calculate(targetPoint);
                    HitResult result = RaytraceUtil2.rayCast(subRotations, renderDistance, 0.2f);
                    if (result == null || result.getType() != HitResult.Type.ENTITY) continue;
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean overBlock(Direction enumFacing, BlockPos pos, boolean strict) {
        return RaytraceUtil2.overBlock(RotationManager.getRotation(), Direction.UP, pos, false);
    }

    public static Boolean overBlock(Rotation rotation, BlockPos pos) {
        return RaytraceUtil2.overBlock(rotation, Direction.UP, pos, false);
    }

    public static Boolean overBlock(Rotation rotation, BlockPos pos, Direction enumFacing) {
        return RaytraceUtil2.overBlock(rotation, enumFacing, pos, true);
    }

    public static boolean overBlock(Rotation rotation, Direction enumFacing, BlockPos pos, boolean strict) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 lookVec = Vec3.directionFromRotation((float)rotation.getPitch(), (float)rotation.getYaw());
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        double reach = 4.5;
        Vec3 endVec = eyePos.add(lookVec.scale(4.5));
        BlockHitResult result = mc.level.clip(new ClipContext(eyePos, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)mc.player));
        if (result.getType() == HitResult.Type.MISS) {
            return false;
        }
        return result.getBlockPos().equals((Object)pos) && (!strict || result.getDirection() == enumFacing);
    }

    public static HitResult rayCast(Rotation rotation, double range) {
        return RaytraceUtil2.rayCast(rotation, range, 0.0f);
    }

    public static HitResult rayCast(Rotation rotation, double range, float expand, Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || entity == null) {
            return null;
        }
        float partialTicks = mc.getFrameTime();
        Vec3 eyePos = entity.getEyePosition(partialTicks);
        Vec3 lookVec = Vec3.directionFromRotation((float)rotation.getPitch(), (float)rotation.getYaw());
        Vec3 endVec = eyePos.add(lookVec.scale(range));
        BlockHitResult objectMouseOver = mc.level.clip(new ClipContext(eyePos, endVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
        double distToBlock = range;
        if (objectMouseOver.getType() != HitResult.Type.MISS) {
            distToBlock = objectMouseOver.getLocation().distanceTo(eyePos);
        }
        Vec3 entitySearchEndVec = eyePos.add(lookVec.scale(range));
        Entity pointedEntity = null;
        Vec3 hitVec = null;
        double currentDist = distToBlock;
        AABB searchBox = entity.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(1.0);
        List<Entity> list = (List<Entity>)(Object)mc.level.getEntities(entity, searchBox, (Predicate)new Predicate<Entity>(){

            @Override
            public boolean test(Entity e) {
                return !e.isSpectator() && e.isPickable();
            }
        });
        for (Entity candidate : list) {
            Vec3 interceptVec;
            double d3;
            float collisionSize = candidate.getPickRadius() + expand;
            AABB entityBox = candidate.getBoundingBox().inflate((double)collisionSize);
            Optional<Vec3> intercept = entityBox.clip(eyePos, entitySearchEndVec);
            if (entityBox.contains(eyePos)) {
                if (!(currentDist >= 0.0)) continue;
                pointedEntity = candidate;
                hitVec = intercept.orElse(eyePos);
                currentDist = 0.0;
                continue;
            }
            if (!intercept.isPresent() || !((d3 = eyePos.distanceTo(interceptVec = (Vec3)intercept.get())) < currentDist) && currentDist != 0.0) continue;
            if (candidate.getRootVehicle() == entity.getRootVehicle() && !candidate.canRiderInteract()) {
                if (currentDist != 0.0) continue;
                pointedEntity = candidate;
                hitVec = interceptVec;
                continue;
            }
            pointedEntity = candidate;
            hitVec = interceptVec;
            currentDist = d3;
        }
        if (pointedEntity != null && (currentDist < distToBlock || objectMouseOver.getType() == HitResult.Type.MISS)) {
            return new EntityHitResult(pointedEntity, hitVec);
        }
        return objectMouseOver;
    }

    public static HitResult rayCast(Rotation rotation, double range, float expand) {
        return RaytraceUtil2.rayCast(rotation, range, expand, (Entity) RaytraceUtil2.mc.player);
    }
}

