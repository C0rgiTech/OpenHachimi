/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.NumberSetting;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class ChestespModule
extends Module {
    private static final byte[] S_605A9EE1 = new byte[]{1, 42, 39, 49, 54, 7, 17, 18};  // XOR0x42 "ChestESP"
    private static final byte[] S_783DE5B2 = new byte[]{1, 42, 39, 49, 54, 49};  // XOR0x42 "Chests"
    private static final byte[] S_5021A62A = new byte[]{7, 44, 38, 39, 48, 98, 1, 42, 39, 49, 54, 49};  // XOR0x42 "Ender Chests"
    private static final byte[] S_4F5D2D6D = new byte[]{0, 35, 48, 48, 39, 46, 49};  // XOR0x42 "Barrels"
    private static final byte[] S_4D909985 = new byte[]{17, 42, 55, 46, 41, 39, 48, 98, 0, 45, 58, 39, 49};  // XOR0x42 "Shulker Boxes"
    private static final byte[] S_2489529F = new byte[]{10, 45, 50, 50, 39, 48, 49};  // XOR0x42 "Hoppers"
    private static final byte[] S_6A1EF685 = new byte[]{6, 48, 45, 50, 50, 39, 48, 49};  // XOR0x42 "Droppers"
    private static final byte[] S_3512087E = new byte[]{6, 43, 49, 50, 39, 44, 49, 39, 48, 49};  // XOR0x42 "Dispensers"
    private static final byte[] S_5D8F823B = new byte[]{4, 55, 48, 44, 35, 33, 39, 49};  // XOR0x42 "Furnaces"
    private static final byte[] S_1506681D = new byte[]{0, 48, 39, 53, 43, 44, 37, 98, 17, 54, 35, 44, 38, 49};  // XOR0x42 "Brewing Stands"
    private static final byte[] S_6530C1FC = new byte[]{14, 39, 33, 54, 39, 48, 44, 49};  // XOR0x42 "Lecterns"
    private static final byte[] S_4B1491D = new byte[]{16, 35, 44, 37, 39};  // XOR0x42 "Range"
    private static final byte[] S_3C6C13E = new byte[]{3, 46, 50, 42, 35};  // XOR0x42 "Alpha"
    private static final float NEAR_W = 0.02f;
    private static final int POLY_CAP = 16;
    private static final int[][] BOX_FACES = new int[][]{{0, 1, 2, 3}, {4, 5, 6, 7}, {0, 1, 5, 4}, {3, 2, 6, 7}, {0, 3, 7, 4}, {1, 2, 6, 5}};
    private static final float[][] AA_OFFSETS = ChestespModule.buildAaOffsets();
    private static final int[] UNOPENED_RGB = new int[]{61, 214, 140};
    private static final int[] OPENED_RGB = new int[]{232, 77, 90};
    private final BooleanSetting chests = new BooleanSetting("Chests", true);
    private final BooleanSetting enderChests = new BooleanSetting("Ender Chests", true);
    private final BooleanSetting barrels = new BooleanSetting("Barrels", true);
    private final BooleanSetting shulkers = new BooleanSetting("Shulker Boxes", true);
    private final BooleanSetting hoppers = new BooleanSetting("Hoppers", true);
    private final BooleanSetting droppers = new BooleanSetting("Droppers", true);
    private final BooleanSetting dispensers = new BooleanSetting("Dispensers", true);
    private final BooleanSetting furnaces = new BooleanSetting("Furnaces", true);
    private final BooleanSetting brewingStands = new BooleanSetting("Brewing Stands", true);
    private final BooleanSetting lecterns = new BooleanSetting("Lecterns", true);
    private final ListSetting range = new ListSetting("Range", 64, 8, 256);
    private final NumberSetting alpha = new NumberSetting("Alpha", 0.35f, 0.05f, 1.0f);
    private final Set<BlockPos> openedOnce = new HashSet<BlockPos>();
    private final Map<BlockPos, AABB> boxes = new HashMap<BlockPos, AABB>();
    private final Vector4f[] nearPoly = new Vector4f[16];
    private final Vector2f[] screenPoly = new Vector2f[16];
    private final Vector2f[] clipPolyA = new Vector2f[16];
    private final Vector2f[] clipPolyB = new Vector2f[16];
    private final Vector4f edgeA = new Vector4f();
    private final double[] cornerX = new double[8];
    private final double[] cornerY = new double[8];
    private final double[] cornerZ = new double[8];
    private final Vector4f[] scratchClip = new Vector4f[8];
    private int renderCount;
    private final List<ClipBox> renderList;

    public ChestespModule() {
        super("ChestESP", Category.RENDER);
        int i;
        for (i = 0; i < 8; ++i) {
            this.scratchClip[i] = new Vector4f();
        }
        this.renderList = new ArrayList<ClipBox>();
        for (i = 0; i < 16; ++i) {
            this.nearPoly[i] = new Vector4f();
            this.screenPoly[i] = new Vector2f();
            this.clipPolyA[i] = new Vector2f();
            this.clipPolyB[i] = new Vector2f();
        }
        this.values.add(this.chests);
        this.values.add(this.enderChests);
        this.values.add(this.barrels);
        this.values.add(this.shulkers);
        this.values.add(this.hoppers);
        this.values.add(this.droppers);
        this.values.add(this.dispensers);
        this.values.add(this.furnaces);
        this.values.add(this.brewingStands);
        this.values.add(this.lecterns);
        this.values.add(this.range);
        this.values.add(this.alpha);
    }

    private void drawAll(GuiGraphics graphics) {
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
        float a = ((Float)this.alpha.get()).floatValue();
        int alpha = Mth.clamp((int)Math.round(a * 255.0f), (int)1, (int)255);
        Color closedColor = new Color(UNOPENED_RGB[0], UNOPENED_RGB[1], UNOPENED_RGB[2], alpha);
        Color openedColor = new Color(OPENED_RGB[0], OPENED_RGB[1], OPENED_RGB[2], alpha);
        Minecraft mc = Minecraft.getInstance();
        float screenW = mc.getWindow().getGuiScaledWidth();
        float screenH = mc.getWindow().getGuiScaledHeight();
        Tesselator tess = Tesselator.getInstance();
        try {
            tess.end();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        BufferBuilder builder = tess.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int ci = 0; ci < this.renderCount; ++ci) {
            ClipBox cb = this.renderList.get(ci);
            Color fill = this.openedOnce.contains(cb.pos) ? openedColor : closedColor;
            for (int[] face : BOX_FACES) {
                int count = this.clipFaceNear(cb.points, face);
                if (count < 3) continue;
                for (int i = 0; i < count; ++i) {
                    W2SUtil.clipToScreen(this.nearPoly[i], this.screenPoly[i]);
                }
                if ((count = this.clipPolygonToScreen(this.screenPoly, count, screenW, screenH)) < 3) continue;
                this.fillPolygonAA(builder, matrix4f, this.clipPolyA, count, fill);
            }
        }
        BufferUploader.drawWithShader((BufferBuilder.RenderedBuffer)builder.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private boolean isOpened(BlockEntity be, BlockPos pos) {
        if (!(be instanceof ChestBlockEntity) || be instanceof EnderChestBlockEntity) {
            return false;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return false;
            }
            return ChestBlockEntity.getOpenCount((BlockGetter)mc.level, (BlockPos)pos) > 0;
        }
        catch (Throwable ignored) {
            return false;
        }
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

    private int clipAxis(Vector2f[] src, int count, boolean xAxis, float bound, boolean keepGreater, Vector2f[] dst) {
        if (count == 0) {
            return 0;
        }
        int out = 0;
        for (int i = 0; i < count; ++i) {
            float denom;
            boolean nextIn = false; // initialised: assignment path not guaranteed
            boolean curIn = false; // initialised: assignment path not guaranteed
            float nextVal;
            Vector2f cur = src[i];
            Vector2f next = src[(i + 1) % count];
            float curVal = xAxis ? cur.x : cur.y;
            float f = nextVal = xAxis ? next.x : next.y;
            boolean bl = keepGreater ? curVal >= bound : (curIn = curVal <= bound);
            boolean bl2 = keepGreater ? nextVal >= bound : (nextIn = nextVal <= bound);
            if (curIn) {
                dst[out++].set((Vector2fc)cur);
            }
            if (curIn == nextIn || Math.abs(denom = nextVal - curVal) < 1.0E-6f) continue;
            float t = (bound - curVal) / denom;
            dst[out++].set(cur.x + (next.x - cur.x) * t, cur.y + (next.y - cur.y) * t);
        }
        return out;
    }

    private AABB boxFor(Minecraft mc, BlockEntity be, BlockPos pos) {
        return new AABB(pos);
    }

    private void collectClip(AABB aabb, BlockPos pos) {
        ClipBox cb;
        W2SUtil.clip((aabb.minX + aabb.maxX) * 0.5, (aabb.minY + aabb.maxY) * 0.5, (aabb.minZ + aabb.maxZ) * 0.5, this.edgeA);
        if (this.edgeA.w <= 0.02f) {
            return;
        }
        this.cornerX[0] = aabb.minX;
        this.cornerY[0] = aabb.minY;
        this.cornerZ[0] = aabb.minZ;
        this.cornerX[1] = aabb.maxX;
        this.cornerY[1] = aabb.minY;
        this.cornerZ[1] = aabb.minZ;
        this.cornerX[2] = aabb.maxX;
        this.cornerY[2] = aabb.minY;
        this.cornerZ[2] = aabb.maxZ;
        this.cornerX[3] = aabb.minX;
        this.cornerY[3] = aabb.minY;
        this.cornerZ[3] = aabb.maxZ;
        this.cornerX[4] = aabb.minX;
        this.cornerY[4] = aabb.maxY;
        this.cornerZ[4] = aabb.minZ;
        this.cornerX[5] = aabb.maxX;
        this.cornerY[5] = aabb.maxY;
        this.cornerZ[5] = aabb.minZ;
        this.cornerX[6] = aabb.maxX;
        this.cornerY[6] = aabb.maxY;
        this.cornerZ[6] = aabb.maxZ;
        this.cornerX[7] = aabb.minX;
        this.cornerY[7] = aabb.maxY;
        this.cornerZ[7] = aabb.maxZ;
        int valid = 0;
        for (int i = 0; i < 8; ++i) {
            W2SUtil.clip(this.cornerX[i], this.cornerY[i], this.cornerZ[i], this.scratchClip[i]);
            if (this.scratchClip[i].w <= 0.02f) continue;
            ++valid;
        }
        if (valid == 0) {
            return;
        }
        if (this.renderCount < this.renderList.size()) {
            cb = this.renderList.get(this.renderCount);
        } else {
            cb = new ClipBox();
            this.renderList.add(cb);
        }
        cb.pos = pos;
        for (int i = 0; i < 8; ++i) {
            cb.points[i].set((Vector4fc)this.scratchClip[i]);
        }
        ++this.renderCount;
    }

    private boolean shouldShow(BlockEntity be) {
        BlockEntityType type = be.getType();
        if (type == BlockEntityType.CHEST) {
            return (Boolean)this.chests.get();
        }
        if (type == BlockEntityType.ENDER_CHEST) {
            return (Boolean)this.enderChests.get();
        }
        if (type == BlockEntityType.BARREL) {
            return (Boolean)this.barrels.get();
        }
        if (type == BlockEntityType.SHULKER_BOX) {
            return (Boolean)this.shulkers.get();
        }
        if (type == BlockEntityType.HOPPER) {
            return (Boolean)this.hoppers.get();
        }
        if (type == BlockEntityType.DROPPER) {
            return (Boolean)this.droppers.get();
        }
        if (type == BlockEntityType.DISPENSER) {
            return (Boolean)this.dispensers.get();
        }
        if (type == BlockEntityType.FURNACE || type == BlockEntityType.BLAST_FURNACE || type == BlockEntityType.SMOKER) {
            return (Boolean)this.furnaces.get();
        }
        if (type == BlockEntityType.BREWING_STAND) {
            return (Boolean)this.brewingStands.get();
        }
        if (type == BlockEntityType.LECTERN) {
            return (Boolean)this.lecterns.get();
        }
        return false;
    }

    private static float[][] buildAaOffsets() {
        int rings = 4;
        int perRing = 6;
        float[][] out = new float[rings * perRing][2];
        int idx = 0;
        for (int ring = 0; ring < rings; ++ring) {
            double radius = ((double)ring + 0.5) / (double)rings * 0.5;
            for (int i = 0; i < perRing; ++i) {
                double angle = Math.PI * 2 / (double)perRing * (double)i + (double)ring * 0.7;
                out[idx][0] = (float)(Math.cos(angle) * radius);
                out[idx][1] = (float)(Math.sin(angle) * radius);
                ++idx;
            }
        }
        return out;
    }

    @Override
    public void onDisable() {
        this.boxes.clear();
        this.renderCount = 0;
    }

    @Override
    public void onEnable() {
        this.boxes.clear();
        this.renderCount = 0;
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

    @Override
    public void onWorldChange() {
        this.boxes.clear();
        this.renderCount = 0;
    }

    @Override
    public void onRender3D() {
        Minecraft mc = Minecraft.getInstance();
        this.boxes.clear();
        this.renderCount = 0;
        if (mc.level == null || mc.player == null) {
            return;
        }
        W2SUtil.updateMatrices();
        int limit = (Integer)this.range.get();
        double limitSq = (double)limit * (double)limit;
        Vec3 eye = mc.player.getEyePosition();
        int cx = (int)Math.floor(eye.x);
        int cy = (int)Math.floor(eye.y);
        int cz = (int)Math.floor(eye.z);
        int chunkRadius = (limit >> 4) + 1;
        int pcx = cx >> 4;
        int pcz = cz >> 4;
        for (int chx = pcx - chunkRadius; chx <= pcx + chunkRadius; ++chx) {
            for (int chz = pcz - chunkRadius; chz <= pcz + chunkRadius; ++chz) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(chx, chz, false);
                if (chunk == null) continue;
                for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                    AABB box;
                    BlockEntity be;
                    if (eye.distanceToSqr((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > limitSq || (be = chunk.getBlockEntity(pos)) == null || !this.shouldShow(be) || (box = this.boxFor(mc, be, pos)) == null) continue;
                    if (this.isOpened(be, pos)) {
                        this.openedOnce.add(pos);
                    }
                    this.boxes.put(pos, box);
                    this.collectClip(box, pos);
                }
            }
        }
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        if (this.renderCount == 0) {
            return;
        }
        try {
            this.drawAll(graphics);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static final class ClipBox {
        final Vector4f[] points = new Vector4f[8];
        BlockPos pos;

        ClipBox() {
            for (int i = 0; i < 8; ++i) {
                this.points[i] = new Vector4f();
            }
        }
    }
}

