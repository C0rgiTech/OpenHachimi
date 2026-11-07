/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.shader;

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

public final class Shader {
    private static int programId;
    private static int uModelViewMat;
    private static int uProjMat;
    private static int uMode;
    private static int uHalf;
    private static int uRadius;
    private static int uCorners;
    private static int uInner;
    private static int uOuter;
    private static int uDir;
    private static int uAperture;
    private static int uSegA;
    private static int uSegB;
    private static int uThick;
    private static int uColor;
    private static int uFeather;
    private static int uGlow;
    private static int vaoId;
    private static int vboId;
    private static boolean failed;
    private static int prevVao;
    private static int prevVbo;
    private static int prevProgram;

    public static boolean corners(Matrix4f pose, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, int color) {
        if (width < 0.05f || height < 0.05f || color >>> 24 == 0) {
            return true;
        }
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float maxR = Math.min(halfW, halfH);
        float tl = Shader.clampRadius(topLeft, maxR);
        float tr = Shader.clampRadius(topRight, maxR);
        float br = Shader.clampRadius(bottomRight, maxR);
        float bl = Shader.clampRadius(bottomLeft, maxR);
        if (!Shader.begin(pose, x + halfW, y + halfH, halfW, halfH, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)1);
        GL20.glUniform2f((int)uHalf, (float)halfW, (float)halfH);
        GL20.glUniform4f((int)uCorners, (float)tl, (float)tr, (float)br, (float)bl);
        GL20.glUniform1f((int)uRadius, (float)0.0f);
        Shader.end();
        return true;
    }

    private Shader() {
    }

    static {
        programId = 0;
        uModelViewMat = -1;
        uProjMat = -1;
        uMode = -1;
        uHalf = -1;
        uRadius = -1;
        uCorners = -1;
        uInner = -1;
        uOuter = -1;
        uDir = -1;
        uAperture = -1;
        uSegA = -1;
        uSegB = -1;
        uThick = -1;
        uColor = -1;
        uFeather = -1;
        uGlow = -1;
        vaoId = 0;
        vboId = 0;
        failed = false;
        prevVao = 0;
        prevVbo = 0;
        prevProgram = 0;
    }

