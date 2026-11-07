/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.modules;

import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.settings.BooleanSetting;
import dev.openhachimi.utils.ModuleManager;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;

public class NoslowModule
extends Module {
    private static final byte[] S_14FBEB0F = new byte[]{36, 29, 115, 114, 122, 119, 122, 113, 29};  // XOR0x42 "f_108583_"
    private static final byte[] S_476BB22B = new byte[]{49, 50, 48, 43, 44, 54, 22, 48, 43, 37, 37, 39, 48, 22, 43, 47, 39};  // XOR0x42 "sprintTriggerTime"
    private static final byte[] S_4A9D021A = new byte[]{49, 50, 48, 43, 44, 54};  // XOR0x42 "sprint"
    private static final byte[] S_33600995 = new byte[]{36, 29, 115, 114, 122, 119};  // XOR0x42 "f_1085"
    private static final byte[] S_B5D8F82 = new byte[]{12, 45, 17, 46, 45, 53};  // XOR0x42 "NoSlow"
    private static final byte[] S_66AA2DAC = new byte[]{0, 45, 53, 12, 45, 17, 46, 45, 53};  // XOR0x42 "BowNoSlow"
    private static final byte[] S_6A2AA251 = new byte[]{21, 35, 54, 39, 48, 1, 42, 39, 33, 41};  // XOR0x42 "WaterCheck"
    public static final int HOLD_LIMIT_BASE = 32;
    public static final int HOLD_LIMIT_RANGED = 35;
    private final BooleanSetting bowNoSlow = new BooleanSetting("BowNoSlow", true);
    private final BooleanSetting waterCheck = new BooleanSetting("WaterCheck", true);
    private static Field sprintTriggerTimeField = NoslowModule.resolveSprintTriggerTime();
    private final Queue<Packet<?>> buffered = new ConcurrentLinkedQueue();
    private final Minecraft mc = Minecraft.getInstance();
    private boolean holding;
    private boolean buffering;
    private boolean swapPending;
    private boolean flushPending;
    private int holdCounter;
    private UseAnim activeAction = UseAnim.NONE;

    public NoslowModule() {
        super("NoSlow", Category.MOVEMENT);
        this.values.add(this.bowNoSlow);
        this.values.add(this.waterCheck);
    }

    private void reset() {
        this.buffered.clear();
        this.holding = false;
        this.buffering = false;
        this.swapPending = false;
        this.flushPending = false;
        this.holdCounter = 0;
        this.activeAction = UseAnim.NONE;
    }

    private boolean isStew(ItemStack stack) {
        return stack.is(Items.MUSHROOM_STEW) || stack.is(Items.RABBIT_STEW) || stack.is(Items.BEETROOT_SOUP) || stack.is(Items.SUSPICIOUS_STEW);
    }

    public static boolean isDesyncing() {
        NoslowModule module = ModuleManager.getModule(NoslowModule.class);
        return module != null && module.buffering;
    }

    private int limitTicks() {
        if (this.activeAction == UseAnim.BOW || this.activeAction == UseAnim.CROSSBOW || this.activeAction == UseAnim.SPEAR) {
            return 35;
        }
        return 32;
    }

    private boolean shouldApplyNoSlow(UseAnim action) {
        if (action == UseAnim.EAT || action == UseAnim.DRINK || action == UseAnim.SPEAR) {
            return true;
        }
        if (action == UseAnim.BOW || action == UseAnim.CROSSBOW) {
            return (Boolean)this.bowNoSlow.get();
        }
        return false;
    }

    private void flushNow() {
        this.buffering = false;
        this.holding = false;
        this.holdCounter = 0;
        this.activeAction = UseAnim.NONE;
        ClientPacketListener listener = this.mc.getConnection();
        while (!this.buffered.isEmpty()) {
            Packet<?> packet = this.buffered.poll();
            if (packet == null) continue;
            try {
                NoslowModule.replay(packet, listener);
            }
            catch (Throwable throwable) {}
        }
        this.restoreHands();
    }

    private boolean waterBlocked() {
        if (!((Boolean)this.waterCheck.get()).booleanValue() || this.mc.player == null) {
            return false;
        }
        return this.mc.player.isInWater() || this.mc.player.isInWaterOrBubble() || this.mc.player.isSwimming();
    }

    private boolean scaffoldActive() {
        ScaffoldModule scaffold = ModuleManager.getModule(ScaffoldModule.class);
        return scaffold != null && scaffold.enabled;
    }

    private void restoreHands() {
        if (this.swapPending) {
            this.sendSwap();
            this.swapPending = false;
        }
    }

    private boolean hasUsableBothHands() {
        ItemStack main = this.mc.player.getMainHandItem();
        ItemStack off = this.mc.player.getOffhandItem();
        if (main.isEmpty() || off.isEmpty()) {
            return false;
        }
        if (main.getItem() == off.getItem()) {
            return true;
        }
        return this.isConsumableAction(main.getUseAnimation()) && this.isConsumableAction(off.getUseAnimation());
    }

    private void sendSwap() {
        EventRegistrar.sendWithoutEvent(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
    }

    private static void replay(Packet<?> packet, ClientPacketListener listener) {
        if (listener == null) {
            return;
        }
        ((Packet)packet).handle(listener);
    }

    private boolean isLiveWhileUsing(Packet<?> packet) {
        if (packet instanceof ClientboundKeepAlivePacket || packet instanceof ClientboundLoginPacket || packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundDisconnectPacket || packet instanceof ClientboundLevelChunkWithLightPacket || packet instanceof ClientboundForgetLevelChunkPacket || packet instanceof ClientboundPlayerPositionPacket) {
            return true;
        }
        if (packet instanceof ClientboundSetEntityDataPacket) {
            ClientboundSetEntityDataPacket data = (ClientboundSetEntityDataPacket)packet;
            return data.id() == this.mc.player.getId();
        }
        if (packet instanceof ClientboundSetEntityMotionPacket) {
            ClientboundSetEntityMotionPacket motion = (ClientboundSetEntityMotionPacket)packet;
            return motion.getId() == this.mc.player.getId();
        }
        return packet instanceof ClientboundPlayerChatPacket || packet instanceof ClientboundSystemChatPacket || packet instanceof ClientboundDisguisedChatPacket || packet instanceof ClientboundSetDisplayObjectivePacket || packet instanceof ClientboundSetEquipmentPacket || packet instanceof ClientboundAddEntityPacket || packet instanceof ClientboundRemoveEntitiesPacket || packet instanceof ClientboundSetHealthPacket || packet instanceof ClientboundBossEventPacket || packet instanceof ClientboundSetObjectivePacket || packet instanceof ClientboundSetScorePacket || packet instanceof ClientboundSetTitleTextPacket || packet instanceof ClientboundSetSubtitleTextPacket || packet instanceof ClientboundSetTitlesAnimationPacket || packet instanceof ClientboundClearTitlesPacket || packet instanceof ClientboundMoveEntityPacket || packet instanceof ClientboundTeleportEntityPacket || packet instanceof ClientboundRotateHeadPacket || packet instanceof ClientboundDamageEventPacket || packet instanceof ClientboundSoundPacket || packet instanceof ClientboundSoundEntityPacket || packet instanceof ClientboundBundlePacket;
    }

    private boolean handleUseItem(ServerboundUseItemPacket packet) {
        if (this.buffering || this.waterBlocked()) {
            return false;
        }
        ItemStack stack = this.mc.player.getItemInHand(packet.getHand());
        if (CrossbowItem.isCharged((ItemStack)stack) || this.isStew(stack)) {
            return false;
        }
        UseAnim action = stack.getUseAnimation();
        if (!this.shouldApplyNoSlow(action)) {
            return false;
        }
        if (this.mc.hitResult != null && this.mc.hitResult.getType() != HitResult.Type.MISS) {
            return false;
        }
        this.holding = true;
        this.buffering = true;
        this.activeAction = action;
        this.holdCounter = 0;
        this.flushPending = false;
        this.buffered.clear();
        if (!(this.hasUsableBothHands() || action != UseAnim.EAT && action != UseAnim.DRINK)) {
            this.sendSwap();
            this.swapPending = true;
            InteractionHand opposite = packet.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            EventRegistrar.sendWithoutEvent(new ServerboundUseItemPacket(opposite, packet.getSequence()));
            return true;
        }
        return false;
    }

    private boolean isConsumableAction(UseAnim action) {
        return action == UseAnim.EAT || action == UseAnim.DRINK || action == UseAnim.BOW || action == UseAnim.CROSSBOW;
    }

    @Override
    public void onDisable() {
        this.flushNow();
        this.reset();
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    private static Field resolveSprintTriggerTime() {
        for (String candidate : new String[]{"f_108583_", "sprintTriggerTime"}) {
            try {
                Field field = LocalPlayer.class.getDeclaredField(candidate);
                field.setAccessible(true);
                return field;
            }
            catch (Throwable throwable) {
            }
        }
        for (Field field : LocalPlayer.class.getDeclaredFields()) {
            if (field.getType() != Integer.TYPE || !field.getName().contains("sprint") && !field.getName().startsWith("f_1085")) continue;
            try {
                field.setAccessible(true);
                return field;
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
        return null;
    }

    private boolean autoSoupBlocksSprint() {
        AutosoupModule soup = ModuleManager.getModule(AutosoupModule.class);
        return soup != null && soup.enabled && AutosoupModule.SPRINT_LOCK;
    }

    @Override
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player != this.mc.player || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!this.holding || !this.buffering || this.mc.player == null || this.waterBlocked()) {
            return;
        }
        if (this.autoSoupBlocksSprint() || !this.mc.player.input.hasForwardImpulse()) {
            return;
        }
        if (!this.mc.player.isSprinting()) {
            this.mc.player.setSprinting(true);
        }
        if (sprintTriggerTimeField != null) {
            try {
                sprintTriggerTimeField.setInt(this.mc.player, 0);
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }

    @Override
    public void onTick() {
    }

    @Override
    public void onPreTick() {
        if (this.mc.player == null) {
            this.reset();
            return;
        }
        if (this.waterBlocked()) {
            if (this.holding || this.buffering) {
                this.flushNow();
            }
            return;
        }
        if (this.buffering && this.scaffoldActive()) {
            this.flushNow();
            return;
        }
        if (this.buffering && this.mc.player.getMainHandItem().is(Items.MILK_BUCKET)) {
            this.flushNow();
            return;
        }
        if (this.flushPending) {
            this.flushNow();
            this.flushPending = false;
        }
        if (!this.buffering) {
            if (this.swapPending && !this.mc.player.isUsingItem()) {
                this.restoreHands();
            }
            return;
        }
        ++this.holdCounter;
        if (this.holdCounter >= this.limitTicks()) {
            this.flushPending = true;
            this.mc.options.keyUse.setDown(false);
        }
    }

    @Override
    public boolean onIncomingPacket(Packet<?> packet) {
        if (this.mc.player == null || !this.buffering || this.waterBlocked()) {
            return false;
        }
        if (this.isLiveWhileUsing(packet)) {
            return false;
        }
        this.buffered.add(packet);
        return true;
    }

    @Override
    public void onWorldChange() {
        this.flushNow();
        this.reset();
    }

    @Override
    public boolean onOutgoingPacket(Packet<?> packet) {
        if (this.mc.player == null || this.waterBlocked()) {
            return false;
        }
        if (packet instanceof ServerboundPlayerActionPacket) {
            ServerboundPlayerActionPacket action = (ServerboundPlayerActionPacket)packet;
            if (action.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM && this.holding) {
                this.flushPending = true;
            }
            return false;
        }
        if (packet instanceof ServerboundUseItemOnPacket) {
            if (this.buffering) {
                this.flushPending = true;
            }
            return false;
        }
        if (packet instanceof ServerboundUseItemPacket) {
            ServerboundUseItemPacket use = (ServerboundUseItemPacket)packet;
            return this.handleUseItem(use);
        }
        return false;
    }

    @Override
    public void onSlowdown(EventRegistrar.SlowdownEvent event) {
        if (this.mc.player == null || this.waterBlocked()) {
            return;
        }
        ItemStack active = this.mc.player.getUseItem();
        if (active.isEmpty() || this.isStew(active)) {
            return;
        }
        if (!this.shouldApplyNoSlow(active.getUseAnimation())) {
            return;
        }
        event.setUsingItem(false);
        if (!this.autoSoupBlocksSprint() && this.mc.player.input.hasForwardImpulse()) {
            this.mc.player.setSprinting(true);
        }
    }
}

