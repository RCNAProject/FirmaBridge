package net.zaltren.firmabridge.worldgen;

import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.objects.blocks.stone.BlockRockVariant;
import net.dries007.tfc.util.climate.ClimateTFC;
import net.dries007.tfc.world.classic.chunkdata.ChunkDataTFC;
import net.dries007.tfc.world.classic.biomes.BiomesTFC;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * Finds valid placement positions for structures in TFC terrain.
 *
 * Two placement modes:
 *
 *   SURFACE — finds a flat natural land surface within a chunk.
 *     Checks: surface type, footprint flatness, climate conditions, biome validity.
 *
 *   UNDERGROUND — finds a solid-rock position at a target depth.
 *     Checks: rock type (if specified), climate conditions.
 *     The caller is responsible for carving space — this returns a position
 *     inside solid TFC rock at the requested depth range.
 *
 * Typical usage inside an IWorldGenerator:
 *
 *   BlockPos pos = StructurePlacementFinder.find(world, rand, chunkX, chunkZ, entry);
 *   if (pos != null) {
 *       myStructure.generate(world, rand, pos);
 *   }
 */
public class StructurePlacementFinder {

    // Number of candidate positions tried per chunk before giving up
    private static final int MAX_ATTEMPTS = 8;

    /**
     * Finds a valid placement position for the given entry within the specified chunk.
     * Delegates to findSurface or findUnderground based on entry.placementType.
     * Returns null if no valid position is found after MAX_ATTEMPTS tries.
     */
    @Nullable
    public static BlockPos find(World world, Random rand, int chunkX, int chunkZ,
                                 StructurePlacementEntry entry) {
        switch (entry.placementType) {
            case SURFACE:     return findSurface(world, rand, chunkX, chunkZ, entry);
            case UNDERGROUND: return findUnderground(world, rand, chunkX, chunkZ, entry);
            default:          return null;
        }
    }

    // -------------------------------------------------------------------------
    // Surface placement
    // -------------------------------------------------------------------------

    /**
     * Finds a flat natural surface position within the chunk.
     * Checks surface type, footprint flatness, climate, and biome validity.
     */
    @Nullable
    private static BlockPos findSurface(World world, Random rand, int chunkX, int chunkZ,
                                         StructurePlacementEntry entry) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Pick a random position within the chunk, offset from edges by footprint radius
            int padding = entry.footprintRadius + 1;
            int x = baseX + padding + rand.nextInt(Math.max(1, 16 - padding * 2));
            int z = baseZ + padding + rand.nextInt(Math.max(1, 16 - padding * 2));

            // Find the surface block at this position
            BlockPos center = TFCSurfaceFinder.findSurface(world, x, z);
            if (center == null) continue;

            // Check surface type
            if (entry.validSurfaces != null) {
                TFCSurfaceFinder.SurfaceType surfaceType = TFCSurfaceFinder.getSurfaceType(world, center);
                if (!entry.validSurfaces.contains(surfaceType)) continue;
            }

            // Exclude beach biomes if requested
            if (entry.excludeBeach && BiomesTFC.isBeachBiome(world.getBiome(center))) continue;

            // Check footprint flatness
            if (!isSurfaceFlat(world, center, entry.footprintRadius, entry.maxHeightVariation)) continue;

            // Check climate conditions
            if (!meetsClimateConditions(world, center, entry)) continue;

            return center;
        }

        return null;
    }

    /**
     * Checks that the surface height variation across the footprint is within the allowed limit.
     * Samples corners and midpoints of the footprint area.
     */
    private static boolean isSurfaceFlat(World world, BlockPos center, int radius, int maxVariation) {
        int minY = center.getY();
        int maxY = center.getY();

        // Sample corners and edge midpoints of the footprint
        int[] offsets = { -radius, 0, radius };
        for (int dx : offsets) {
            for (int dz : offsets) {
                if (dx == 0 && dz == 0) continue; // center already known
                BlockPos sample = TFCSurfaceFinder.findSurface(world, center.getX() + dx, center.getZ() + dz);
                if (sample == null) return false; // edge fell into water/void
                if (sample.getY() < minY) minY = sample.getY();
                if (sample.getY() > maxY) maxY = sample.getY();
            }
        }

        return (maxY - minY) <= maxVariation;
    }

    // -------------------------------------------------------------------------
    // Underground placement
    // -------------------------------------------------------------------------

    /**
     * Finds a position inside solid TFC rock at the target depth range.
     * Checks rock type (if specified) and climate conditions.
     */
    @Nullable
    private static BlockPos findUnderground(World world, Random rand, int chunkX, int chunkZ,
                                             StructurePlacementEntry entry) {
        int baseX = chunkX * 16;
        int baseZ = chunkZ * 16;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int x = baseX + rand.nextInt(16);
            int z = baseZ + rand.nextInt(16);
            int y = entry.minY + rand.nextInt(Math.max(1, entry.maxY - entry.minY));

            BlockPos pos = new BlockPos(x, y, z);

            // Must be inside solid TFC raw rock
            IBlockState state = world.getBlockState(pos);
            if (!(state.getBlock() instanceof BlockRockVariant)) continue;
            if (((BlockRockVariant) state.getBlock()).getType() != Rock.Type.RAW) continue;

            // Check rock type if specified
            if (entry.validRocks != null) {
                Rock rock = ChunkDataTFC.getRockHeight(world, pos);
                if (rock == null || !entry.validRocks.contains(rock)) continue;
            }

            // Check climate conditions
            if (!meetsClimateConditions(world, pos, entry)) continue;

            return pos;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    private static boolean meetsClimateConditions(World world, BlockPos pos, StructurePlacementEntry entry) {
        if (entry.minTemp > -Float.MAX_VALUE || entry.maxTemp < Float.MAX_VALUE) {
            float temp = ClimateTFC.getActualTemp(world, pos);
            if (temp < entry.minTemp || temp > entry.maxTemp) return false;
        }
        if (entry.minRainfall > 0f || entry.maxRainfall < Float.MAX_VALUE) {
            float rainfall = ClimateTFC.getRainfall(world, pos);
            if (rainfall < entry.minRainfall || rainfall > entry.maxRainfall) return false;
        }
        return true;
    }

}
