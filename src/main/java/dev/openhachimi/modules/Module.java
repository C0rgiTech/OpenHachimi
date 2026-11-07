/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.hud.Notifications;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.Setting;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class Module {
    private static final byte[] S_900F73A = new byte[]{10, 43, 38, 39, 98, 43, 44, 98, 3, 48, 48, 35, 59, 14, 43, 49, 54};  // XOR0x42 "Hide in ArrayList"
    public String name;
    public boolean enabled;
    public boolean expanded;
    public float animOffset = 0.0f;
    public float animAlpha = 0.0f;
    public Category category;
    public int key;
    public boolean canBeEnabled = true;
    public static int skipTicks = 0;
    public final BooleanSetting invisible = new BooleanSetting("Hide in ArrayList", false);
    public List<Setting<?>> values = new ArrayList();

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
        this.expanded = false;
    }

    public void onDisable() {
    }

    public void onEnable() {
    }

    public boolean shouldRender2DWhenDisabled() {
        return false;
    }

    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
    }

    public void onTick() {
    }

    public void toggle() {
        if (!this.canBeEnabled) {
            if (this.enabled) {
                this.enabled = false;
                this.onDisable();
            }
            return;
        }
        this.enabled = !this.enabled;
        try {
            Notifications.showToggle(this.name, this.enabled);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (this.enabled) {
            this.onEnable();
        } else {
            this.onDisable();
        }
    }

    public void onPreTick() {
    }

    public boolean onIncomingPacket(Packet<?> packet) {
        return false;
    }

    public void onWorldChange() {
    }

    public void onRender3D() {
    }

    public boolean onOutgoingPacket(Packet<?> packet) {
        return false;
    }

    public void onSlowdown(EventRegistrar.SlowdownEvent event) {
    }

    public void onRender2D(GuiGraphics graphics, float partialTick) {
    }

    public void onUseItem(EventRegistrar.UseItemEvent event) {
    }

    public void onMovementInput(Input input) {
    }

    public void onKeyPressed(int keyCode) {
    }

    public void onAttackEntity(AttackEntityEvent event) {
    }

    public void onAttackSlowdown(EventRegistrar.AttackSlowdownEvent event) {
    }

    public void prepareSettings(JsonObject values) {
    }
}

