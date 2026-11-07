/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RayTraceUtil {
    public static HitResult pick(double pHitDistance, float pPartialTicks, boolean pHitFluids, float pYRot, float pXRot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return null;
        }
        Vec3 vec3 = new Vec3(mc.player.getX(), mc.player.getY() + (double)mc.player.getEyeHeight(), mc.player.getZ());
        Vec3 vec31 = RayTraceUtil.calculateViewVector(pXRot, pYRot);
        Vec3 vec32 = vec3.add(vec31.x * pHitDistance, vec31.y * pHitDistance, vec31.z * pHitDistance);
        return mc.player.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, pHitFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, (Entity)mc.player));
    }

    public static HitResult rayTraceBlocks(Vec3 vec31, Vec3 vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, Entity var6) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        ClipContext.Block var7 = ignoreBlockWithoutBoundingBox ? ClipContext.Block.COLLIDER : (returnLastUncollidableBlock ? ClipContext.Block.VISUAL : ClipContext.Block.OUTLINE);
        ClipContext.Fluid var8 = stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext var9 = new ClipContext(vec31, vec32, var7, var8, var6);
        return mc.level.clip(var9);
    }

    public static EntityHitResult calculateIntercept(AABB instance, Vec3 var1, Vec3 var2) {
        Optional e = instance.clip(var1, var2);
        return (EntityHitResult) e.map(new Function<Vec3, EntityHitResult>(){

            @Override
            public EntityHitResult apply(Vec3 vec3) {
                return new EntityHitResult(null, vec3);
            }
        }).orElse(null);
    }

    public static Vec3 calculateViewVector(float pXRot, float pYRot) {
        float f = pXRot * ((float)Math.PI / 180);
        float f1 = -pYRot * ((float)Math.PI / 180);
        float f2 = Mth.cos((float)f1);
        float f3 = Mth.sin((float)f1);
        float f4 = Mth.cos((float)f);
        float f5 = Mth.sin((float)f);
        return new Vec3((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
    }
}

