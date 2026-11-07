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
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

public final class KawaseBlur {
    private static int quadProgram;
    private static int liquidProgram;
    private static int copyDir;
    private static int uModelViewMat;
    private static int uProjMat;
    private static int uHalf;
    private static int uRadius;
    private static int uVeil;
    private static int uVeilAmt;
    private static int uAlpha;
    private static int uBrightness;
    private static int uFade;
    private static int uHover;
    private static int uScreen;
    private static int uGuiScale;
    private static int uFeather;
    private static int uSpec;
    private static int uLevel;
    private static int uSampler0;
    private static int quadVao;
    private static int quadVbo;
    private static int shapeVao;
    private static int shapeVbo;
    private static int fboSrc;
    private static int texSrc;
    private static int fboDst;
    private static int texDst;
    private static int workW;
    private static int workH;
    private static int srcW;
    private static int srcH;
    private static boolean failed;
    private static boolean ready;
    private static final int[] prevViewport;
    private static int prevFbo;
    private static int prevProgram;
    private static int prevVao;
    private static int prevVbo;
    private static int prevActiveUnit;
    private static int prevTex0;
    private static boolean prevBlend;
    private static boolean prevDepth;
    private static boolean prevCull;
    private static boolean prevScissor;
    private static int prevBSrc;
    private static int prevBDst;
    private static int prevBSrcA;
    private static int prevBDstA;
    public static final float CONTROL_GAIN = 3.0f;

    public static boolean isReady() {
        return !failed && ready && texSrc != 0;
    }

    public static boolean panel(Matrix4f pose, float x, float y, float width, float height, float radius, int tint, float fade, float opacity, float brightness) {
        return KawaseBlur.draw(pose, x, y, width, height, radius, 0xFFFFFF, 0.0f, opacity, brightness, fade, 0.0f, 0.04f);
    }

    private KawaseBlur() {
    }

