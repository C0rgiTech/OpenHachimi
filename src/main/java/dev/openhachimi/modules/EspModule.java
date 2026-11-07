/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.utils.W2SUtil;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class EspModule
extends Module {
    private static final byte[] S_10D62 = new byte[]{7, 17, 18};  // XOR0x42 "ESP"
    private static final byte[] S_45CD5992 = new byte[]{18, 46, 35, 59, 39, 48, 49};  // XOR0x42 "Players"
    private static final byte[] S_27941A0 = new byte[]{17, 42, 45, 53, 98, 22, 39, 35, 47};  // XOR0x42 "Show Team"
    private static final byte[] S_2713ACF = new byte[]{17, 42, 45, 53, 98, 0, 45, 54, 49};  // XOR0x42 "Show Bots"
    private static final byte[] S_24AD93 = new byte[]{15, 45, 32, 49};  // XOR0x42 "Mobs"
    private static final byte[] S_3024C9D7 = new byte[]{3, 44, 43, 47, 35, 46, 49};  // XOR0x42 "Animals"
    private static final byte[] S_43AF7A0 = new byte[]{11, 54, 39, 47, 49};  // XOR0x42 "Items"
    private static final byte[] S_7566F9AA = new byte[]{3, 48, 48, 45, 53, 49};  // XOR0x42 "Arrows"
    private static final byte[] S_2C0D4532 = new byte[]{17, 42, 45, 53, 98, 10, 39, 35, 46, 54, 42, 98, 0, 35, 48};  // XOR0x42 "Show Health Bar"
    private static final byte[] S_516AE5FA = new byte[]{10, 39, 35, 46, 54, 42, 98, 0, 35, 48, 98, 18, 45, 49, 43, 54, 43, 45, 44};  // XOR0x42 "Health Bar Position"
    private static final byte[] S_76F2864B = new byte[]{0, 45, 54, 54, 45, 47};  // XOR0x42 "Bottom"
    private static final byte[] S_14935 = new byte[]{22, 45, 50};  // XOR0x42 "Top"
    private static final byte[] S_241427 = new byte[]{14, 39, 36, 54};  // XOR0x42 "Left"
    private static final byte[] S_4B4D1FC = new byte[]{16, 43, 37, 42, 54};  // XOR0x42 "Right"
    private static final byte[] S_105AB = new byte[]{0, 45, 58};  // XOR0x42 "Box"
    private static final byte[] S_68E6B18 = new byte[]{0, 45, 58, 98, 15, 45, 38, 39};  // XOR0x42 "Box Mode"
    private static final byte[] S_2148A022 = new byte[]{13, 55, 54, 46, 43, 44, 39};  // XOR0x42 "Outline"
    private static final byte[] S_671 = new byte[]{113, 6};  // XOR0x42 "3D"
    private static final byte[] S_1C25709E = new byte[]{1, 45, 48, 44, 39, 48, 49};  // XOR0x42 "Corners"
    private static final int[][] BOX_FACES = new int[][]{{0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {3, 2, 6, 7}, {0, 3, 7, 4}, {1, 2, 6, 5}};
    private static final int[][] BOX_EDGES = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};
    private static final float NEAR_W = 0.02f;
    private static final int POLY_CAP = 16;
    private static final int AA_GRID = 4;
    private static final float[][] AA_OFFSETS = EspModule.buildAaOffsets();
    private final Vector4f[] nearPoly = new Vector4f[16];
    private final Vector2f[] screenPoly = new Vector2f[16];
    private final Vector2f[] clipPolyA = new Vector2f[16];
    private final Vector2f[] clipPolyB = new Vector2f[16];
    private final Vector4f edgeA = new Vector4f();
    private final BooleanSetting playersSetting = new BooleanSetting("Players", true);
    private final BooleanSetting showTeamSetting = new BooleanSetting("Show Team", true);
    private final BooleanSetting showBotsSetting = new BooleanSetting("Show Bots", true);
    private final BooleanSetting mobsSetting = new BooleanSetting("Mobs", false);
    private final BooleanSetting animalsSetting = new BooleanSetting("Animals", false);
    private final BooleanSetting itemsSetting = new BooleanSetting("Items", false);
    private final BooleanSetting arrowsSetting = new BooleanSetting("Arrows", true);
    private final BooleanSetting showHealthBarSetting = new BooleanSetting("Show Health Bar", true);
    private final ModeSetting healthBarPositionSetting = new ModeSetting("Health Bar Position", new String[]{"Bottom", "Top", "Left", "Right"}, "Bottom");
    private final BooleanSetting boxSetting = new BooleanSetting("Box", true);
    private final ModeSetting boxModeSetting = new ModeSetting("Box Mode", new String[]{"Outline", "Box", "3D", "Corners"}, "Outline");
    private final Map<Entity, ESPData> entityBoxPositions = new HashMap<Entity, ESPData>();
    private final List<Entity> visibleEntities = new ArrayList<Entity>();

    public EspModule() {
        super("ESP", Category.RENDER);
        for (int i = 0; i < 16; ++i) {
            this.nearPoly[i] = new Vector4f();
            this.screenPoly[i] = new Vector2f();
            this.clipPolyA[i] = new Vector2f();
            this.clipPolyB[i] = new Vector2f();
        }
        this.values.add(this.playersSetting);
        this.values.add(this.showTeamSetting);
        this.values.add(this.showBotsSetting);
        this.values.add(this.mobsSetting);
        this.values.add(this.animalsSetting);
        this.values.add(this.itemsSetting);
        this.values.add(this.arrowsSetting);
        this.values.add(this.showHealthBarSetting);
        this.values.add(this.healthBarPositionSetting);
        this.values.add(this.boxSetting);
        this.values.add(this.boxModeSetting);
    }

    private boolean isInRange(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        double distSq = mc.player.distanceToSqr(entity);
        return distSq < 10000.0;
    }

    private int clipFaceNear(Vector4f[] clipPoints, int[] face) {
        int out = 0;
        int size = face.length;
        for (int i = 0; i < size; ++i) {
            boolean nextInside;
            Vector4f cur = clipPoints[face[i]];
            Vector4f next = clipPoints[face[(i + 1) % size]];
            boolean curInside = cur.w > 0.02f;
            boolean bl = nextInside = next.w > 0.02f;
            if (curInside) {
                this.nearPoly[out++].set((Vector4fc)cur);
            }
            if (curInside == nextInside) continue;
            float t = (0.02f - cur.w) / (next.w - cur.w);
            this.nearPoly[out++].set(cur.x + (next.x - cur.x) * t, cur.y + (next.y - cur.y) * t, cur.z + (next.z - cur.z) * t, 0.02f);
        }
        return out;
    }

    private void fillPolygonAA(BufferBuilder builder, Matrix4f matrix4f, Vector2f[] points, int count, Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int samples = AA_OFFSETS.length;
        int a = Math.max(1, Math.round((float)color.getAlpha() / (float)samples));
        double guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        float unit = guiScale > 0.0 ? (float)(1.0 / guiScale) : 1.0f;
        for (float[] offset : AA_OFFSETS) {
            float ox = offset[0] * unit;
            float oy = offset[1] * unit;
            for (int i = 1; i < count - 1; ++i) {
                builder.vertex(matrix4f, points[0].x + ox, points[0].y + oy, 0.0f).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, points[i].x + ox, points[i].y + oy, 0.0f).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, points[i + 1].x + ox, points[i + 1].y + oy, 0.0f).color(r, g, b, a).endVertex();
                builder.vertex(matrix4f, points[i + 1].x + ox, points[i + 1].y + oy, 0.0f).color(r, g, b, a).endVertex();
            }
        }
    }

    private int clipAxis(Vector2f[] src, int count, boolean horizontal, float limit, boolean keepGreater, Vector2f[] dst) {
        int out = 0;
        for (int i = 0; i < count; ++i) {
            boolean nextInside = false; // initialised: assignment path not guaranteed
            boolean curInside = false; // initialised: assignment path not guaranteed
            float nextValue;
            Vector2f cur = src[i];
            Vector2f next = src[(i + 1) % count];
            float curValue = horizontal ? cur.x : cur.y;
            float f = nextValue = horizontal ? next.x : next.y;
            boolean bl = keepGreater ? curValue >= limit : (curInside = curValue <= limit);
            boolean bl2 = keepGreater ? nextValue >= limit : (nextInside = nextValue <= limit);
            if (curInside) {
                dst[out++].set((Vector2fc)cur);
            }
            if (curInside == nextInside) continue;
            float t = (limit - curValue) / (nextValue - curValue);
            dst[out++].set(cur.x + (next.x - cur.x) * t, cur.y + (next.y - cur.y) * t);
        }
        return out;
    }

    private static float[][] buildAaOffsets() {
        float[][] offsets = new float[16][];
        int index = 0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                offsets[index++] = new float[]{(float)((double)x + 0.5) / 4.0f - 0.5f, (float)((double)y + 0.5) / 4.0f - 0.5f};
            }
        }
        return offsets;
    }

    private boolean shouldShowEntity(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return false;
        }
        if (entity instanceof Player && ((Boolean)this.playersSetting.get()).booleanValue()) {
            Player p = (Player)entity;
            if (TeamsModule.isSameTeam((Entity)p) && !((Boolean)this.showTeamSetting.get()).booleanValue()) {
                return false;
            }
            return !AntibotModule.isBot((Entity)p) || (Boolean)this.showBotsSetting.get() != false;
        }
        if (entity instanceof Animal && ((Boolean)this.animalsSetting.get()).booleanValue()) {
            return true;
        }
        if (entity instanceof Mob && ((Boolean)this.mobsSetting.get()).booleanValue()) {
            return true;
        }
        if (entity instanceof ItemEntity && ((Boolean)this.itemsSetting.get()).booleanValue()) {
            return true;
        }
        return entity instanceof Arrow && (Boolean)this.arrowsSetting.get() != false;
    }

    private void draw2DPlane(BufferBuilder builder, Matrix4f matrix4f, float x1, float y1, float x2, float y2, Color color) {
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 120);
        this.drawQuad(builder, matrix4f, x1, y1, x2, y2, fillColor);
    }

    private void draw3DBox2D(BufferBuilder builder, Matrix4f matrix4f, Vector4f[] clipPoints, Color color) {
        Minecraft mc = Minecraft.getInstance();
        float screenW = mc.getWindow().getGuiScaledWidth();
        float screenH = mc.getWindow().getGuiScaledHeight();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 30);
        for (int[] face : BOX_FACES) {
            int count = this.clipFaceNear(clipPoints, face);
            if (count < 3) continue;
            for (int i = 0; i < count; ++i) {
                W2SUtil.clipToScreen(this.nearPoly[i], this.screenPoly[i]);
            }
            if ((count = this.clipPolygonToScreen(this.screenPoly, count, screenW, screenH)) < 3) continue;
            this.fillPolygonAA(builder, matrix4f, this.clipPolyA, count, fillColor);
        }
    }

    private void drawHealthBar(BufferBuilder builder, Matrix4f matrix4f, LivingEntity entity, Vector4d v, Color color) {
        float barY;
        float barX;
        float barH;
        float barW;
        float healthFrac;
        if (entity instanceof Player) {
            Player p = (Player)entity;
            healthFrac = Mth.clamp((float)(Math.min(p.getHealth(), p.getMaxHealth()) / p.getMaxHealth()), (float)0.0f, (float)1.0f);
        } else {
            healthFrac = Math.min(entity.getHealth() / entity.getMaxHealth(), 1.0f);
        }
        float x = (float)v.x();
        float y = (float)v.y();
        float right = x + (float)v.z();
        float bottom = y + (float)v.w();
        String position = (String)this.healthBarPositionSetting.get();
        if (position == null) {
            position = "Bottom";
        }
        if (position.equals("Top")) {
            barW = (float)v.z();
            barH = 2.0f;
            barX = x;
            barY = y - 4.0f;
        } else if (position.equals("Left")) {
            barW = 2.0f;
            barH = (float)v.w();
            barX = x - 4.0f;
            barY = y;
        } else if (position.equals("Right")) {
            barW = 2.0f;
            barH = (float)v.w();
            barX = right + 2.0f;
            barY = y;
        } else {
            barW = (float)v.z();
            barH = 2.0f;
            barX = x;
            barY = bottom + 2.0f;
        }
        this.drawQuad(builder, matrix4f, barX - 0.6f, barY - 0.6f, barX + barW + 0.6f, barY + barH + 0.6f, Color.BLACK);
        this.drawQuad(builder, matrix4f, barX, barY, barX + barW, barY + barH, color.darker().darker());
        Color healthColor = this.getHealthColor(healthFrac);
        if (position.equals("Left") || position.equals("Right")) {
            this.drawQuad(builder, matrix4f, barX, barY + barH * (1.0f - healthFrac), barX + barW, barY + barH, healthColor);
        } else {
            this.drawQuad(builder, matrix4f, barX, barY, barX + barW * healthFrac, barY + barH, healthColor);
        }
    }

    private void drawQuad(BufferBuilder builder, Matrix4f matrix4f, float x1, float y1, float x2, float y2, Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        builder.vertex(matrix4f, x1, y2, 0.0f).color(r, g, b, a).endVertex();
        builder.vertex(matrix4f, x2, y2, 0.0f).color(r, g, b, a).endVertex();
        builder.vertex(matrix4f, x2, y1, 0.0f).color(r, g, b, a).endVertex();
        builder.vertex(matrix4f, x1, y1, 0.0f).color(r, g, b, a).endVertex();
    }

    private void drawBracket(BufferBuilder builder, Matrix4f matrix4f, float x1, float y1, float x2, float y2, Color color) {
        this.drawQuad(builder, matrix4f, x1 - 0.6f, y1 - 0.6f, x2 + 0.6f, y2 + 0.6f, new Color(0, 0, 0, 170));
        this.drawQuad(builder, matrix4f, x1, y1, x2, y2, color);
    }

    private Color getHealthColor(float fraction) {
        return Color.getHSBColor(Math.max(0.0f, fraction) / 3.0f, 1.0f, 1.0f);
    }

    private void drawCornerBox(BufferBuilder builder, Matrix4f matrix4f, float x1, float y1, float x2, float y2, Color color) {
        float width = x2 - x1;
        float height = y2 - y1;
        float len = Math.min(width, height) * 0.28f;
        if (len > 9.0f) {
            len = 9.0f;
        }
        if (len < 3.0f) {
            len = 3.0f;
        }
        float thickness = 1.2f;
        this.drawBracket(builder, matrix4f, x1, y1, x1 + len, y1 + thickness, color);
        this.drawBracket(builder, matrix4f, x1, y1, x1 + thickness, y1 + len, color);
        this.drawBracket(builder, matrix4f, x2 - len, y1, x2, y1 + thickness, color);
        this.drawBracket(builder, matrix4f, x2 - thickness, y1, x2, y1 + len, color);
        this.drawBracket(builder, matrix4f, x1, y2 - thickness, x1 + len, y2, color);
        this.drawBracket(builder, matrix4f, x1, y2 - len, x1 + thickness, y2, color);
        this.drawBracket(builder, matrix4f, x2 - len, y2 - thickness, x2, y2, color);
        this.drawBracket(builder, matrix4f, x2 - thickness, y2 - len, x2, y2, color);
    }

    private void drawFilledRect2D(BufferBuilder builder, Matrix4f matrix4f, float x1, float y1, float x2, float y2, Color color) {
        this.drawQuad(builder, matrix4f, x1 - 1.0f, y1, x1 + 0.5f, y2 + 0.5f, Color.BLACK);
        this.drawQuad(builder, matrix4f, x1 - 1.0f, y1 - 0.5f, x2 + 0.5f, y1 + 1.0f, Color.BLACK);
        this.drawQuad(builder, matrix4f, x2 - 0.5f, y1, x2 + 0.5f, y2 + 0.5f, Color.BLACK);
        this.drawQuad(builder, matrix4f, x1 - 1.0f, y2 - 0.5f, x2 + 0.5f, y2 + 0.5f, Color.BLACK);
        this.drawQuad(builder, matrix4f, x1 - 0.5f, y1, x1, y2, color);
        this.drawQuad(builder, matrix4f, x1, y2 - 0.5f, x2, y2, color);
        this.drawQuad(builder, matrix4f, x1, y1, x2, y1 + 0.5f, color);
        this.drawQuad(builder, matrix4f, x2 - 0.5f, y1, x2, y2, color);
    }

    @Override
    public void onDisable() {
        this.entityBoxPositions.clear();
        this.visibleEntities.clear();
        super.onDisable();
    }

    private int clipPolygonToScreen(Vector2f[] src, int count, float screenW, float screenH) {
        for (int i = 0; i < count; ++i) {
            this.clipPolyA[i].set((Vector2fc)src[i]);
        }
        int n = this.clipAxis(this.clipPolyA, count, true, 0.0f, true, this.clipPolyB);
        n = this.clipAxis(this.clipPolyB, n, true, screenW, false, this.clipPolyA);
        n = this.clipAxis(this.clipPolyA, n, false, 0.0f, true, this.clipPolyB);
        n = this.clipAxis(this.clipPolyB, n, false, screenH, false, this.clipPolyA);
        return n;
    }

    private AABB getInterpolatedAABB(Entity entity, float partial) {
        double x = Mth.lerp((double)partial, (double)entity.xOld, (double)entity.getX());
        double y = Mth.lerp((double)partial, (double)entity.yOld, (double)entity.getY());
        double z = Mth.lerp((double)partial, (double)entity.zOld, (double)entity.getZ());
        AABB box = entity.getBoundingBox();
        double wX = (box.maxX - box.minX) / 2.0;
        double wY = box.maxY - box.minY;
        double wZ = (box.maxZ - box.minZ) / 2.0;
        return new AABB(x - wX, y, z - wZ, x + wX, y + wY, z + wZ);
    }

    @Override
    public void onRender3D() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        W2SUtil.updateMatrices();
        this.entityBoxPositions.clear();
        this.visibleEntities.clear();
        float partial = mc.getFrameTime();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!this.shouldShowEntity(entity) || !this.isInRange(entity)) continue;
            this.visibleEntities.add(entity);
        }
        for (Entity entity : this.visibleEntities) {
            AABB aabb = this.getInterpolatedAABB(entity, partial);
            W2SUtil.clip((aabb.minX + aabb.maxX) * 0.5, (aabb.minY + aabb.maxY) * 0.5, (aabb.minZ + aabb.maxZ) * 0.5, this.edgeA);
            if (this.edgeA.w <= 0.02f) continue;
            Vec3[] corners = new Vec3[]{new Vec3(aabb.minX, aabb.minY, aabb.minZ), new Vec3(aabb.maxX, aabb.minY, aabb.minZ), new Vec3(aabb.maxX, aabb.minY, aabb.maxZ), new Vec3(aabb.minX, aabb.minY, aabb.maxZ), new Vec3(aabb.minX, aabb.maxY, aabb.minZ), new Vec3(aabb.maxX, aabb.maxY, aabb.minZ), new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ), new Vec3(aabb.minX, aabb.maxY, aabb.maxZ)};
            Vector4f[] clipPoints = new Vector4f[8];
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float maxX = -3.4028235E38f;
            float maxY = -3.4028235E38f;
            int valid = 0;
            for (int i = 0; i < corners.length; ++i) {
                Vec3 corner = corners[i];
                clipPoints[i] = W2SUtil.clip(corner.x, corner.y, corner.z, new Vector4f());
                if (clipPoints[i].w <= 0.02f) continue;
                Vector2f point = W2SUtil.clipToScreen(clipPoints[i], new Vector2f());
                minX = Math.min(minX, point.x);
                minY = Math.min(minY, point.y);
                maxX = Math.max(maxX, point.x);
                maxY = Math.max(maxY, point.y);
                ++valid;
            }
            for (int[] edge : BOX_EDGES) {
                boolean inB;
                Vector4f a = clipPoints[edge[0]];
                Vector4f b = clipPoints[edge[1]];
                boolean inA = a.w > 0.02f;
                boolean bl = inB = b.w > 0.02f;
                if (inA == inB) continue;
                float t = (0.02f - a.w) / (b.w - a.w);
                this.edgeA.set(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t, 0.02f);
                Vector2f point = W2SUtil.clipToScreen(this.edgeA, new Vector2f());
                minX = Math.min(minX, point.x);
                minY = Math.min(minY, point.y);
                maxX = Math.max(maxX, point.x);
                maxY = Math.max(maxY, point.y);
                ++valid;
            }
            if (valid == 0) continue;
            float screenW = mc.getWindow().getGuiScaledWidth();
            float screenH = mc.getWindow().getGuiScaledHeight();
            minX = Mth.clamp((float)minX, (float)0.0f, (float)screenW);
            minY = Mth.clamp((float)minY, (float)0.0f, (float)screenH);
            maxX = Mth.clamp((float)maxX, (float)0.0f, (float)screenW);
            maxY = Mth.clamp((float)maxY, (float)0.0f, (float)screenH);
            if (maxX - minX < 1.0f || maxY - minY < 1.0f) continue;
            int pad = 3;
            Vector4d box = new Vector4d((double)((int)(minX - (float)pad)), (double)((int)(minY - (float)pad)), (double)((int)(maxX - minX + (float)(pad * 2))), (double)((int)(maxY - minY + (float)(pad * 2))));
            this.entityBoxPositions.put(entity, new ESPData(box, clipPoints));
        }
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (this.entityBoxPositions.isEmpty()) {
            return;
        }
        Matrix4f matrix4f = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (Map.Entry<Entity, ESPData> entry : this.entityBoxPositions.entrySet()) {
            Vector4d v = entry.getValue().box2d;
            Vector4f[] clipPoints = entry.getValue().clipPoints;
            if (v.z <= 0.0 || v.w <= 0.0) continue;
            Entity entity = entry.getKey();
            Color color = entity instanceof Player ? Color.WHITE : Color.WHITE;
            float x1 = (float)v.x();
            float y1 = (float)v.y();
            float x2 = x1 + (float)v.z();
            float y2 = y1 + (float)v.w();
            if (((Boolean)this.boxSetting.get()).booleanValue()) {
                String mode = (String)this.boxModeSetting.get();
                if ("Outline".equals(mode)) {
                    this.drawFilledRect2D(builder, matrix4f, x1, y1, x2, y2, color);
                } else if ("Box".equals(mode)) {
                    this.draw2DPlane(builder, matrix4f, x1, y1, x2, y2, color);
                } else if ("3D".equals(mode)) {
                    this.draw3DBox2D(builder, matrix4f, clipPoints, color);
                } else if ("Corners".equals(mode)) {
                    this.drawCornerBox(builder, matrix4f, x1, y1, x2, y2, color);
                }
            }
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity le = (LivingEntity)entity;
            if (!((Boolean)this.showHealthBarSetting.get()).booleanValue()) continue;
            this.drawHealthBar(builder, matrix4f, le, v, color);
        }
        BufferUploader.drawWithShader((BufferBuilder.RenderedBuffer)builder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private static class ESPData {
        public Vector4d box2d;
        public Vector4f[] clipPoints;

        public ESPData(Vector4d box2d, Vector4f[] clipPoints) {
            this.box2d = box2d;
            this.clipPoints = clipPoints;
        }
    }

    public record Pair<A, B>(A first, B second) {
    }
}

