package net.zaltren.firmabridge.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;
import gregtech.common.blocks.MetaBlocks;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.compat.MaterialBridgeHandler;
import net.zaltren.firmabridge.compat.OreQualityHandler;
import net.zaltren.firmabridge.compat.RecipeCompatHandler;
import net.zaltren.firmabridge.compat.StoneCompatHandler;
import net.zaltren.firmabridge.config.FirmaBridgeConfig;
import net.zaltren.firmabridge.integration.TFCStoneTypeHandler;
import net.zaltren.firmabridge.registry.RockRegistry;
import net.zaltren.firmabridge.worldgen.GTVeinPatcher;

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
    }

    public void postInit(FMLPostInitializationEvent event) {
        if (FirmaBridgeConfig.enableGTIntegration) {
            RockRegistry.resolve();
            GTVeinPatcher.apply();
            RecipeCompatHandler.register();
            OreQualityHandler.register();
            StoneCompatHandler.register();
            TFCStoneTypeHandler.hideFromCreativeAndJEI("stone");
            diagnoseTFCStoneTypes();
        }
    }

    private void diagnoseTFCStoneTypes() {
        FirmaBridge.LOGGER.info("=== FirmaBridge StoneType Diagnostic ===");

        int tfcInRegistry = 0;
        for (StoneType st : StoneType.STONE_TYPE_REGISTRY) {
            if (st.name.startsWith("tfc_")) {
                FirmaBridge.LOGGER.info("  REGISTRY: found TFC stone type '{}'", st.name);
                tfcInRegistry++;
            }
        }
        FirmaBridge.LOGGER.info("  REGISTRY total TFC stone types: {}", tfcInRegistry);

        int tfcInOres = 0;
        for (BlockOre oreBlock : MetaBlocks.ORES) {
            for (StoneType st : oreBlock.STONE_TYPE.getAllowedValues()) {
                if (st.name.startsWith("tfc_")) {
                    FirmaBridge.LOGGER.info("  BLOCKORE {}: contains TFC stone type '{}'", oreBlock.getRegistryName(), st.name);
                    tfcInOres++;
                }
            }
        }
        FirmaBridge.LOGGER.info("  BLOCKORE total TFC stone type slots: {}", tfcInOres);
        FirmaBridge.LOGGER.info("=========================================");
    }

}
