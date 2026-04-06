package net.zaltren.firmabridge.worldgen;

import net.dries007.tfc.api.types.Rock;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes the TFC-aware placement requirements for a structure.
 *
 * Use StructurePlacementEntry.surface() or StructurePlacementEntry.underground()
 * to start building an entry:
 *
 *   // Surface structure (ruins, altars, camps)
 *   StructurePlacementEntry entry = StructurePlacementEntry.surface()
 *       .footprint(7, 7)
 *       .maxHeightVariation(2)
 *       .surfaces(SurfaceType.GRASSLAND, SurfaceType.ARID)
 *       .temperature(-5f, 30f)
 *       .rarity(40)
 *       .build();
 *
 *   // Underground structure (dungeons, cavern rooms)
 *   StructurePlacementEntry entry = StructurePlacementEntry.underground()
 *       .depthRange(20, 80)
 *       .rocks(rock1, rock2)
 *       .rarity(60)
 *       .build();
 */
public class StructurePlacementEntry {

    public enum PlacementType { SURFACE, UNDERGROUND }

    /** Surface or underground placement. */
    public final PlacementType placementType;

    // --- Surface fields ---

    /** Half-width of the footprint in blocks (checks this many blocks in each direction from center). */
    public final int footprintRadius;

    /** Maximum allowed height difference in blocks across the footprint. */
    public final int maxHeightVariation;

    /** Required surface types. Null means any surface type is accepted. */
    @Nullable
    public final Set<TFCSurfaceFinder.SurfaceType> validSurfaces;

    /** Exclude beach biomes from surface placement. Default true. */
    public final boolean excludeBeach;

    // --- Underground fields ---

    /** Minimum Y level for underground placement. */
    public final int minY;

    /** Maximum Y level for underground placement. */
    public final int maxY;

    /** Required TFC rock types at the placement position. Null means any rock. */
    @Nullable
    public final Set<Rock> validRocks;

    // --- Shared fields ---

    /** Temperature constraints (TFC Celsius scale, ~-30 to +30). */
    public final float minTemp;
    public final float maxTemp;

    /** Rainfall constraints (mm/year, ~0 to 500). */
    public final float minRainfall;
    public final float maxRainfall;

    /**
     * Chunk rarity — structure attempts to place once every N chunks on average.
     * Higher = rarer. Use with RarityBasedWorldGen or your own IWorldGenerator.
     */
    public final int rarity;

    private StructurePlacementEntry(Builder builder) {
        this.placementType    = builder.placementType;
        this.footprintRadius  = builder.footprintRadius;
        this.maxHeightVariation = builder.maxHeightVariation;
        this.validSurfaces    = builder.validSurfaces;
        this.excludeBeach     = builder.excludeBeach;
        this.minY             = builder.minY;
        this.maxY             = builder.maxY;
        this.validRocks       = builder.validRocks;
        this.minTemp          = builder.minTemp;
        this.maxTemp          = builder.maxTemp;
        this.minRainfall      = builder.minRainfall;
        this.maxRainfall      = builder.maxRainfall;
        this.rarity           = builder.rarity;
    }

    /** Start building a surface placement entry. */
    public static Builder surface() {
        return new Builder(PlacementType.SURFACE);
    }

    /** Start building an underground placement entry. */
    public static Builder underground() {
        return new Builder(PlacementType.UNDERGROUND);
    }

    public static class Builder {

        private final PlacementType placementType;
        private int footprintRadius     = 4;
        private int maxHeightVariation  = 2;
        private Set<TFCSurfaceFinder.SurfaceType> validSurfaces = null;
        private boolean excludeBeach    = true;
        private int minY                = 20;
        private int maxY                = 80;
        private Set<Rock> validRocks    = null;
        private float minTemp           = -Float.MAX_VALUE;
        private float maxTemp           =  Float.MAX_VALUE;
        private float minRainfall       = 0f;
        private float maxRainfall       =  Float.MAX_VALUE;
        private int rarity              = 32;

        private Builder(PlacementType type) {
            this.placementType = type;
        }

        /** Footprint half-size. E.g. radius=4 checks an 8×8 area for flatness. */
        public Builder footprint(int radius) {
            this.footprintRadius = radius;
            return this;
        }

        /** Maximum height difference in blocks across the footprint before rejecting the position. */
        public Builder maxHeightVariation(int variation) {
            this.maxHeightVariation = variation;
            return this;
        }

        /** Restrict to specific TFC surface types. Omit to allow any surface. */
        public Builder surfaces(TFCSurfaceFinder.SurfaceType... types) {
            Set<TFCSurfaceFinder.SurfaceType> set = EnumSet.noneOf(TFCSurfaceFinder.SurfaceType.class);
            set.addAll(Arrays.asList(types));
            this.validSurfaces = Collections.unmodifiableSet(set);
            return this;
        }

        /** Whether to exclude beach biomes from placement. Default true. */
        public Builder excludeBeach(boolean exclude) {
            this.excludeBeach = exclude;
            return this;
        }

        /** Y range for underground placement. */
        public Builder depthRange(int minY, int maxY) {
            this.minY = minY;
            this.maxY = maxY;
            return this;
        }

        /** Restrict underground placement to specific TFC rock types. */
        public Builder rocks(Rock... rocks) {
            this.validRocks = Collections.unmodifiableSet(new HashSet<Rock>(Arrays.asList(rocks)));
            return this;
        }

        /** Temperature range in Celsius (TFC scale: ~-30 to +30). */
        public Builder temperature(float min, float max) {
            this.minTemp = min;
            this.maxTemp = max;
            return this;
        }

        /** Rainfall range in mm/year (TFC scale: ~0 to 500). */
        public Builder rainfall(float min, float max) {
            this.minRainfall = min;
            this.maxRainfall = max;
            return this;
        }

        /** Place once every N chunks on average. */
        public Builder rarity(int rarity) {
            this.rarity = rarity;
            return this;
        }

        public StructurePlacementEntry build() {
            return new StructurePlacementEntry(this);
        }
    }

}