    private static void end() {
        GL11.glDrawArrays((int)4, (int)0, (int)6);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        GL30.glBindVertexArray((int)prevVao);
        GL20.glUseProgram((int)prevProgram);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static boolean begin(Matrix4f pose, float centerX, float centerY, float halfW, float halfH, int color) {
        if (!Shader.init()) {
            return false;
        }
        float pad = 1.5f;
        float[] offsets = new float[]{-(halfW + pad), -(halfH + pad), halfW + pad, -(halfH + pad), halfW + pad, halfH + pad, -(halfW + pad), -(halfH + pad), halfW + pad, halfH + pad, -(halfW + pad), halfH + pad};
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
        int currentVao = GL11.glGetInteger((int)34229);
        int currentVbo = GL11.glGetInteger((int)34964);
        prevProgram = GL11.glGetInteger((int)35725);
        prevVao = currentVao;
        prevVbo = currentVbo;
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
        GL20.glUniform4f((int)uColor, (float)((float)(color >> 16 & 0xFF) / 255.0f), (float)((float)(color >> 8 & 0xFF) / 255.0f), (float)((float)(color & 0xFF) / 255.0f), (float)((float)(color >>> 24) / 255.0f));
        GL20.glUniform1f((int)uFeather, (float) Shader.resolveFeather(pose));
        GL20.glUniform1f((int)uGlow, (float)0.0f);
        return true;
    }

    private static boolean init() {
        if (failed) {
            return false;
        }
        if (programId != 0) {
            return true;
        }
        int vertexShader = Shader.compileShader(35633, "#version 150\nin vec2 Position;\nin vec2 LocalPos;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n}\n");
        if (vertexShader == 0) {
            failed = true;
            return false;
        }
        int fragmentShader = Shader.compileShader(35632, "#version 150\nuniform int uMode;\nuniform vec2 uHalf;\nuniform float uRadius;\nuniform vec4 uCorners;\nuniform float uInner;\nuniform float uOuter;\nuniform vec2 uDir;\nuniform float uAperture;\nuniform vec2 uSegA;\nuniform vec2 uSegB;\nuniform float uThick;\nuniform vec4 uColor;\nuniform float uFeather;\nuniform float uGlow;\nin vec2 localPos;\nout vec4 fragColor;\nfloat sdBox(vec2 p, vec2 b, float r) {\n    vec2 q = abs(p) - b + r;\n    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - r;\n}\nfloat sdBoxQuad(vec2 p, vec2 b, vec4 rad) {\n    float r = (p.x > 0.0) ? ((p.y < 0.0) ? rad.y : rad.z) : ((p.y < 0.0) ? rad.x : rad.w);\n    return sdBox(p, b, r);\n}\nfloat sdRing(vec2 p) {\n    float d = length(p);\n    if (uInner <= 0.0) {\n        return d - uOuter;\n    }\n    return max(d - uOuter, uInner - d);\n}\nfloat sdArc(vec2 p, vec2 sc, float ra, float rb) {\n    p.x = abs(p.x);\n    return ((sc.y * p.x > sc.x * p.y) ? length(p - sc * ra) : abs(length(p) - ra)) - rb;\n}\nfloat sdCapsule(vec2 p, vec2 a, vec2 b, float r) {\n    vec2 pa = p - a;\n    vec2 ba = b - a;\n    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);\n    return length(pa - ba * h) - r;\n}\nvoid main() {\n    float d;\n    if (uMode == 0) {\n        d = sdBox(localPos, uHalf, uRadius);\n    } else if (uMode == 1) {\n        d = sdBoxQuad(localPos, uHalf, uCorners);\n    } else if (uMode == 2) {\n        d = sdRing(localPos);\n    } else if (uMode == 3) {\n        vec2 rp = vec2(dot(localPos, uDir), dot(localPos, vec2(-uDir.y, uDir.x)));\n        d = sdArc(rp, vec2(sin(uAperture), cos(uAperture)), (uOuter + uInner) * 0.5, max((uOuter - uInner) * 0.5, 0.0));\n    } else {\n        d = sdCapsule(localPos, uSegA, uSegB, uThick);\n    }\n    float alpha = 1.0 - smoothstep(-uFeather, uFeather, d);\n    if (uGlow > 0.5) {\n        alpha = exp(-max(d, 0.0) / max(uThick, 0.001));\n    }\n    if (alpha <= 0.0) discard;\n    fragColor = vec4(uColor.rgb, uColor.a * alpha);\n}\n");
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
        uMode = GL20.glGetUniformLocation((int)programId, (CharSequence)"uMode");
        uHalf = GL20.glGetUniformLocation((int)programId, (CharSequence)"uHalf");
        uRadius = GL20.glGetUniformLocation((int)programId, (CharSequence)"uRadius");
        uCorners = GL20.glGetUniformLocation((int)programId, (CharSequence)"uCorners");
        uInner = GL20.glGetUniformLocation((int)programId, (CharSequence)"uInner");
        uOuter = GL20.glGetUniformLocation((int)programId, (CharSequence)"uOuter");
        uDir = GL20.glGetUniformLocation((int)programId, (CharSequence)"uDir");
        uAperture = GL20.glGetUniformLocation((int)programId, (CharSequence)"uAperture");
        uSegA = GL20.glGetUniformLocation((int)programId, (CharSequence)"uSegA");
        uSegB = GL20.glGetUniformLocation((int)programId, (CharSequence)"uSegB");
        uThick = GL20.glGetUniformLocation((int)programId, (CharSequence)"uThick");
        uColor = GL20.glGetUniformLocation((int)programId, (CharSequence)"uColor");
        uFeather = GL20.glGetUniformLocation((int)programId, (CharSequence)"uFeather");
        uGlow = GL20.glGetUniformLocation((int)programId, (CharSequence)"uGlow");
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

    public static boolean box(Matrix4f pose, float x, float y, float width, float height, float radius, int color) {
        if (width < 0.05f || height < 0.05f || color >>> 24 == 0) {
            return true;
        }
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float r = radius;
        if (r < 0.0f) {
            r = 0.0f;
        }
        if (r > Math.min(halfW, halfH)) {
            r = Math.min(halfW, halfH);
        }
        if (!Shader.begin(pose, x + halfW, y + halfH, halfW, halfH, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)0);
        GL20.glUniform2f((int)uHalf, (float)halfW, (float)halfH);
        GL20.glUniform1f((int)uRadius, (float)r);
        Shader.end();
        return true;
    }

    public static boolean glow(Matrix4f pose, float x, float y, float width, float height, float radius, float sigma, int color) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float maxR = Math.min(halfW, halfH);
        float r = radius < 0.0f ? 0.0f : (radius > maxR ? maxR : radius);
        float expand = Math.max(sigma * 3.2f, 1.0f);
        if (!Shader.begin(pose, x + halfW, y + halfH, halfW + expand, halfH + expand, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)0);
        GL20.glUniform2f((int)uHalf, (float)halfW, (float)halfH);
        GL20.glUniform1f((int)uRadius, (float)r);
        GL20.glUniform1f((int)uGlow, (float)1.0f);
        GL20.glUniform1f((int)uThick, (float)Math.max(sigma, 0.05f));
        Shader.end();
        return true;
    }

    public static boolean isAvailable() {
        return !failed;
    }

    public static boolean arc(Matrix4f pose, float centerX, float centerY, float inner, float outer, float startDeg, float endDeg, int color) {
        if (outer <= 0.0f || outer <= inner || color >>> 24 == 0) {
            return true;
        }
        float sweep = endDeg - startDeg;
        if (sweep < 0.0f) {
            sweep += 360.0f;
        }
        if (sweep >= 359.0f) {
            return Shader.ring(pose, centerX, centerY, inner, outer, color);
        }
        float midDeg = startDeg + sweep * 0.5f;
        double midRad = Math.toRadians(midDeg);
        double aperture = Math.toRadians((double)sweep * 0.5);
        if (!Shader.begin(pose, centerX, centerY, outer, outer, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)3);
        GL20.glUniform1f((int)uInner, (float)Math.max(inner, 0.0f));
        GL20.glUniform1f((int)uOuter, (float)outer);
        GL20.glUniform2f((int)uDir, (float)((float)Math.cos(midRad)), (float)((float)Math.sin(midRad)));
        GL20.glUniform1f((int)uAperture, (float)((float)aperture));
        Shader.end();
        return true;
    }

    public static boolean glowCorners(Matrix4f pose, float x, float y, float width, float height, float topLeft, float topRight, float bottomRight, float bottomLeft, float sigma, int color) {
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float maxR = Math.min(halfW, halfH);
        float expand = Math.max(sigma * 3.2f, 1.0f);
        if (!Shader.begin(pose, x + halfW, y + halfH, halfW + expand, halfH + expand, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)1);
        GL20.glUniform2f((int)uHalf, (float)halfW, (float)halfH);
        GL20.glUniform4f((int)uCorners, (float) Shader.clampRadius(topLeft, maxR), (float) Shader.clampRadius(topRight, maxR), (float) Shader.clampRadius(bottomRight, maxR), (float) Shader.clampRadius(bottomLeft, maxR));
        GL20.glUniform1f((int)uGlow, (float)1.0f);
        GL20.glUniform1f((int)uThick, (float)Math.max(sigma, 0.05f));
        Shader.end();
        return true;
    }

    public static boolean capsule(Matrix4f pose, float x1, float y1, float x2, float y2, float thickness, int color) {
        float halfH;
        if (thickness <= 0.0f || color >>> 24 == 0) {
            return true;
        }
        float centerX = (x1 + x2) * 0.5f;
        float centerY = (y1 + y2) * 0.5f;
        float halfW = Math.abs(x2 - x1) * 0.5f + thickness;
        if (!Shader.begin(pose, centerX, centerY, halfW, halfH = Math.abs(y2 - y1) * 0.5f + thickness, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)4);
        GL20.glUniform2f((int)uSegA, (float)(x1 - centerX), (float)(y1 - centerY));
        GL20.glUniform2f((int)uSegB, (float)(x2 - centerX), (float)(y2 - centerY));
        GL20.glUniform1f((int)uThick, (float)(thickness * 0.5f));
        Shader.end();
        return true;
    }

    public static boolean ring(Matrix4f pose, float centerX, float centerY, float inner, float outer, int color) {
        if (outer <= 0.0f || outer <= inner || color >>> 24 == 0) {
            return true;
        }
        if (!Shader.begin(pose, centerX, centerY, outer, outer, color)) {
            return false;
        }
        GL20.glUniform1i((int)uMode, (int)2);
        GL20.glUniform1f((int)uInner, (float)Math.max(inner, 0.0f));
        GL20.glUniform1f((int)uOuter, (float)outer);
        Shader.end();
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

    private static float clampRadius(float value, float max) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}