    static {
        copyDir = -1;
        uModelViewMat = -1;
        uProjMat = -1;
        uHalf = -1;
        uRadius = -1;
        uVeil = -1;
        uVeilAmt = -1;
        uAlpha = -1;
        uBrightness = -1;
        uFade = -1;
        uHover = -1;
        uScreen = -1;
        uGuiScale = -1;
        uFeather = -1;
        uSpec = -1;
        uLevel = -1;
        uSampler0 = -1;
        prevViewport = new int[4];
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
        if (quadProgram != 0) {
            return true;
        }
        quadProgram = KawaseBlur.program("#version 150\nin vec2 aPos;\nout vec2 vUV;\nvoid main() {\n    vUV = aPos * 0.5 + 0.5;\n    gl_Position = vec4(aPos, 0.0, 1.0);\n}\n", "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 uDir;\nin vec2 vUV;\nout vec4 fragColor;\nvoid main() {\n    vec4 c = texture(Sampler0, vUV) * 0.2270270270;\n    c += texture(Sampler0, vUV + uDir * 1.3846153846) * 0.3162162162;\n    c += texture(Sampler0, vUV - uDir * 1.3846153846) * 0.3162162162;\n    c += texture(Sampler0, vUV + uDir * 3.2307692308) * 0.0702702703;\n    c += texture(Sampler0, vUV - uDir * 3.2307692308) * 0.0702702703;\n    fragColor = vec4(c.rgb, 1.0);\n}\n", new String[]{"aPos"});
        liquidProgram = KawaseBlur.program("#version 150\nin vec2 Position;\nin vec2 LocalPos;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n}\n", "#version 150\nuniform sampler2D Sampler0;\nuniform vec2 uHalf;\nuniform float uRadius;\nuniform vec3 uVeil;\nuniform float uVeilAmt;\nuniform float uAlpha;\nuniform float uBrightness;\nuniform float uFade;\nuniform float uHover;\nuniform vec2 uScreen;\nuniform float uGuiScale;\nuniform float uFeather;\nuniform float uSpec;\nuniform float uLevel;\nin vec2 localPos;\nout vec4 fragColor;\nfloat sdBox(vec2 p, vec2 b, float r) {\n    vec2 q = abs(p) - b + r;\n    return length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0) - r;\n}\nvoid main() {\n    float d = sdBox(localPos, uHalf, uRadius);\n    float shape = 1.0 - smoothstep(-uFeather, uFeather, d);\n    if (shape <= 0.001) discard;\n\n    vec2 uv = clamp(gl_FragCoord.xy / uScreen, vec2(0.001), vec2(0.999));\n    vec3 c = texture(Sampler0, uv).rgb;\n    float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));\n    c = mix(vec3(luma), c, 0.25);\n    c = mix(c, vec3(uLevel), 0.55);\n    c = mix(c, uVeil, uVeilAmt);\n    c *= max(uBrightness, 0.05);\n\n    c *= 1.0 + uHover * 0.08;\n\n    float a = clamp(uAlpha, 0.0, 1.0) * uFade * shape;\n    fragColor = vec4(c, a);\n}\n", new String[]{"Position", "LocalPos"});
        if (quadProgram == 0 || liquidProgram == 0) {
            failed = true;
            return false;
        }
        copyDir = GL20.glGetUniformLocation((int)quadProgram, (CharSequence)"uDir");
        uModelViewMat = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"ModelViewMat");
        uProjMat = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"ProjMat");
        uHalf = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uHalf");
        uRadius = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uRadius");
        uVeil = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uVeil");
        uVeilAmt = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uVeilAmt");
        uAlpha = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uAlpha");
        uBrightness = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uBrightness");
        uFade = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uFade");
        uHover = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uHover");
        uScreen = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uScreen");
        uGuiScale = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uGuiScale");
        uFeather = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uFeather");
        uSpec = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uSpec");
        uLevel = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"uLevel");
        uSampler0 = GL20.glGetUniformLocation((int)liquidProgram, (CharSequence)"Sampler0");
        float[] quad = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
        KawaseBlur.pushState();
        quadVao = GL30.glGenVertexArrays();
        quadVbo = GL15.glGenBuffers();
        GL30.glBindVertexArray((int)quadVao);
        GL15.glBindBuffer((int)34962, (int)quadVbo);
        try (MemoryStack stack = MemoryStack.stackPush();){
            FloatBuffer b = stack.mallocFloat(quad.length);
            b.put(quad).flip();
            GL15.glBufferData((int)34962, (FloatBuffer)b, (int)35044);
        }
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)8, (long)0L);
        shapeVao = GL30.glGenVertexArrays();
        shapeVbo = GL15.glGenBuffers();
        GL30.glBindVertexArray((int)shapeVao);
        GL15.glBindBuffer((int)34962, (int)shapeVbo);
        GL15.glBufferData((int)34962, (long)96L, (int)35048);
        GL20.glEnableVertexAttribArray((int)0);
        GL20.glVertexAttribPointer((int)0, (int)2, (int)5126, (boolean)false, (int)16, (long)0L);
        GL20.glEnableVertexAttribArray((int)1);
        GL20.glVertexAttribPointer((int)1, (int)2, (int)5126, (boolean)false, (int)16, (long)8L);
        KawaseBlur.popState();
        return true;
    }

    public static void prepare(float level) {
        int h;
        RenderTarget target;
        if (failed) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        RenderTarget renderTarget = target = mc == null ? null : mc.getMainRenderTarget();
        if (target == null || target.width < 16 || target.height < 16) {
            return;
        }
        if (!KawaseBlur.init()) {
            return;
        }
        int fullW = target.width;
        int fullH = target.height;
        int w = Math.max(8, fullW / 2);
        if (!KawaseBlur.ensureTargets(w, h = Math.max(8, fullH / 2), fullW, fullH)) {
            return;
        }
        float lv = Math.max(0.1f, Math.min(8.0f, level));
        int passes = Math.max(2, Math.min(26, Math.round(4.0f + lv * 2.0f)));
        KawaseBlur.pushState();
        GL11.glDisable((int)3089);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2884);
        GL20.glUseProgram((int)quadProgram);
        GL13.glActiveTexture((int)33984);
        GL20.glUniform1i((int)GL20.glGetUniformLocation((int)quadProgram, (CharSequence)"Sampler0"), (int)0);
        GL20.glUniform2f((int)copyDir, (float)0.0f, (float)0.0f);
        GL11.glBindTexture((int)3553, (int)target.getColorTextureId());
        GL30.glBindFramebuffer((int)36160, (int)fboSrc);
        GL11.glViewport((int)0, (int)0, (int)w, (int)h);
        KawaseBlur.drawQuad();
        for (int i = 0; i < passes; ++i) {
            float radius = 1.5f + (float)i * 1.15f;
            float invW = radius / (float)w;
            float invH = radius / (float)h;
            GL20.glUniform2f((int)copyDir, (float)invW, (float)0.0f);
            GL11.glBindTexture((int)3553, (int)texSrc);
            GL30.glBindFramebuffer((int)36160, (int)fboDst);
            KawaseBlur.drawQuad();
            GL20.glUniform2f((int)copyDir, (float)0.0f, (float)invH);
            GL11.glBindTexture((int)3553, (int)texDst);
            GL30.glBindFramebuffer((int)36160, (int)fboSrc);
            KawaseBlur.drawQuad();
        }
        KawaseBlur.popState();
        ready = true;
    }

    private static boolean attach(int fbo, int tex, int w, int h) {
        GL11.glBindTexture((int)3553, (int)tex);
        GL11.glTexImage2D((int)3553, (int)0, (int)32856, (int)w, (int)h, (int)0, (int)6408, (int)5121, (long)0L);
        GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
        GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
        GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
        GL30.glBindFramebuffer((int)36160, (int)fbo);
        GL30.glFramebufferTexture2D((int)36160, (int)36064, (int)3553, (int)tex, (int)0);
        boolean complete = GL30.glCheckFramebufferStatus((int)36160) == 36053;
        GL30.glBindFramebuffer((int)36160, (int)prevFbo);
        return complete;
    }

    public static boolean control(Matrix4f pose, float x, float y, float width, float height, float radius, int tint, float fade, float hover, float panelBrightness) {
        int color = (tint & 0xFFFFFF) == 0 ? 0xFFFFFF : tint;
        float strength = (float)(tint >>> 24 & 0xFF) / 255.0f;
        float pillBrightness = Math.max(0.05f, panelBrightness) * 3.0f;
        float amt = Math.max(0.0f, Math.min(1.0f, strength * 1.0f));
        return KawaseBlur.draw(pose, x, y, width, height, radius, color, amt, 0.96f, pillBrightness, fade, hover, 0.16f);
    }

    private static void pushState() {
        prevFbo = GL11.glGetInteger((int)36006);
        prevProgram = GL11.glGetInteger((int)35725);
        prevVao = GL11.glGetInteger((int)34229);
        prevVbo = GL11.glGetInteger((int)34964);
        prevActiveUnit = GL11.glGetInteger((int)34016);
        GL11.glGetIntegerv((int)2978, (int[])prevViewport);
        prevBlend = GL11.glIsEnabled((int)3042);
        prevDepth = GL11.glIsEnabled((int)2929);
        prevCull = GL11.glIsEnabled((int)2884);
        prevScissor = GL11.glIsEnabled((int)3089);
        prevBSrc = GL11.glGetInteger((int)32969);
        prevBDst = GL11.glGetInteger((int)32968);
        prevBSrcA = GL11.glGetInteger((int)32971);
        prevBDstA = GL11.glGetInteger((int)32970);
        GL13.glActiveTexture((int)33984);
        prevTex0 = GL11.glGetInteger((int)32873);
        GL13.glActiveTexture((int)prevActiveUnit);
    }

    public static boolean isAvailable() {
        return !failed;
    }

    private static int program(String vertex, String fragment, String[] attribs) {
        int vs = KawaseBlur.compile(35633, vertex);
        if (vs == 0) {
            return 0;
        }
        int fs = KawaseBlur.compile(35632, fragment);
        if (fs == 0) {
            GL20.glDeleteShader((int)vs);
            return 0;
        }
        int id = GL20.glCreateProgram();
        GL20.glAttachShader((int)id, (int)vs);
        GL20.glAttachShader((int)id, (int)fs);
        for (int i = 0; i < attribs.length; ++i) {
            GL20.glBindAttribLocation((int)id, (int)i, (CharSequence)attribs[i]);
        }
        GL20.glLinkProgram((int)id);
        GL20.glDeleteShader((int)vs);
        GL20.glDeleteShader((int)fs);
        if (GL20.glGetProgrami((int)id, (int)35714) == 0) {
            GL20.glDeleteProgram((int)id);
            return 0;
        }
        return id;
    }

    private static void drawQuad() {
        GL30.glBindVertexArray((int)quadVao);
        GL15.glBindBuffer((int)34962, (int)quadVbo);
        GL11.glDrawArrays((int)4, (int)0, (int)6);
    }

    private static boolean draw(Matrix4f pose, float x, float y, float width, float height, float radius, int veilColor, float veilAmount, float alpha, float brightness, float fade, float hover, float spec) {
        float guiScale;
        RenderTarget target;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget renderTarget = target = mc == null ? null : mc.getMainRenderTarget();
        if (target == null || target.width < 16 || target.height < 16) {
            return false;
        }
        if (!KawaseBlur.isReady()) {
            return false;
        }
        if (target.width != srcW || target.height != srcH) {
            ready = false;
            return false;
        }
        float f = guiScale = mc.getWindow() != null ? (float)mc.getWindow().getGuiScale() : 1.0f;
        if (guiScale < 1.0E-4f) {
            guiScale = 1.0f;
        }
        KawaseBlur.pushState();
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)2929);
        float halfW = width * 0.5f;
        float halfH = height * 0.5f;
        float centerX = x + halfW;
        float centerY = y + halfH;
        float r = radius < 0.0f ? 0.0f : Math.min(radius, Math.min(halfW, halfH));
        float pad = 2.0f;
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
        GL20.glUseProgram((int)liquidProgram);
        GL30.glBindVertexArray((int)shapeVao);
        GL15.glBindBuffer((int)34962, (int)shapeVbo);
        try (MemoryStack stack = MemoryStack.stackPush();){
            FloatBuffer vb = stack.mallocFloat(vertexData.length);
            vb.put(vertexData).flip();
            GL15.glBufferSubData((int)34962, (long)0L, (FloatBuffer)vb);
            FloatBuffer mv = stack.mallocFloat(16);
            RenderSystem.getModelViewMatrix().get(mv);
            GL20.glUniformMatrix4fv((int)uModelViewMat, (boolean)false, (FloatBuffer)mv);
            FloatBuffer pj = stack.mallocFloat(16);
            RenderSystem.getProjectionMatrix().get(pj);
            GL20.glUniformMatrix4fv((int)uProjMat, (boolean)false, (FloatBuffer)pj);
        }
        GL13.glActiveTexture((int)33984);
        GL11.glBindTexture((int)3553, (int)texSrc);
        GL20.glUniform1i((int)uSampler0, (int)0);
        GL20.glUniform2f((int)uHalf, (float)halfW, (float)halfH);
        GL20.glUniform1f((int)uRadius, (float)r);
        GL20.glUniform3f((int)uVeil, (float)((float)(veilColor >> 16 & 0xFF) / 255.0f), (float)((float)(veilColor >> 8 & 0xFF) / 255.0f), (float)((float)(veilColor & 0xFF) / 255.0f));
        GL20.glUniform1f((int)uVeilAmt, (float)Math.max(0.0f, Math.min(1.0f, veilAmount)));
        GL20.glUniform1f((int)uAlpha, (float)Math.max(0.0f, Math.min(1.0f, alpha)));
        GL20.glUniform1f((int)uBrightness, (float)brightness);
        GL20.glUniform1f((int)uFade, (float)fade);
        GL20.glUniform1f((int)uHover, (float)hover);
        GL20.glUniform2f((int)uScreen, (float)srcW, (float)srcH);
        GL20.glUniform1f((int)uGuiScale, (float)guiScale);
        GL20.glUniform1f((int)uFeather, (float)Math.max(0.02f, 0.8f / guiScale));
        GL20.glUniform1f((int)uSpec, (float)spec);
        GL20.glUniform1f((int)uLevel, (float)0.72f);
        GL11.glDrawArrays((int)4, (int)0, (int)6);
        KawaseBlur.popState();
        return true;
    }

    private static boolean ensureTargets(int w, int h, int fullW, int fullH) {
        if (texSrc != 0 && workW == w && workH == h && srcW == fullW && srcH == fullH) {
            return true;
        }
        KawaseBlur.releaseTargets();
        workW = w;
        workH = h;
        srcW = fullW;
        srcH = fullH;
        KawaseBlur.pushState();
        fboSrc = GL30.glGenFramebuffers();
        texSrc = GL11.glGenTextures();
        fboDst = GL30.glGenFramebuffers();
        texDst = GL11.glGenTextures();
        boolean ok = KawaseBlur.attach(fboSrc, texSrc, w, h) && KawaseBlur.attach(fboDst, texDst, w, h);
        KawaseBlur.popState();
        if (!ok) {
            KawaseBlur.releaseTargets();
            failed = true;
            return false;
        }
        return true;
    }

    private static void releaseTargets() {
        if (fboSrc != 0) {
            GL30.glDeleteFramebuffers((int)fboSrc);
            GL11.glDeleteTextures((int)texSrc);
            fboSrc = 0;
            texSrc = 0;
        }
        if (fboDst != 0) {
            GL30.glDeleteFramebuffers((int)fboDst);
            GL11.glDeleteTextures((int)texDst);
            fboDst = 0;
            texDst = 0;
        }
        ready = false;
        workW = 0;
        workH = 0;
    }

    private static void popState() {
        GL13.glActiveTexture((int)33984);
        GL11.glBindTexture((int)3553, (int)prevTex0);
        GL13.glActiveTexture((int)prevActiveUnit);
        GL30.glBindFramebuffer((int)36160, (int)prevFbo);
        GL11.glViewport((int)prevViewport[0], (int)prevViewport[1], (int)prevViewport[2], (int)prevViewport[3]);
        GL15.glBindBuffer((int)34962, (int)prevVbo);
        GL30.glBindVertexArray((int)prevVao);
        GL20.glUseProgram((int)prevProgram);
        KawaseBlur.toggle(3042, prevBlend);
        KawaseBlur.toggle(2929, prevDepth);
        KawaseBlur.toggle(2884, prevCull);
        KawaseBlur.toggle(3089, prevScissor);
        GL14.glBlendFuncSeparate((int)prevBSrc, (int)prevBDst, (int)prevBSrcA, (int)prevBDstA);
    }

    private static void toggle(int cap, boolean on) {
        if (on) {
            GL11.glEnable((int)cap);
        } else {
            GL11.glDisable((int)cap);
        }
    }
}

