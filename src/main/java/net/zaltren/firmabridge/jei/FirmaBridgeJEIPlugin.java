package net.zaltren.firmabridge.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.zaltren.firmabridge.registry.RockRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers FirmaBridge's JEI integration.
 *
 * All FirmaBridge recipes (ore quality, alloy macerator, stone macerator) are added
 * directly to GT's RecipeMaps, so GT's own JEI plugin discovers and displays them
 * automatically in the correct machine categories.
 *
 * TFC stone-type GT ore variants appear in GT's creative tab and JEI item list
 * automatically because StoneType registration happens before MetaBlocks.init().
 *
 * This plugin adds ingredient info entries for TFC raw stone blocks that have GT
 * material mappings, surfacing the macerator connection in JEI's info tab.
 */
@JEIPlugin
public class FirmaBridgeJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        registerRawStoneInfo(registry);
    }

    private void registerRawStoneInfo(IModRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();

        for (Block block : Block.REGISTRY) {
            if (!(block instanceof BlockRockRaw)) continue;
            BlockRockRaw rawBlock = (BlockRockRaw) block;
            if (RockRegistry.getMaterial(rawBlock.getRock()) == null) continue;
            stacks.add(new ItemStack(rawBlock));
        }

        if (!stacks.isEmpty()) {
            registry.addIngredientInfo(stacks, ItemStack.class,
                    "firmabridge.jei.info.raw_stone");
        }
    }
}
