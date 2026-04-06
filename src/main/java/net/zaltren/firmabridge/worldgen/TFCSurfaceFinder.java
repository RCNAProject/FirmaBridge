package net.zaltren.firmabridge.worldgen;

import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.objects.blocks.BlockPeatGrass;
import net.dries007.tfc.objects.blocks.stone.BlockRockVariant;
import net.dries007.tfc.world.classic.biomes.BiomesTFC;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Utility for locating valid placement surfaces in TFC terrain.
 *
 * TFC uses rock-typed variants for all surface blocks (grass, sand, gravel, raw rock, etc.)
 * and a climate system layered on top of terrain biomes. This finder handles both correctly,
 * letting structure and mob spawn systems ask "is this a valid surface, and what kind is it?"
 * without duplicating terrain-reading logic in every consumer.
 *
 * Usage:
 *   BlockPos surface = TFCSurfaceFinder.findSurface(world, x, z);
 *   if (surface != null && TFCSurfaceFinder.getSurfaceType(world, surface) == SurfaceType.GRASSLAND) {
 *       // place structure here
 *   }
 */
public class TFCSurfaceFinder {

    /**
     * Rock.Type values that represent naturally generated, walkable surfaces.
     * Non-natural types (SMOOTH, COBBLE, BRICKS, FARMLAND, PATH, ANVIL, SPIKE)
     * indicate player-modified or underground positions and are excluded.
     */
    private static final Set<Rock.Type> NATURAL_SURFACE_TYPES;

    static {
        Set<Rock.Type> types = EnumSet.noneOf(Rock.Type.class);
        types.add(Rock.Type.GRASS);
        types.add(Rock.Type.DRY_GRASS);
        types.add(Rock.Type.CLAY_GRASS);
        types.add(Rock.Type.DIRT);
        types.add(Rock.Type.CLAY);
        types.add(Rock.Type.SAND);
        types.add(Rock.Type.GRAVEL);
        types.add(Rock.Type.RAW);
        NATURAL_SURFACE_TYPES = Collections.unmodifiableSet(types);
    }

    /**
     * Classifies a surface position for use by structure placement and mob spawn systems.
     *
     * GRASSLAND  — temperate grass, clay-grass, dirt (most common land surface)
     * ARID       — dry grass (hot/dry climate zones)
     * MOUNTAIN   — bare rock surface (high altitude, cliff tops)
     * SANDY      — sand surface (beaches, arid areas)
     * GRAVELLY   — gravel surface (gravel beaches, riverbanks)
     * SWAMP      — peat/peat-grass (swampland biome)
     * WATER      — ocean, river, lake — not valid for land placement
     * INVALID    — player-modified, underground, or unresolvable
     */
    public enum SurfaceType {
        GRASSLAND,
        ARID,
        MOUNTAIN,
        SANDY,
        GRAVELLY,
        SWAMP,
        WATER,
        INVALID
    }

    /**
     * Finds the topmost natural TFC surface block at (x, z).
     * Scans downward from the world heightmap.
     *
     * Returns null if:
     * - The position is in an oceanic, river, or lake biome
     * - The top solid block is water or lava
     * - The top solid block is a non-natural TFC type (player-modified area)
     */
    @Nullable
    public static BlockPos findSurface(World world, int x, int z) {
        Biome biome = world.getBiome(new BlockPos(x, 0, z));

        // Reject water biomes immediately — no land surface here
        if (BiomesTFC.isOceanicBiome(biome) || BiomesTFC.isRiverBiome(biome) || BiomesTFC.isLakeBiome(biome)) {
            return null;
        }

        // world.getHeight returns the y of the highest solid block + 1 in 1.12.2
        int startY = world.getHeight(x, z);

        for (int y = startY; y > 0; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            Material material = state.getMaterial();

            // Skip non-solid blocks: air, plants, snow layers, leaves, etc.
            if (!material.isSolid()) continue;

            // Liquid surface → invalid placement
            if (material == Material.WATER || material == Material.LAVA) return null;

            // TFC rock variant — check if it's a natural surface type
            if (block instanceof BlockRockVariant) {
                Rock.Type type = ((BlockRockVariant) block).getType();
                return NATURAL_SURFACE_TYPES.contains(type) ? pos : null;
            }

            // TFC peat grass — natural swamp surface
            if (block instanceof BlockPeatGrass) return pos;

            // Any other solid block (vanilla or third-party) — treat as valid surface
            return pos;
        }

        return null;
    }

    /**
     * Returns the SurfaceType at the given block position.
     * The pos should be the actual surface block (e.g. obtained from findSurface).
     */
    public static SurfaceType getSurfaceType(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Material material = state.getMaterial();

        if (material == Material.WATER || material == Material.LAVA) return SurfaceType.WATER;

        if (block instanceof BlockPeatGrass) return SurfaceType.SWAMP;

        if (block instanceof BlockRockVariant) {
            Rock.Type type = ((BlockRockVariant) block).getType();
            switch (type) {
                case GRASS:
                case CLAY_GRASS:
                case DIRT:
                case CLAY:
                    return SurfaceType.GRASSLAND;
                case DRY_GRASS:
                    return SurfaceType.ARID;
                case RAW:
                    return SurfaceType.MOUNTAIN;
                case SAND:
                    return SurfaceType.SANDY;
                case GRAVEL:
                    return SurfaceType.GRAVELLY;
                default:
                    return SurfaceType.INVALID; // SMOOTH, COBBLE, BRICKS, etc.
            }
        }

        // Non-TFC block: use biome to classify
        Biome biome = world.getBiome(pos);
        if (BiomesTFC.isOceanicBiome(biome) || BiomesTFC.isRiverBiome(biome) || BiomesTFC.isLakeBiome(biome)) {
            return SurfaceType.WATER;
        }
        if (BiomesTFC.isMountainBiome(biome)) return SurfaceType.MOUNTAIN;
        if (biome == BiomesTFC.SWAMPLAND) return SurfaceType.SWAMP;

        return SurfaceType.GRASSLAND; // default for unrecognized solid blocks on land
    }

    /**
     * Returns true if the position is a valid natural land surface suitable for placement.
     * Excludes water, lava, and player-modified surfaces.
     */
    public static boolean isValidLandSurface(World world, BlockPos pos) {
        SurfaceType type = getSurfaceType(world, pos);
        return type != SurfaceType.WATER && type != SurfaceType.INVALID;
    }

    /**
     * Returns true if the position is in a beach biome (BEACH or GRAVEL_BEACH).
     * Useful for filtering out beach areas from inland structure placement.
     */
    public static boolean isBeach(World world, BlockPos pos) {
        return BiomesTFC.isBeachBiome(world.getBiome(pos));
    }

    /**
     * Returns true if the position is on a mountain biome or has a bare rock surface.
     */
    public static boolean isMountain(World world, BlockPos pos) {
        return getSurfaceType(world, pos) == SurfaceType.MOUNTAIN
                || BiomesTFC.isMountainBiome(world.getBiome(pos));
    }

}
