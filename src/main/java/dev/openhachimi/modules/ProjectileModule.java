/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.utils.rotation.RotationManager;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.utils.ExecutorHelper;
import dev.openhachimi.utils.player.RayTraceUtil;
import dev.openhachimi.utils.PotionData;
import dev.openhachimi.utils.ThrowableData;
import dev.openhachimi.utils.TargetHelper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class ProjectileModule
extends Module {
    private static final byte[] S_166A609 = new byte[]{18, 48, 45, 40, 39, 33, 54, 43, 46, 39};  // XOR0x42 "Projectile"
    private static final byte[] S_2982480D = new byte[]{17, 42, 45, 53, 98, 3, 48, 48, 45, 53, 49};  // XOR0x42 "Show Arrows"
    private static final byte[] S_425C1770 = new byte[]{17, 42, 45, 53, 98, 18, 39, 35, 48, 46, 49};  // XOR0x42 "Show Pearls"
    private static final byte[] S_1B3EFF1D = new byte[]{17, 42, 45, 53, 98, 18, 45, 54, 43, 45, 44, 49};  // XOR0x42 "Show Potions"
    private static final byte[] S_2727851 = new byte[]{17, 42, 45, 53, 98, 7, 37, 37, 49};  // XOR0x42 "Show Eggs"
    private static final byte[] S_2B0F554E = new byte[]{17, 42, 45, 53, 98, 17, 44, 45, 53, 32, 35, 46, 46, 49};  // XOR0x42 "Show Snowballs"
    private final ExecutorHelper arrowsColor = new ExecutorHelper();
    private final PotionData potionsColor = new PotionData();
    private final TargetHelper enderPearlColor = new TargetHelper(Collections.singleton(ThrownEnderpearl.class), new Color(173, 12, 255));
    private final TargetHelper eggColor = new TargetHelper(Collections.singleton(ThrownEgg.class), new Color(255, 238, 154));
    private final TargetHelper snowballColor = new TargetHelper(Collections.singleton(Snowball.class), new Color(255, 255, 255));
    private final BooleanSetting showArrows = new BooleanSetting("Show Arrows", true);
    private final BooleanSetting showPearls = new BooleanSetting("Show Pearls", true);
    private final BooleanSetting showPotions = new BooleanSetting("Show Potions", false);
    private final BooleanSetting showEggs = new BooleanSetting("Show Eggs", false);
    private final BooleanSetting showSnowballs = new BooleanSetting("Show Snowballs", false);

    public ProjectileModule() {
        super("Projectile", Category.RENDER);
        this.values.add(this.showArrows);
        this.values.add(this.showPearls);
        this.values.add(this.showPotions);
        this.values.add(this.showEggs);
        this.values.add(this.showSnowballs);
    }

    private Path getPath(float partialTicks, Minecraft mc) {
        LocalPlayer player = mc.player;
        ArrayList<Vec3> path = new ArrayList<Vec3>();
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        if (!stack.isEmpty() && this.isThrowable(item)) {
            double pitch;
            double yaw;
            if (item instanceof BowItem && !((Boolean)this.showArrows.get()).booleanValue()) {
                return null;
            }
            if (item instanceof CrossbowItem && !((Boolean)this.showArrows.get()).booleanValue()) {
                return null;
            }
            if (item instanceof SnowballItem && !((Boolean)this.showSnowballs.get()).booleanValue()) {
                return null;
            }
            if (item instanceof EnderpearlItem && !((Boolean)this.showPearls.get()).booleanValue()) {
                return null;
            }
            if (item instanceof EggItem && !((Boolean)this.showEggs.get()).booleanValue()) {
                return null;
            }
            if (item instanceof SplashPotionItem && !((Boolean)this.showPotions.get()).booleanValue()) {
                return null;
            }
            if (item instanceof LingeringPotionItem && !((Boolean)this.showPotions.get()).booleanValue()) {
                return null;
            }
            boolean isBow = item instanceof BowItem || item instanceof CrossbowItem;
            double arrowPosX = player.xOld + (player.getX() - player.xOld) * (double)partialTicks;
            double arrowPosY = player.yOld + (player.getY() - player.yOld) * (double)partialTicks + (double)player.getEyeHeight() - 0.1;
            double arrowPosZ = player.zOld + (player.getZ() - player.zOld) * (double)partialTicks;
            if (RotationManager.isRotating) {
                yaw = Math.toRadians(RotationManager.targetYaw);
                pitch = Math.toRadians(RotationManager.targetPitch);
            } else {
                yaw = Math.toRadians(Mth.lerp((float)partialTicks, (float)player.yRotO, (float)player.getYRot()));
                pitch = Math.toRadians(Mth.lerp((float)partialTicks, (float)player.xRotO, (float)player.getXRot()));
            }
            arrowPosX -= Math.cos(yaw) * 0.16;
            arrowPosZ -= Math.sin(yaw) * 0.16;
            float pitchDifference = 0.0f;
            float motionFactor = 1.5f;
            float motionSlowdown = 0.99f;
            if (isBow) {
                float bowPower = (float)(72000 - player.getUseItemRemainingTicks()) / 20.0f;
                if ((bowPower = (bowPower * bowPower + bowPower * 2.0f) / 3.0f) > 1.0f || bowPower <= 0.1f) {
                    bowPower = 1.0f;
                }
                motionFactor = bowPower * 3.0f;
            } else if (item instanceof FishingRodItem) {
                motionSlowdown = 0.92f;
            } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
                pitchDifference = -20.0f;
                motionFactor = 0.5f;
            }
            double arrowMotionX = -Math.sin(yaw) * Math.cos(pitch) * (isBow ? 1.0 : 0.4);
            double arrowMotionY = -Math.sin(pitch + Math.toRadians(pitchDifference)) * (isBow ? 1.0 : 0.4);
            double arrowMotionZ = Math.cos(yaw) * Math.cos(pitch) * (isBow ? 1.0 : 0.4);
            double arrowMotion = Math.sqrt(arrowMotionX * arrowMotionX + arrowMotionY * arrowMotionY + arrowMotionZ * arrowMotionZ);
            arrowMotionX /= arrowMotion;
            arrowMotionY /= arrowMotion;
            arrowMotionZ /= arrowMotion;
            arrowMotionX *= (double)motionFactor;
            arrowMotionY *= (double)motionFactor;
            arrowMotionZ *= (double)motionFactor;
            double gravity = this.getProjectileGravity(item);
            for (int i = 0; i < 1000; ++i) {
                Vec3 arrowPos = new Vec3(arrowPosX, arrowPosY, arrowPosZ);
                Vec3 postArrowPos = new Vec3(arrowPosX + arrowMotionX, arrowPosY + arrowMotionY, arrowPosZ + arrowMotionZ);
                path.add(arrowPos);
                ClipContext context = new ClipContext(arrowPos, postArrowPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)mc.player);
                BlockHitResult clip = mc.level.clip(context);
                if (clip != null && clip.getType() != HitResult.Type.MISS) {
                    return new Path(path, (HitResult)clip);
                }
                AABB aabb = new AABB(arrowPosX - 0.25, arrowPosY - 0.25, arrowPosZ - 0.25, arrowPosX + 0.25, arrowPosY + 0.25, arrowPosZ + 0.25);
                EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult((Level)mc.level, (Entity)player, (Vec3)arrowPos, (Vec3)postArrowPos, (AABB)aabb.expandTowards(new Vec3(arrowMotionX, arrowMotionY, arrowMotionZ)).inflate(1.0), entity -> entity != player && entity instanceof LivingEntity);
                if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                    return new Path(path, (HitResult)entityHitResult);
                }
                arrowPosX += arrowMotionX;
                arrowPosY += arrowMotionY;
                arrowPosZ += arrowMotionZ;
                arrowMotionX *= (double)motionSlowdown;
                arrowMotionY *= (double)motionSlowdown;
                arrowMotionZ *= (double)motionSlowdown;
                arrowMotionY -= gravity;
            }
            return new Path(path, null);
        }
        return null;
    }

    private boolean isThrowable(Item item) {
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof SnowballItem || item instanceof EggItem || item instanceof EnderpearlItem || item instanceof SplashPotionItem || item instanceof LingeringPotionItem || item instanceof FishingRodItem || item instanceof TridentItem;
    }

    private void addRing(BufferBuilder builder, Matrix4f matrix, float cx, float cy, float cz, float[] u, float[] v, float innerR, float outerR, int r, int g, int b, int innerAlpha, int outerAlpha) {
        int segments = 40;
        for (int i = 0; i < segments; ++i) {
            double a0 = Math.PI * 2 * (double)i / (double)segments;
            double a1 = Math.PI * 2 * (double)(i + 1) / (double)segments;
            float c0 = (float)Math.cos(a0);
            float s0 = (float)Math.sin(a0);
            float c1 = (float)Math.cos(a1);
            float s1 = (float)Math.sin(a1);
            builder.vertex(matrix, cx + (c0 * u[0] + s0 * v[0]) * innerR, cy + (c0 * u[1] + s0 * v[1]) * innerR, cz + (c0 * u[2] + s0 * v[2]) * innerR).color(r, g, b, innerAlpha).endVertex();
            builder.vertex(matrix, cx + (c0 * u[0] + s0 * v[0]) * outerR, cy + (c0 * u[1] + s0 * v[1]) * outerR, cz + (c0 * u[2] + s0 * v[2]) * outerR).color(r, g, b, outerAlpha).endVertex();
            builder.vertex(matrix, cx + (c1 * u[0] + s1 * v[0]) * outerR, cy + (c1 * u[1] + s1 * v[1]) * outerR, cz + (c1 * u[2] + s1 * v[2]) * outerR).color(r, g, b, outerAlpha).endVertex();
            builder.vertex(matrix, cx + (c1 * u[0] + s1 * v[0]) * innerR, cy + (c1 * u[1] + s1 * v[1]) * innerR, cz + (c1 * u[2] + s1 * v[2]) * innerR).color(r, g, b, innerAlpha).endVertex();
        }
    }

    private List<ThrowableData> getProjectileInfos() {
        ArrayList<ThrowableData> infos = new ArrayList<ThrowableData>();
        if (((Boolean)this.showArrows.get()).booleanValue()) {
            infos.add(this.arrowsColor);
        }
        if (((Boolean)this.showPotions.get()).booleanValue()) {
            infos.add(this.potionsColor);
        }
        if (((Boolean)this.showPearls.get()).booleanValue()) {
            infos.add(this.enderPearlColor);
        }
        if (((Boolean)this.showEggs.get()).booleanValue()) {
            infos.add(this.eggColor);
        }
        if (((Boolean)this.showSnowballs.get()).booleanValue()) {
            infos.add(this.snowballColor);
        }
        return infos;
    }

    private Path calculateActiveProjectilePath(Entity entity, ThrowableData projectileInfo, Minecraft mc) {
        ArrayList<Vec3> out = new ArrayList<Vec3>();
        double posX = entity.getX();
        double posY = entity.getY();
        double posZ = entity.getZ();
        double motionX = entity.getDeltaMovement().x;
        double motionY = entity.getDeltaMovement().y;
        double motionZ = entity.getDeltaMovement().z;
        out.add(new Vec3(posX, posY, posZ));
        HitResult movingObj = null;
        for (int i = 0; i < 200; ++i) {
            float data1 = projectileInfo.getData1();
            float data2 = projectileInfo.getData2();
            AABB aabb = new AABB(posX - (double)data1, posY, posZ - (double)data1, posX + (double)data1, posY + (double)data2, posZ + (double)data1);
            Vec3 vec3 = new Vec3(posX, posY, posZ);
            Vec3 vec3WithMotion = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            movingObj = RayTraceUtil.rayTraceBlocks(vec3, vec3WithMotion, false, entity instanceof Arrow, false, entity);
            if (movingObj != null && !movingObj.getType().equals((Object)HitResult.Type.MISS)) {
                vec3WithMotion = new Vec3(movingObj.getLocation().x(), movingObj.getLocation().y(), movingObj.getLocation().z());
            }
            List<Entity> getByAABBEntitys = mc.level.getEntities((Entity)mc.player, aabb.contract(motionX, motionY, motionZ).expandTowards(1.0, 1.0, 1.0));
            double lastMinDistance = 0.0;
            for (Entity aabbEntity : getByAABBEntitys) {
                double distance;
                AABB aabb2;
                EntityHitResult aabbMovingObj;
                if (!(aabbEntity instanceof LivingEntity) || aabbEntity instanceof EnderMan || !aabbEntity.canBeCollidedWith() || aabbEntity.equals((Object)mc.player) || (aabbMovingObj = RayTraceUtil.calculateIntercept(aabb2 = aabbEntity.getBoundingBox().expandTowards(0.3, 0.3, 0.3), vec3, vec3WithMotion)) == null || !((distance = vec3.distanceTo(aabbMovingObj.getLocation())) < lastMinDistance) && lastMinDistance != 0.0) continue;
                lastMinDistance = distance;
                movingObj = aabbMovingObj;
            }
            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            if (movingObj != null && !movingObj.getType().equals((Object)HitResult.Type.MISS)) {
                posX = movingObj.getLocation().x();
                posY = movingObj.getLocation().y();
                posZ = movingObj.getLocation().z();
                out.add(new Vec3(posX, posY, posZ));
                break;
            }
            if (posY < -128.0) break;
            motionX *= entity.isInWater() ? 0.8 : 0.99;
            double var39 = motionY * (entity.isInWater() ? 0.8 : 0.99);
            motionZ *= entity.isInWater() ? 0.8 : 0.99;
            motionY = var39 - (double)projectileInfo.getGravity();
            out.add(new Vec3(posX, posY, posZ));
        }
        return new Path(out, movingObj);
    }

    private void drawPearlLandingMarker(BufferBuilder builder, Matrix4f matrix, Vec3 camPos, Path pathResult) {
        float[] v;
        float[] u;
        if (pathResult == null || pathResult.getResult() == null) {
            return;
        }
        HitResult hit = pathResult.getResult();
        if (hit.getType() == HitResult.Type.MISS) {
            return;
        }
        Vec3 hitPos = hit.getLocation();
        Direction face = Direction.UP;
        boolean flat = false;
        if (hit instanceof BlockHitResult) {
            face = ((BlockHitResult)hit).getDirection();
            flat = face == Direction.UP;
        }
        int r = flat ? 144 : 255;
        int g = flat ? 238 : 255;
        int b = flat ? 144 : 255;
        float[] n = new float[]{face.getStepX(), face.getStepY(), face.getStepZ()};
        if (face.getAxis() == Direction.Axis.Y) {
            u = new float[]{1.0f, 0.0f, 0.0f};
            v = new float[]{0.0f, 0.0f, 1.0f};
        } else if (face.getAxis() == Direction.Axis.Z) {
            u = new float[]{1.0f, 0.0f, 0.0f};
            v = new float[]{0.0f, 1.0f, 0.0f};
        } else {
            u = new float[]{0.0f, 0.0f, 1.0f};
            v = new float[]{0.0f, 1.0f, 0.0f};
        }
        float lift = 0.02f;
        float ox = (float)(hitPos.x - camPos.x) + n[0] * lift;
        float oy = (float)(hitPos.y - camPos.y) + n[1] * lift;
        float oz = (float)(hitPos.z - camPos.z) + n[2] * lift;
        float dist = (float)Math.sqrt(ox * ox + oy * oy + oz * oz);
        float scale = Math.max(1.0f, dist / 10.0f);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        this.addRing(builder, matrix, ox, oy, oz, u, v, 0.0f, 0.18f * scale, r, g, b, 80, 0);
        this.addRing(builder, matrix, ox, oy, oz, u, v, 0.47f * scale, 0.5f * scale, r, g, b, 160, 160);
        BufferUploader.drawWithShader((BufferBuilder.RenderedBuffer)builder.end());
    }

    private ThrowableData getProjectileDataByEntity(Entity entity) {
        if (entity.onGround()) {
            return null;
        }
        if (entity.getX() == entity.xOld && entity.getZ() == entity.zOld) {
            return null;
        }
        for (ThrowableData data : this.getProjectileInfos()) {
            if (!data.isTargetEntity(entity)) continue;
            return data;
        }
        return null;
    }

    private double getProjectileGravity(Item item) {
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            return 0.05;
        }
        if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            return 0.05;
        }
        if (item instanceof FishingRodItem) {
            return 0.04;
        }
        return 0.03;
    }

    @Override
    public void onRender3D() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        RenderLevelStageEvent event = ModuleManager.lastRenderEvent;
        if (event == null || event.getPoseStack() == null || event.getCamera() == null) {
            return;
        }
        float partialTicks = mc.getFrameTime();
        PoseStack stack = event.getPoseStack();
        Matrix4f matrix = stack.last().pose();
        Vec3 camPos = event.getCamera().getPosition();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        RenderSystem.lineWidth((float)8.0f);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();
        for (Entity entity : mc.level.entitiesForRendering()) {
            Path activePathResult;
            ThrowableData data;
            if (!(entity instanceof Projectile) || (data = this.getProjectileDataByEntity(entity)) == null) continue;
            Color c = data.getColor(entity);
            if (c == null) {
                c = Color.WHITE;
            }
            if ((activePathResult = this.calculateActiveProjectilePath(entity, data, mc)) == null) continue;
            List<Vec3> activePath = activePathResult.getPath();
            if (activePath.size() >= 2) {
                builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                for (Vec3 p : activePath) {
                    builder.vertex(matrix, (float)(p.x - camPos.x), (float)(p.y - camPos.y), (float)(p.z - camPos.z)).color(c.getRed(), c.getGreen(), c.getBlue(), 255).endVertex();
                }
                BufferUploader.drawWithShader((BufferBuilder.RenderedBuffer)builder.end());
            }
            if (!(entity instanceof ThrownEnderpearl)) continue;
            this.drawPearlLandingMarker(builder, matrix, camPos, activePathResult);
        }
        Path pathResult = this.getPath(partialTicks, mc);
        if (pathResult != null) {
            ItemStack held;
            List<Vec3> path = pathResult.getPath();
            if (path.size() >= 2) {
                builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
                for (Vec3 p : path) {
                    builder.vertex(matrix, (float)(p.x - camPos.x), (float)(p.y - camPos.y), (float)(p.z - camPos.z)).color(255, 255, 255, 255).endVertex();
                }
                BufferUploader.drawWithShader((BufferBuilder.RenderedBuffer)builder.end());
            }
            if (!(held = mc.player.getMainHandItem()).isEmpty() && held.getItem() instanceof EnderpearlItem) {
                this.drawPearlLandingMarker(builder, matrix, camPos, pathResult);
            }
        }
        GL11.glDisable((int)2848);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static class Path {
        private final List<Vec3> path;
        private final HitResult result;

        public Path(List<Vec3> path, HitResult result) {
            this.path = path;
            this.result = result;
        }

        public List<Vec3> getPath() {
            return this.path;
        }

        public HitResult getResult() {
            return this.result;
        }
    }
}

