package net.zaltren.firmabridge.spawning;

import net.dries007.tfc.world.classic.biomes.BiomesTFC;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.worldgen.TFCSurfaceFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry for TFC-aware mob spawns.
 *
 * Other mods register their spawns via the Java API:
 *   MobSpawnRegistry.register(new MobSpawnEntry.Builder(...).build());
 *
 * Modpack developers add spawns via the config file (firmabridge.cfg):
 *   S:mobSpawnEntries <
 *     minecraft:wolf 10 1 4 CREATURE GRASSLAND,MOUNTAIN -5.0 25.0 100.0 500.0
 *   >
 *
 * Config entry format (space-separated):
 *   modid:entity  weight  minGroup  maxGroup  creatureType  surfaces  minTemp  maxTemp  minRainfall  maxRainfall
 *
 *   creatureType: MONSTER | CREATURE | AMBIENT | WATER_CREATURE
 *   surfaces: comma-separated list of: GRASSLAND, ARID, MOUNTAIN, SANDY, GRAVELLY, SWAMP — or ANY
 *   temp range: degrees Celsius (TFC scale, roughly -30 to +30)
 *   rainfall range: mm/year (TFC scale, roughly 0 to 500)
 */
public class MobSpawnRegistry {

    private static final List<MobSpawnEntry> ENTRIES = new ArrayList<MobSpawnEntry>();

    /**
     * Java API — register a TFC-aware spawn entry.
     * Call this during FMLInitializationEvent or earlier.
     */
    public static void register(MobSpawnEntry entry) {
        ENTRIES.add(entry);
    }

    /**
     * Returns an unmodifiable view of all registered spawn entries.
     */
    public static List<MobSpawnEntry> getEntries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    /**
     * Loads spawn entries from FirmaBridgeConfig.mobSpawnEntries and parses them.
     * Call during init phase, after config is loaded.
     */
    public static void loadFromConfig() {
        // Config-driven spawn entries are disabled until mod integration phase resumes.
    }

    /**
     * Applies all registered entries to TFC biome spawn lists.
     * Call during postInit, after all mods have registered their entities.
     */
    public static void apply() {
        int applied = 0;
        List<Biome> spawnBiomes = BiomesTFC.getSpawnBiomes();

        for (MobSpawnEntry entry : ENTRIES) {
            EntityEntry entityEntry = ForgeRegistries.ENTITIES.getValue(entry.entityId);
            if (entityEntry == null) {
                FirmaBridge.LOGGER.warn("MobSpawnRegistry: entity not found: {} — skipping.", entry.entityId);
                continue;
            }

            Class<? extends net.minecraft.entity.EntityLiving> entityClass = getEntityClass(entityEntry);
            if (entityClass == null) {
                FirmaBridge.LOGGER.warn("MobSpawnRegistry: {} is not an EntityLiving — skipping.", entry.entityId);
                continue;
            }

            Biome.SpawnListEntry spawnEntry = new Biome.SpawnListEntry(entityClass,
                    entry.weight, entry.minGroupSize, entry.maxGroupSize);

            for (Biome biome : spawnBiomes) {
                biome.getSpawnableList(entry.creatureType).add(spawnEntry);
            }
            applied++;
        }

        FirmaBridge.LOGGER.info("MobSpawnRegistry: applied {} TFC-aware spawn entries to {} biomes.",
                applied, spawnBiomes.size());
    }

    // -------------------------------------------------------------------------
    // Config parsing
    // -------------------------------------------------------------------------

    /**
     * Parses one config line into a MobSpawnEntry.
     * Format: modid:entity weight minGroup maxGroup creatureType surfaces minTemp maxTemp minRainfall maxRainfall
     * Returns null and logs a warning on parse failure.
     */
    private static MobSpawnEntry parseConfigLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 5) {
            FirmaBridge.LOGGER.warn("MobSpawnRegistry: invalid config entry (too few fields): '{}'", line);
            return null;
        }

        try {
            ResourceLocation entityId   = new ResourceLocation(parts[0]);
            int weight                  = Integer.parseInt(parts[1]);
            int minGroup                = Integer.parseInt(parts[2]);
            int maxGroup                = Integer.parseInt(parts[3]);
            EnumCreatureType type       = EnumCreatureType.valueOf(parts[4].toUpperCase());

            MobSpawnEntry.Builder builder = new MobSpawnEntry.Builder(entityId, type)
                    .weight(weight)
                    .group(minGroup, maxGroup);

            // Optional: surfaces (index 5)
            if (parts.length > 5 && !parts[5].equalsIgnoreCase("ANY")) {
                String[] surfaceNames = parts[5].split(",");
                TFCSurfaceFinder.SurfaceType[] surfaces =
                        new TFCSurfaceFinder.SurfaceType[surfaceNames.length];
                for (int i = 0; i < surfaceNames.length; i++) {
                    surfaces[i] = TFCSurfaceFinder.SurfaceType.valueOf(surfaceNames[i].toUpperCase());
                }
                builder.surfaces(surfaces);
            }

            // Optional: temperature range (indices 6, 7)
            if (parts.length > 7) {
                builder.temperature(Float.parseFloat(parts[6]), Float.parseFloat(parts[7]));
            }

            // Optional: rainfall range (indices 8, 9)
            if (parts.length > 9) {
                builder.rainfall(Float.parseFloat(parts[8]), Float.parseFloat(parts[9]));
            }

            return builder.build();

        } catch (Exception e) {
            FirmaBridge.LOGGER.warn("MobSpawnRegistry: failed to parse config entry '{}': {}", line, e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends net.minecraft.entity.EntityLiving> getEntityClass(EntityEntry entry) {
        Class<?> cls = entry.getEntityClass();
        if (cls != null && net.minecraft.entity.EntityLiving.class.isAssignableFrom(cls)) {
            return (Class<? extends net.minecraft.entity.EntityLiving>) cls;
        }
        return null;
    }

}
