package net.zaltren.firmabridge.compat;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import net.minecraft.item.ItemStack;
import net.zaltren.firmabridge.FirmaBridge;

/**
 * Adds explicit GT machine recipes for TFC metals that GT's auto-recipe
 * generation doesn't cover — specifically TFC-unique alloys that have no
 * GT material counterpart but are registered in OreDict.
 *
 * GT's OreIngredient-based recipes (macerator ingot→dust, etc.) are
 * auto-generated for GT materials only. TFC alloys like Black Steel,
 * Bismuth Bronze, etc. have no GT material, so we add them here.
 *
 * Called in postInit, after GT has registered its own recipes.
 */
public class RecipeCompatHandler {

    public static void register() {
        int added = addMaceratorRecipes();
        FirmaBridge.LOGGER.info("RecipeCompatHandler: added {} GT machine recipes for TFC-unique alloys.", added);
    }

    /**
     * Macerator recipes: TFC alloy ingots → correct GT material dust.
     *
     * GT CEu has materials for all TFC alloys, so each ingot maps to its own
     * dust rather than a lossy nearest-equivalent. These explicit recipes act
     * as a safety net alongside GT's OreDict auto-generated recipes.
     */
    private static int addMaceratorRecipes() {
        int count = 0;

        // Steel-family alloys → their own GT material dust
        count += macerate("ingotBlackSteel",     Materials.BlackSteel,     1, 400, 2);
        count += macerate("ingotBlueSteel",      Materials.BlueSteel,      1, 400, 2);
        count += macerate("ingotRedSteel",       Materials.RedSteel,       1, 400, 2);

        // Bronze-family alloys → their own GT material dust
        count += macerate("ingotBismuthBronze",  Materials.BismuthBronze,  1, 300, 2);
        count += macerate("ingotBlackBronze",    Materials.BlackBronze,    1, 300, 2);

        // Precious metal alloys → their own GT material dust
        count += macerate("ingotRoseGold",       Materials.RoseGold,       1, 300, 2);
        count += macerate("ingotSterlingSilver", Materials.SterlingSilver, 1, 300, 2);

        return count;
    }

    /**
     * Registers a single GT macerator recipe: oreDictInput ingot → outputAmount × Material dust.
     * Returns 1 if the recipe was registered successfully, 0 if the output item didn't exist.
     */
    private static int macerate(String oreDictInput, Material outputMaterial,
                                 int outputAmount, int duration, int eut) {
        ItemStack output = OreDictUnifier.get(OrePrefix.dust, outputMaterial, outputAmount);
        if (output == null || output.isEmpty()) {
            FirmaBridge.LOGGER.warn("RecipeCompatHandler: no dust item found for {} — skipping '{}'",
                    outputMaterial, oreDictInput);
            return 0;
        }
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new GTRecipeOreInput(oreDictInput))
                .outputs(output)
                .duration(duration)
                .EUt(eut)
                .buildAndRegister();
        return 1;
    }

}
