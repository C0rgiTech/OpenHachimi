/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.render.shader;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class LiquidGlassRender {
    private static int copyProgram;
    private static int glassProgram;
    private static int copyDir;
    private static int glassHalf;
    private static int glassRadius;
    private static int glassTint;
    private static int glassOrigin;
    private static int glassScreen;
    private static int glassGuiScale;
    private static int glassFeather;
    private static int quadVao;
    private static int quadVbo;
    private static int shapeVao;
    private static int shapeVbo;
    private static int fboA;
    private static int texA;
    private static int fboB;
    private static int texB;
    private static int targetW;
    private static int targetH;
    private static boolean failed;
    private static int prevVao;
    private static int prevVbo;
    private static int prevProgram;
    private static int prevFramebuffer;
    private static int prevTexture;

    private LiquidGlassRender() {
    }

    static {
        copyProgram = 0;
        glassProgram = 0;
        copyDir = -1;
        glassHalf = -1;
        glassRadius = -1;
        glassTint = -1;
        glassOrigin = -1;
        glassScreen = -1;
        glassGuiScale = -1;
        glassFeather = -1;
        quadVao = 0;
        quadVbo = 0;
        shapeVao = 0;
        shapeVbo = 0;
        fboA = 0;
        texA = 0;
        fboB = 0;
        texB = 0;
        failed = false;
        prevVao = 0;
        prevVbo = 0;
        prevProgram = 0;
        prevFramebuffer = 0;
        prevTexture = 0;
    }

    private static int compile(int type, String source) {
        int shader = GL20.glCreateShader((int)type);
        GL20.glShaderSource((int)shader, (CharSequence)source);
        GL20.glCompileShader((int)shader);
        if (GL20.glGetShaderi((int)shader, (int)35713) == 0) {
            GL20.glDeleteShader((int)shader);
            return 0;
        }
        return shader;
    }

    private static boolean init() {
        if (failed) {
            return false;
        }
        if (copyProgram != 0) {
            return true;
        }
        copyProgram = LiquidGlassRender.program("#version 150\nin vec2 aPos;\nout vec2 vUV;\nvoid main() {\n    vUV = aPos * 0.5 + 0.5;\n    gl_Position = vec4(aPos, 0.0, 1.0);\n}\n", "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 uDir;\nin vec2 vUV;\nout vec4 fragColor;\nvoid main() {\n    vec4 c = texture(Sampler0, vUV) * 0.227027;\n    c += texture(Sampler0, vUV + uDir * 1.3846) * 0.316216;\n    c += texture(Sampler0, vUV - uDir * 1.3846) * 0.316216;\n    c += texture(Sampler0, vUV + uDir * 3.2308) * 0.070270;\n    c += texture(Sampler0, vUV - uDir * 3.2308) * 0.070270;\n    fragColor = c;\n}\n", new String[]{"aPos"});
        glassProgram = LiquidGlassRender.program("#version 150\nin vec2 Position;\nin vec2 LocalPos;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n}\n", "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 uHalf;\nuniform float uRadius;\nuniform vec4 uTint;\nuniform vec2 uOrigin;\nuniform vec2 uScreen;\nuniform float uGuiScale;\nuniform float uFeather;\nin vec2 localPos;\nout vec4 fragColor;\nvoid main() {\n    vec2 q = abs(localPos) - uHalf + uRadius;\n    float d = length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - uRadius;\n    float alpha = 1.0 - smoothstep(-uFeather, uFeather, d);\n    if (alpha <= 0.0) discard;\n    vec2 px = (uOrigin + (localPos + uHalf) * uGuiScale) / uScreen;\n    px.y = 1.0 - px.y;\n    vec3 c = texture(Sampler0, clamp(px, vec2(0.001), vec2(0.999))).rgb;\n    fragColor = vec4(c * uTint.rgb, uTint.a * alpha);\n}\n", new String[]{"Position", "LocalPos"});
        if (copyProgram == 0 || glassProgram == 0) {
            failed = true;
            return false;
        }
        copyDir = GL20.glGetUniformLocation((int)copyProgram, (CharSequence)"uDir");
        glassHalf = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uHalf");
        glassRadius = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uRadius");
        glassTint = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uTint");
        glassOrigin = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uOrigin");
        glassScreen = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uScreen");
        glassGuiScale = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uGuiScale");
        glassFeather = GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"uFeather");
        quadVao = GL30.glGenVertexArrays();
        quadVbo = GL15.glGenBuffers();
        float[] quad = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
        prevVao = GL11.glGetInteger((int)34229);
        prevVbo = GL11.glGetInteger((int)34964);
        GL30.glBindVertexArray((int)quadVao);
        GL15.glBindBuffer((int)34962, (int)quadVbo);
        GL15.glBufferData((int)34962, (long)(quad.length * 4), (int)35044);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)8, (long)0L);
        GL30.glBindVertexArray((int)prevVao);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        shapeVao = GL30.glGenVertexArrays();
        shapeVbo = GL15.glGenBuffers();
        GL30.glBindVertexArray((int)shapeVao);
        GL15.glBindBuffer((int)34962, (int)shapeVbo);
        GL15.glBufferData((int)34962, (long)96L, (int)35048);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)16, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)16, (long)8L);
        GL30.glBindVertexArray((int)prevVao);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        return true;
    }

    private static boolean attach(int fbo, int texture, int width, int height) {
        GL11.glBindTexture((int)3553, (int)texture);
        GL11.glTexImage2D((int)3553, (int)0, (int)32856, (int)width, (int)height, (int)0, (int)6408, (int)5121, (long)0L);
        GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
        GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
        GL30.glBindFramebuffer((int)36160, (int)fbo);
        GL30.glFramebufferTexture2D((int)36160, (int)36064, (int)3553, (int)texture, (int)0);
        boolean complete = GL30.glCheckFramebufferStatus((int)36160) == 36053;
        GL30.glBindFramebuffer((int)36160, (int)prevFramebuffer);
        return complete;
    }

    public static boolean isAvailable() {
        return !failed;
    }

    private static int program(String vertex, String fragment, String[] attribs) {
        int vertexShader = LiquidGlassRender.compile(35633, vertex);
        if (vertexShader == 0) {
            return 0;
        }
        int fragmentShader = LiquidGlassRender.compile(35632, fragment);
        if (fragmentShader == 0) {
            GL20.glDeleteShader((int)vertexShader);
            return 0;
        }
        int program = GL20.glCreateProgram();
        GL20.glAttachShader((int)program, (int)vertexShader);
        GL20.glAttachShader((int)program, (int)fragmentShader);
        for (int i = 0; i < attribs.length; ++i) {
            GL20.glBindAttribLocation((int)program, (int)i, (CharSequence)attribs[i]);
        }
        GL20.glLinkProgram((int)program);
        GL20.glDeleteShader((int)vertexShader);
        GL20.glDeleteShader((int)fragmentShader);
        if (GL20.glGetProgrami((int)program, (int)35714) == 0) {
            GL20.glDeleteProgram((int)program);
            return 0;
        }
        return program;
    }

    public static boolean draw(Matrix4f pose, float x, float y, float width, float height, float radius, int tint) {
        int smallH;
        float guiScale;
        RenderTarget target;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget renderTarget = target = mc == null ? null : mc.getMainRenderTarget();
        if (target == null || target.width < 8 || target.height < 8) {
            return false;
        }
        if (!LiquidGlassRender.init()) {
            return false;
        }
        prevFramebuffer = GL11.glGetInteger((int)36006);
        prevTexture = GL11.glGetInteger((int)32873);
        prevProgram = GL11.glGetInteger((int)35725);
        prevVao = GL11.glGetInteger((int)34229);
        prevVbo = GL11.glGetInteger((int)34964);
        float f = guiScale = mc.getWindow() != null ? (float)mc.getWindow().getGuiScale() : 1.0f;
        if (guiScale < 1.0E-4f) {
            guiScale = 1.0f;
        }
        int mainW = target.width;
        int mainH = target.height;
        int smallW = Math.max(1, mainW / 6);
        if (!LiquidGlassRender.ensureTargets(smallW, smallH = Math.max(1, mainH / 6))) {
            return false;
        }
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2884);
        GL20.glUseProgram((int)copyProgram);
        GL13.glActiveTexture((int)33984);
        GL11.glBindTexture((int)3553, (int)target.getColorTextureId());
        GL20.glUniform1i((int)GL20.glGetUniformLocation((int)copyProgram, (CharSequence)"Sampler0"), (int)0);
        GL20.glUniform2f((int)copyDir, (float)0.0f, (float)0.0f);
        GL30.glBindFramebuffer((int)36160, (int)fboA);
        GL11.glViewport((int)0, (int)0, (int)smallW, (int)smallH);
        LiquidGlassRender.drawFullscreenQuad();
        GL20.glUseProgram((int)copyProgram);
        for (int pass = 0; pass < 2; ++pass) {
            GL20.glUniform2f((int)copyDir, (float)(1.0f / (float)smallW), (float)0.0f);
            GL11.glBindTexture((int)3553, (int)texA);
            GL30.glBindFramebuffer((int)36160, (int)fboB);
            LiquidGlassRender.drawFullscreenQuad();
            GL20.glUniform2f((int)copyDir, (float)0.0f, (float)(1.0f / (float)smallH));
            GL11.glBindTexture((int)3553, (int)texB);
            GL30.glBindFramebuffer((int)36160, (int)fboA);
            LiquidGlassRender.drawFullscreenQuad();
        }
        GL30.glBindFramebuffer((int)36160, (int)prevFramebuffer);
        GL11.glViewport((int)0, (int)0, (int)mainW, (int)mainH);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float centerX = x + halfW;
        float centerY = y + halfH;
        float r = radius < 0.0f ? 0.0f : (radius > halfW || radius > halfH ? Math.min(halfW, halfH) : radius);
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
        GL20.glUseProgram((int)glassProgram);
        GL30.glBindVertexArray((int)shapeVao);
        GL15.glBindBuffer((int)34962, (int)shapeVbo);
        try (MemoryStack stack = MemoryStack.stackPush();){
            FloatBuffer vertexBuffer = stack.mallocFloat(vertexData.length);
            vertexBuffer.put(vertexData).flip();
            GL15.glBufferSubData((int)34962, (long)0L, (FloatBuffer)vertexBuffer);
            FloatBuffer modelViewBuffer = stack.mallocFloat(16);
            RenderSystem.getModelViewMatrix().get(modelViewBuffer);
            GL20.glUniformMatrix4fv((int)GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"ModelViewMat"), (boolean)false, (FloatBuffer)modelViewBuffer);
            FloatBuffer projBuffer = stack.mallocFloat(16);
            RenderSystem.getProjectionMatrix().get(projBuffer);
            GL20.glUniformMatrix4fv((int)GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"ProjMat"), (boolean)false, (FloatBuffer)projBuffer);
        }
        GL11.glBindTexture((int)3553, (int)texA);
        GL20.glUniform1i((int)GL20.glGetUniformLocation((int)glassProgram, (CharSequence)"Sampler0"), (int)0);
        GL20.glUniform2f((int)glassHalf, (float)halfW, (float)halfH);
        GL20.glUniform1f((int)glassRadius, (float)r);
        GL20.glUniform4f((int)glassTint, (float)((float)(tint >> 16 & 0xFF) / 255.0f), (float)((float)(tint >> 8 & 0xFF) / 255.0f), (float)((float)(tint & 0xFF) / 255.0f), (float)((float)(tint >>> 24) / 255.0f));
        GL20.glUniform2f((int)glassOrigin, (float)(x * guiScale), (float)(y * guiScale));
        GL20.glUniform2f((int)glassScreen, (float)mainW, (float)mainH);
        GL20.glUniform1f((int)glassGuiScale, (float)guiScale);
        float feather = 0.7f / guiScale;
        if (feather < 0.02f) {
            feather = 0.02f;
        }
        GL20.glUniform1f((int)glassFeather, (float)feather);
        GL30.glBindVertexArray((int)shapeVao);
        GL11.glDrawArrays((int)4, (int)0, (int)6);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        GL30.glBindVertexArray((int)prevVao);
        GL20.glUseProgram((int)prevProgram);
        GL11.glBindTexture((int)3553, (int)prevTexture);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        return true;
    }

    private static boolean ensureTargets(int width, int height) {
        if (fboA != 0 && targetW == width && targetH == height) {
            return true;
        }
        LiquidGlassRender.releaseTargets();
        targetW = width;
        targetH = height;
        fboA = GL30.glGenFramebuffers();
        texA = GL11.glGenTextures();
        fboB = GL30.glGenFramebuffers();
        texB = GL11.glGenTextures();
        if (!LiquidGlassRender.attach(fboA, texA, width, height) || !LiquidGlassRender.attach(fboB, texB, width, height)) {
            LiquidGlassRender.releaseTargets();
            failed = true;
            return false;
        }
        return true;
    }

    private static void releaseTargets() {
        if (fboA != 0) {
            GL30.glDeleteFramebuffers((int)fboA);
            GL11.glDeleteTextures((int)texA);
            fboA = 0;
            texA = 0;
        }
        if (fboB != 0) {
            GL30.glDeleteFramebuffers((int)fboB);
            GL11.glDeleteTextures((int)texB);
            fboB = 0;
            texB = 0;
        }
    }

    private static void drawFullscreenQuad() {
        GL30.glBindVertexArray((int)quadVao);
        GL11.glDrawArrays((int)4, (int)0, (int)6);
    }
}

