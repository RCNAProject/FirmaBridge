package net.zaltren.firmabridge.worldgen;

import gregtech.api.util.WorldBlockPredicate;
import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.config.WorldGenRegistry;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.zaltren.firmabridge.FirmaBridge;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;

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
                final String depositName = def.getDepositName();

                WorldBlockPredicate newPredicate = (state, world, pos) -> {
                    // Keep vanilla GT behavior for non-TFC blocks
                    if (original.test(state, world, pos)) return true;
                    if (!(state.getBlock() instanceof BlockRockRaw)) return false;

                    // Look up preference weight for this vein in this TFC rock type
                    BlockRockRaw raw = (BlockRockRaw) state.getBlock();
                    float weight = VeinRockWeightRegistry.getWeight(
                            depositName,
                            raw.getRock().getRegistryName());

                    if (weight <= 0f) return false;
                    if (weight >= 1f) return true;

                    // Seeded per-(chunk, vein) roll — all blocks in the same chunk
                    // see the same result, so the whole vein either spawns or not.
                    long seed = (long)(pos.getX() >> 4) * 341873128712L
                              ^ (long)(pos.getZ() >> 4) * 132897987541L
                              ^ (long) depositName.hashCode();
                    return new Random(seed).nextFloat() < weight;
                };

                predicateField.set(def, newPredicate);
                patched++;
            }

            FirmaBridge.LOGGER.info("GTVeinPatcher: patched {} GT vein definitions to support TFC rock layers with geological weights.", patched);

        } catch (Exception e) {
            FirmaBridge.LOGGER.error("GTVeinPatcher: failed to patch GT vein definitions!", e);
        }
    }

}
