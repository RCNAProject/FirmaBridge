package net.zaltren.firmabridge.worldgen;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.config.FirmaBridgeConfig;

import java.util.Random;
import java.util.WeakHashMap;

/**
 * Drives vanilla village generation in TFC worlds.
 *
 * TFC's ChunkProviderTFC does not call MapGenVillage.generateStructure(), so
 * vanilla villages never spawn in TFC worlds. This IWorldGenerator fills that
 * gap by calling generateStructure() per chunk.
 *
 * MapGenVillageMixin patches MapGenVillage's biomeList so TFC land biomes pass
 * the vanilla biome check. Together the two components give vanilla village
 * structures on TFC terrain as a proof-of-concept baseline.
 *
 * One MapGenVillage instance is kept per World. WeakHashMap ensures the world
 * can be garbage collected when unloaded.
 *
 * Only runs in dimension 0 (overworld). Controlled by FirmaBridgeConfig.enableVillages.
 */
public class VillageWorldGen implements IWorldGenerator {

    private final WeakHashMap<World, MapGenVillage> generators = new WeakHashMap<>();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!FirmaBridgeConfig.enableVillages) return;
        if (world.provider.getDimension() != 0) return;

        MapGenVillage gen = generators.computeIfAbsent(world, w -> new MapGenVillage());
        // Populate structureMap by running the vanilla structure-scan pass for nearby chunks.
        // TFC's ChunkProviderTFC never calls MapGenVillage.generate(), so structureMap would
        // otherwise remain empty and generateStructure() would always return false.
        // ChunkPrimer is not used by MapGenStructure.recursiveGenerate, so null is safe.
        gen.generate(world, chunkX, chunkZ, null);
        boolean placed = gen.generateStructure(world, random, new ChunkPos(chunkX, chunkZ));
        if (placed) {
            FirmaBridge.LOGGER.info("VillageWorldGen: village structure placed at chunk [{}, {}].", chunkX, chunkZ);
        }
    }
}
