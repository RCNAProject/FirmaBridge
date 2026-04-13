package net.zaltren.firmabridge.compat;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Metal;
import net.dries007.tfc.objects.items.metal.ItemMetal;
import net.dries007.tfc.types.DefaultMetals;
import net.minecraft.item.Item;
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
        added += addBlastFurnaceRecipes();
        FirmaBridge.LOGGER.info("RecipeCompatHandler: added {} GT machine recipes for TFC materials.", added);
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

        // Pig iron — use TFC item instances directly to guarantee machine recipe matching.
        count += maceratePigIron();

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
     * Macerator recipe for TFC pig iron ingot → pig iron dust.
     * Uses TFC item instances directly (GTRecipeItemInput) to guarantee machine matching —
     * GTRecipeOreInput can miss items not registered by GT itself.
     */
    private static int maceratePigIron() {
        Metal metal = TFCRegistries.METALS.getValue(DefaultMetals.PIG_IRON);
        if (metal == null) {
            FirmaBridge.LOGGER.warn("RecipeCompatHandler: TFC pig iron metal not found, skipping macerator recipe.");
            return 0;
        }
        Item ingot = ItemMetal.get(metal, Metal.ItemType.INGOT);
        Item dust  = ItemMetal.get(metal, Metal.ItemType.DUST);
        if (ingot == null || dust == null) {
            FirmaBridge.LOGGER.warn("RecipeCompatHandler: TFC pig iron ingot or dust item not found, skipping macerator recipe.");
            return 0;
        }
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(new GTRecipeItemInput(new ItemStack(ingot)))
                .outputs(new ItemStack(dust, 1))
                .duration(400)
                .EUt(2)
                .buildAndRegister();
        return 1;
    }

    /**
     * EBF recipe: TFC pig iron dust → iron ingot + 25% carbon dust.
     * Uses TFC item instance directly for the input to guarantee machine matching.
     *
     * Chain: TFC blast furnace → pig iron ingot
     *            → GT macerator → pig iron dust
     *            → GT EBF 800°C → iron ingot (+25% carbon dust)
     *            → GT EBF 1000°C (auto) → wrought iron → steel
     */
    private static int addBlastFurnaceRecipes() {
        Metal metal = TFCRegistries.METALS.getValue(DefaultMetals.PIG_IRON);
        if (metal == null) {
            FirmaBridge.LOGGER.warn("RecipeCompatHandler: TFC pig iron metal not found, skipping EBF recipe.");
            return 0;
        }
        Item dust = ItemMetal.get(metal, Metal.ItemType.DUST);
        if (dust == null) {
            FirmaBridge.LOGGER.warn("RecipeCompatHandler: TFC pig iron dust item not found, skipping EBF recipe.");
            return 0;
        }
        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new GTRecipeItemInput(new ItemStack(dust)))
                .output(OrePrefix.ingot, Materials.Iron)
                .chancedOutput(OrePrefix.dust, Materials.Carbon, 2500, 0) // 25% carbon dust byproduct
                .blastFurnaceTemp(800)
                .duration(400)
                .EUt(120)
                .buildAndRegister();
        return 1;
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
