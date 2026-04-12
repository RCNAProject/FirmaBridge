package net.zaltren.firmabridge.compat;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Metal;
import net.dries007.tfc.api.types.Ore;
import net.dries007.tfc.objects.items.metal.ItemOreTFC;
import net.dries007.tfc.objects.items.metal.ItemSmallOre;
import net.dries007.tfc.types.DefaultMetals;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.zaltren.firmabridge.FirmaBridge;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adds GT macerator recipes for TFC ore items, scaling output by ore grade.
 *
 * TFC ores drop ItemOreTFC (graded chunks) and ItemSmallOre (surface finds).
 * GT's auto-recipe generation doesn't cover these TFC-specific item types,
 * so without this handler players can't process TFC ore drops through GT machines.
 *
 * Yield by grade:
 *   Small ore  → 1× dustSmall  (surface find, partial yield)
 *   POOR chunk → 1× dust
 *   NORMAL chunk → 2× dust     (matches GT standard ore yield)
 *   RICH chunk → 3× dust
 *
 * Covers both metal ores (mapped via TFC Metal type) and non-metal ores
 * (coal, graphite, lapis, sulfur, etc.) mapped by registry name.
 * TFC-unique alloys (Black Steel, etc.) are skipped — no GT dust exists for them.
 *
 * Called in postInit, after GT has registered its own recipes.
 */
public class OreQualityHandler {

    // TFC Metal ResourceLocation → GT Material for dust output.
    // Only covers TFC metals that have a GT material counterpart.
    private static final Map<ResourceLocation, Material> METAL_TO_GT =
            new LinkedHashMap<>();

    // TFC non-metal ore ResourceLocation → GT material name (resolved at register() time).
    // Uses string-based GT material lookup so missing GT materials are skipped gracefully.
    private static final Map<ResourceLocation, String> NON_METAL_GT_NAMES =
            new LinkedHashMap<>();

