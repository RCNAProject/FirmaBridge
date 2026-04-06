package net.zaltren.firmabridge.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.compat.MaterialBridgeHandler;
import net.zaltren.firmabridge.compat.RecipeCompatHandler;
import net.zaltren.firmabridge.config.FirmaBridgeConfig;
import net.zaltren.firmabridge.registry.RockRegistry;
import net.zaltren.firmabridge.worldgen.GTVeinPatcher;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        FirmaBridge.LOGGER.info("{} pre-initializing.", net.zaltren.firmabridge.Tags.MOD_NAME);
    }

    public void init(FMLInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            MaterialBridgeHandler.register();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            RockRegistry.resolve();
            GTVeinPatcher.apply();
            RecipeCompatHandler.register();
        }
    }

}
