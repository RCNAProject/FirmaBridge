package net.zaltren.firmabridge.compat;

import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.registry.RockRegistry;

/**
 * Adds GT macerator recipes for TFC raw stone blocks.
 *
 * For each TFC rock type that has a GT material equivalent in RockRegistry,
 * a macerator recipe is registered so players can process TFC stone into
 * GT stone dusts using GT machines.
 *
 * Applied in postInit after RockRegistry has resolved.
 */
public class StoneCompatHandler {

    public static void register() {
        int added = 0;

        for (Block block : Block.REGISTRY) {
            if (!(block instanceof BlockRockRaw)) continue;

            BlockRockRaw rawBlock = (BlockRockRaw) block;
            Rock rock = rawBlock.getRock();
            Material material = RockRegistry.getMaterial(rock);

            if (material == null) continue;
            if (!OrePrefix.dust.doGenerateItem(material)) continue;

            RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                    .inputs(new ItemStack(rawBlock))
                    .output(OrePrefix.dust, material)
                    .duration(100)
                    .EUt(2)
                    .buildAndRegister();

            added++;
        }

        FirmaBridge.LOGGER.info("StoneCompatHandler: added {} TFC stone macerator recipes.", added);
    }

}
