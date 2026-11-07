/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.ParticleStatus;

public class FpsboostModule
extends Module {
    private static final byte[] S_545FB45A = new byte[]{4, 50, 49, 0, 45, 45, 49, 54};  // XOR0x42 "FpsBoost"
    private final List<Runnable> restorers = new ArrayList<Runnable>();

    public FpsboostModule() {
        super("FpsBoost", Category.RENDER);
    }

    public void apply() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        this.restorers.clear();
        try {
            this.setOption(mc.options.ambientOcclusion(), Boolean.FALSE);
            this.setOption(mc.options.cloudStatus(), CloudStatus.OFF);
            this.setOption(mc.options.particles(), ParticleStatus.DECREASED);
            this.setOption(mc.options.entityShadows(), Boolean.FALSE);
            this.setOption(mc.options.biomeBlendRadius(), 0);
            this.setOption(mc.options.entityDistanceScaling(), 0.75);
            if ((Integer)mc.options.framerateLimit().get() < 260) {
                this.setOption(mc.options.framerateLimit(), 260);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    private <T> void setOption(OptionInstance<T> option, T value) {
        if (option == null) {
            return;
        }
        try {
            Object old = option.get();
            this.restorers.add(() -> ((OptionInstance)option).set(old)); // raw cast: T is erased here
            option.set(value);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void restore() {
        for (Runnable r : this.restorers) {
            try {
                r.run();
            }
            catch (Throwable throwable) {}
        }
        this.restorers.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onDisable() {
        this.restore();
    }

    @Override
    public void onEnable() {
        this.apply();
    }

    @Override
    public void onWorldChange() {
        this.restore();
        if (this.enabled) {
            this.apply();
        }
    }
}

