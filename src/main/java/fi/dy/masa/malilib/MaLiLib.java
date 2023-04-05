package fi.dy.masa.malilib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fi.dy.masa.malilib.event.InitializationHandler;
//import net.fabricmc.api.ModInitializer;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;

@Mod(MaLiLibReference.MOD_ID)
public class MaLiLib {
    public static final Logger logger = LogManager.getLogger(MaLiLibReference.MOD_ID);

    public MaLiLib() {
        InitializationHandler.getInstance().registerInitializationHandler(new MaLiLibInitHandler());

        ModLoadingContext.get().registerExtensionPoint(
                ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory((minecraftClient, screen) -> {
                    MaLiLibConfigGui gui = new MaLiLibConfigGui();
                    gui.setParent(screen);
                    return gui;
                })
        );
    }
}

/*
public class MaLiLib implements ModInitializer
{
    public static final Logger logger = LogManager.getLogger(MaLiLibReference.MOD_ID);

    @Override
    public void onInitialize()
    {
        InitializationHandler.getInstance().registerInitializationHandler(new MaLiLibInitHandler());
    }
}
*/