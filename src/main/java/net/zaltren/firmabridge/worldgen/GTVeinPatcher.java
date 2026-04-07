package net.zaltren.firmabridge.worldgen;

import gregtech.api.util.WorldBlockPredicate;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.zaltren.firmabridge.FirmaBridge;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Patches GT CEu ore vein generation predicates to accept TFC raw rock blocks
 * as valid host blocks in place of vanilla stone.
 *
 * GT vein definitions target vanilla stone by default. TFC replaces all
 * underground stone with its own raw rock blocks, so without this patch
 * no GT veins would generate in TFC terrain.
 *
 * The patch accepts any TFC raw rock block regardless of rock type or category,
 * ensuring GT veins generate in all three TFC strata layers across all chunk types.
 *
 * Applied once in postInit after GT has loaded its worldgen definitions.
 */
public class GTVeinPatcher {

    @SuppressWarnings("unchecked")
    public static void apply() {
        try {
            Field veinsField = WorldGenRegistry.class.getDeclaredField("registeredVeinDefinitions");
            veinsField.setAccessible(true);
            List<OreDepositDefinition> definitions =
                    (List<OreDepositDefinition>) veinsField.get(WorldGenRegistry.INSTANCE);

            Field predicateField = OreDepositDefinition.class.getDeclaredField("generationPredicate");
            predicateField.setAccessible(true);

            int patched = 0;
            for (OreDepositDefinition def : definitions) {
                WorldBlockPredicate original = def.getGenerationPredicate();
                WorldBlockPredicate newPredicate = (state, world, pos) ->
                        original.test(state, world, pos) || state.getBlock() instanceof BlockRockRaw;

                predicateField.set(def, newPredicate);
                patched++;
            }

            FirmaBridge.LOGGER.info("GTVeinPatcher: patched {} GT vein definitions to support TFC rock layers.", patched);

        } catch (Exception e) {
            FirmaBridge.LOGGER.error("GTVeinPatcher: failed to patch GT vein definitions!", e);
        }
    }

}
