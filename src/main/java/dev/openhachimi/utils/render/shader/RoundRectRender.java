/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class RoundRectRender {
    private static int programId;
    private static int uModelViewMat;
    private static int uProjMat;
    private static int uSampler;
    private static int uHalf;
    private static int uRadius;
    private static int uTint;
    private static int uFeather;
    private static int vaoId;
    private static int vboId;
    private static boolean failed;
    private static int prevVao;
    private static int prevVbo;
    private static int prevProgram;
    private static int prevTexture;

    public static boolean roundedTexture(Matrix4f pose, ResourceLocation texture, float x, float y, float size, float radius, float u0, float v0, float uvSize, float tintR, float tintG, float tintB, float alpha) {
        try {
            return RoundRectRender.roundedTextureInner(pose, texture, x, y, size, radius, u0, v0, uvSize, tintR, tintG, tintB, alpha);
        }
        catch (Throwable error) {
            failed = true;
            return false;
        }
    }

    private RoundRectRender() {
    }

    static {
        programId = 0;
        uModelViewMat = -1;
        uProjMat = -1;
        uSampler = -1;
        uHalf = -1;
        uRadius = -1;
        uTint = -1;
        uFeather = -1;
        vaoId = 0;
        vboId = 0;
        failed = false;
        prevVao = 0;
        prevVbo = 0;
        prevProgram = 0;
        prevTexture = 0;
    }

    private static boolean init() {
        if (failed) {
            return false;
        }
        if (programId != 0) {
            return true;
        }
        int vertexShader = RoundRectRender.compileShader(35633, "#version 150\nin vec2 Position;\nin vec2 UV;\nin vec2 LocalPos;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nout vec2 outUV;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n    outUV = UV;\n}\n");
        if (vertexShader == 0) {
            failed = true;
            return false;
        }
        int fragmentShader = RoundRectRender.compileShader(35632, "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 uHalf;\nuniform float uRadius;\nuniform vec4 uTint;\nuniform float uFeather;\nin vec2 localPos;\nin vec2 outUV;\nout vec4 fragColor;\nvoid main() {\n    vec2 q = abs(localPos) - uHalf + uRadius;\n    float d = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - uRadius;\n    float alpha = 1.0 - smoothstep(-uFeather, uFeather, d);\n    if (alpha <= 0.0) discard;\n    vec4 tex = texture(Sampler0, outUV);\n    fragColor = vec4(tex.rgb * uTint.rgb, tex.a * uTint.a * alpha);\n}\n");
        if (fragmentShader == 0) {
            GL20.glDeleteShader((int)vertexShader);
            failed = true;
            return false;
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader((int)program, (int)vertexShader);
        GL20.glAttachShader((int)program, (int)fragmentShader);
        GL20.glBindAttribLocation((int)program, (int)0, (CharSequence)"Position");
        GL20.glBindAttribLocation((int)program, (int)1, (CharSequence)"UV");
        GL20.glBindAttribLocation((int)program, (int)2, (CharSequence)"LocalPos");
        GL20.glLinkProgram((int)program);
        GL20.glDeleteShader((int)vertexShader);
        GL20.glDeleteShader((int)fragmentShader);
        if (GL20.glGetProgrami((int)program, (int)35714) == 0) {
            GL20.glDeleteProgram((int)program);
            failed = true;
            return false;
        }
        programId = program;
        uModelViewMat = GL20.glGetUniformLocation((int)programId, (CharSequence)"ModelViewMat");
        uProjMat = GL20.glGetUniformLocation((int)programId, (CharSequence)"ProjMat");
        uSampler = GL20.glGetUniformLocation((int)programId, (CharSequence)"Sampler0");
        uHalf = GL20.glGetUniformLocation((int)programId, (CharSequence)"uHalf");
        uRadius = GL20.glGetUniformLocation((int)programId, (CharSequence)"uRadius");
        uTint = GL20.glGetUniformLocation((int)programId, (CharSequence)"uTint");
        uFeather = GL20.glGetUniformLocation((int)programId, (CharSequence)"uFeather");
        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        prevVao = GL11.glGetInteger((int)34229);
        prevVbo = GL11.glGetInteger((int)34964);
        GL30.glBindVertexArray((int)vaoId);
        GL15.glBindBuffer((int)34962, (int)vboId);
        GL15.glBufferData((int)34962, (long)144L, (int)35048);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)24, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)24, (long)8L);
        GL20.glEnableVertexAttribArray((int)2);
        GL20.glVertexAttribPointer((int)2, (int)2, (int)5126, (boolean)false, (int)24, (long)16L);
        GL30.glBindVertexArray((int)prevVao);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        return true;
    }

    public static boolean isAvailable() {
        return !failed;
    }

    private static int compileShader(int type, String source) {
        int shader = GL20.glCreateShader((int)type);
        GL20.glShaderSource((int)shader, (CharSequence)source);
        GL20.glCompileShader((int)shader);
        if (GL20.glGetShaderi((int)shader, (int)35713) == 0) {
            GL20.glDeleteShader((int)shader);
            return 0;
        }
        return shader;
    }

    private static float resolveFeather(Matrix4f pose) {
        float feather;
        float scaleY;
        float scaleX = (float)Math.sqrt(pose.m00() * pose.m00() + pose.m01() * pose.m01());
        float scale = Math.max(scaleX, scaleY = (float)Math.sqrt(pose.m10() * pose.m10() + pose.m11() * pose.m11()));
        if (scale < 1.0E-4f) {
            scale = 1.0f;
        }
        float guiScale = 1.0f;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getWindow() != null) {
            float f = 0;
            guiScale = (float)minecraft.getWindow().getGuiScale();
            if (f < 1.0E-4f) {
                guiScale = 1.0f;
            }
        }
        if ((feather = 0.7f / (scale * guiScale)) < 0.02f) {
            feather = 0.02f;
        }
        if (feather > 1.2f) {
            feather = 1.2f;
        }
        return feather;
    }

    private static boolean roundedTextureInner(Matrix4f pose, ResourceLocation texture, float x, float y, float size, float radius, float u0, float v0, float uvSize, float tintR, float tintG, float tintB, float alpha) {
        if (texture == null || size < 0.5f || alpha <= 0.01f) {
            return true;
        }
        if (!RoundRectRender.init()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        AbstractTexture abstractTexture = minecraft.getTextureManager().getTexture(texture);
        if (abstractTexture == null) {
            return false;
        }
        abstractTexture.setFilter(false, false);
        float half = size * 0.5f;
        float centerX = x + half;
        float centerY = y + half;
        float r = radius < 0.0f ? 0.0f : (radius > half ? half : radius);
        float pad = 1.5f;
        float[] offsets = new float[]{-(half + pad), -(half + pad), half + pad, -(half + pad), half + pad, half + pad, -(half + pad), -(half + pad), half + pad, half + pad, -(half + pad), half + pad};
        Vector4f transformed = new Vector4f();
        float[] vertexData = new float[36];
        for (int i = 0; i < 6; ++i) {
            float localX = offsets[i * 2];
            float localY = offsets[i * 2 + 1];
            transformed.set(centerX + localX, centerY + localY, 0.0f, 1.0f).mul((Matrix4fc)pose);
            int base = i * 6;
            vertexData[base] = transformed.x;
            vertexData[base + 1] = transformed.y;
            vertexData[base + 2] = u0 + (localX + half) / size * uvSize;
            vertexData[base + 3] = v0 + (localY + half) / size * uvSize;
            vertexData[base + 4] = localX;
            vertexData[base + 5] = localY;
        }
        prevProgram = GL11.glGetInteger((int)35725);
        prevVao = GL11.glGetInteger((int)34229);
        prevVbo = GL11.glGetInteger((int)34964);
        GL13.glActiveTexture((int)33984);
        prevTexture = GL11.glGetInteger((int)32873);
        GL11.glBindTexture((int)3553, (int)abstractTexture.getId());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        GL20.glUseProgram((int)programId);
        GL30.glBindVertexArray((int)vaoId);
        GL15.glBindBuffer((int)34962, (int)vboId);
        try (MemoryStack stack = MemoryStack.stackPush();){
            FloatBuffer vertexBuffer = stack.mallocFloat(vertexData.length);
            vertexBuffer.put(vertexData).flip();
            GL15.glBufferSubData((int)34962, (long)0L, (FloatBuffer)vertexBuffer);
            FloatBuffer modelViewBuffer = stack.mallocFloat(16);
            RenderSystem.getModelViewMatrix().get(modelViewBuffer);
            GL20.glUniformMatrix4fv((int)uModelViewMat, (boolean)false, (FloatBuffer)modelViewBuffer);
            FloatBuffer projBuffer = stack.mallocFloat(16);
            RenderSystem.getProjectionMatrix().get(projBuffer);
            GL20.glUniformMatrix4fv((int)uProjMat, (boolean)false, (FloatBuffer)projBuffer);
        }
        GL20.glUniform1i((int)uSampler, (int)0);
        GL20.glUniform2f((int)uHalf, (float)half, (float)half);
        GL20.glUniform1f((int)uRadius, (float)r);
        GL20.glUniform4f((int)uTint, (float)tintR, (float)tintG, (float)tintB, (float)alpha);
        GL20.glUniform1f((int)uFeather, (float) RoundRectRender.resolveFeather(pose));
        GL11.glDrawArrays((int)4, (int)0, (int)6);
        GL11.glBindTexture((int)3553, (int)prevTexture);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        GL30.glBindVertexArray((int)prevVao);
        GL20.glUseProgram((int)prevProgram);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        return true;
    }
}

