package net.zaltren.firmabridge.compat;

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
 * Adds GT macerator recipes for TFC metal ore items, scaling output by ore grade.
 *
 * TFC metal ores drop ItemOreTFC (graded chunks) and ItemSmallOre (surface finds).
 * GT's auto-recipe generation only covers its own OreDict ore entries; TFC metal
 * ore items are not in GT's standard OreDict, so we add them explicitly here.
 *
 * Yield by grade:
 *   Small ore    → 1× dustSmall
 *   POOR chunk   → 1× dust
 *   NORMAL chunk → 2× dust  (matches GT standard ore yield)
 *   RICH chunk   → 3× dust
 *
 * Non-metal TFC ores (coal, graphite, lignite, etc.) are excluded — GT auto-generates
 * macerator recipes for those when the FirmaBridge/GT materials are registered.
 * TFC-unique alloys (Black Steel, etc.) are also skipped — no GT dust exists for them.
 *
 * Called in postInit, after GT has registered its own recipes.
 */
public class OreQualityHandler {

    // TFC Metal ResourceLocation → GT Material for dust output.
    // Only covers TFC metals that have a GT material counterpart.
    private static final Map<ResourceLocation, Material> METAL_TO_GT =
            new LinkedHashMap<>();

    static {
        METAL_TO_GT.put(DefaultMetals.COPPER,       Materials.Copper);
        METAL_TO_GT.put(DefaultMetals.TIN,           Materials.Tin);
        METAL_TO_GT.put(DefaultMetals.GOLD,          Materials.Gold);
        METAL_TO_GT.put(DefaultMetals.SILVER,        Materials.Silver);
        METAL_TO_GT.put(DefaultMetals.LEAD,          Materials.Lead);
        METAL_TO_GT.put(DefaultMetals.NICKEL,        Materials.Nickel);
        METAL_TO_GT.put(DefaultMetals.BISMUTH,       Materials.Bismuth);
        METAL_TO_GT.put(DefaultMetals.ZINC,          Materials.Zinc);
        METAL_TO_GT.put(DefaultMetals.PLATINUM,      Materials.Platinum);
        // PIG_IRON omitted — not a naturally spawning ore, TFC produces it via blast furnace
        METAL_TO_GT.put(DefaultMetals.WROUGHT_IRON,  Materials.WroughtIron);
        METAL_TO_GT.put(DefaultMetals.STEEL,         Materials.Steel);
        METAL_TO_GT.put(DefaultMetals.BRONZE,        Materials.Bronze);
        METAL_TO_GT.put(DefaultMetals.BRASS,         Materials.Brass);
    }

    public static void register() {
        int added = 0;

        for (Ore ore : TFCRegistries.ORES.getValuesCollection()) {
            Metal metal = ore.getMetal();
            if (metal == null) continue; // non-metal ores handled by GT auto-generation

            Material gtMaterial = METAL_TO_GT.get(metal.getRegistryName());
            if (gtMaterial == null) continue; // TFC-unique alloy, no GT dust

            if (ore.isGraded()) {
                added += addGradedRecipes(ore, gtMaterial);
            } else {
                added += addUngradedRecipe(ore, gtMaterial);
            }
            added += addSmallOreRecipe(ore, gtMaterial);
        }

        FirmaBridge.LOGGER.info("OreQualityHandler: added {} GT macerator recipes for TFC metal ore grades.", added);
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
