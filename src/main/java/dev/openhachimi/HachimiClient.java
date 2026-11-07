package dev.openhachimi;

import dev.openhachimi.event.EventRegistrar;
import dev.openhachimi.config.ConfigLoadManager;
import dev.openhachimi.modules.ModuleManager;
import dev.openhachimi.utils.render.font.FontManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(HachimiClient.MOD_ID)
@SuppressWarnings("all")
public final class HachimiClient {

    public static final String MOD_ID = "hachimi";
    public static final String NAME = "Hachimi";
    public static final String VERSION = "1.20.1";

    private static HachimiClient instance;

    public HachimiClient() {
        instance = this;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::clientSetup);
    }

    public static HachimiClient get() {
        return instance;
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            dev.openhachimi.config.FontManager.init();
            FontManager.init();
            try {
                ModuleManager.init();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                ConfigLoadManager.init();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            EventRegistrar.register();
        });
    }
}
