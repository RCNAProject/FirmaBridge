package net.zaltren.firmabridge.config;

import net.minecraftforge.common.config.Config;
import net.zaltren.firmabridge.Tags;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
public class FirmaBridgeConfig {

    @Config.Comment("Enable GregTech CEu integration module (ore gen coexistence, material bridges, recipe compat).")
    public static boolean enableGTIntegration = true;

}
