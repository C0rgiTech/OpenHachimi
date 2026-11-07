/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class ShaderUniforms {
    private static int programId;
    private static int uModelViewMat;
    private static int uProjMat;
    private static int uHalfSize;
    private static int uRadius;
    private static int uColor1;
    private static int uColor2;
    private static int uGradientAxis;
    private static int uFeather;
    private static int vaoId;
    private static int vboId;
    private static boolean failed;

    private ShaderUniforms() {
    }

    static {
        programId = 0;
        uModelViewMat = -1;
        uProjMat = -1;
        uHalfSize = -1;
        uRadius = -1;
        uColor1 = -1;
        uColor2 = -1;
        uGradientAxis = -1;
        uFeather = -1;
        vaoId = 0;
        vboId = 0;
        failed = false;
    }

    private static boolean init() {
        if (failed) {
            return false;
        }
        if (programId != 0) {
            return true;
        }
        int vertexShader = ShaderUniforms.compileShader(35633, "#version 150\nin vec2 Position;\nin vec2 LocalPos;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n}\n");
        if (vertexShader == 0) {
            failed = true;
            return false;
        }
        int fragmentShader = ShaderUniforms.compileShader(35632, "#version 150\nuniform vec2 HalfSize;\nuniform float Radius;\nuniform vec4 Color1;\nuniform vec4 Color2;\nuniform int GradientAxis;\nuniform float Feather;\nin vec2 localPos;\nout vec4 fragColor;\nvoid main() {\n    vec2 q = abs(localPos) - HalfSize + Radius;\n    float d = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - Radius;\n    float alpha = 1.0 - smoothstep(-Feather, Feather, d);\n    if (alpha <= 0.0) discard;\n    vec4 col;\n    if (GradientAxis == 1) {\n        float t = clamp((localPos.y + HalfSize.y) / max(2.0 * HalfSize.y, 0.0001), 0.0, 1.0);\n        col = mix(Color1, Color2, t);\n    } else if (GradientAxis == 2) {\n        float t = clamp((localPos.x + HalfSize.x) / max(2.0 * HalfSize.x, 0.0001), 0.0, 1.0);\n        col = mix(Color1, Color2, t);\n    } else {\n        col = Color1;\n    }\n    fragColor = vec4(col.rgb, col.a * alpha);\n}\n");
        if (fragmentShader == 0) {
            GL20.glDeleteShader((int)vertexShader);
            failed = true;
            return false;
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader((int)program, (int)vertexShader);
        GL20.glAttachShader((int)program, (int)fragmentShader);
        GL20.glBindAttribLocation((int)program, (int)0, (CharSequence)"Position");
        GL20.glBindAttribLocation((int)program, (int)1, (CharSequence)"LocalPos");
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
        uHalfSize = GL20.glGetUniformLocation((int)programId, (CharSequence)"HalfSize");
        uRadius = GL20.glGetUniformLocation((int)programId, (CharSequence)"Radius");
        uColor1 = GL20.glGetUniformLocation((int)programId, (CharSequence)"Color1");
        uColor2 = GL20.glGetUniformLocation((int)programId, (CharSequence)"Color2");
        uGradientAxis = GL20.glGetUniformLocation((int)programId, (CharSequence)"GradientAxis");
        uFeather = GL20.glGetUniformLocation((int)programId, (CharSequence)"Feather");
        vaoId = GL30.glGenVertexArrays();
        vboId = GL15.glGenBuffers();
        int prevVao = GL11.glGetInteger((int)34229);
        int prevVbo = GL11.glGetInteger((int)34964);
        GL30.glBindVertexArray((int)vaoId);
        GL15.glBindBuffer((int)34962, (int)vboId);
        GL15.glBufferData((int)34962, (long)96L, (int)35048);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)16, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)16, (long)8L);
        GL30.glBindVertexArray((int)prevVao);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        return true;
    }

    public static boolean isAvailable() {
        return !failed;
    }

    public static boolean draw(Matrix4f pose, float x, float y, float width, float height, float radius, int color1, int color2, int gradientAxis) {
        if (width < 0.05f || height < 0.05f) {
            return true;
        }
        if (!ShaderUniforms.init()) {
            return false;
        }
        float halfWidth = width * 0.5f;
        float halfHeight = height * 0.5f;
        float centerX = x + halfWidth;
        float centerY = y + halfHeight;
        float maxRadius = Math.min(halfWidth, halfHeight);
        float clamped = radius;
        if (clamped < 0.0f) {
            clamped = 0.0f;
        }
        if (clamped > maxRadius) {
            clamped = maxRadius;
        }
        float padW = halfWidth + 1.5f;
        float padH = halfHeight + 1.5f;
        float[] offsets = new float[]{-padW, -padH, padW, -padH, padW, padH, -padW, -padH, padW, padH, -padW, padH};
        Vector4f transformed = new Vector4f();
        float[] vertexData = new float[24];
        for (int i = 0; i < 6; ++i) {
            float localX = offsets[i * 2];
            float localY = offsets[i * 2 + 1];
            transformed.set(centerX + localX, centerY + localY, 0.0f, 1.0f).mul((Matrix4fc)pose);
            int base = i * 4;
            vertexData[base] = transformed.x;
            vertexData[base + 1] = transformed.y;
            vertexData[base + 2] = localX;
            vertexData[base + 3] = localY;
        }
        int prevProgram = GL11.glGetInteger((int)35725);
        int prevVao = GL11.glGetInteger((int)34229);
        int prevVbo = GL11.glGetInteger((int)34964);
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
        GL20.glUniform2f((int)uHalfSize, (float)halfWidth, (float)halfHeight);
        GL20.glUniform1f((int)uRadius, (float)clamped);
        GL20.glUniform4f((int)uColor1, (float)((float)(color1 >> 16 & 0xFF) / 255.0f), (float)((float)(color1 >> 8 & 0xFF) / 255.0f), (float)((float)(color1 & 0xFF) / 255.0f), (float)((float)(color1 >>> 24) / 255.0f));
        GL20.glUniform4f((int)uColor2, (float)((float)(color2 >> 16 & 0xFF) / 255.0f), (float)((float)(color2 >> 8 & 0xFF) / 255.0f), (float)((float)(color2 & 0xFF) / 255.0f), (float)((float)(color2 >>> 24) / 255.0f));
        GL20.glUniform1i((int)uGradientAxis, (int)gradientAxis);
        GL20.glUniform1f((int)uFeather, (float)ShaderUniforms.resolveFeather(pose));
        GL11.glDrawArrays((int)4, (int)0, (int)6);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        GL30.glBindVertexArray((int)prevVao);
        GL20.glUseProgram((int)prevProgram);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        return true;
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
        float f = 0;
        float scaleY;
        float scaleX = (float)Math.sqrt(pose.m00() * pose.m00() + pose.m01() * pose.m01());
        float scale = Math.max(scaleX, scaleY = (float)Math.sqrt(pose.m10() * pose.m10() + pose.m11() * pose.m11()));
        if (scale < 1.0E-4f) {
            scale = 1.0f;
        }
        float guiScale = 1.0f;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getWindow() != null) {
            float f2 = 0;
            guiScale = (float)minecraft.getWindow().getGuiScale();
            if (f2 < 1.0E-4f) {
                guiScale = 1.0f;
            }
        }
        float feather = 0.5f / (scale * guiScale);
        if (f < 0.02f) {
            feather = 0.02f;
        }
        if (feather > 1.2f) {
            feather = 1.2f;
        }
        return feather;
    }
}

