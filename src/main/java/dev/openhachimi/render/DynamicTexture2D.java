/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render;

import dev.openhachimi.settings.ListSetting;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30C;

public class DynamicTexture2D
extends AbstractTexture {
    // AbstractTexture declares load(ResourceManager); the body uploads the texture manually,
    // so the resource reload hook is a no-op here.
    @Override
    public void load(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
    }

    private volatile boolean uploaded;

    public DynamicTexture2D(final int width, final int height, final byte[] data, final Format format, final Filter filterMin, final Filter filterMag) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall((RenderCall)new RenderCall(){

                public void execute() {
                    DynamicTexture2D.this.upload(width, height, data, format, filterMin, filterMag);
                }
            });
        } else {
            this.upload(width, height, data, format, filterMin, filterMag);
        }
    }

    public DynamicTexture2D(final int width, final int height, final ByteBuffer buffer, final Format format, final Filter filterMin, final Filter filterMag) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall((RenderCall)new RenderCall(){

                public void execute() {
                    DynamicTexture2D.this.upload(width, height, buffer, format, filterMin, filterMag);
                }
            });
        } else {
            this.upload(width, height, buffer, format, filterMin, filterMag);
        }
    }

    public void load(ListSetting manager) throws IOException {
    }

    private void upload(int width, int height, byte[] data, Format format, Filter filterMin, Filter filterMag) {
        ByteBuffer buffer = BufferUtils.createByteBuffer((int)data.length).put(data);
        this.upload(width, height, buffer, format, filterMin, filterMag);
    }

    private void upload(int width, int height, ByteBuffer buffer, Format format, Filter filterMin, Filter filterMag) {
        this.bind();
        GL30C.glPixelStorei((int)3312, (int)0);
        GL30C.glPixelStorei((int)3313, (int)0);
        GL30C.glPixelStorei((int)3314, (int)0);
        GL30C.glPixelStorei((int)32878, (int)0);
        GL30C.glPixelStorei((int)3315, (int)0);
        GL30C.glPixelStorei((int)3316, (int)0);
        GL30C.glPixelStorei((int)32877, (int)0);
        GL30C.glPixelStorei((int)3317, (int)4);
        GL30C.glTexParameteri((int)3553, (int)10242, (int)33071);
        GL30C.glTexParameteri((int)3553, (int)10243, (int)33071);
        GL30C.glTexParameteri((int)3553, (int)10241, (int)(filterMin == Filter.Nearest ? 9728 : 9987));
        GL30C.glTexParameteri((int)3553, (int)10240, (int)(filterMag == Filter.Nearest ? 9728 : 9729));
        if (format == Format.A) {
            GL30C.glTexParameteri((int)3553, (int)36418, (int)1);
            GL30C.glTexParameteri((int)3553, (int)36419, (int)1);
            GL30C.glTexParameteri((int)3553, (int)36420, (int)1);
            GL30C.glTexParameteri((int)3553, (int)36421, (int)6403);
        }
        ((Buffer)buffer).rewind();
        GL30C.glTexImage2D((int)3553, (int)0, (int)format.toOpenGL(), (int)width, (int)height, (int)0, (int)format.toOpenGL(), (int)5121, (ByteBuffer)buffer);
        if (filterMin != Filter.Nearest) {
            try {
                GL30C.glGenerateMipmap((int)3553);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        this.uploaded = true;
    }

    public boolean isUploaded() {
        return this.uploaded;
    }

    public enum Format {
        A(6403), RGB(6407), RGBA(6408);

        private final int gl;

        Format(int gl) {
            this.gl = gl;
        }

        public int toOpenGL() {
            return this.gl;
        }
    }

    public enum Filter {
        Nearest(9728), Linear(9729);

        private final int gl;

        Filter(int gl) {
            this.gl = gl;
        }

        public int toOpenGL() {
            return this.gl;
        }
    }
}

