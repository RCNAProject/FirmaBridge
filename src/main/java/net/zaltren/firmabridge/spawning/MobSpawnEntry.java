package net.zaltren.firmabridge.spawning;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.zaltren.firmabridge.worldgen.TFCSurfaceFinder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Describes the TFC-aware spawn conditions for a single entity type.
 *
 * Use MobSpawnEntry.Builder to construct instances:
 *   new MobSpawnEntry.Builder(new ResourceLocation("modid:entity"), EnumCreatureType.CREATURE)
 *       .surfaces(SurfaceType.GRASSLAND, SurfaceType.ARID)
 *       .temperature(-5f, 30f)
 *       .rainfall(100f, 500f)
 *       .group(4, 8)
 *       .weight(10)
 *       .build();
 */
public class MobSpawnEntry {

    /** Entity registry name. */
    public final ResourceLocation entityId;

    /** Forge creature type (MONSTER, CREATURE, AMBIENT, WATER_CREATURE). */
    public final EnumCreatureType creatureType;

    /**
     * Valid surface types for spawning. Null means any surface type is accepted.
     * Uses TFCSurfaceFinder.SurfaceType.
     */
    @Nullable
    public final Set<TFCSurfaceFinder.SurfaceType> validSurfaces;

    /** Minimum temperature in °C (TFC scale). Default: Float.MIN_VALUE (no minimum). */
    public final float minTemp;

    /** Maximum temperature in °C (TFC scale). Default: Float.MAX_VALUE (no maximum). */
    public final float maxTemp;

    /** Minimum annual rainfall in mm. Default: 0. */
    public final float minRainfall;

    /** Maximum annual rainfall in mm. Default: Float.MAX_VALUE (no maximum). */
    public final float maxRainfall;

    /** Spawn weight — higher = more common relative to other mobs. */
    public final int weight;

    /** Minimum group size per spawn attempt. */
    public final int minGroupSize;

    /** Maximum group size per spawn attempt. */
    public final int maxGroupSize;

    private MobSpawnEntry(Builder builder) {
        this.entityId      = builder.entityId;
        this.creatureType  = builder.creatureType;
        this.validSurfaces = builder.validSurfaces;
        this.minTemp       = builder.minTemp;
        this.maxTemp       = builder.maxTemp;
        this.minRainfall   = builder.minRainfall;
        this.maxRainfall   = builder.maxRainfall;
        this.weight        = builder.weight;
        this.minGroupSize  = builder.minGroupSize;
        this.maxGroupSize  = builder.maxGroupSize;
    }

    public static class Builder {

        private final ResourceLocation entityId;
        private final EnumCreatureType creatureType;
        private Set<TFCSurfaceFinder.SurfaceType> validSurfaces = null;
        private float minTemp       = -Float.MAX_VALUE;
        private float maxTemp       =  Float.MAX_VALUE;
        private float minRainfall   = 0f;
        private float maxRainfall   =  Float.MAX_VALUE;
        private int   weight        = 10;
        private int   minGroupSize  = 1;
        private int   maxGroupSize  = 4;

        public Builder(ResourceLocation entityId, EnumCreatureType creatureType) {
            this.entityId     = entityId;
            this.creatureType = creatureType;
        }

        /** Restrict spawning to specific TFC surface types. Omit to allow any surface. */
        public Builder surfaces(TFCSurfaceFinder.SurfaceType... types) {
            Set<TFCSurfaceFinder.SurfaceType> set = EnumSet.noneOf(TFCSurfaceFinder.SurfaceType.class);
            set.addAll(Arrays.asList(types));
            this.validSurfaces = Collections.unmodifiableSet(set);
            return this;
        }

        /** Temperature range in °C (TFC scale: roughly -30 to +30). */
        public Builder temperature(float min, float max) {
            this.minTemp = min;
            this.maxTemp = max;
            return this;
        }

        /** Annual rainfall range in mm (TFC scale: roughly 0 to 500). */
        public Builder rainfall(float min, float max) {
            this.minRainfall = min;
            this.maxRainfall = max;
            return this;
        }

        /** Spawn group size range. */
        public Builder group(int min, int max) {
            this.minGroupSize = min;
            this.maxGroupSize = max;
            return this;
        }

        /** Spawn weight (higher = more frequent relative to other mobs). */
        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public MobSpawnEntry build() {
            return new MobSpawnEntry(this);
        }
    }

}
