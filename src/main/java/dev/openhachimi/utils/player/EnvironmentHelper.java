/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils.player;

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

public final class EnvironmentHelper {
    private static final byte[] S_5047E16D = new byte[]{97, 52, 39, 48, 49, 43, 45, 44, 98, 115, 119, 114, 72, 43, 44, 98, 52, 39, 33, 112, 98, 18, 45, 49, 43, 54, 43, 45, 44, 121, 72, 43, 44, 98, 52, 39, 33, 112, 98, 14, 45, 33, 35, 46, 18, 45, 49, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 47, 35, 54, 118, 98, 15, 45, 38, 39, 46, 20, 43, 39, 53, 15, 35, 54, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 47, 35, 54, 118, 98, 18, 48, 45, 40, 15, 35, 54, 121, 72, 45, 55, 54, 98, 52, 39, 33, 112, 98, 46, 45, 33, 35, 46, 18, 45, 49, 121, 72, 52, 45, 43, 38, 98, 47, 35, 43, 44, 106, 107, 98, 57, 72, 98, 98, 98, 98, 37, 46, 29, 18, 45, 49, 43, 54, 43, 45, 44, 98, 127, 98, 18, 48, 45, 40, 15, 35, 54, 98, 104, 98, 15, 45, 38, 39, 46, 20, 43, 39, 53, 15, 35, 54, 98, 104, 98, 52, 39, 33, 118, 106, 18, 45, 49, 43, 54, 43, 45, 44, 110, 98, 114, 108, 114, 110, 98, 115, 108, 114, 107, 121, 72, 98, 98, 98, 98, 46, 45, 33, 35, 46, 18, 45, 49, 98, 127, 98, 14, 45, 33, 35, 46, 18, 45, 49, 121, 72, 63, 72};
    private static final byte[] S_1DED28EE = new byte[]{97, 52, 39, 48, 49, 43, 45, 44, 98, 115, 119, 114, 72, 55, 44, 43, 36, 45, 48, 47, 98, 52, 39, 33, 118, 98, 55, 16, 45, 53, 49, 25, 123, 116, 31, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 43, 44, 54, 98, 55, 1, 45, 55, 44, 54, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 36, 46, 45, 35, 54, 98, 55, 17, 43, 37, 47, 35, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 36, 46, 45, 35, 54, 98, 55, 5, 46, 45, 53, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 36, 46, 45, 35, 54, 98, 55, 4, 39, 35, 54, 42, 39, 48, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 36, 46, 45, 35, 54, 98, 55, 3, 33, 33, 39, 44, 54, 21, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 52, 39, 33, 118, 98, 55, 4, 43, 46, 46, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 52, 39, 33, 118, 98, 55, 17, 42, 35, 38, 45, 53, 121, 72, 55, 44, 43, 36, 45, 48, 47, 98, 52, 39, 33, 118, 98, 55, 3, 33, 33, 39, 44, 54, 121, 72, 43, 44, 98, 52, 39, 33, 112, 98, 46, 45, 33, 35, 46, 18, 45, 49, 121, 72, 45, 55, 54, 98, 52, 39, 33, 118, 98, 36, 48, 35, 37, 1, 45, 46, 45, 48, 121, 72, 36, 46, 45, 35, 54, 98, 39, 48, 36, 3, 106, 36, 46, 45, 35, 54, 98, 58, 107, 98, 57, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 49, 98, 127, 98, 49, 43, 37, 44, 106, 58, 107, 121, 72, 98, 98, 98, 98, 58, 98, 127, 98, 35, 32, 49, 106, 58, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 54, 98, 127, 98, 115, 108, 114, 98, 109, 98, 106, 115, 108, 114, 98, 105, 98, 114, 108, 113, 112, 117, 119, 123, 115, 115, 98, 104, 98, 58, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 59, 98, 127, 98, 115, 108, 114, 98, 111, 98, 106, 106, 106, 106, 106, 115, 108, 114, 116, 115, 118, 114, 119, 118, 112, 123, 98, 104, 98, 54, 98, 111, 98, 115, 108, 118, 119, 113, 115, 119, 112, 114, 112, 117, 107, 98, 104, 98, 54, 107, 98, 105, 98, 115, 108, 118, 112, 115, 118, 115, 113, 117, 118, 115, 107, 98, 104, 98, 54, 98, 111, 98, 114, 108, 112, 122, 118, 118, 123, 116, 117, 113, 116, 107, 98, 104, 98, 54, 98, 105, 98, 114, 108, 112, 119, 118, 122, 112, 123, 119, 123, 112, 107, 98, 104, 98, 54, 98, 104, 98, 39, 58, 50, 106, 111, 58, 98, 104, 98, 58, 107, 121, 72, 98, 98, 98, 98, 48, 39, 54, 55, 48, 44, 98, 49, 98, 104, 98, 59, 121, 72, 63, 72, 36, 46, 45, 35, 54, 98, 33, 38, 36, 106, 36, 46, 45, 35, 54, 98, 58, 107, 98, 57, 72, 98, 98, 98, 98, 48, 39, 54, 55, 48, 44, 98, 114, 108, 119, 98, 105, 98, 114, 108, 119, 98, 104, 98, 39, 48, 36, 3, 106, 58, 98, 104, 98, 114, 108, 117, 114, 117, 115, 114, 116, 117, 122, 107, 121, 72, 63, 72, 36, 46, 45, 35, 54, 98, 50, 48, 45, 32, 43, 54, 106, 36, 46, 45, 35, 54, 98, 50, 107, 98, 57, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 56, 98, 127, 98, 33, 46, 35, 47, 50, 106, 112, 108, 114, 98, 104, 98, 50, 98, 111, 98, 115, 108, 114, 110, 98, 111, 114, 108, 123, 123, 123, 123, 123, 123, 110, 98, 114, 108, 123, 123, 123, 123, 123, 123, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 46, 98, 127, 98, 46, 45, 37, 106, 115, 108, 114, 98, 111, 98, 56, 98, 104, 98, 56, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 54, 98, 127, 98, 118, 108, 113, 113, 114, 117, 118, 116, 117, 119, 114, 117, 123, 123, 122, 117, 113, 98, 105, 98, 114, 108, 119, 98, 104, 98, 46, 121, 72, 98, 98, 98, 98, 48, 39, 54, 55, 48, 44, 98, 115, 108, 118, 115, 118, 112, 115, 113, 119, 116, 98, 104, 98, 49, 43, 37, 44, 106, 56, 107, 98, 104, 98, 49, 51, 48, 54, 106, 49, 51, 48, 54, 106, 54, 98, 104, 98, 54, 98, 111, 98, 46, 98, 109, 98, 114, 108, 115, 118, 117, 107, 98, 111, 98, 54, 107, 121, 72, 63, 72, 52, 45, 43, 38, 98, 47, 35, 43, 44, 106, 107, 98, 57, 72, 98, 98, 98, 98, 52, 39, 33, 112, 98, 50, 98, 127, 98, 46, 45, 33, 35, 46, 18, 45, 49, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 43, 44, 52, 98, 127, 98, 115, 108, 114, 98, 109, 98, 55, 17, 43, 37, 47, 35, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 48, 39, 35, 33, 42, 98, 127, 98, 118, 108, 114, 98, 104, 98, 55, 17, 43, 37, 47, 35, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 32, 98, 127, 98, 114, 108, 114, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 32, 45, 58, 6, 43, 49, 54, 98, 127, 98, 115, 108, 114, 39, 123, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 33, 33, 39, 44, 54, 98, 127, 98, 114, 108, 114, 121, 72, 98, 98, 98, 98, 36, 45, 48, 98, 106, 43, 44, 54, 98, 43, 98, 127, 98, 114, 121, 98, 43, 98, 126, 98, 55, 1, 45, 55, 44, 54, 121, 98, 105, 105, 43, 107, 98, 57, 72, 98, 98, 98, 98, 98, 98, 98, 98, 52, 39, 33, 118, 98, 48, 98, 127, 98, 55, 16, 45, 53, 49, 25, 43, 31, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 52, 39, 33, 112, 98, 33, 98, 127, 98, 114, 108, 119, 98, 104, 98, 106, 48, 108, 58, 59, 98, 105, 98, 48, 108, 56, 53, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 52, 39, 33, 112, 98, 42, 98, 127, 98, 114, 108, 119, 98, 104, 98, 106, 48, 108, 56, 53, 98, 111, 98, 48, 108, 58, 59, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 52, 39, 33, 112, 98, 51, 98, 127, 98, 35, 32, 49, 106, 50, 98, 111, 98, 33, 107, 98, 111, 98, 42, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 32, 45, 58, 6, 43, 49, 54, 98, 127, 98, 47, 43, 44, 106, 32, 45, 58, 6, 43, 49, 54, 110, 98, 46, 39, 44, 37, 54, 42, 106, 47, 35, 58, 106, 51, 110, 98, 52, 39, 33, 112, 106, 114, 108, 114, 107, 107, 107, 98, 105, 98, 47, 43, 44, 106, 47, 35, 58, 106, 51, 108, 58, 110, 98, 51, 108, 59, 107, 110, 98, 114, 108, 114, 107, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 43, 36, 98, 106, 51, 108, 58, 98, 124, 98, 48, 39, 35, 33, 42, 98, 62, 62, 98, 51, 108, 59, 98, 124, 98, 48, 39, 35, 33, 42, 107, 98, 33, 45, 44, 54, 43, 44, 55, 39, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 33, 58, 98, 127, 98, 33, 38, 36, 106, 106, 50, 108, 58, 98, 111, 98, 48, 108, 58, 107, 98, 104, 98, 43, 44, 52, 107, 98, 111, 98, 33, 38, 36, 106, 106, 50, 108, 58, 98, 111, 98, 48, 108, 56, 107, 98, 104, 98, 43, 44, 52, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 33, 59, 98, 127, 98, 33, 38, 36, 106, 106, 50, 108, 59, 98, 111, 98, 48, 108, 59, 107, 98, 104, 98, 43, 44, 52, 107, 98, 111, 98, 33, 38, 36, 106, 106, 50, 108, 59, 98, 111, 98, 48, 108, 53, 107, 98, 104, 98, 43, 44, 52, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 32, 98, 105, 127, 98, 33, 58, 98, 104, 98, 33, 59, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 58, 98, 127, 98, 49, 47, 45, 45, 54, 42, 49, 54, 39, 50, 106, 111, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 50, 108, 58, 98, 111, 98, 106, 48, 108, 56, 98, 111, 98, 55, 3, 33, 33, 39, 44, 54, 21, 107, 107, 98, 104, 98, 106, 115, 108, 114, 98, 111, 98, 49, 47, 45, 45, 54, 42, 49, 54, 39, 50, 106, 111, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 50, 108, 58, 98, 111, 98, 48, 108, 56, 107, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 59, 98, 127, 98, 49, 47, 45, 45, 54, 42, 49, 54, 39, 50, 106, 111, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 50, 108, 59, 98, 111, 98, 48, 108, 59, 107, 98, 104, 98, 106, 115, 108, 114, 98, 111, 98, 49, 47, 45, 45, 54, 42, 49, 54, 39, 50, 106, 111, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 50, 108, 59, 98, 111, 98, 48, 108, 53, 107, 107, 121, 72, 98, 98, 98, 98, 98, 98, 98, 98, 35, 33, 33, 39, 44, 54, 98, 105, 127, 98, 35, 58, 98, 104, 98, 35, 59, 121, 72, 98, 98, 98, 98, 63, 72, 98, 98, 98, 98, 32, 98, 127, 98, 33, 46, 35, 47, 50, 106, 32, 110, 98, 114, 108, 114, 110, 98, 115, 108, 114, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 32, 46, 55, 48, 6, 43, 49, 54, 98, 127, 98, 111, 55, 17, 43, 37, 47, 35, 98, 104, 98, 50, 48, 45, 32, 43, 54, 106, 33, 46, 35, 47, 50, 106, 32, 110, 98, 114, 108, 114, 114, 114, 114, 114, 115, 110, 98, 114, 108, 123, 123, 123, 123, 123, 123, 107, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 38, 98, 127, 98, 47, 43, 58, 106, 47, 35, 58, 106, 32, 45, 58, 6, 43, 49, 54, 110, 98, 114, 108, 114, 107, 110, 98, 32, 46, 55, 48, 6, 43, 49, 54, 110, 98, 49, 47, 45, 45, 54, 42, 49, 54, 39, 50, 106, 114, 108, 114, 114, 112, 110, 98, 114, 108, 114, 113, 110, 98, 32, 107, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 33, 45, 52, 39, 48, 98, 127, 98, 115, 108, 114, 98, 111, 98, 49, 47, 45, 45, 54, 42, 49, 54, 39, 50, 106, 111, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 55, 4, 39, 35, 54, 42, 39, 48, 110, 98, 38, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 114, 98, 127, 98, 55, 17, 42, 35, 38, 45, 53, 108, 35, 98, 104, 98, 39, 58, 50, 106, 111, 47, 35, 58, 106, 38, 110, 98, 114, 108, 114, 107, 98, 109, 98, 47, 35, 58, 106, 55, 5, 46, 45, 53, 110, 98, 114, 108, 114, 114, 115, 107, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 115, 98, 127, 98, 55, 4, 43, 46, 46, 108, 35, 98, 104, 98, 33, 45, 52, 39, 48, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 112, 98, 127, 98, 55, 3, 33, 33, 39, 44, 54, 108, 35, 98, 104, 98, 33, 46, 35, 47, 50, 106, 35, 33, 33, 39, 44, 54, 110, 98, 114, 108, 114, 110, 98, 115, 108, 114, 107, 98, 104, 98, 33, 45, 52, 39, 48, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 114, 115, 98, 127, 98, 35, 115, 98, 105, 98, 35, 114, 98, 104, 98, 106, 115, 108, 114, 98, 111, 98, 35, 115, 107, 121, 72, 98, 98, 98, 98, 52, 39, 33, 113, 98, 33, 114, 115, 98, 127, 98, 35, 114, 115, 98, 124, 98, 114, 108, 114, 98, 125, 98, 106, 55, 4, 43, 46, 46, 108, 48, 37, 32, 98, 104, 98, 35, 115, 98, 105, 98, 55, 17, 42, 35, 38, 45, 53, 108, 48, 37, 32, 98, 104, 98, 35, 114, 98, 104, 98, 106, 115, 108, 114, 98, 111, 98, 35, 115, 107, 107, 98, 109, 98, 35, 114, 115, 98, 120, 98, 52, 39, 33, 113, 106, 114, 108, 114, 107, 121, 72, 98, 98, 98, 98, 36, 46, 45, 35, 54, 98, 35, 13, 55, 54, 98, 127, 98, 35, 112, 98, 105, 98, 35, 114, 115, 98, 104, 98, 106, 115, 108, 114, 98, 111, 98, 35, 112, 107, 121, 72, 98, 98, 98, 98, 43, 36, 98, 106, 35, 13, 55, 54, 98, 126, 127, 98, 114, 108, 114, 114, 114, 119, 107, 98, 38, 43, 49, 33, 35, 48, 38, 121, 72, 98, 98, 98, 98, 52, 39, 33, 113, 98, 33, 13, 55, 54, 98, 127, 98, 106, 55, 3, 33, 33, 39, 44, 54, 108, 48, 37, 32, 98, 104, 98, 35, 112, 98, 105, 98, 33, 114, 115, 98, 104, 98, 35, 114, 115, 98, 104, 98, 106, 115, 108, 114, 98, 111, 98, 35, 112, 107, 107, 98, 109, 98, 35, 13, 55, 54, 121, 72, 98, 98, 98, 98, 36, 48, 35, 37, 1, 45, 46, 45, 48, 98, 127, 98, 52, 39, 33, 118, 106, 33, 13, 55, 54, 110, 98, 35, 13, 55, 54, 107, 121, 72, 63, 72};
    private static final byte[] S_306CFD49 = new byte[]{18, 45, 49, 43, 54, 43, 45, 44};  // XOR0x42 "Position"
    private static final byte[] S_75260309 = new byte[]{14, 45, 33, 35, 46, 18, 45, 49};  // XOR0x42 "LocalPos"
    private static final byte[] S_1C927532 = new byte[]{15, 45, 38, 39, 46, 20, 43, 39, 53, 15, 35, 54};  // XOR0x42 "ModelViewMat"
    private static final byte[] S_50C888A3 = new byte[]{18, 48, 45, 40, 15, 35, 54};  // XOR0x42 "ProjMat"
    private static final byte[] S_697B3EE = new byte[]{55, 16, 45, 53, 49};  // XOR0x42 "uRows"
    private static final byte[] S_4B8B619A = new byte[]{55, 1, 45, 55, 44, 54};  // XOR0x42 "uCount"
    private static final byte[] S_4C69EAB0 = new byte[]{55, 17, 43, 37, 47, 35};  // XOR0x42 "uSigma"
    private static final byte[] S_692A7A2 = new byte[]{55, 5, 46, 45, 53};  // XOR0x42 "uGlow"
    private static final byte[] S_22C4626E = new byte[]{55, 4, 39, 35, 54, 42, 39, 48};  // XOR0x42 "uFeather"
    private static final byte[] S_16F0FE98 = new byte[]{55, 3, 33, 33, 39, 44, 54, 21};  // XOR0x42 "uAccentW"
    private static final byte[] S_6922798 = new byte[]{55, 4, 43, 46, 46};  // XOR0x42 "uFill"
    private static final byte[] S_40C279F5 = new byte[]{55, 17, 42, 35, 38, 45, 53};  // XOR0x42 "uShadow"
    private static final byte[] S_21C5B59F = new byte[]{55, 3, 33, 33, 39, 44, 54};  // XOR0x42 "uAccent"
    public static final int MAX_ROWS = 96;
    private static final String VERTEX_SOURCE = null; // initialised: assignment path not guaranteed
    private static final String FRAGMENT_SOURCE = null; // initialised: assignment path not guaranteed
    private static int programId;
    private static int uModelViewMat;
    private static int uProjMat;
    private static int uRows;
    private static int uCount;
    private static int uSigma;
    private static int uGlow;
    private static int uFeather;
    private static int uAccentW;
    private static int uFill;
    private static int uShadow;
    private static int uAccent;
    private static int vaoId;
    private static int vboId;
    private static boolean failed;

