/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.utils.rotation.RotationManager;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.settings.ListSetting;
import dev.openhachimi.settings.NumberSetting;
import dev.openhachimi.utils.rotation.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutosoupModule
extends Module {
    private static final byte[] S_599B3C66 = new byte[]{3, 55, 54, 45, 17, 45, 55, 50};  // XOR0x42 "AutoSoup"
    private static final byte[] S_59FA27B = new byte[]{7, 35, 54, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Eat Delay"
    private static final byte[] S_7A2EB197 = new byte[]{17, 53, 43, 54, 33, 42, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Switch Delay"
    private static final byte[] S_616BCA93 = new byte[]{16, 39, 54, 55, 48, 44, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Return Delay"
    private static final byte[] S_99EE7C = new byte[]{10, 39, 35, 46, 54, 42};  // XOR0x42 "Health"
    private static final byte[] S_24A368A3 = new byte[]{16, 39, 49, 54, 45, 33, 41};  // XOR0x42 "Restock"
    private static final byte[] S_4F95FFA6 = new byte[]{16, 39, 49, 54, 45, 33, 41, 98, 6, 39, 46, 35, 59};  // XOR0x42 "Restock Delay"
    private static final byte[] S_234F7E88 = new byte[]{3, 46, 46, 45, 53, 98, 15, 45, 52, 39};  // XOR0x42 "Allow Move"
    private static final byte[] S_5ED71EB5 = new byte[]{3, 46, 46, 45, 53, 98, 16, 45, 54, 35, 54, 43, 45, 44};  // XOR0x42 "Allow Rotation"
    public static boolean SPRINT_LOCK = false;
    public static boolean hasTargetRotation;
    public static float targetYaw;
    public static float targetPitch;
    private static final int MAIN_SLOT_START = 9;
    private static final int MAIN_SLOT_END = 35;
    private static final int PLAYER_INVENTORY_CONTAINER = 0;
    private final NumberSetting eatDelay = new NumberSetting("Eat Delay", 0.0f, 0.0f, 10.0f);
    private final NumberSetting switchDelay = new NumberSetting("Switch Delay", 0.0f, 0.0f, 10.0f);
    private final NumberSetting returnDelay = new NumberSetting("Return Delay", 0.0f, 0.0f, 10.0f);
    private final NumberSetting health = new NumberSetting("Health", 14.0f, 1.0f, 20.0f);
    private final BooleanSetting restock = new BooleanSetting("Restock", true);
    private final ListSetting restockStartDelay = new ListSetting("Restock Delay", 150, 0, 3000);
    private final BooleanSetting restockMove = new BooleanSetting("Allow Move", false);
    private final BooleanSetting allowRotation = new BooleanSetting("Allow Rotation", true);
    private boolean eating;
    private int currentDelay;
    private int previousSlot;
    private boolean needReturn;
    private float healthBefore;
    private boolean restocking;
    private boolean restockFinishing;
    private int restockDelay;
    private long restockWait;
    private boolean rotationHeld;

    public AutosoupModule() {
        super("AutoSoup", Category.MISC);
        Supplier<Boolean> restockVisible = new Supplier<Boolean>(){

            @Override
            public Boolean get() {
                return (Boolean)AutosoupModule.this.restock.get();
            }
        };
        this.restockStartDelay.setVisible(restockVisible);
        this.restockMove.setVisible(restockVisible);
        this.allowRotation.setVisible(restockVisible);
        this.values.add(this.eatDelay);
        this.values.add(this.switchDelay);
        this.values.add(this.returnDelay);
        this.values.add(this.health);
        this.values.add(this.restock);
        this.values.add(this.restockStartDelay);
        this.values.add(this.restockMove);
        this.values.add(this.allowRotation);
    }

    private void dropRotationHold() {
        this.rotationHeld = false;
        hasTargetRotation = false;
        RotationManager.clear();
    }

    private void beginRestockFinish() {
        if (this.restockFinishing) {
            return;
        }
        this.restocking = false;
        this.restockDelay = 0;
        int delay = (Integer)this.restockStartDelay.get();
        if (delay <= 0) {
            return;
        }
        this.restockFinishing = true;
        this.restockWait = System.currentTimeMillis() + (long)delay;
    }

    private boolean playerMoving(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) {
            return false;
        }
        Input input = player.input;
        if (input != null && (input.forwardImpulse != 0.0f || input.leftImpulse != 0.0f)) {
            return true;
        }
        if (input != null && (input.jumping || input.shiftKeyDown)) {
            return true;
        }
        return player.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    private boolean restockActive() {
        return this.restocking || this.restockFinishing || this.restockDelay > 0;
    }

    private void tickRestock(Minecraft mc, LocalPlayer player) {
        if (mc.gameMode == null || !player.isAlive()) {
            this.abortRestock();
            return;
        }
        if (this.restockFinishing) {
            if (System.currentTimeMillis() >= this.restockWait) {
                this.restockFinishing = false;
            }
            return;
        }
        if (mc.screen != null) {
            return;
        }
        if (player.containerMenu == null || player.containerMenu.containerId != 0) {
            if (this.restocking || this.restockDelay > 0) {
                this.beginRestockFinish();
            }
            return;
        }
        if (this.restockDelay > 0) {
            --this.restockDelay;
            return;
        }
        if (!this.restocking) {
            if (this.findHotbarMushroomStew(player) != -1) {
                return;
            }
            if (this.findEmptyHotbarSlot(player) == -1) {
                return;
            }
            if (this.collectSoupSlots(player).isEmpty()) {
                return;
            }
            this.restocking = true;
            this.restockWait = System.currentTimeMillis() + (long)((Integer)this.restockStartDelay.get()).intValue();
            this.restockDelay = 0;
            return;
        }
        if (System.currentTimeMillis() < this.restockWait) {
            return;
        }
        List<Integer> candidates = this.collectSoupSlots(player);
        if (this.findEmptyHotbarSlot(player) == -1 || candidates.isEmpty()) {
            this.beginRestockFinish();
            return;
        }
        int slot = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        mc.gameMode.handleInventoryMouseClick(0, slot, 0, ClickType.QUICK_MOVE, (Player)player);
        this.restockDelay = 1;
    }

    private void beginRotationHold(LocalPlayer player) {
        if (this.rotationHeld) {
            return;
        }
        Rotation last = RotationManager.getLastRotation();
        targetYaw = last.getYaw();
        targetPitch = last.getPitch();
        hasTargetRotation = true;
        this.rotationHeld = true;
    }

    private void abortRestock() {
        this.restocking = false;
        this.restockFinishing = false;
        this.restockDelay = 0;
    }

    private List<Integer> collectSoupSlots(LocalPlayer player) {
        ArrayList<Integer> slots = new ArrayList<Integer>();
        for (int slot = 9; slot <= 35; ++slot) {
            ItemStack stack = player.containerMenu.getSlot(slot).getItem();
            if (stack.getItem() != Items.MUSHROOM_STEW) continue;
            slots.add(slot);
        }
        return slots;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && this.eating) {
            mc.options.keyUse.setDown(false);
        }
        this.eating = false;
        this.currentDelay = 0;
        this.needReturn = false;
        this.restocking = false;
        this.restockFinishing = false;
        this.restockDelay = 0;
        SPRINT_LOCK = false;
        this.dropRotationHold();
    }

    @Override
    public void onEnable() {
        this.eating = false;
        this.currentDelay = 0;
        this.needReturn = false;
        this.previousSlot = 0;
        this.restocking = false;
        this.restockFinishing = false;
        this.restockDelay = 0;
        SPRINT_LOCK = false;
        this.dropRotationHold();
    }

    public static boolean ownsInteractionKeys() {
        AutosoupModule self = ModuleManager.getModule(AutosoupModule.class);
        return self != null && self.enabled && (self.eating || self.rotationHeld);
    }

    private int findHotbarMushroomStew(LocalPlayer player) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() != Items.MUSHROOM_STEW) continue;
            return i;
        }
        return -1;
    }

    private int findEmptyHotbarSlot(LocalPlayer player) {
        for (int i = 0; i < 9; ++i) {
            if (!player.getInventory().getItem(i).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private void releaseRotationForMovement() {
        this.rotationHeld = false;
        hasTargetRotation = false;
        RotationManager.clear();
    }

    @Override
    public void onTick() {
        if (!SPRINT_LOCK) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.options.keySprint.setDown(false);
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @Override
    public void onPreTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            this.dropRotationHold();
            return;
        }
        if (((Boolean)this.restock.get()).booleanValue()) {
            this.tickRestock(mc, player);
        } else {
            this.restocking = false;
            this.restockFinishing = false;
            this.restockDelay = 0;
        }
        if (this.rotationHeld && (!this.restockActive() || this.playerMoving(mc))) {
            this.releaseRotationForMovement();
        }
        if (this.restockActive() && !((Boolean)this.allowRotation.get()).booleanValue() && !this.rotationHeld) {
            this.beginRotationHold(player);
        }
        if (SPRINT_LOCK) {
            player.setSprinting(false);
        }
        if (this.currentDelay > 0) {
            --this.currentDelay;
            return;
        }
        if (this.eating) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty() || stack.getItem() != Items.MUSHROOM_STEW) {
                mc.options.keyUse.setDown(false);
                this.needReturn = true;
                this.eating = false;
                this.currentDelay = (int)((Float)this.returnDelay.get()).floatValue();
            } else if (player.getHealth() > this.healthBefore) {
                mc.options.keyUse.setDown(false);
                this.needReturn = true;
                this.eating = false;
                this.currentDelay = (int)((Float)this.returnDelay.get()).floatValue();
            }
            return;
        }
        if (this.needReturn) {
            player.getInventory().selected = this.previousSlot;
            this.needReturn = false;
            this.currentDelay = (int)((Float)this.returnDelay.get()).floatValue();
            return;
        }
        if (player.getHealth() >= ((Float)this.health.get()).floatValue()) {
            return;
        }
        if (mc.screen != null || !player.isAlive()) {
            return;
        }
        int hotbarIndex = this.findHotbarMushroomStew(player);
        if (hotbarIndex == -1) {
            return;
        }
        if (player.getInventory().selected != hotbarIndex) {
            this.previousSlot = player.getInventory().selected;
            player.getInventory().selected = hotbarIndex;
            this.currentDelay = (int)((Float)this.switchDelay.get()).floatValue();
        } else {
            this.healthBefore = player.getHealth();
            mc.options.keyUse.setDown(true);
            this.eating = true;
            this.currentDelay = (int)((Float)this.eatDelay.get()).floatValue();
        }
    }

    @Override
    public void onWorldChange() {
        this.onDisable();
    }

    @Override
    public void onMovementInput(Input input) {
        boolean freezeMovement;
        if (!this.restockActive()) {
            SPRINT_LOCK = false;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        SPRINT_LOCK = freezeMovement = (Boolean)this.restockMove.get() == false;
        if (freezeMovement) {
            mc.options.keySprint.setDown(false);
            if (player != null) {
                player.setSprinting(false);
            }
        }
        input.jumping = false;
        if (freezeMovement) {
            input.forwardImpulse = 0.0f;
            input.leftImpulse = 0.0f;
            input.shiftKeyDown = false;
            return;
        }
        if (this.rotationHeld) {
            this.releaseRotationForMovement();
        }
    }
}

