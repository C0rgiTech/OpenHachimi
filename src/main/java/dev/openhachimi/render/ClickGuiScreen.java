/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.render;

import dev.openhachimi.modules.Module;
import dev.openhachimi.utils.ModuleManager;
import dev.openhachimi.config.ConfigLoadManager;
import dev.openhachimi.utils.ClickGUI;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen
extends Screen {
    private static final byte[] S_64B32B3C = new byte[]{5, 55, 43, 1, 45, 44, 36, 43, 37, 15, 39, 44, 55};  // XOR0x42 "GuiConfigMenu"
    private static final byte[] S_39E16AB3 = new byte[]{1, 46, 43, 33, 41, 5, 55, 43};  // XOR0x42 "ClickGui"

    public ClickGuiScreen() {
        super((Component)Component.literal((String)"GuiConfigMenu"));
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        try {
            if (this.minecraft == null) {
                ClickGUI.isOpen = false;
                return;
            }
            super.render(g, mouseX, mouseY, partialTicks);
            ClickGUI.onRender2D(g, mouseX, mouseY);
        }
        catch (Throwable error) {
            ClickGUI.isOpen = false;
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClickGUI.onKeyPressed(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void onClose() {
        super.onClose();
        ClickGUI.finishEdit();
        ClickGUI.isOpen = false;
        Module clickGui = ModuleManager.getModule("ClickGui");
        if (clickGui != null && clickGui.enabled) {
            clickGui.enabled = false;
        }
        ConfigLoadManager.getInstance().saveConfig();
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        ClickGUI.handleMouseReleased(button, (int)mouseX, (int)mouseY);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        ClickGUI.handleScroll(delta, (int)mouseX, (int)mouseY);
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ClickGUI.handleMousePressed(button, (int)mouseX, (int)mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (ClickGUI.onCharTyped(codePoint)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    public boolean isPauseScreen() {
        return false;
    }
}