    private EnvironmentHelper() {
    }

    static {
        programId = 0;
        uModelViewMat = -1;
        uProjMat = -1;
        uRows = -1;
        uCount = -1;
        uSigma = -1;
        uGlow = -1;
        uFeather = -1;
        uAccentW = -1;
        uFill = -1;
        uShadow = -1;
        uAccent = -1;
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
        int vertexShader = EnvironmentHelper.compileShader(35633, "#version 150\nin vec2 Position;\nin vec2 LocalPos;\nuniform mat4 ModelViewMat;\nuniform mat4 ProjMat;\nout vec2 localPos;\nvoid main() {\n    gl_Position = ProjMat * ModelViewMat * vec4(Position, 0.0, 1.0);\n    localPos = LocalPos;\n}\n");
        if (vertexShader == 0) {
            failed = true;
            return false;
        }
        int fragmentShader = EnvironmentHelper.compileShader(35632, "#version 150\nuniform vec4 uRows[96];\nuniform int uCount;\nuniform float uSigma;\nuniform float uGlow;\nuniform float uFeather;\nuniform float uAccentW;\nuniform vec4 uFill;\nuniform vec4 uShadow;\nuniform vec4 uAccent;\nin vec2 localPos;\nout vec4 fragColor;\nfloat erfA(float x) {\n    float s = sign(x);\n    x = abs(x);\n    float t = 1.0 / (1.0 + 0.3275911 * x);\n    float y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592) * t * exp(-x * x);\n    return s * y;\n}\nfloat cdf(float x) {\n    return 0.5 + 0.5 * erfA(x * 0.70710678);\n}\nfloat probit(float p) {\n    float z = clamp(2.0 * p - 1.0, -0.999999, 0.999999);\n    float l = log(1.0 - z * z);\n    float t = 4.330746750799873 + 0.5 * l;\n    return 1.41421356 * sign(z) * sqrt(sqrt(t * t - l / 0.147) - t);\n}\nvoid main() {\n    vec2 p = localPos;\n    float inv = 1.0 / uSigma;\n    float reach = 4.0 * uSigma;\n    float b = 0.0;\n    float boxDist = 1.0e9;\n    float accent = 0.0;\n    for (int i = 0; i < uCount; ++i) {\n        vec4 r = uRows[i];\n        vec2 c = 0.5 * (r.xy + r.zw);\n        vec2 h = 0.5 * (r.zw - r.xy);\n        vec2 q = abs(p - c) - h;\n        boxDist = min(boxDist, length(max(q, vec2(0.0))) + min(max(q.x, q.y), 0.0));\n        if (q.x > reach || q.y > reach) continue;\n        float cx = cdf((p.x - r.x) * inv) - cdf((p.x - r.z) * inv);\n        float cy = cdf((p.y - r.y) * inv) - cdf((p.y - r.w) * inv);\n        b += cx * cy;\n        float ax = smoothstep(-uFeather, uFeather, p.x - (r.z - uAccentW)) * (1.0 - smoothstep(-uFeather, uFeather, p.x - r.z));\n        float ay = smoothstep(-uFeather, uFeather, p.y - r.y) * (1.0 - smoothstep(-uFeather, uFeather, p.y - r.w));\n        accent += ax * ay;\n    }\n    b = clamp(b, 0.0, 1.0);\n    float blurDist = -uSigma * probit(clamp(b, 0.000001, 0.999999));\n    float d = mix(max(boxDist, 0.0), blurDist, smoothstep(0.002, 0.03, b));\n    float cover = 1.0 - smoothstep(-uFeather, uFeather, d);\n    float a0 = uShadow.a * exp(-max(d, 0.0) / max(uGlow, 0.001));\n    float a1 = uFill.a * cover;\n    float a2 = uAccent.a * clamp(accent, 0.0, 1.0) * cover;\n    float a01 = a1 + a0 * (1.0 - a1);\n    vec3 c01 = a01 > 0.0 ? (uFill.rgb * a1 + uShadow.rgb * a0 * (1.0 - a1)) / a01 : vec3(0.0);\n    float aOut = a2 + a01 * (1.0 - a2);\n    if (aOut <= 0.0005) discard;\n    vec3 cOut = (uAccent.rgb * a2 + c01 * a01 * (1.0 - a2)) / aOut;\n    fragColor = vec4(cOut, aOut);\n}\n");
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
        uRows = GL20.glGetUniformLocation((int)programId, (CharSequence)"uRows");
        uCount = GL20.glGetUniformLocation((int)programId, (CharSequence)"uCount");
        uSigma = GL20.glGetUniformLocation((int)programId, (CharSequence)"uSigma");
        uGlow = GL20.glGetUniformLocation((int)programId, (CharSequence)"uGlow");
        uFeather = GL20.glGetUniformLocation((int)programId, (CharSequence)"uFeather");
        uAccentW = GL20.glGetUniformLocation((int)programId, (CharSequence)"uAccentW");
        uFill = GL20.glGetUniformLocation((int)programId, (CharSequence)"uFill");
        uShadow = GL20.glGetUniformLocation((int)programId, (CharSequence)"uShadow");
        uAccent = GL20.glGetUniformLocation((int)programId, (CharSequence)"uAccent");
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

    private static void setColor(int location, int argb) {
        GL20.glUniform4f((int)location, (float)((float)(argb >> 16 & 0xFF) / 255.0f), (float)((float)(argb >> 8 & 0xFF) / 255.0f), (float)((float)(argb & 0xFF) / 255.0f), (float)((float)(argb >>> 24) / 255.0f));
    }

    public static boolean isAvailable() {
        return !failed;
    }

    private static float feather(Matrix4f pose) {
        float feather;
        float scaleY;
        float scaleX = (float)Math.sqrt(pose.m00() * pose.m00() + pose.m01() * pose.m01());
        float scale = Math.max(scaleX, scaleY = (float)Math.sqrt(pose.m10() * pose.m10() + pose.m11() * pose.m11()));
        if (scale < 1.0E-4f) {
            scale = 1.0f;
        }
        float guiScale = 1.0f;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getWindow() != null && (guiScale = (float)minecraft.getWindow().getGuiScale()) < 1.0E-4f) {
            guiScale = 1.0f;
        }
        if ((feather = 0.7f / (scale * guiScale)) < 0.02f) {
            feather = 0.02f;
        }
        if (feather > 1.2f) {
            feather = 1.2f;
        }
        return feather;
    }

