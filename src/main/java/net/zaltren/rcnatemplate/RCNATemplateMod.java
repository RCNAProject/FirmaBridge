package net.zaltren.rcnatemplate;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// RCNA Template Mod Entrypoint
//
// Before using this template you MUST update the following values in gradle.properties:
//
// root_package = net.yourname
// mod_id = yourmodid
// mod_name = Your Mod Name
//
// After changing these values run:
//   ./gradlew clean
//   ./gradlew build
//
// This will regenerate the Tags class used by the mod.
//
// Please make sure to read README.md if you are someone else other than TheZaltren
//
// As this template was created for RCNA Projects

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class RCNATemplateMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    /**
     * https://cleanroommc.com/wiki/forge-mod-development/event#overview
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("{} is initializing.", Tags.MOD_NAME);
    }

}
