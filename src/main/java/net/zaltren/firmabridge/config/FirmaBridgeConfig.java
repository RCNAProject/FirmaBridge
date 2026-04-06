package net.zaltren.firmabridge.config;

import net.minecraftforge.common.config.Config;
import net.zaltren.firmabridge.Tags;

@Config(modid = Tags.MOD_ID, name = Tags.MOD_ID)
public class FirmaBridgeConfig {

    @Config.Comment("Enable GregTech CEu integration module (ore gen coexistence, material bridges, recipe compat).")
    public static boolean enableGTIntegration = true;

    @Config.Comment("Enable TFC-aware mob spawn module.")
    public static boolean enableMobSpawns = true;

    @Config.Comment({
        "Custom TFC-aware mob spawn entries for modpack developers.",
        "Each entry is a space-separated line with the following fields:",
        "  modid:entity  weight  minGroup  maxGroup  creatureType  surfaces  minTemp  maxTemp  minRainfall  maxRainfall",
        "",
        "Fields:",
        "  modid:entity   — entity registry name (e.g. minecraft:wolf)",
        "  weight         — spawn weight, higher = more common (e.g. 10)",
        "  minGroup       — minimum group size per spawn attempt (e.g. 1)",
        "  maxGroup       — maximum group size per spawn attempt (e.g. 4)",
        "  creatureType   — MONSTER | CREATURE | AMBIENT | WATER_CREATURE",
        "  surfaces       — comma-separated surface types, or ANY:",
        "                   GRASSLAND, ARID, MOUNTAIN, SANDY, GRAVELLY, SWAMP",
        "  minTemp        — minimum temperature in Celsius, TFC scale (~-30 to +30)",
        "  maxTemp        — maximum temperature in Celsius",
        "  minRainfall    — minimum annual rainfall in mm (~0 to 500)",
        "  maxRainfall    — maximum annual rainfall in mm",
        "",
        "Temperature and rainfall fields are optional — omit them to allow any climate.",
        "",
        "Examples:",
        "  minecraft:wolf 10 1 4 CREATURE GRASSLAND,MOUNTAIN -5.0 20.0 100.0 400.0",
        "  minecraft:spider 8 1 3 MONSTER ANY",
        "  minecraft:polar_bear 5 1 2 CREATURE GRASSLAND,MOUNTAIN -30.0 -5.0 0.0 300.0"
    })
    public static String[] mobSpawnEntries = new String[0];

}
