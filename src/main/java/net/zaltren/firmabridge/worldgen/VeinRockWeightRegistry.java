package net.zaltren.firmabridge.worldgen;

import net.dries007.tfc.types.DefaultRocks;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines per-rock preference weights for each GT ore vein.
 *
 * Weights control how likely a GT ore vein is to spawn when its center lands
 * in a given TFC rock type. The roll is performed once per (chunk, vein) pair
 * using a deterministic seed, so the entire vein either appears or does not —
 * vein shapes are never corrupted.
 *
 * Weight semantics:
 *   1.0 = always spawn (primary geological habitat)
 *   0.5 = spawn roughly half the time (secondary habitat)
 *   0.0 = never spawn (geologically incompatible)
 *   DEFAULT_WEIGHT (0.05) = applied to any rock not listed — permissive fallback
 *   so players are never fully locked out if local TFC geology is unfavorable.
 *
 * Veins not listed here use DEFAULT_WEIGHT for all rocks (vanilla GT behavior
 * with a small bias).
 */
public class VeinRockWeightRegistry {

    static final float DEFAULT_WEIGHT = 0.05f;

    // vein deposit name -> (TFC rock ResourceLocation -> weight)
    private static final Map<String, Map<ResourceLocation, Float>> REGISTRY = new HashMap<>();