    static {
        // Metal ores
        METAL_TO_GT.put(DefaultMetals.COPPER,       Materials.Copper);
        METAL_TO_GT.put(DefaultMetals.TIN,           Materials.Tin);
        METAL_TO_GT.put(DefaultMetals.GOLD,          Materials.Gold);
        METAL_TO_GT.put(DefaultMetals.SILVER,        Materials.Silver);
        METAL_TO_GT.put(DefaultMetals.LEAD,          Materials.Lead);
        METAL_TO_GT.put(DefaultMetals.NICKEL,        Materials.Nickel);
        METAL_TO_GT.put(DefaultMetals.BISMUTH,       Materials.Bismuth);
        METAL_TO_GT.put(DefaultMetals.ZINC,          Materials.Zinc);
        METAL_TO_GT.put(DefaultMetals.PLATINUM,      Materials.Platinum);
        // PIG_IRON resolved at register() time — FirmaBridgeMaterials.PIG_IRON isn't set yet at class load
        METAL_TO_GT.put(DefaultMetals.WROUGHT_IRON,  Materials.WroughtIron);
        METAL_TO_GT.put(DefaultMetals.STEEL,         Materials.Steel);
        METAL_TO_GT.put(DefaultMetals.BRONZE,        Materials.Bronze);
        METAL_TO_GT.put(DefaultMetals.BRASS,         Materials.Brass);

        // Non-metal ores — resolved by name at register() time.
        // GT built-in materials (always present):
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "bituminous_coal"), "coal");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "graphite"),        "graphite");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "lapis_lazuli"),    "lapis");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "saltpeter"),       "saltpeter");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "sulfur"),          "sulfur");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "cinnabar"),        "cinnabar");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "pitchblende"),     "pitchblende");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "borax"),           "borax");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "olivine"),         "olivine");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "gypsum"),          "gypsum");
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "satinspar"),       "gypsum");    // gypsum variety
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "selenite"),        "gypsum");    // gypsum variety
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "microcline"),      "potassium_feldspar");

        // FirmaBridge custom materials (registered in FirmaBridgeMaterials, available at postInit):
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "lignite"),         "lignite");   // FirmaBridgeMaterials.LIGNITE
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "jet"),             "lignite");   // TFC jet is a lignite variety
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "kaolinite"),       "kaolinite"); // FirmaBridgeMaterials.KAOLINITE
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "sylvite"),         "sylvite");   // FirmaBridgeMaterials.SYLVITE
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "cryolite"),        "cryolite");  // FirmaBridgeMaterials.CRYOLITE
        NON_METAL_GT_NAMES.put(new ResourceLocation("tfc", "serpentine"),      "serpentine");// FirmaBridgeMaterials.SERPENTINE
    }

    public static void register() {
        // Resolve pig iron to FirmaBridge's custom material (registered during MaterialEvent).
        // Falls back to Materials.Iron if the custom material failed to register.
        Material pigIron = GregTechAPI.materialManager.getMaterial("pig_iron");
        if (pigIron != null) {
            METAL_TO_GT.put(DefaultMetals.PIG_IRON, pigIron);
        } else {
            METAL_TO_GT.put(DefaultMetals.PIG_IRON, Materials.Iron);
            FirmaBridge.LOGGER.warn("OreQualityHandler: pig_iron material not found, falling back to Iron.");
        }

        // Resolve non-metal GT materials by name (GT registry is ready at postInit)
        Map<ResourceLocation, Material> nonMetalToGT = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, String> e : NON_METAL_GT_NAMES.entrySet()) {
            Material m = GregTechAPI.materialManager.getMaterial(e.getValue());
            if (m != null) {
                nonMetalToGT.put(e.getKey(), m);
            } else {
                FirmaBridge.LOGGER.warn("OreQualityHandler: GT material '{}' not found for TFC ore {}, skipping.",
                        e.getValue(), e.getKey());
            }
        }

        int added = 0;

        for (Ore ore : TFCRegistries.ORES.getValuesCollection()) {
            Metal metal = ore.getMetal();
            Material gtMaterial;

            if (metal != null) {
                gtMaterial = METAL_TO_GT.get(metal.getRegistryName());
                if (gtMaterial == null) continue; // TFC-unique alloy, no GT dust
            } else {
                gtMaterial = nonMetalToGT.get(ore.getRegistryName());
                if (gtMaterial == null) continue; // not mapped
            }

            if (ore.isGraded()) {
                added += addGradedRecipes(ore, gtMaterial);
            } else {
                added += addUngradedRecipe(ore, gtMaterial);
            }
            added += addSmallOreRecipe(ore, gtMaterial);
        }

        FirmaBridge.LOGGER.info("OreQualityHandler: added {} GT macerator recipes for TFC ore grades.", added);
    }

    /**
     * Adds one macerator recipe per grade (POOR/NORMAL/RICH) for a graded ore.
     */
    private static int addGradedRecipes(Ore ore, Material gtMaterial) {
        int count = 0;
        for (Ore.Grade grade : Ore.Grade.values()) {
            ItemStack input = ItemOreTFC.get(ore, grade, 1);
            if (input == null || input.isEmpty()) continue;

            int dustAmount;
            switch (grade) {
                case POOR:   dustAmount = 1; break;
                case RICH:   dustAmount = 3; break;
                default:     dustAmount = 2; break; // NORMAL
            }

            ItemStack output = OreDictUnifier.get(OrePrefix.dust, gtMaterial, dustAmount);
            if (output == null || output.isEmpty()) continue;

            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(new GTRecipeItemInput(input))
                    .outputs(output)
                    .duration(400)
                    .EUt(2)
                    .buildAndRegister();
            count++;
        }
        return count;
    }

    /**
     * Adds a single macerator recipe for an ungraded ore (2× dust, same as GT standard).
     */
    private static int addUngradedRecipe(Ore ore, Material gtMaterial) {
        ItemStack input = ItemOreTFC.get(ore, Ore.Grade.NORMAL, 1);
        if (input == null || input.isEmpty()) return 0;

        ItemStack output = OreDictUnifier.get(OrePrefix.dust, gtMaterial, 2);
        if (output == null || output.isEmpty()) return 0;

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input)
                .outputs(output)
                .duration(400)
                .EUt(2)
                .buildAndRegister();
        return 1;
    }

    /**
     * Adds a macerator recipe for the surface small ore variant (1× dustSmall).
     */
    private static int addSmallOreRecipe(Ore ore, Material gtMaterial) {
        ItemStack input = ItemSmallOre.get(ore, 1);
        if (input == null || input.isEmpty()) return 0;

        ItemStack output = OreDictUnifier.get(OrePrefix.dustSmall, gtMaterial, 1);
        if (output == null || output.isEmpty()) return 0;

        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input)
                .outputs(output)
                .duration(200)
                .EUt(2)
                .buildAndRegister();
        return 1;
    }

}
