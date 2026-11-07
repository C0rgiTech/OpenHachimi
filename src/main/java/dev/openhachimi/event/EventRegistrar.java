/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.event;

import dev.openhachimi.command.CommandManager;
import dev.openhachimi.modules.ChatoverlayModule;
import dev.openhachimi.modules.DisablebossbarModule;
import dev.openhachimi.modules.Module;
import dev.openhachimi.modules.NametagsModule;
import dev.openhachimi.modules.NicknameModule;
import dev.openhachimi.modules.NofovModule;
import dev.openhachimi.modules.PotionHudModule;
import dev.openhachimi.modules.ScoreboardhudModule;
import dev.openhachimi.render.StencilBuffer;
import dev.openhachimi.utils.ModuleManager;
import dev.openhachimi.config.ConfigLoadManager;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;

public class EventRegistrar {
    private static final byte[] S_79E18972 = new byte[]{54, 43, 33, 41, 1, 45, 55, 44, 54};  // XOR0x42 "tickCount"
    private static final byte[] S_8CDAC1B = new byte[]{42, 35, 49, 42, 1, 45, 38, 39};  // XOR0x42 "hashCode"
    private static final byte[] S_474A45C1 = new byte[]{35, 50, 50, 39, 44, 38, 39, 48, 29, 46, 39, 37, 35, 33, 59};  // XOR0x42 "appender_legacy"
    private static final byte[] S_2BCE4293 = new byte[]{50, 35, 33, 41, 39, 54, 29, 42, 35, 44, 38, 46, 39, 48};  // XOR0x42 "packet_handler"
    private static final byte[] S_54155015 = new byte[]{108, 32, 43, 44, 38, 98};  // XOR0x42 ".bind "
    private static final byte[] S_20 = new byte[]{98};
    private boolean nettyInjected = false;
    public static boolean lastSprint = false;
    private Vec3 savedVelocity = null;
    private boolean restoreVelocity = false;
    public static boolean isSkipping = false;
    private static double skipX;
    private static double skipY;
    private static double skipZ;
    private static double skipMX;
    private static double skipMY;
    private static double skipMZ;
    public static boolean bypassPacketEvent;

    static {
        bypassPacketEvent = false;
    }

