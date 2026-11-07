/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render;

import dev.openhachimi.settings.ListSetting;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL30C;

public class InterpolationTexture
extends AbstractTexture {
    // AbstractTexture declares load(ResourceManager); the body uploads the texture manually,
    // so the resource reload hook is a no-op here.
    @Override
    public void load(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
    }

    public InterpolationTexture(int width, int height, byte[] data, Format format, Filter min, Filter max) {
        this(width, height, InterpolationTexture.wrap(data), format, min, max);
    }

    public InterpolationTexture(final int width, final int height, final ByteBuffer data, final Format format, final Filter min, final Filter max) {
        if (RenderSystem.isOnRenderThread()) {
            this.upload(width, height, data, format, min, max);
        } else {
            RenderSystem.recordRenderCall((RenderCall)new RenderCall(){

                public void execute() {
                    InterpolationTexture.this.upload(width, height, data, format, min, max);
                }
            });
        }
    }

    private static ByteBuffer wrap(byte[] data) {
        ByteBuffer buf = ByteBuffer.allocateDirect(data.length);
        buf.put(data);
        buf.flip();
        return buf;
    }

    public void load(ListSetting resourceManager) {
    }

    private void upload(int width, int height, ByteBuffer data, Format format, Filter min, Filter max) {
        this.bind();
        GL30C.glPixelStorei((int)3312, (int)0);
        GL30C.glPixelStorei((int)3313, (int)0);
        GL30C.glPixelStorei((int)3314, (int)0);
        GL30C.glPixelStorei((int)32878, (int)0);
        GL30C.glPixelStorei((int)3315, (int)0);
        GL30C.glPixelStorei((int)3316, (int)0);
        GL30C.glPixelStorei((int)32877, (int)0);
        GL30C.glTexParameteri((int)3553, (int)10241, (int)min.toOpenGL());
        GL30C.glTexParameteri((int)3553, (int)10240, (int)max.toOpenGL());
        if (format == Format.A) {
            GL30C.glTexParameteri((int)3553, (int)36418, (int)1);
            GL30C.glTexParameteri((int)3553, (int)36419, (int)1);
            GL30C.glTexParameteri((int)3553, (int)36420, (int)1);
            GL30C.glTexParameteri((int)3553, (int)36421, (int)6403);
        }
        data.rewind();
        GL30C.glTexImage2D((int)3553, (int)0, (int)format.toOpenGL(), (int)width, (int)height, (int)0, (int)format.toOpenGL(), (int)5121, (ByteBuffer)data);
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

