package net.zaltren.firmabridge.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.compat.MaterialBridgeHandler;
import net.zaltren.firmabridge.compat.OreQualityHandler;
import net.zaltren.firmabridge.compat.RecipeCompatHandler;
import net.zaltren.firmabridge.config.FirmaBridgeConfig;
import net.zaltren.firmabridge.registry.RockRegistry;
import net.zaltren.firmabridge.spawning.MobSpawnHandler;
import net.zaltren.firmabridge.spawning.MobSpawnRegistry;
import net.zaltren.firmabridge.worldgen.GTVeinPatcher;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        FirmaBridge.LOGGER.info("{} pre-initializing.", net.zaltren.firmabridge.Tags.MOD_NAME);
        if (FirmaBridgeConfig.enableMobSpawns) {
            MinecraftForge.EVENT_BUS.register(MobSpawnHandler.class);
        }
    }

    public void init(FMLInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            MaterialBridgeHandler.register();
        }
        if (FirmaBridgeConfig.enableMobSpawns) {
            MobSpawnRegistry.loadFromConfig();
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            RockRegistry.resolve();
            GTVeinPatcher.apply();
            RecipeCompatHandler.register();
            OreQualityHandler.register();
        }
        if (FirmaBridgeConfig.enableMobSpawns) {
            MobSpawnRegistry.apply();
        }
    }

}
