/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.ModeSetting;
import dev.openhachimi.utils.ModuleManager;

import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class AutorodModule
extends Module {
    private static final byte[] S_3CB26978 = new byte[]{3, 55, 54, 45, 16, 45, 38};  // XOR0x42 "AutoRod"
    private static final byte[] S_77471352 = new byte[]{0, 55, 54, 54, 45, 44};  // XOR0x42 "Button"
    private static final byte[] S_95BC5B5 = new byte[]{15, 43, 38, 38, 46, 39};  // XOR0x42 "Middle"
    private static final byte[] S_2D504359 = new byte[]{15, 45, 55, 49, 39, 98, 118};  // XOR0x42 "Mouse 4"
    private static final byte[] S_2D50435A = new byte[]{15, 45, 55, 49, 39, 98, 119};  // XOR0x42 "Mouse 5"
    private static final byte[] S_3EDC963 = new byte[]{6, 39, 46, 35, 59};  // XOR0x42 "Delay"
    private static final byte[] S_62675549 = new byte[]{39, 44, 49, 55, 48, 39, 10, 35, 49, 17, 39, 44, 54, 1, 35, 48, 48, 43, 39, 38, 11, 54, 39, 47};  // XOR0x42 "ensureHasSentCarriedItem"
    private static final byte[] S_22AFBD34 = new byte[]{47, 29, 115, 114, 119, 113, 116, 112, 29};  // XOR0x42 "m_105362_"
    private static final byte[] S_D92 = new byte[]{47, 29};  // XOR0x42 "m_"
    public static AutorodModule INSTANCE;
    private final ModeSetting mouseButton = new ModeSetting("Button", new String[]{"Middle", "Mouse 4", "Mouse 5"}, "Mouse 4");
    private final ListSetting delay = new ListSetting("Delay", 2, 0, 20);
    private boolean slotSwitched;
    private boolean isActive;
    private int previousSlot = -1;
    private int tickDelay;
    private static Method ensureHasSentCarriedItemMethod;
    private static boolean ensureHasSentCarriedItemResolved;

    public AutorodModule() {
        super("AutoRod", Category.MISC);
        INSTANCE = this;
        this.values.add(this.mouseButton);
        this.values.add(this.delay);
    }

    private void resetState() {
        this.slotSwitched = false;
        this.isActive = false;
        this.previousSlot = -1;
        this.tickDelay = 0;
    }

    private void useItem(Minecraft mc) {
        if (mc.gameMode == null || mc.player == null || !this.isUsableItem(mc.player.getMainHandItem().getItem())) {
            return;
        }
        mc.gameMode.useItem((Player)mc.player, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private void restoreSlot() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && this.previousSlot != -1) {
            mc.player.getInventory().selected = this.previousSlot;
            this.sendCarriedItem(mc);
            this.previousSlot = -1;
        }
    }

    private void sendCarriedItem(Minecraft mc) {
        MultiPlayerGameMode gameMode = mc.gameMode;
        if (gameMode == null) {
            return;
        }
        Method method = AutorodModule.resolveEnsureHasSentCarriedItem(gameMode);
        if (method == null) {
            return;
        }
        try {
            method.invoke((Object)gameMode, new Object[0]);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private int findUsableItemSlot(Minecraft mc) {
        int throwableSlot = this.findThrowableSlot(mc);
        if (throwableSlot != -1) {
            return throwableSlot;
        }
        return this.findItemInHotbar(mc, Items.FISHING_ROD);
    }

    private boolean isMouseButtonDown(Minecraft mc) {
        return GLFW.glfwGetMouseButton((long)mc.getWindow().getWindow(), (int)this.getMouseButtonCode()) == 1;
    }

    private boolean isMidPearlBlocking() {
        MidpearlModule midPearl = ModuleManager.getModule(MidpearlModule.class);
        return midPearl != null && midPearl.enabled && midPearl.isInterceptButton(this.getMouseButtonCode());
    }

    private static Method findNoArgMethod(Class<?> clazz, String name) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                Method method = current.getDeclaredMethod(name, new Class[0]);
                method.setAccessible(true);
                return method;
            }
            catch (NoSuchMethodException noSuchMethodException) {
                continue;
            }
        }
        return null;
    }

    private boolean isThrowable(Item item) {
        return item == Items.EGG || item == Items.SNOWBALL;
    }

    private int findThrowableSlot(Minecraft mc) {
        if (mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (!this.isThrowable(mc.player.getInventory().getItem(i).getItem())) continue;
            return i;
        }
        return -1;
    }

    private int getMouseButtonCode() {
        String mode = (String)this.mouseButton.get();
        if ("Mouse 5".equals(mode)) {
            return 4;
        }
        if ("Middle".equals(mode)) {
            return 2;
        }
        return 3;
    }

    private int findItemInHotbar(Minecraft mc, Item item) {
        if (mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (mc.player.getInventory().getItem(i).getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private boolean isUsableItem(Item item) {
        return item == Items.FISHING_ROD || this.isThrowable(item);
    }

    public boolean isActiveOrPending() {
        return this.slotSwitched || this.tickDelay > 0;
    }

    private void selectHotbarSlot(Minecraft mc, int slot) {
        if (mc.player == null) {
            return;
        }
        mc.player.getInventory().selected = slot;
        this.sendCarriedItem(mc);
    }

    @Override
    public void onDisable() {
        this.restoreSlot();
        this.resetState();
    }

    @Override
    public void onEnable() {
        this.resetState();
    }

    private static Method resolveEnsureHasSentCarriedItem(MultiPlayerGameMode gameMode) {
        if (ensureHasSentCarriedItemResolved) {
            return ensureHasSentCarriedItemMethod;
        }
        ensureHasSentCarriedItemResolved = true;
        Class<?> clazz = gameMode.getClass();
        Method found = AutorodModule.findNoArgMethod(clazz, "ensureHasSentCarriedItem");
        if (found == null) {
            found = AutorodModule.findNoArgMethod(clazz, "m_105362_");
        }
        if (found == null) {
            found = AutorodModule.findNoArgVoidPrivate(clazz);
        }
        ensureHasSentCarriedItemMethod = found;
        return found;
    }

    public boolean isCurrentMouseButton(int button) {
        return this.getMouseButtonCode() == button;
    }

    private static Method findNoArgVoidPrivate(Class<?> clazz) {
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            Method[] methods = current.getDeclaredMethods();
            for (int i = 0; i < methods.length; ++i) {
                Method method = methods[i];
                if (method.getParameterCount() != 0 || method.getReturnType() != Void.TYPE || method.isSynthetic() || !method.getName().startsWith("m_")) continue;
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    public boolean shouldInterceptButton(int button) {
        return this.isCurrentMouseButton(button) && (this.isActiveOrPending() || this.findUsableItemSlot(Minecraft.getInstance()) != -1);
    }

    @Override
    public void onPreTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            this.resetState();
            return;
        }
        if (mc.screen != null) {
            this.isActive = false;
            return;
        }
        if (this.tickDelay > 0) {
            --this.tickDelay;
            if (this.tickDelay == 0) {
                this.restoreSlot();
                this.slotSwitched = false;
            }
            return;
        }
        boolean mouseDown = this.isMouseButtonDown(mc);
        if (!mouseDown) {
            this.isActive = false;
            return;
        }
        if (this.isActive || this.isMidPearlBlocking()) {
            return;
        }
        int slot = this.findUsableItemSlot(mc);
        if (slot == -1) {
            return;
        }
        this.isActive = true;
        this.previousSlot = mc.player.getInventory().selected;
        boolean isFishingRod = mc.player.getInventory().getItem(slot).getItem() == Items.FISHING_ROD;
        this.slotSwitched = true;
        this.selectHotbarSlot(mc, slot);
        this.useItem(mc);
        if (isFishingRod) {
            this.tickDelay = (Integer)this.delay.get();
        }
        if (!isFishingRod || this.tickDelay <= 0) {
            this.restoreSlot();
            this.slotSwitched = false;
        }
    }
}