    public static boolean stairs(Matrix4f pose, float[] rows, int count, float sigma, float glowSigma, int fill, int shadow, int accent, float accentWidth) {
        if (count <= 0) {
            return true;
        }
        if (count > 96 || rows.length < count * 4 || !EnvironmentHelper.init()) {
            return false;
        }
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -3.4028235E38f;
        float maxY = -3.4028235E38f;
        for (int i = 0; i < count; ++i) {
            minX = Math.min(minX, rows[i * 4]);
            minY = Math.min(minY, rows[i * 4 + 1]);
            maxX = Math.max(maxX, rows[i * 4 + 2]);
            maxY = Math.max(maxY, rows[i * 4 + 3]);
        }
        float s = Math.max(sigma, 0.05f);
        float centerX = (minX + maxX) * 0.5f;
        float centerY = (minY + maxY) * 0.5f;
        float expand = s * 4.0f + Math.max(glowSigma, 0.05f) * 3.2f + 1.5f;
        float halfW = (maxX - minX) * 0.5f + expand;
        float halfH = (maxY - minY) * 0.5f + expand;
        float[] corners = new float[]{-halfW, -halfH, halfW, -halfH, halfW, halfH, -halfW, -halfH, halfW, halfH, -halfW, halfH};
        Vector4f transformed = new Vector4f();
        float[] vertexData = new float[24];
        for (int i = 0; i < 6; ++i) {
            float localX = corners[i * 2];
            float localY = corners[i * 2 + 1];
            transformed.set(centerX + localX, centerY + localY, 0.0f, 1.0f).mul((Matrix4fc)pose);
            int base = i * 4;
            vertexData[base] = transformed.x;
            vertexData[base + 1] = transformed.y;
            vertexData[base + 2] = localX;
            vertexData[base + 3] = localY;
        }
        int prevVao = GL11.glGetInteger((int)34229);
        int prevVbo = GL11.glGetInteger((int)34964);
        int prevProgram = GL11.glGetInteger((int)35725);
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
            FloatBuffer rowBuffer = stack.mallocFloat(count * 4);
            for (int i = 0; i < count; ++i) {
                rowBuffer.put(rows[i * 4] - centerX).put(rows[i * 4 + 1] - centerY).put(rows[i * 4 + 2] - centerX).put(rows[i * 4 + 3] - centerY);
            }
            rowBuffer.flip();
            GL20.glUniform4fv((int)uRows, (FloatBuffer)rowBuffer);
        }
        GL20.glUniform1i((int)uCount, (int)count);
        GL20.glUniform1f((int)uSigma, (float)s);
        GL20.glUniform1f((int)uGlow, (float)glowSigma);
        GL20.glUniform1f((int)uFeather, (float)EnvironmentHelper.feather(pose));
        GL20.glUniform1f((int)uAccentW, (float)accentWidth);
        EnvironmentHelper.setColor(uFill, fill);
        EnvironmentHelper.setColor(uShadow, shadow);
        EnvironmentHelper.setColor(uAccent, accent);
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
}

