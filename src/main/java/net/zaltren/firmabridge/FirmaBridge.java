package net.zaltren.firmabridge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.zaltren.firmabridge.proxy.CommonProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:tfc;required-after:gregtech")
public class FirmaBridge implements ILateMixinLoader {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.firmabridge.json");
    }

    @SidedProxy(
            clientSide = "net.zaltren.firmabridge.proxy.ClientProxy",
            serverSide = "net.zaltren.firmabridge.proxy.ServerProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
