package net.zaltren.firmabridge.mixin;

import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.fml.common.Loader;
import net.zaltren.firmabridge.FirmaBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Patches MapGenVillage to allow village generation in TFC worlds.
 *
 * canSpawnStructureAtCoords() calls areBiomesViable() against the static
 * VILLAGE_SPAWN_BIOMES (an unmodifiable list). TFC biomes are not in that list,
 * so the check always fails and no villages spawn.
 *
 * Strategy: @Inject at RETURN of canSpawnStructureAtCoords. When the method
 * returns false AND TFC is loaded, re-run the vanilla grid-position math to
 * distinguish a grid miss (keep false) from a biome miss (set true for TFC).
 * Village spacing is preserved; only the biome gate is bypassed.
 *
 * `world` is declared in MapGenBase (grandparent), so @Shadow cannot resolve it
 * via refmap. It is retrieved via reflection instead.
 * `distance` and `minTownSeparation` are declared directly in MapGenVillage,
 * so @Shadow resolves them normally.
 *
 * TFC's ChunkProviderTFC never calls MapGenVillage.generateStructure(), so this
 * mixin alone is not enough — VillageWorldGen registers an IWorldGenerator that
 * drives generation.
 */
@Mixin(MapGenVillage.class)
public abstract class MapGenVillageMixin {

    @Shadow private int distance;
    @Shadow private int minTownSeparation;

    /** Cached reflection handle for MapGenBase.world (declared two levels up). */
    private static final Field WORLD_FIELD;
    static {
        Field f = null;
        try {
            f = MapGenBase.class.getDeclaredField("world");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            FirmaBridge.LOGGER.error("MapGenVillageMixin: could not locate MapGenBase.world via reflection — village bypass disabled.", e);
        }
        WORLD_FIELD = f;
    }

    @Inject(method = "canSpawnStructureAtCoords", at = @At("RETURN"), cancellable = true)
    private void firmabridge$allowTFCTerrain(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || !Loader.isModLoaded("tfc")) return;
        if (WORLD_FIELD == null) return;

        World world;
        try {
            world = (World) WORLD_FIELD.get(this);
        } catch (IllegalAccessException e) {
            return;
        }
        if (world == null) return;

        // Re-run the vanilla grid-position check (same math as canSpawnStructureAtCoords)
        // so we only allow positions that passed the grid check but failed the biome check.
        int i = chunkX;
        int j = chunkZ;
        if (chunkX < 0) i -= this.distance - 1;
        if (chunkZ < 0) j -= this.distance - 1;

        int k = i / this.distance;
        int l = j / this.distance;

        Random random = world.setRandomSeed(k, l, 10387312);
        k = k * this.distance;
        l = l * this.distance;
        k = k + random.nextInt(this.distance - 8);
        l = l + random.nextInt(this.distance - 8);

        if (chunkX == k && chunkZ == l) {
            FirmaBridge.LOGGER.debug("MapGenVillageMixin: allowing village at grid chunk [{}, {}]", chunkX, chunkZ);
            cir.setReturnValue(true);
        }
    }
}
