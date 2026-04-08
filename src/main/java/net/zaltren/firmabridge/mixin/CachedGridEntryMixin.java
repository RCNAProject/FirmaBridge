package net.zaltren.firmabridge.mixin;

import gregtech.api.unification.ore.StoneType;
import gregtech.api.worldgen.filler.BlockFiller;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.zaltren.firmabridge.FirmaBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Guards GT's ore worldgen against null block states returned by BlockFiller.apply().
 *
 * Root cause: OreFilterEntry.apply() calls blockStateMap.get(stoneType) where stoneType
 * is non-null (a registered TFC StoneType) but missing from the per-material blockStateMap
 * built by OreConfigUtils.getOreForMaterial(), so Map.get() returns null with no fallback.
 *
 * populateChunk() uses the result in two places: World.setBlockState() and Block.getStateId().
 * Both throw NullPointerException when passed null. Redirecting blockFiller.apply() and
 * returning source (the existing block) when null is produced makes both calls safe — the
 * position is effectively skipped without crashing.
 */
@Mixin(targets = "gregtech.api.worldgen.generator.CachedGridEntry$ChunkDataEntry", remap = false)
public class CachedGridEntryMixin {

    // Log the first null-producing (source, stoneType) pair once, then stop logging.
    private static final AtomicBoolean loggedOnce = new AtomicBoolean(false);

    @Redirect(
            method = "populateChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lgregtech/api/worldgen/filler/BlockFiller;apply(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;IIIDLjava/util/Random;I)Lnet/minecraft/block/state/IBlockState;"
            ),
            remap = false
    )
    private IBlockState firmaBridge$nullSafeApply(
            BlockFiller filler,
            IBlockState source, IBlockAccess world, BlockPos pos,
            int x, int y, int z, double density, Random random, int minY) {
        IBlockState result = filler.apply(source, world, pos, x, y, z, density, random, minY);
        if (result == null) {
            if (loggedOnce.compareAndSet(false, true)) {
                StoneType computed = StoneType.computeStoneType(source, world, pos);
                FirmaBridge.LOGGER.warn(
                        "FirmaBridge: GT worldgen BlockFiller.apply() returned null at {}. " +
                        "source={} computeStoneType={} — " +
                        "This stone type is missing from the ore's blockStateMap. " +
                        "Subsequent identical warnings suppressed.",
                        pos,
                        source == null ? "null" : source.getBlock().getRegistryName() + "[" + source + "]",
                        computed == null ? "null(no TFC type registered)" : computed.name);
            }
            return source;
        }
        return result;
    }

}