    public static void register() {
        EventRegistrar handler = new EventRegistrar();
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, ComputeFovModifierEvent.class, (Consumer)new Consumer<ComputeFovModifierEvent>(){

            @Override
            public void accept(ComputeFovModifierEvent event) {
                NofovModule.apply(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TickEvent.ClientTickEvent.class, (Consumer)new Consumer<TickEvent.ClientTickEvent>(){

            @Override
            public void accept(TickEvent.ClientTickEvent event) {
                handler.onTick(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TickEvent.PlayerTickEvent.class, (Consumer)new Consumer<TickEvent.PlayerTickEvent>(){

            @Override
            public void accept(TickEvent.PlayerTickEvent event) {
                handler.onPlayerTick(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, InputEvent.Key.class, (Consumer)new Consumer<InputEvent.Key>(){

            @Override
            public void accept(InputEvent.Key event) {
                handler.onKey(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, MovementInputUpdateEvent.class, (Consumer)new Consumer<MovementInputUpdateEvent>(){

            @Override
            public void accept(MovementInputUpdateEvent event) {
                handler.onMovementInput(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderLevelStageEvent.class, (Consumer)new Consumer<RenderLevelStageEvent>(){

            @Override
            public void accept(RenderLevelStageEvent event) {
                handler.onRender3D(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiEvent.Post.class, (Consumer)new Consumer<RenderGuiEvent.Post>(){

            @Override
            public void accept(RenderGuiEvent.Post event) {
                handler.onRender(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ScreenEvent.Render.Pre.class, (Consumer)new Consumer<ScreenEvent.Render.Pre>(){

            @Override
            public void accept(ScreenEvent.Render.Pre event) {
                if (handler.onScreenRenderPre(event)) {
                    event.setCanceled(true);
                }
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ScreenEvent.MouseScrolled.Pre.class, (Consumer)new Consumer<ScreenEvent.MouseScrolled.Pre>(){

            @Override
            public void accept(ScreenEvent.MouseScrolled.Pre event) {
                handler.onMouseScrolled(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderGuiOverlayEvent.Pre.class, (Consumer)new Consumer<RenderGuiOverlayEvent.Pre>(){

            @Override
            public void accept(RenderGuiOverlayEvent.Pre event) {
                handler.onRenderOverlay(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderNameTagEvent.class, (Consumer)new Consumer<RenderNameTagEvent>(){

            @Override
            public void accept(RenderNameTagEvent event) {
                handler.onRenderNameTag(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ClientChatEvent.class, (Consumer)new Consumer<ClientChatEvent>(){

            @Override
            public void accept(ClientChatEvent event) {
                handler.onChat(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ClientChatReceivedEvent.class, (Consumer)new Consumer<ClientChatReceivedEvent>(){

            @Override
            public void accept(ClientChatReceivedEvent event) {
                handler.onChatReceived(event);
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ItemTooltipEvent.class, (Consumer)new Consumer<ItemTooltipEvent>(){

            @Override
            public void accept(ItemTooltipEvent event) {
                NicknameModule.rewriteTooltip(event.getToolTip());
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, EntityJoinLevelEvent.class, (Consumer)new Consumer<EntityJoinLevelEvent>(){

            @Override
            public void accept(EntityJoinLevelEvent event) {
                if (event.getEntity() == Minecraft.getInstance().player) {
                    ModuleManager.onWorldChange();
                }
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ClientPlayerNetworkEvent.LoggingOut.class, (Consumer)new Consumer<ClientPlayerNetworkEvent.LoggingOut>(){

            @Override
            public void accept(ClientPlayerNetworkEvent.LoggingOut event) {
                ModuleManager.onWorldChange();
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, AttackEntityEvent.class, (Consumer)new Consumer<AttackEntityEvent>(){

            @Override
            public void accept(AttackEntityEvent event) {
                handler.onAttackEntity(event);
            }
        });
    }

    private void ensureNettyInjected() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || mc.getConnection().getConnection() == null) {
            this.nettyInjected = false;
            return;
        }
        if (this.nettyInjected) {
            return;
        }
        try {
            ChannelPipeline pipeline = mc.getConnection().getConnection().channel().pipeline();
            String handlerName = "appender_legacy";
            if (pipeline.get(handlerName) == null) {
                pipeline.addBefore("packet_handler", handlerName, (ChannelHandler)new ChannelDuplexHandler(){

                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        if (msg instanceof Packet) {
                            if (bypassPacketEvent) {
                                super.write(ctx, msg, promise);
                                return;
                            }
                            if (isSkipping && msg instanceof ServerboundMovePlayerPacket) {
                                return;
                            }
                            Object out = ModuleManager.transformOutgoingPacket(msg);
                            if (out == null) {
                                return;
                            }
                            super.write(ctx, out, promise);
                            return;
                        }
                        super.write(ctx, msg, promise);
                    }

                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof Packet && ModuleManager.onIncomingPacket((Packet)msg)) {
                            return;
                        }
                        super.channelRead(ctx, msg);
                    }
                });
            }
            this.nettyInjected = true;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public boolean onScreenRenderPre(ScreenEvent.Render.Pre event) {
        return ModuleManager.onScreenRenderPre(event);
    }

    public void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        ModuleManager.onMouseScrolled(event);
    }

    public void onChat(ClientChatEvent event) {
        String msg = event.getMessage();
        if (msg == null) {
            return;
        }
        if (CommandManager.matches(msg)) {
            event.setCanceled(true);
            CommandManager.execute(msg);
            return;
        }
        if (msg.startsWith(".bind ")) {
            event.setCanceled(true);
            String[] args = msg.split(" ");
            if (args.length >= 3) {
                String moduleName = args[1];
                String keyStr = args[2];
                Module targetModule = ModuleManager.getModule(moduleName);
                if (targetModule != null) {
                    int keyCode = 0;
                    if (keyStr.length() == 1) {
                        keyCode = Character.toUpperCase(keyStr.charAt(0));
                    } else {
                        try {
                            keyCode = Integer.parseInt(keyStr);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    targetModule.key = keyCode;
                    ConfigLoadManager.getInstance().saveConfig();
                }
            }
        }
    }

    public void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals((Object)VanillaGuiOverlay.CHAT_PANEL.id())) {
            ChatoverlayModule chat = ModuleManager.getModule(ChatoverlayModule.class);
            if (chat != null && chat.enabled) {
                event.setCanceled(true);
            }
            return;
        }
        if (event.getOverlay().id().equals((Object)VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id())) {
            if (DisablebossbarModule.active()) {
                event.setCanceled(true);
            }
            return;
        }
        if (event.getOverlay().id().equals((Object)VanillaGuiOverlay.POTION_ICONS.id())) {
            PotionHudModule potionDisplay = ModuleManager.getModule(PotionHudModule.class);
            if (potionDisplay != null && potionDisplay.enabled) {
                event.setCanceled(true);
            }
            return;
        }
        NicknameModule nickname = ModuleManager.getModule(NicknameModule.class);
        if (event.getOverlay().id().equals((Object)VanillaGuiOverlay.SCOREBOARD.id())) {
            ScoreboardhudModule scoreboard = ModuleManager.getModule(ScoreboardhudModule.class);
            if (scoreboard != null && scoreboard.enabled) {
                event.setCanceled(true);
            }
            return;
        }
        if (nickname == null || !nickname.enabled) {
            return;
        }
        if (event.getOverlay().id().equals((Object)VanillaGuiOverlay.ITEM_NAME.id()) && nickname.renderItemName(event.getGuiGraphics())) {
            event.setCanceled(true);
        }
    }

    public void onRenderNameTag(RenderNameTagEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() == mc.player && NicknameModule.active()) {
            event.setContent(NicknameModule.rewrite(event.getOriginalContent()));
        }
        if (NametagsModule.isRenderingNameTag(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!NicknameModule.active()) {
            return;
        }
        Component message = event.getMessage();
        Component replaced = NicknameModule.rewrite(message);
        if (replaced != message) {
            event.setMessage(replaced);
        }
    }

    public void onKey(InputEvent.Key event) {
        if (event.getAction() == 1 && Minecraft.getInstance().screen == null) {
            boolean changed = false;
            for (Module m : ModuleManager.modules) {
                if (m.key == 0 || m.key != event.getKey()) continue;
                m.toggle();
                changed = true;
            }
            for (Module m : ModuleManager.modules) {
                if (!m.enabled) continue;
                try {
                    m.onKeyPressed(event.getKey());
                }
                catch (Throwable throwable) {}
            }
            if (changed) {
                try {
                    ConfigLoadManager.getInstance().saveConfig();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
        }
    }

    public void onRender(RenderGuiEvent.Post event) {
        ModuleManager.onRender2D(event.getGuiGraphics(), event.getPartialTick());
    }

    public static void sendWithoutEvent(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.getConnection().getConnection() == null) {
            return;
        }
        bypassPacketEvent = true;
        mc.getConnection().getConnection().send(packet);
        bypassPacketEvent = false;
    }

    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.player != mc.player) {
            return;
        }
        if (Module.skipTicks > 0 || isSkipping) {
            if (event.phase == TickEvent.Phase.START) {
                if (Module.skipTicks > 0) {
                    --Module.skipTicks;
                    isSkipping = true;
                    skipX = mc.player.getX();
                    skipY = mc.player.getY();
                    skipZ = mc.player.getZ();
                    skipMX = mc.player.getDeltaMovement().x;
                    skipMY = mc.player.getDeltaMovement().y;
                    skipMZ = mc.player.getDeltaMovement().z;
                    try {
                        Field tickCountField = null;
                        for (Field f : Entity.class.getDeclaredFields()) {
                            if (f.getType() != Integer.TYPE || !f.getName().equals("tickCount") && !f.getName().equals("hashCode")) continue;
                            tickCountField = f;
                            break;
                        }
                        if (tickCountField != null) {
                            tickCountField.setAccessible(true);
                            int current = tickCountField.getInt(mc.player);
                            tickCountField.setInt(mc.player, current - 1);
                        }
                    }
                    catch (Exception exception) {}
                }
            } else if (event.phase == TickEvent.Phase.END && isSkipping) {
                mc.player.setPos(skipX, skipY, skipZ);
                mc.player.xo = skipX;
                mc.player.yo = skipY;
                mc.player.zo = skipZ;
                mc.player.setDeltaMovement(skipMX, skipMY, skipMZ);
                isSkipping = false;
            }
            return;
        }
        if (event.phase == TickEvent.Phase.START) {
            if (this.restoreVelocity && this.savedVelocity != null) {
                mc.player.setSprinting(lastSprint);
                mc.player.setDeltaMovement(this.savedVelocity);
                this.restoreVelocity = false;
            }
            StencilBuffer.onPlayerTickStart();
            ModuleManager.onPlayerTick(event);
            return;
        }
        if (event.phase == TickEvent.Phase.END) {
            lastSprint = mc.player.isSprinting();
            StencilBuffer.onPlayerTickEnd();
            ModuleManager.onPlayerTick(event);
        }
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        this.ensureNettyInjected();
        if (event.phase == TickEvent.Phase.START) {
            ModuleManager.onPreTick();
            return;
        }
        if (event.phase == TickEvent.Phase.END) {
            ModuleManager.onTick();
        }
    }

    public void onRender3D(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            ModuleManager.lastRenderEvent = event;
            ModuleManager.onRender3D();
        }
    }

    public void onMovementInput(MovementInputUpdateEvent event) {
        StencilBuffer.fixInput(event.getInput());
        ModuleManager.onMovementInput(event.getInput());
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isUsingItem() && !mc.player.isPassenger()) {
            SlowdownEvent slowdownEvent = new SlowdownEvent(true);
            ModuleManager.onSlowdown(slowdownEvent);
            if (!slowdownEvent.isUsingItem()) {
                event.getInput().forwardImpulse *= 5.0f;
                event.getInput().leftImpulse *= 5.0f;
            }
        }
    }

    public void onAttackEntity(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() == mc.player) {
            ModuleManager.onAttackEntity(event);
            AttackSlowdownEvent slowdownEvent = new AttackSlowdownEvent();
            ModuleManager.onAttackSlowdown(slowdownEvent);
            if (slowdownEvent.isCanceled()) {
                lastSprint = mc.player.isSprinting();
                this.savedVelocity = mc.player.getDeltaMovement();
                this.restoreVelocity = true;
            }
        }
    }

    @Cancelable
    public static class AttackSlowdownEvent
    extends Event {
    }

    @Cancelable
    public static class SlowdownEvent
    extends Event {
        private boolean usingItem;

        public SlowdownEvent(boolean usingItem) {
            this.usingItem = usingItem;
        }

        public boolean isUsingItem() {
            return this.usingItem;
        }

        public void setUsingItem(boolean usingItem) {
            this.usingItem = usingItem;
        }
    }

    @Cancelable
    public static class UseItemEvent
    extends Event {
        private final InteractionHand hand;
        private final ItemStack stack;

        public UseItemEvent(InteractionHand hand, ItemStack stack) {
            this.hand = hand;
            this.stack = stack;
        }

        public ItemStack getStack() {
            return this.stack;
        }

        public InteractionHand getHand() {
            return this.hand;
        }
    }
}

