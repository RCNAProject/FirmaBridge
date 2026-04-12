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
 * Rocks with a direct GT equivalent use that material. Rocks with no direct
 * GT equivalent use the nearest geological approximation (e.g. limestone →
 * calcite, gabbro → basalt) so GT machine processing works for all 21 TFC
 * rock types rather than only the 8 with exact matches.
 */
public class RockRegistry {

    // GT material registry name for each TFC rock ResourceLocation.
    // Direct equivalents: exact GT material match.
    // Approximations: nearest GT material by composition/geology (noted in comment).
    private static final Map<ResourceLocation, String> GT_NAMES = new LinkedHashMap<ResourceLocation, String>();

    // Resolved at postInit: Rock instance -> GT Material.
    private static final Map<Rock, Material> ROCK_TO_MATERIAL = new LinkedHashMap<Rock, Material>();

    static {
        // --- Igneous Intrusive ---
        GT_NAMES.put(DefaultRocks.GRANITE,      "granite");           // direct
        GT_NAMES.put(DefaultRocks.DIORITE,      "diorite");           // direct
        GT_NAMES.put(DefaultRocks.GABBRO,       "basalt");            // approx: mafic equivalent

        // --- Igneous Extrusive ---
        GT_NAMES.put(DefaultRocks.BASALT,       "basalt");            // direct
        GT_NAMES.put(DefaultRocks.ANDESITE,     "andesite");          // direct
        GT_NAMES.put(DefaultRocks.RHYOLITE,     "granite");           // approx: felsic equivalent
        GT_NAMES.put(DefaultRocks.DACITE,       "andesite");          // approx: intermediate equivalent

        // --- Sedimentary ---
        GT_NAMES.put(DefaultRocks.SHALE,        "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.CLAYSTONE,    "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.ROCKSALT,     "rock_salt");         // direct
        GT_NAMES.put(DefaultRocks.LIMESTONE,    "calcite");           // approx: CaCO3 → calcite
        GT_NAMES.put(DefaultRocks.CONGLOMERATE, "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.DOLOMITE,     "calcite");           // approx: CaMg(CO3)2 → calcite
        GT_NAMES.put(DefaultRocks.CHERT,        "flint");             // approx: siliceous equivalent
        GT_NAMES.put(DefaultRocks.CHALK,        "calcite");           // approx: CaCO3 → calcite

        // --- Metamorphic ---
        GT_NAMES.put(DefaultRocks.QUARTZITE,    "quartzite");         // direct
        GT_NAMES.put(DefaultRocks.SLATE,        "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.PHYLLITE,     "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.SCHIST,       "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.GNEISS,       "stone");             // approx: generic fallback
        GT_NAMES.put(DefaultRocks.MARBLE,       "marble");            // direct
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
