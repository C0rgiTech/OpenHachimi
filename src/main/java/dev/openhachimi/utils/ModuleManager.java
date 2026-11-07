/*
 * Decompiled with CFR 0.152.
 */
package dev.openhachimi.utils;

import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.modules.AimassistModule;
import dev.openhachimi.modules.AntibotModule;
import dev.openhachimi.modules.AntinickModule;
import dev.openhachimi.modules.AntistaffModule;
import dev.openhachimi.modules.ArraylistModule;
import dev.openhachimi.modules.AutoclickerModule;
import dev.openhachimi.modules.AutopearlModule;
import dev.openhachimi.modules.AutorodModule;
import dev.openhachimi.modules.AutosoupModule;
import dev.openhachimi.modules.AutosprintModule;
import dev.openhachimi.modules.AutotoolsModule;
import dev.openhachimi.modules.ChatoverlayModule;
import dev.openhachimi.modules.ChestespModule;
import dev.openhachimi.modules.CheststealerModule;
import dev.openhachimi.modules.ClickguiModule;
import dev.openhachimi.modules.CrystalauraModule;
import dev.openhachimi.modules.DisablebossbarModule;
import dev.openhachimi.modules.EagleModule;
import dev.openhachimi.modules.EspModule;
import dev.openhachimi.modules.FakelagModule;
import dev.openhachimi.modules.FastplaceModule;
import dev.openhachimi.modules.FriendModule;
import dev.openhachimi.modules.HealthbypassModule;
import dev.openhachimi.modules.InvmanagerModule;
import dev.openhachimi.modules.JumpresetModule;
import dev.openhachimi.modules.KillauraModule;
import dev.openhachimi.modules.MidpearlModule;
import dev.openhachimi.modules.Module;
import dev.openhachimi.modules.NametagsModule;
import dev.openhachimi.modules.NicknameModule;
import dev.openhachimi.modules.NofovModule;
import dev.openhachimi.modules.NojumpdelayModule;
import dev.openhachimi.modules.NoslowModule;
import dev.openhachimi.modules.NotificationModule;
import dev.openhachimi.modules.PotionHudModule;
import dev.openhachimi.modules.ProjaimModule;
import dev.openhachimi.modules.ProjectileModule;
import dev.openhachimi.modules.ScaffoldModule;
import dev.openhachimi.modules.ScoreboardhudModule;
import dev.openhachimi.modules.TargethudModule;
import dev.openhachimi.modules.TeamsModule;
import dev.openhachimi.modules.WatermarkModule;
import dev.openhachimi.render.StencilBuffer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.Input;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class ModuleManager {
    public static List<Module> modules = new CopyOnWriteArrayList<Module>();
    public static RenderLevelStageEvent lastRenderEvent;
    private static volatile boolean initialized;

    public static Module getModule(String name) {
        for (Module m : modules) {
            if (!m.name.equalsIgnoreCase(name)) continue;
            return m;
        }
        return null;
    }

    public static <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (!clazz.isInstance(m)) continue;
            return (T)((Module)clazz.cast(m));
        }
        return null;
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        ModuleManager.addEnabled(new AimassistModule());
        modules.add(new ProjaimModule());
        modules.add(new CrystalauraModule());
        ModuleManager.addEnabled(new AutoclickerModule());
        ModuleManager.addEnabled(new JumpresetModule());
        modules.add(new KillauraModule());
        ModuleManager.addEnabled(new AutotoolsModule());
        ModuleManager.addEnabled(new EagleModule());
        ModuleManager.addEnabled(new FastplaceModule());
        modules.add(new CheststealerModule());
        modules.add(new InvmanagerModule());
        modules.add(new ScaffoldModule());
        modules.add(new AutosprintModule());
        modules.add(new NoslowModule());
        modules.add(new NojumpdelayModule());
        modules.add(new AntibotModule());
        modules.add(new TeamsModule());
        modules.add(new MidpearlModule());
        modules.add(new AutorodModule());
        modules.add(new AutosoupModule());
        ModuleManager.addEnabled(new AutopearlModule());
        modules.add(new HealthbypassModule());
        modules.add(new AntinickModule());
        modules.add(new FakelagModule());
        modules.add(new FriendModule());
        modules.add(new AntistaffModule());
        modules.add(new NotificationModule());
        modules.add(new WatermarkModule());
        modules.add(new EspModule());
        modules.add(new NametagsModule());
        modules.add(new NicknameModule());
        modules.add(new DisablebossbarModule());
        modules.add(new NofovModule());
        ModuleManager.addEnabled(new ScoreboardhudModule());
        modules.add(new ProjectileModule());
        modules.add(new ArraylistModule());
        modules.add(new ChatoverlayModule());
        modules.add(new ChestespModule());
        modules.add(new PotionHudModule());
        modules.add(new TargethudModule());
        modules.add(new ClickguiModule());
        for (Module module : modules) {
            if (module.values.contains(module.invisible)) continue;
            module.values.add(module.invisible);
        }
    }

    public static Object transformOutgoingPacket(Object packet) {
        Object transformed = StencilBuffer.transformOutgoing(packet);
        if (transformed == null) {
            return null;
        }
        if (!(transformed instanceof Packet)) {
            return transformed;
        }
        Packet p = (Packet)transformed;
        for (Module m : modules) {
            if (!m.enabled || !m.onOutgoingPacket(p)) continue;
            return null;
        }
        return transformed;
    }

    public static void addUnenablable(Module config) {
        config.canBeEnabled = false;
        config.enabled = false;
        modules.add(config);
    }

    private static void safeInvoke(Module module, Runnable action) {
        try {
            action.run();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void addEnabled(Module config) {
        modules.add(config);
        if (!config.enabled) {
            config.toggle();
        }
    }

    public static boolean onScreenRenderPre(ScreenEvent.Render.Pre event) {
        for (Module m : modules) {
            if (!(m instanceof ChatoverlayModule)) continue;
            ChatoverlayModule overlay = (ChatoverlayModule)m;
            if (!overlay.enabled || !overlay.suppressesVanillaScreen() || !(event.getScreen() instanceof ChatScreen)) continue;
            return true;
        }
        return false;
    }

    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!(event.getScreen() instanceof ChatScreen)) {
            return;
        }
        for (Module m : modules) {
            if (!(m instanceof ChatoverlayModule)) continue;
            ChatoverlayModule overlay = (ChatoverlayModule)m;
            if (!overlay.enabled) continue;
            ModuleManager.safeInvoke(m, () -> overlay.onScroll(event.getScrollDelta()));
        }
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        for (Module m : modules) {
            if (!m.enabled) continue;
            ModuleManager.safeInvoke(m, () -> m.onPlayerTick(event));
        }
    }

    public static void onTick() {
        for (Module m : modules) {
            if (!m.enabled) continue;
            ModuleManager.safeInvoke(m, () -> m.onTick());
        }
        StencilBuffer.onPostTick();
    }

    public static void onPreTick() {
        StencilBuffer.beginTick();
        NoslowModule noSlow = ModuleManager.getModule(NoslowModule.class);
        if (noSlow != null && noSlow.enabled) {
            ModuleManager.safeInvoke(noSlow, () -> noSlow.onPreTick());
        }
        for (Module m : modules) {
            if (!m.enabled || m instanceof NoslowModule) continue;
            ModuleManager.safeInvoke(m, () -> m.onPreTick());
        }
        StencilBuffer.afterModules();
    }

    public static boolean onIncomingPacket(Packet<?> packet) {
        StencilBuffer.onIncomingPacket(packet);
        for (Module m : modules) {
            if (!m.enabled || !m.onIncomingPacket(packet)) continue;
            return true;
        }
        return false;
    }

    public static void onWorldChange() {
        for (Module m : modules) {
            if (!m.enabled) continue;
            m.onWorldChange();
        }
    }

    public static void onRender3D() {
        for (Module m : modules) {
            if (!m.enabled) continue;
            ModuleManager.safeInvoke(m, () -> m.onRender3D());
        }
    }

    public static boolean onOutgoingPacket(Packet<?> packet) {
        Object result = ModuleManager.transformOutgoingPacket(packet);
        return result == null;
    }

    public static void onSlowdown(EventRegistrar.SlowdownEvent event) {
        for (Module m : modules) {
            if (!m.enabled) continue;
            ModuleManager.safeInvoke(m, () -> m.onSlowdown(event));
        }
    }

    public static void onRender2D(GuiGraphics g, float partialTick) {
        FrameHelper.begin();
        for (Module m : modules) {
            if (!m.enabled && !m.shouldRender2DWhenDisabled()) continue;
            ModuleManager.safeInvoke(m, () -> m.onRender2D(g, partialTick));
        }
        Minecraft mc = Minecraft.getInstance();
        for (Module m : modules) {
            if (!(m instanceof ChatoverlayModule)) continue;
            ChatoverlayModule overlay = (ChatoverlayModule)m;
            if (!overlay.enabled) continue;
            ModuleManager.safeInvoke(m, () -> overlay.render(mc, g));
        }
        FrameHelper.commitForFrame();
    }

    public static void onUseItem(EventRegistrar.UseItemEvent event) {
        NoslowModule noSlow = ModuleManager.getModule(NoslowModule.class);
        if (noSlow != null && noSlow.enabled) {
            noSlow.onUseItem(event);
        }
        for (Module m : modules) {
            if (!m.enabled || m instanceof NoslowModule) continue;
            m.onUseItem(event);
        }
    }

    public static void onMovementInput(Input input) {
        for (Module m : modules) {
            if (!m.enabled) continue;
            m.onMovementInput(input);
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        for (Module m : modules) {
            if (!m.enabled) continue;
            m.onAttackEntity(event);
        }
    }

    public static void onAttackSlowdown(EventRegistrar.AttackSlowdownEvent event) {
        for (Module m : modules) {
            if (!m.enabled) continue;
            ModuleManager.safeInvoke(m, () -> m.onAttackSlowdown(event));
        }
    }
}

