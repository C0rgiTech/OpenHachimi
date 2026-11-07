/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render;

import dev.openhachimi.render.ClickGuiScreen;
import dev.openhachimi.render.ShaderUniforms;
import dev.openhachimi.settings.NumberSetting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import dev.openhachimi.hud.DraggedHUDManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class RenderUtil {
    public static Object hudHover;
    private static final Map<Object, DraggedHUDManager.Draggable> DRAG_TOKENS;
    public static float deltaTime;
    private static long lastFrameTime;

    static {
        DRAG_TOKENS = new WeakHashMap<Object, DraggedHUDManager.Draggable>();
        deltaTime = 0.016f;
        lastFrameTime = System.currentTimeMillis();
    }

    public static void drawRoundedImage(GuiGraphics graphics, ResourceLocation resourceLocation, final float x, final float y, final float width, final float height, float radius, final float uOffset, final float vOffset, final float uWidth, final float vWidth, final float texWidth, final float texHeight) {
        float[] p;
        float py;
        float px;
        double angle;
        graphics.flush();
        final Matrix4f matrix = graphics.pose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionTexShader();
            }
        });
        RenderSystem.setShaderTexture((int)0, (ResourceLocation)resourceLocation);
        Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX);
        final float cx = x + width / 2.0f;
        final float cy = y + height / 2.0f;
        float cornerPoints = 15.0f;
        final float cu = (uOffset + uWidth / 2.0f) / texWidth;
        final float cv = (vOffset + vWidth / 2.0f) / texHeight;
        class TriAdder {
            TriAdder() {
            }

            void add(float[] p1, float[] p2) {
                bufferbuilder.vertex(matrix, cx, cy, 0.0f).uv(cu, cv).endVertex();
                bufferbuilder.vertex(matrix, p1[0], p1[1], 0.0f).uv((uOffset + (p1[0] - x) / width * uWidth) / texWidth, (vOffset + (p1[1] - y) / height * vWidth) / texHeight).endVertex();
                bufferbuilder.vertex(matrix, p2[0], p2[1], 0.0f).uv((uOffset + (p2[0] - x) / width * uWidth) / texWidth, (vOffset + (p2[1] - y) / height * vWidth) / texHeight).endVertex();
            }
        }
        TriAdder addTri = new TriAdder();
        float[] lastP = null;
        float[] firstP = null;
        int i = 0;
        while ((float)i <= cornerPoints) {
            angle = (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + width - radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            if (lastP != null) {
                addTri.add(lastP, p);
            } else {
                firstP = p;
            }
            lastP = p;
            ++i;
        }
        i = 0;
        while ((float)i <= cornerPoints) {
            angle = 1.5707963267948966 + (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + width - radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + height - radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            addTri.add(lastP, p);
            lastP = p;
            ++i;
        }
        i = 0;
        while ((float)i <= cornerPoints) {
            angle = Math.PI + (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + height - radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            addTri.add(lastP, p);
            lastP = p;
            ++i;
        }
        i = 0;
        while ((float)i <= cornerPoints) {
            angle = 4.71238898038469 + (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            addTri.add(lastP, p);
            lastP = p;
            ++i;
        }
        addTri.add(lastP, firstP);
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static float[] hudDragOffset(Minecraft mc, float x, float y, float anchorX, float anchorY, float width, float height, NumberSetting offsetX, NumberSetting offsetY, Object owner) {
        boolean inside;
        boolean screenOk;
        if (mc == null || mc.getWindow() == null) {
            return new float[]{x, y};
        }
        boolean bl = screenOk = mc.screen instanceof ChatScreen || mc.screen instanceof ClickGuiScreen;
        if (!screenOk) {
            hudHover = null;
            DraggedHUDManager.release();
            return new float[]{x, y};
        }
        double rawX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
        double rawY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
        boolean down = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
        DraggedHUDManager.updateMouse(rawX, rawY, down);
        DraggedHUDManager.Draggable token = RenderUtil.dragToken(owner);
        DraggedHUDManager.offer(token, x, y, width, height);
        boolean bl2 = inside = rawX >= (double)x && rawX <= (double)(x + width) && rawY >= (double)y && rawY <= (double)(y + height);
        Object object = inside ? owner : (hudHover = hudHover == owner ? null : hudHover);
        if (!DraggedHUDManager.isActive(token)) {
            return new float[]{x, y};
        }
        float nx = (float)rawX - DraggedHUDManager.grabOffsetX();
        float ny = (float)rawY - DraggedHUDManager.grabOffsetY();
        float maxX = (float)mc.getWindow().getGuiScaledWidth() - width - 2.0f;
        float maxY = (float)mc.getWindow().getGuiScaledHeight() - height - 2.0f;
        nx = Math.max(2.0f, Math.min(maxX, nx));
        ny = Math.max(2.0f, Math.min(maxY, ny));
        offsetX.set(Float.valueOf(nx - anchorX));
        offsetY.set(Float.valueOf(ny - anchorY));
        return new float[]{nx, ny};
    }

    public static void updateDeltaTime() {
        long current = System.currentTimeMillis();
        deltaTime = (float)(current - lastFrameTime) / 1000.0f;
        lastFrameTime = current;
        if (deltaTime > 0.1f) {
            deltaTime = 0.1f;
        }
        if (deltaTime < 0.001f) {
            deltaTime = 0.001f;
        }
    }

    public static float animateSmooth(float current, float target, float speed) {
        float f = 0;
        if (current == target) {
            return current;
        }
        float factor = 1.0f - (float)Math.exp(-speed * deltaTime);
        current += (target - current) * factor;
        if (Math.abs(target - f) < 0.001f) {
            return target;
        }
        return current;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DraggedHUDManager.Draggable dragToken(Object owner) {
        Map<Object, DraggedHUDManager.Draggable> map = DRAG_TOKENS;
        synchronized (map) {
            DraggedHUDManager.Draggable token = DRAG_TOKENS.get(owner);
            if (token == null) {
                token = new DraggedHUDManager.Draggable(){};
                DRAG_TOKENS.put(owner, token);
            }
            return token;
        }
    }

    public static void drawRect(GuiGraphics graphics, float x, float y, float width, float height, int color) {
        graphics.fill((int)x, (int)y, (int)(x + width), (int)(y + height), color);
    }

    public static boolean isHovering(float x, float y, float width, float height, double mouseX, double mouseY) {
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    public static void drawImage(GuiGraphics graphics, ResourceLocation resourceLocation, int x, int y, int width, int height, int color) {
        if (resourceLocation == null) {
            return;
        }
        RenderSystem.enableBlend();
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        RenderSystem.setShaderColor((float)r, (float)g, (float)b, (float)a);
        graphics.blit(resourceLocation, x, y, 0.0f, 0.0f, width, height, width, height);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.disableBlend();
    }

    public static void drawGradientRoundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int firstColor, int secondColor, boolean horizontal) {
        if (width < 0.5f || height < 0.5f) {
            return;
        }
        float clampedRadius = Math.min(radius, Math.min(width, height) / 2.0f);
        if (clampedRadius < 0.0f) {
            clampedRadius = 0.0f;
        }
        graphics.flush();
        Matrix4f matrix = graphics.pose().last().pose();
        if (ShaderUniforms.draw(matrix, x, y, width, height, clampedRadius, firstColor, secondColor, horizontal ? 2 : 1)) {
            return;
        }
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
        float firstAlpha = (float)(firstColor >>> 24) / 255.0f;
        float firstRed = (float)(firstColor >> 16 & 0xFF) / 255.0f;
        float firstGreen = (float)(firstColor >> 8 & 0xFF) / 255.0f;
        float firstBlue = (float)(firstColor & 0xFF) / 255.0f;
        float secondAlpha = (float)(secondColor >>> 24) / 255.0f;
        float secondRed = (float)(secondColor >> 16 & 0xFF) / 255.0f;
        float secondGreen = (float)(secondColor >> 8 & 0xFF) / 255.0f;
        float secondBlue = (float)(secondColor & 0xFF) / 255.0f;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        float centerX = x + width / 2.0f;
        float centerY = y + height / 2.0f;
        buffer.vertex(matrix, centerX, centerY, 0.0f).color((firstRed + secondRed) / 2.0f, (firstGreen + secondGreen) / 2.0f, (firstBlue + secondBlue) / 2.0f, (firstAlpha + secondAlpha) / 2.0f).endVertex();
        float[] cornerX = new float[]{x + width - clampedRadius, x + width - clampedRadius, x + clampedRadius, x + clampedRadius};
        float[] cornerY = new float[]{y + clampedRadius, y + height - clampedRadius, y + height - clampedRadius, y + clampedRadius};
        float firstX = 0.0f;
        float firstY = 0.0f;
        float firstVertexRed = 0.0f;
        float firstVertexGreen = 0.0f;
        float firstVertexBlue = 0.0f;
        float firstVertexAlpha = 0.0f;
        boolean firstVertex = true;
        for (int corner = 0; corner < 4; ++corner) {
            for (int step = 0; step <= 10; ++step) {
                double angle = Math.toRadians((double)corner * 90.0 + (double)step * 9.0);
                float pointX = cornerX[corner] + (float)Math.sin(angle) * clampedRadius;
                float pointY = cornerY[corner] - (float)Math.cos(angle) * clampedRadius;
                float progress = horizontal ? (pointX - x) / width : (pointY - y) / height;
                float f = progress;
                if (progress < 0.0f) {
                    progress = 0.0f;
                }
                if (progress > 1.0f) {
                    progress = 1.0f;
                }
                float red = firstRed + (secondRed - firstRed) * progress;
                float green = firstGreen + (secondGreen - firstGreen) * progress;
                float blue = firstBlue + (secondBlue - firstBlue) * progress;
                float alpha = firstAlpha + (secondAlpha - firstAlpha) * progress;
                buffer.vertex(matrix, pointX, pointY, 0.0f).color(red, green, blue, alpha).endVertex();
                if (!firstVertex) continue;
                firstX = pointX;
                firstY = pointY;
                firstVertexRed = red;
                firstVertexGreen = green;
                firstVertexBlue = blue;
                firstVertexAlpha = alpha;
                firstVertex = false;
            }
        }
        buffer.vertex(matrix, firstX, firstY, 0.0f).color(firstVertexRed, firstVertexGreen, firstVertexBlue, firstVertexAlpha).endVertex();
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawRoundedRect(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
        float[] p;
        float py;
        float px;
        double angle;
        graphics.flush();
        final Matrix4f matrix = graphics.pose().last().pose();
        if (ShaderUniforms.draw(matrix, x, y, width, height, radius, color, color, 0)) {
            return;
        }
        final float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        final float red = (float)(color >> 16 & 0xFF) / 255.0f;
        final float green = (float)(color >> 8 & 0xFF) / 255.0f;
        final float blue = (float)(color & 0xFF) / 255.0f;
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader((Supplier)new Supplier<ShaderInstance>(){

            @Override
            public ShaderInstance get() {
                return GameRenderer.getPositionColorShader();
            }
        });
        Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        final float cx = x + width / 2.0f;
        final float cy = y + height / 2.0f;
        float cornerPoints = 15.0f;
        class TriAdder {
            TriAdder() {
            }

            void add(float[] p1, float[] p2) {
                bufferbuilder.vertex(matrix, cx, cy, 0.0f).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(matrix, p1[0], p1[1], 0.0f).color(red, green, blue, alpha).endVertex();
                bufferbuilder.vertex(matrix, p2[0], p2[1], 0.0f).color(red, green, blue, alpha).endVertex();
            }
        }
        TriAdder addTri = new TriAdder();
        float[] lastP = null;
        float[] firstP = null;
        int i = 0;
        while ((float)i <= cornerPoints) {
            angle = (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + width - radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            if (lastP != null) {
                addTri.add(lastP, p);
            } else {
                firstP = p;
            }
            lastP = p;
            ++i;
        }
        i = 0;
        while ((float)i <= cornerPoints) {
            angle = 1.5707963267948966 + (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + width - radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + height - radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            addTri.add(lastP, p);
            lastP = p;
            ++i;
        }
        i = 0;
        while ((float)i <= cornerPoints) {
            angle = Math.PI + (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + height - radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            addTri.add(lastP, p);
            lastP = p;
            ++i;
        }
        i = 0;
        while ((float)i <= cornerPoints) {
            angle = 4.71238898038469 + (double)i * Math.PI / 2.0 / (double)cornerPoints;
            px = (float)((double)(x + radius) + Math.sin(angle) * (double)radius);
            py = (float)((double)(y + radius) - Math.cos(angle) * (double)radius);
            p = new float[]{px, py};
            addTri.add(lastP, p);
            lastP = p;
            ++i;
        }
        addTri.add(lastP, firstP);
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}

