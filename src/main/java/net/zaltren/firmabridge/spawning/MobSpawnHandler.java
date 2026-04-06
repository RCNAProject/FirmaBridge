package net.zaltren.firmabridge.spawning;

import net.dries007.tfc.util.climate.ClimateTFC;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.zaltren.firmabridge.worldgen.TFCSurfaceFinder;

/**
 * Validates TFC-specific spawn conditions for mobs registered via MobSpawnRegistry.
 *
 * When a mob registered in MobSpawnRegistry attempts to spawn, this handler checks:
 *   1. Surface type — is the spawn block the right surface type?
 *   2. Temperature — is the TFC temperature within the allowed range?
 *   3. Rainfall — is the TFC rainfall within the allowed range?
 *
 * If any check fails the spawn is denied. Mobs not registered in MobSpawnRegistry
 * are not affected — this handler only intervenes for registered entries.
 */
public class MobSpawnHandler {

    @SubscribeEvent
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!(event.getEntity() instanceof EntityLiving)) return;

        EntityLiving entity = (EntityLiving) event.getEntity();
        ResourceLocation entityId = EntityRegistry.getEntry(entity.getClass()) != null
                ? EntityRegistry.getEntry(entity.getClass()).getRegistryName()
                : null;

        if (entityId == null) return;

        // Find if this entity has a registered FirmaBridge spawn entry
        MobSpawnEntry entry = findEntry(entityId);
        if (entry == null) return; // not our mob, don't interfere

        World world = event.getWorld();
        BlockPos pos = new BlockPos(event.getX(), event.getY(), event.getZ());

        // 1. Surface type check
        if (entry.validSurfaces != null) {
            BlockPos surface = TFCSurfaceFinder.findSurface(world, pos.getX(), pos.getZ());
            if (surface == null) {
                event.setResult(Event.Result.DENY);
                return;
            }
            TFCSurfaceFinder.SurfaceType surfaceType = TFCSurfaceFinder.getSurfaceType(world, surface);
            if (!entry.validSurfaces.contains(surfaceType)) {
                event.setResult(Event.Result.DENY);
                return;
            }
        }

        // 2. Temperature check
        if (entry.minTemp > -Float.MAX_VALUE || entry.maxTemp < Float.MAX_VALUE) {
            float temp = ClimateTFC.getActualTemp(world, pos);
            if (temp < entry.minTemp || temp > entry.maxTemp) {
                event.setResult(Event.Result.DENY);
                return;
            }
        }

        // 3. Rainfall check
        if (entry.minRainfall > 0f || entry.maxRainfall < Float.MAX_VALUE) {
            float rainfall = ClimateTFC.getRainfall(world, pos);
            if (rainfall < entry.minRainfall || rainfall > entry.maxRainfall) {
                event.setResult(Event.Result.DENY);
                return;
            }
        }

        // All checks passed — allow the spawn
        event.setResult(Event.Result.ALLOW);
    }

    private static MobSpawnEntry findEntry(ResourceLocation entityId) {
        for (MobSpawnEntry entry : MobSpawnRegistry.getEntries()) {
            if (entry.entityId.equals(entityId)) return entry;
        }
        return null;
    }

}
