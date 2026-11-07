/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.render.shader.Shader;
import dev.openhachimi.utils.FrameHelper;
import dev.openhachimi.utils.render.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class InventoryrenderModule
extends Module {
    private static final byte[] S_705989F2 = new byte[]{11, 44, 52, 39, 44, 54, 45, 48, 59, 16, 39, 44, 38, 39, 48};  // XOR0x42 "InventoryRender"
    private static final byte[] S_58 = new byte[]{26};
    private static final byte[] S_59 = new byte[]{27};
    private static final byte[] S_1CDF08E3 = new byte[]{17, 46, 45, 54, 98, 17, 43, 56, 39};  // XOR0x42 "Slot Size"
    private static final byte[] S_17B08FEB = new byte[]{13, 50, 35, 33, 43, 54, 59};  // XOR0x42 "Opacity"
    private static final byte[] S_5842AEB8 = new byte[]{14, 43, 37, 42, 54, 98, 0, 35, 33, 41, 37, 48, 45, 55, 44, 38};  // XOR0x42 "Light Background"
    private static final byte[] S_5D827E6E = new byte[]{17, 42, 45, 53, 98, 11, 54, 39, 47, 98, 1, 45, 55, 44, 54, 49};  // XOR0x42 "Show Item Counts"
    private static final int COLUMNS = 9;
    private static final int ROWS = 3;
    private static final int HOTBAR_START = 0;
    private static final int INVENTORY_START = 9;
    private static final float RADIUS = 6.0f;
    private static final float SLOT_RADIUS = 3.5f;
    private static final float PADDING = 5.0f;
    private static final float SLOT_GAP = 2.0f;
    private static final float HOTBAR_GAP = 6.0f;
    private static final float RIM = 1.0f;
    public final ListSetting posX = new ListSetting("X", 20, 0, 4000);
    public final ListSetting posY = new ListSetting("Y", 20, 0, 4000);
    public final ListSetting slotSize = new ListSetting("Slot Size", 24, 16, 48);
    public final NumberSetting opacity = new NumberSetting("Opacity", 0.45f, 0.05f, 1.0f);
    public final BooleanSetting lightTheme = new BooleanSetting("Light Background", false);
    public final BooleanSetting showCounts = new BooleanSetting("Show Item Counts", true);
    private static final FrameHelper.Draggable OWNER = new FrameHelper.Draggable(){};

    public InventoryrenderModule() {
        super("InventoryRender", Category.RENDER);
        this.values.add(this.posX);
        this.values.add(this.posY);
        this.values.add(this.slotSize);
        this.values.add(this.opacity);
        this.values.add(this.lightTheme);
        this.values.add(this.showCounts);
    }

    private static float hit(double mouseX, double mouseY, float gridX, float slotY, float size, float step) {
        if (mouseY < (double)slotY || mouseY > (double)(slotY + size)) {
            return -1.0f;
        }
        for (int col = 0; col < 9; ++col) {
            float slotX = gridX + (float)col * step;
            if (!(mouseX >= (double)slotX) || !(mouseX <= (double)(slotX + size))) continue;
            return slotX;
        }
        return -1.0f;
    }

    private static void highlight(GuiGraphics graphics, Minecraft mc, float gridX, float gridY, float size, float step, boolean light, float op, float invH, float hotY) {
        double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
        int hoverAlpha = Math.round((float)(light ? 32 : 30) * op);
        int hoverColor = hoverAlpha << 24 | (light ? 0 : 0xFFFFFF);
        for (int row = 0; row < 3; ++row) {
            float slotY = gridY + (float)row * step;
            float hit = InventoryrenderModule.hit(mouseX, mouseY, gridX, slotY, size, step);
            if (!(hit >= 0.0f)) continue;
            InventoryrenderModule.rounded(graphics, hit, slotY, size, size, 3.5f, hoverColor);
            return;
        }
        float hit = InventoryrenderModule.hit(mouseX, mouseY, gridX, hotY, size, step);
        if (hit >= 0.0f) {
            InventoryrenderModule.rounded(graphics, hit, hotY, size, size, 3.5f, hoverColor);
        }
    }

    private static void drawSlots(GuiGraphics graphics, Minecraft mc, float gridX, float gridY, float size, float step, int rows, int startIndex, int slotColor, boolean counts) {
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < 9; ++col) {
                float slotX = gridX + (float)col * step;
                float slotY = gridY + (float)row * step;
                InventoryrenderModule.rounded(graphics, slotX, slotY, size, size, 3.5f, slotColor);
                ItemStack stack = mc.player.getInventory().getItem(startIndex + row * 9 + col);
                if (stack.isEmpty()) continue;
                float offset = (size - 16.0f) * 0.5f;
                int itemX = Math.round(slotX + offset);
                int itemY = Math.round(slotY + offset);
                graphics.renderItem(stack, itemX, itemY);
                if (!counts) continue;
                graphics.renderItemDecorations(mc.font, stack, itemX, itemY);
            }
        }
    }

    private static float[] handleDrag(Minecraft mc, float x, float y, float width, float height) {
        double mouseX = mc.mouseHandler.xpos() * (double)mc.getWindow().getGuiScaledWidth() / (double)mc.getWindow().getScreenWidth();
        double mouseY = mc.mouseHandler.ypos() * (double)mc.getWindow().getGuiScaledHeight() / (double)mc.getWindow().getScreenHeight();
        boolean down = GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)0) == 1;
        FrameHelper.updateMouse(mouseX, mouseY, down);
        FrameHelper.offer(OWNER, x - 5.0f, y - 5.0f, width + 10.0f, height + 10.0f);
        if (!FrameHelper.isActive(OWNER)) {
            return new float[]{x, y};
        }
        return new float[]{(float)mouseX - FrameHelper.grabOffsetX(), (float)mouseY - FrameHelper.grabOffsetY()};
    }

    private static void rounded(GuiGraphics graphics, float x, float y, float width, float height, float radius, int color) {
        graphics.flush();
        if (!Shader.box(graphics.pose().last().pose(), x, y, width, height, radius, color)) {
            RenderUtil.drawRoundedRect(graphics, x, y, width, height, radius, color);
        }
    }

    @Override
    public void onRender2D(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        float size = ((Integer)this.slotSize.get()).floatValue();
        float op = ((Float)this.opacity.get()).floatValue();
        boolean light = (Boolean)this.lightTheme.get();
        boolean counts = (Boolean)this.showCounts.get();
        float step = size + 2.0f;
        float gridW = 9.0f * step - 2.0f;
        float invH = 3.0f * step - 2.0f;
        float panelW = gridW + 10.0f;
        float panelH = 10.0f + invH + 6.0f + size;
        float x = ((Integer)this.posX.get()).floatValue();
        float y = ((Integer)this.posY.get()).floatValue();
        if (mc.screen != null) {
            float[] drag = InventoryrenderModule.handleDrag(mc, x, y, panelW, panelH);
            x = drag[0];
            y = drag[1];
            this.posX.set(Math.round(x));
            this.posY.set(Math.round(y));
        } else {
            FrameHelper.release();
        }
        int panelAlpha = Math.round(255.0f * op);
        int panelRgb = light ? 0xECECEC : 0x101010;
        int rimRgb = light ? 0 : 0xFFFFFF;
        int slotRgb = light ? 0 : 0xFFFFFF;
        int slotAlpha = Math.round((float)(light ? 18 : 26) * Math.min(1.0f, op * 1.4f));
        int rimAlpha = Math.round((float)(light ? 28 : 22) * op);
        float gridX = x + 5.0f;
        float gridY = y + 5.0f;
        float hotY = gridY + invH + 6.0f;
        InventoryrenderModule.rounded(graphics, x - 1.0f, y - 1.0f, panelW + 2.0f, panelH + 2.0f, 7.0f, rimAlpha << 24 | rimRgb);
        InventoryrenderModule.rounded(graphics, x, y, panelW, panelH, 6.0f, panelAlpha << 24 | panelRgb);
        int slotColor = slotAlpha << 24 | slotRgb;
        InventoryrenderModule.drawSlots(graphics, mc, gridX, gridY, size, step, 3, 9, slotColor, counts);
        InventoryrenderModule.drawSlots(graphics, mc, gridX, hotY, size, step, 1, 0, slotColor, counts);
        float divY = gridY + invH + 3.0f;
        int divAlpha = Math.round((float)(light ? 26 : 18) * op);
        graphics.flush();
        Shader.capsule(graphics.pose().last().pose(), gridX + 2.0f, divY, gridX + gridW - 2.0f, divY, 0.5f, divAlpha << 24 | rimRgb);
        InventoryrenderModule.highlight(graphics, mc, gridX, gridY, size, step, light, op, invH, hotY);
    }
}

