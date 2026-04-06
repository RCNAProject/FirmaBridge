package net.zaltren.firmabridge.worldgen;

import gregtech.api.util.WorldBlockPredicate;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.RockCategory;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.dries007.tfc.types.DefaultRocks;
import net.minecraft.util.ResourceLocation;
import net.zaltren.firmabridge.FirmaBridge;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Patches GT CEu ore vein generation predicates to accept TFC raw rock blocks
 * as valid host blocks, replacing the vanilla stone-only default.
 *
 * Each vein is mapped to one or more TFC RockCategory values based on geology.
 * Unmapped veins fall back to accepting any TFC raw rock.
 *
 * Applied once in postInit, after GT has loaded its worldgen definitions.
 */
public class GTVeinPatcher {

    // GT vein deposit name → allowed TFC rock categories (null entry = any TFC rock)
    private static final Map<String, Set<ResourceLocation>> VEIN_CATEGORY_MAP =
            new HashMap<String, Set<ResourceLocation>>();

    static {
        Set<ResourceLocation> intrusive   = cats(DefaultRocks.IGNEOUS_INTRUSIVE);
        Set<ResourceLocation> extrusive   = cats(DefaultRocks.IGNEOUS_EXTRUSIVE);
        Set<ResourceLocation> sedimentary = cats(DefaultRocks.SEDIMENTARY);
        Set<ResourceLocation> metamorphic = cats(DefaultRocks.METAMORPHIC);
        Set<ResourceLocation> igneousAll  = cats(DefaultRocks.IGNEOUS_INTRUSIVE, DefaultRocks.IGNEOUS_EXTRUSIVE);
        Set<ResourceLocation> sedMeta     = cats(DefaultRocks.SEDIMENTARY, DefaultRocks.METAMORPHIC);
        Set<ResourceLocation> intruMeta   = cats(DefaultRocks.IGNEOUS_INTRUSIVE, DefaultRocks.METAMORPHIC);

        // --- Igneous Intrusive (granitic / deep plutonic) ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.cassiterite",          intrusive);   // tin in granite
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.copper_tin",           intrusive);   // chalcopyrite/cassiterite
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.diamond",              intrusive);   // kimberlite
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.nickel",               intrusive);   // mafic/ultramafic
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.olivine",              intrusive);   // ultramafic intrusions
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.redstone",             intrusive);   // deep igneous
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.black_granite_sphere", intrusive);
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.red_granite_sphere",   intrusive);

        // --- Igneous Extrusive (volcanic) ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.copper",               extrusive);   // VMS copper deposits
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.basalt_sphere",        extrusive);

        // --- Sedimentary ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.coal",                 sedimentary);
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.apatite",              sedimentary); // phosphate beds
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.salts",                sedimentary); // evaporite
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.manganese",            sedimentary); // marine sedimentary
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.oilsands",             sedimentary);
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.raw_oil_sphere",       sedimentary);
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.mineral_sand",         sedimentary);

        // --- Metamorphic ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.garnet",               metamorphic);
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.garnet_tin",           metamorphic);
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.lapis",                metamorphic); // marble-hosted
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.marble_sphere",        metamorphic);

        // --- Both igneous types ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.magnetite",            igneousAll);

        // --- Sedimentary + Metamorphic ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.iron",                 sedMeta);     // BIF
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.galena",               sedMeta);     // MVT lead-zinc

        // --- Intrusive + Metamorphic ---
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.sapphire",             intruMeta);   // corundum
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.mica",                 intruMeta);   // pegmatite/schist
        VEIN_CATEGORY_MAP.put("gregtech.veins.ore.lubricant",            intruMeta);   // molybdenite
    }

    private static Set<ResourceLocation> cats(ResourceLocation... rls) {
        Set<ResourceLocation> set = new HashSet<ResourceLocation>();
        Collections.addAll(set, rls);
        return Collections.unmodifiableSet(set);
    }

    @SuppressWarnings("unchecked")
    public static void apply() {
        try {
            // Resolve TFC rock category ResourceLocation → RockCategory instance
            Map<ResourceLocation, RockCategory> categoryLookup = new HashMap<ResourceLocation, RockCategory>();
            for (RockCategory cat : TFCRegistries.ROCK_CATEGORIES.getValuesCollection()) {
                categoryLookup.put(cat.getRegistryName(), cat);
            }

            // Access private WorldGenRegistry.registeredVeinDefinitions
            Field veinsField = WorldGenRegistry.class.getDeclaredField("registeredVeinDefinitions");
            veinsField.setAccessible(true);
            List<OreDepositDefinition> definitions =
                    (List<OreDepositDefinition>) veinsField.get(WorldGenRegistry.INSTANCE);

            // Access private OreDepositDefinition.generationPredicate
            Field predicateField = OreDepositDefinition.class.getDeclaredField("generationPredicate");
            predicateField.setAccessible(true);

            int patched = 0;
            for (OreDepositDefinition def : definitions) {
                Set<ResourceLocation> allowedCatRLs = VEIN_CATEGORY_MAP.get(def.getDepositName());

                // Resolve allowed category instances (null = any TFC rock)
                final Set<RockCategory> allowedCats;
                if (allowedCatRLs == null) {
                    allowedCats = null;
                } else {
                    allowedCats = new HashSet<RockCategory>();
                    for (ResourceLocation rl : allowedCatRLs) {
                        RockCategory cat = categoryLookup.get(rl);
                        if (cat != null) allowedCats.add(cat);
                    }
                }

                WorldBlockPredicate original = def.getGenerationPredicate();
                WorldBlockPredicate newPredicate = (state, world, pos) -> {
                    if (original.test(state, world, pos)) return true;
                    if (!(state.getBlock() instanceof BlockRockRaw)) return false;
                    if (allowedCats == null) return true;
                    return allowedCats.contains(((BlockRockRaw) state.getBlock()).getRock().getRockCategory());
                };

                predicateField.set(def, newPredicate);
                patched++;
            }

            FirmaBridge.LOGGER.info("GTVeinPatcher: patched {} GT vein definitions to support TFC rock layers.", patched);

        } catch (Exception e) {
            FirmaBridge.LOGGER.error("GTVeinPatcher: failed to patch GT vein definitions!", e);
        }
    }

}
