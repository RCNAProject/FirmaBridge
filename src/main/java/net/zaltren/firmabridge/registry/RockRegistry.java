package net.zaltren.firmabridge.registry;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.types.DefaultRocks;
import net.minecraft.util.ResourceLocation;
import net.zaltren.firmabridge.FirmaBridge;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps TFC rock types to their GregTech CEu Material equivalents.
 * This registry is the backbone of FirmaBridge — all other modules
 * (ore gen, material bridges, recipe compat) build on top of it.
 *
 * Resolved lazily in postInit after both TFC and GT registries are populated.
 * Rocks with no GT equivalent map to null and are silently skipped by consumers.
 */
public class RockRegistry {

    // GT material registry name for each TFC rock ResourceLocation. Null = no GT equivalent.
    private static final Map<ResourceLocation, String> GT_NAMES = new LinkedHashMap<ResourceLocation, String>();

    // Resolved at postInit: Rock instance -> GT Material.
    private static final Map<Rock, Material> ROCK_TO_MATERIAL = new LinkedHashMap<Rock, Material>();

    static {
        // Igneous Intrusive
        GT_NAMES.put(DefaultRocks.GRANITE,      "granite");
        GT_NAMES.put(DefaultRocks.DIORITE,      "diorite");
        GT_NAMES.put(DefaultRocks.GABBRO,       null);       // not in GT CEu 2.8.10

        // Igneous Extrusive
        GT_NAMES.put(DefaultRocks.RHYOLITE,     null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.BASALT,       "basalt");
        GT_NAMES.put(DefaultRocks.ANDESITE,     "andesite");
        GT_NAMES.put(DefaultRocks.DACITE,       null);       // not in GT CEu 2.8.10

        // Sedimentary
        GT_NAMES.put(DefaultRocks.SHALE,        null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.CLAYSTONE,    null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.ROCKSALT,     "rock_salt");
        GT_NAMES.put(DefaultRocks.LIMESTONE,    null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.CONGLOMERATE, null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.DOLOMITE,     null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.CHERT,        "flint");    // closest GT equivalent
        GT_NAMES.put(DefaultRocks.CHALK,        null);       // not in GT CEu 2.8.10

        // Metamorphic
        GT_NAMES.put(DefaultRocks.QUARTZITE,    "quartzite");
        GT_NAMES.put(DefaultRocks.SLATE,        null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.PHYLLITE,     null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.SCHIST,       null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.GNEISS,       null);       // not in GT CEu 2.8.10
        GT_NAMES.put(DefaultRocks.MARBLE,       "marble");
    }

    /**
     * Resolve TFC Rock instances and GT Materials from their respective registries.
     * Call this in postInit, after both TFC and GT have finished registering content.
     */
    public static void resolve() {
        ROCK_TO_MATERIAL.clear();
        int mapped = 0;

        for (Map.Entry<ResourceLocation, String> entry : GT_NAMES.entrySet()) {
            Rock rock = TFCRegistries.ROCKS.getValue(entry.getKey());
            if (rock == null) {
                FirmaBridge.LOGGER.warn("RockRegistry: TFC rock not found in registry: {}", entry.getKey());
                continue;
            }

            String gtName = entry.getValue();
            if (gtName == null) continue;

            Material material = GregTechAPI.materialManager.getMaterial(gtName);
            if (material != null) {
                ROCK_TO_MATERIAL.put(rock, material);
                mapped++;
            } else {
                FirmaBridge.LOGGER.warn("RockRegistry: no GT material found for TFC rock {} (looked up '{}')",
                        entry.getKey(), gtName);
            }
        }

        FirmaBridge.LOGGER.info("RockRegistry resolved: {}/{} TFC rocks mapped to GT materials.",
                mapped, GT_NAMES.size());
    }

    /**
     * Returns the GT Material for a TFC rock, or null if none is mapped.
     */
    @Nullable
    public static Material getMaterial(Rock rock) {
        return ROCK_TO_MATERIAL.get(rock);
    }

    /**
     * Returns true if the given TFC rock has a GT material mapped.
     */
    public static boolean hasMaterial(Rock rock) {
        return ROCK_TO_MATERIAL.containsKey(rock);
    }

    /**
     * Returns an unmodifiable view of all resolved mappings.
     */
    public static Map<Rock, Material> getAll() {
        return Collections.unmodifiableMap(ROCK_TO_MATERIAL);
    }

}
