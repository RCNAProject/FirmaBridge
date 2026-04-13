package net.zaltren.firmabridge.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.compat.MaterialBridgeHandler;
import net.zaltren.firmabridge.compat.OreQualityHandler;
import net.zaltren.firmabridge.compat.RecipeCompatHandler;
import net.zaltren.firmabridge.compat.StoneCompatHandler;
import net.zaltren.firmabridge.config.FirmaBridgeConfig;
import net.zaltren.firmabridge.integration.TFCStoneTypeHandler;
import net.zaltren.firmabridge.registry.RockRegistry;
import net.zaltren.firmabridge.worldgen.GTVeinPatcher;
import net.zaltren.firmabridge.worldgen.VillageWorldGen;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        FirmaBridge.LOGGER.info("{} pre-initializing.", net.zaltren.firmabridge.Tags.MOD_NAME);
        // TFCStoneTypeHandler.register() runs via MetaBlocksMixin before MetaBlocks.init(),
        // so no action needed here for stone type registration.
    }

    public void init(FMLInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            MaterialBridgeHandler.register();
        }
        if (FirmaBridgeConfig.enableVillages) {
            GameRegistry.registerWorldGenerator(new VillageWorldGen(), 0);
            FirmaBridge.LOGGER.info("FirmaBridge: village world generator registered.");
        }
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            RockRegistry.resolve();
            GTVeinPatcher.apply();
            RecipeCompatHandler.register();
            OreQualityHandler.register();
            StoneCompatHandler.register();
            TFCStoneTypeHandler.hideFromCreativeAndJEI("stone");
        }
    }

}
