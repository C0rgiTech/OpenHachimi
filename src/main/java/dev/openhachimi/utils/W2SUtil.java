/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class W2SUtil {
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final Matrix4f projectionMatrix = new Matrix4f();
    private static final Vector4f tempVec4a = new Vector4f();
    private static final Vector4f tempVec4b = new Vector4f();
    private static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer((int)16);
    private static final IntBuffer viewport = BufferUtils.createIntBuffer((int)16);
    private static final Vector3f tempVec3 = new Vector3f();
    private static final Quaternionf tempQuat = new Quaternionf();

    public static void updateMatrices() {
        floatBuffer.clear();
        RenderSystem.getProjectionMatrix().get(floatBuffer);
        projectionMatrix.set(floatBuffer);
        floatBuffer.clear();
        RenderSystem.getModelViewMatrix().get(floatBuffer);
        modelViewMatrix.set(floatBuffer);
        viewport.clear();
        GL11.glGetIntegerv((int)2978, (IntBuffer)viewport);
        tempVec4a.set((float)viewport.get(0), (float)viewport.get(1), (float)viewport.get(2), (float)viewport.get(3));
    }

    public static Vector2f clipToScreen(Vector4f clip, Vector2f dest) {
        Minecraft mc = Minecraft.getInstance();
        double guiScale = mc.getWindow().getGuiScale();
        if (guiScale <= 0.0) {
            guiScale = 1.0;
        }
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        float screenX = tempVec4a.x() + (1.0f + ndcX) * tempVec4a.z() / 2.0f;
        float screenY = tempVec4a.y() + (1.0f - ndcY) * tempVec4a.w() / 2.0f;
        dest.set((float)((double)screenX / guiScale), (float)((double)screenY / guiScale));
        return dest;
    }

    public static Vector2f project(double worldX, double worldY, double worldZ) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        tempVec3.set((float)(worldX - cameraPos.x), (float)(worldY - cameraPos.y), (float)(worldZ - cameraPos.z));
        tempQuat.set((Quaternionfc)camera.rotation()).conjugate();
        tempVec3.rotate((Quaternionfc)tempQuat);
        W2SUtil.tempVec3.x = -W2SUtil.tempVec3.x;
        tempVec4b.set(W2SUtil.tempVec3.x, W2SUtil.tempVec3.y, -W2SUtil.tempVec3.z, 1.0f);
        projectionMatrix.transform(tempVec4b);
        if (W2SUtil.tempVec4b.w <= 0.0f) {
            return null;
        }
        float ndcX = W2SUtil.tempVec4b.x / W2SUtil.tempVec4b.w;
        float ndcY = W2SUtil.tempVec4b.y / W2SUtil.tempVec4b.w;
        if (Float.isNaN(ndcX) || Float.isNaN(ndcY) || ndcX < -1.2f || ndcX > 1.2f || ndcY < -1.2f || ndcY > 1.2f) {
            return null;
        }
        float screenX = tempVec4a.x() + (1.0f + ndcX) * tempVec4a.z() / 2.0f;
        float screenY = tempVec4a.y() + (1.0f - ndcY) * tempVec4a.w() / 2.0f;
        double guiScale = mc.getWindow().getGuiScale();
        if (guiScale == 0.0) {
            guiScale = 1.0;
        }
        screenX = (float)((double)screenX / guiScale);
        screenY = (float)((double)screenY / guiScale);
        return new Vector2f(screenX, screenY);
    }

    public static Vector4f clip(double worldX, double worldY, double worldZ, Vector4f dest) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        tempVec3.set((float)(worldX - cameraPos.x), (float)(worldY - cameraPos.y), (float)(worldZ - cameraPos.z));
        tempQuat.set((Quaternionfc)camera.rotation()).conjugate();
        tempVec3.rotate((Quaternionfc)tempQuat);
        W2SUtil.tempVec3.x = -W2SUtil.tempVec3.x;
        dest.set(W2SUtil.tempVec3.x, W2SUtil.tempVec3.y, -W2SUtil.tempVec3.z, 1.0f);
        return projectionMatrix.transform(dest);
    }
}