    static {
        // --- Mafic igneous ---
        define("gregtech.veins.ore.magnetite",
            r(DefaultRocks.GABBRO,       1.0f),
            r(DefaultRocks.BASALT,       1.0f),
            r(DefaultRocks.CHERT,        0.6f),
            r(DefaultRocks.GNEISS,       0.6f),
            r(DefaultRocks.SHALE,        0.3f));

        define("gregtech.veins.ore.olivine",
            r(DefaultRocks.GABBRO,       1.0f),
            r(DefaultRocks.BASALT,       0.8f),
            r(DefaultRocks.ANDESITE,     0.2f));

        define("gregtech.veins.ore.nickel",
            r(DefaultRocks.GABBRO,       1.0f),
            r(DefaultRocks.BASALT,       1.0f),
            r(DefaultRocks.SCHIST,       0.3f),
            r(DefaultRocks.CLAYSTONE,    0.3f));

        // --- Porphyry / felsic intrusive ---
        define("gregtech.veins.ore.copper",
            r(DefaultRocks.DIORITE,      1.0f),
            r(DefaultRocks.GRANITE,      1.0f),
            r(DefaultRocks.GABBRO,       0.6f),
            r(DefaultRocks.ANDESITE,     0.6f),
            r(DefaultRocks.BASALT,       0.6f),
            r(DefaultRocks.SCHIST,       0.3f),
            r(DefaultRocks.GNEISS,       0.3f));

        // Volcanic hydrothermal (zeolite, realgar indicate altered volcanic host)
        define("gregtech.veins.ore.copper_tin",
            r(DefaultRocks.ANDESITE,     1.0f),
            r(DefaultRocks.DACITE,       1.0f),
            r(DefaultRocks.RHYOLITE,     1.0f),
            r(DefaultRocks.BASALT,       0.6f),
            r(DefaultRocks.GRANITE,      0.6f));

        // Granite pegmatite / greisen
        define("gregtech.veins.ore.cassiterite",
            r(DefaultRocks.GRANITE,      1.0f),
            r(DefaultRocks.QUARTZITE,    0.5f),
            r(DefaultRocks.SCHIST,       0.5f),
            r(DefaultRocks.GNEISS,       0.5f));

        // --- MVT (Mississippi Valley Type) lead-silver ---
        define("gregtech.veins.ore.galena",
            r(DefaultRocks.LIMESTONE,    1.0f),
            r(DefaultRocks.DOLOMITE,     1.0f),
            r(DefaultRocks.MARBLE,       0.4f),
            r(DefaultRocks.GRANITE,      0.3f),
            r(DefaultRocks.SCHIST,       0.3f));

        // --- Sedimentary ---
        define("gregtech.veins.ore.coal",
            r(DefaultRocks.SHALE,        1.0f),
            r(DefaultRocks.CLAYSTONE,    1.0f),
            r(DefaultRocks.CONGLOMERATE, 0.4f),
            r(DefaultRocks.LIMESTONE,    0.4f));

        // Banded iron / limonite weathering
        define("gregtech.veins.ore.iron",
            r(DefaultRocks.SHALE,        1.0f),
            r(DefaultRocks.CLAYSTONE,    1.0f),
            r(DefaultRocks.CHERT,        0.7f),
            r(DefaultRocks.CONGLOMERATE, 0.7f),
            r(DefaultRocks.LIMESTONE,    0.4f));

        // Evaporite + granite pegmatite (lepidolite/spodumene inherited from GT vein design)
        define("gregtech.veins.ore.salts",
            r(DefaultRocks.ROCKSALT,     1.0f),
            r(DefaultRocks.GRANITE,      0.5f),
            r(DefaultRocks.SHALE,        0.2f),
            r(DefaultRocks.LIMESTONE,    0.2f));

        // Coastal / beach placer
        define("gregtech.veins.ore.mineral_sand",
            r(DefaultRocks.CONGLOMERATE, 1.0f),
            r(DefaultRocks.SHALE,        0.6f),
            r(DefaultRocks.CLAYSTONE,    0.6f),
            r(DefaultRocks.ROCKSALT,     0.3f));

        define("gregtech.veins.ore.oilsands",
            r(DefaultRocks.SHALE,        1.0f),
            r(DefaultRocks.CLAYSTONE,    1.0f),
            r(DefaultRocks.CONGLOMERATE, 0.4f),
            r(DefaultRocks.LIMESTONE,    0.4f));

        // Alluvial placer (cassiterite_sand, garnet_sand)
        define("gregtech.veins.ore.garnet_tin",
            r(DefaultRocks.CONGLOMERATE, 1.0f),
            r(DefaultRocks.SHALE,        0.5f),
            r(DefaultRocks.SCHIST,       0.5f));

        // Phosphate: carbonatite / sedimentary
        define("gregtech.veins.ore.apatite",
            r(DefaultRocks.LIMESTONE,    1.0f),
            r(DefaultRocks.CHALK,        1.0f),
            r(DefaultRocks.GRANITE,      0.5f),
            r(DefaultRocks.GNEISS,       0.5f));

        // --- Metamorphic ---
        define("gregtech.veins.ore.garnet",
            r(DefaultRocks.SCHIST,       1.0f),
            r(DefaultRocks.GNEISS,       1.0f),
            r(DefaultRocks.MARBLE,       0.4f),
            r(DefaultRocks.GRANITE,      0.4f));

        // Corundum family (almandine, pyrope, sapphire) — high-pressure metamorphic
        define("gregtech.veins.ore.sapphire",
            r(DefaultRocks.SCHIST,       1.0f),
            r(DefaultRocks.GNEISS,       1.0f),
            r(DefaultRocks.MARBLE,       0.6f),
            r(DefaultRocks.GABBRO,       0.3f));

        // Contact metamorphic skarn (lazurite forms where limestone meets granite intrusions)
        define("gregtech.veins.ore.lapis",
            r(DefaultRocks.MARBLE,       1.0f),
            r(DefaultRocks.LIMESTONE,    0.7f),
            r(DefaultRocks.GNEISS,       0.4f));

        // Soapstone / talc — altered ultramafic
        define("gregtech.veins.ore.lubricant",
            r(DefaultRocks.SCHIST,       1.0f),
            r(DefaultRocks.MARBLE,       1.0f),
            r(DefaultRocks.GABBRO,       0.5f),
            r(DefaultRocks.SHALE,        0.3f));

        // Kyanite / mica — high-pressure metamorphic; bauxite sporadic (laterite)
        define("gregtech.veins.ore.mica",
            r(DefaultRocks.SCHIST,       1.0f),
            r(DefaultRocks.GNEISS,       1.0f),
            r(DefaultRocks.GRANITE,      0.5f),
            r(DefaultRocks.CLAYSTONE,    0.3f));

        // Grossular skarn / metamorphic manganese
        define("gregtech.veins.ore.manganese",
            r(DefaultRocks.MARBLE,       1.0f),
            r(DefaultRocks.SCHIST,       1.0f),
            r(DefaultRocks.GRANITE,      0.5f),
            r(DefaultRocks.LIMESTONE,    0.5f));

        // Graphite / diamond — ultra-deep metamorphic / crustal
        define("gregtech.veins.ore.diamond",
            r(DefaultRocks.GNEISS,       0.8f),
            r(DefaultRocks.SCHIST,       0.8f),
            r(DefaultRocks.GABBRO,       0.6f),
            r(DefaultRocks.QUARTZITE,    0.4f));

        // --- Volcanic hydrothermal / fantasy ---
        // Ruby and cinnabar indicate high-temp volcanic association
        define("gregtech.veins.ore.redstone",
            r(DefaultRocks.ANDESITE,     1.0f),
            r(DefaultRocks.RHYOLITE,     1.0f),
            r(DefaultRocks.DACITE,       1.0f),
            r(DefaultRocks.BASALT,       0.5f),
            r(DefaultRocks.GRANITE,      0.5f),
            r(DefaultRocks.MARBLE,       0.4f));
    }

    /**
     * Returns the spawn weight for a given vein in a given TFC rock type.
     * Returns DEFAULT_WEIGHT for any vein or rock not explicitly defined.
     */
    public static float getWeight(String veinName, ResourceLocation rock) {
        Map<ResourceLocation, Float> map = REGISTRY.get(veinName);
        if (map == null) return DEFAULT_WEIGHT;
        Float w = map.get(rock);
        return w != null ? w : DEFAULT_WEIGHT;
    }

    private static void define(String veinName, Object[]... entries) {
        Map<ResourceLocation, Float> map = new HashMap<>();
        for (Object[] entry : entries) {
            map.put((ResourceLocation) entry[0], (Float) entry[1]);
        }
        REGISTRY.put(veinName, map);
    }

    private static Object[] r(ResourceLocation rock, float weight) {
        return new Object[] { rock, weight };
    }

}
