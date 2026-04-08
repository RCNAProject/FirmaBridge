package net.zaltren.firmabridge.integration;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import net.dries007.tfc.objects.blocks.stone.BlockRockRaw;
import net.dries007.tfc.types.DefaultRocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.zaltren.firmabridge.FirmaBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Registers TFC raw rock blocks as GT stone types so GT ore blocks use
 * TFC stone textures as their background instead of vanilla stone.
 *
 * This must run BEFORE MetaBlocks.init() creates BlockOre instances, which is
 * why it is injected via Mixin at the head of MetaBlocks.init().
 *
 * Stone type suppliers are lazy — TFC blocks are looked up at render/worldgen
 * time, not at registration time, to avoid block registration order issues.
 */
public class TFCStoneTypeHandler {

    private static boolean registered = false;

    // Mapping: TFC rock ResourceLocation -> GT material name -> stone type id
    // All 20 TFC rock types are registered so GT ore worldgen never skips them.
    // Rocks with no direct GT equivalent use the nearest material (affects texture/maceration drops, not ore spawning).
    private static final Object[][] STONE_TYPE_DEFS = {
        // Igneous Intrusive — direct GT equivalents
        { DefaultRocks.GRANITE,      "granite",    12 },
        { DefaultRocks.DIORITE,      "diorite",    13 },
        { DefaultRocks.GABBRO,       "basalt",     20 }, // no GT gabbro; basalt is nearest mafic equivalent
        // Igneous Extrusive
        { DefaultRocks.BASALT,       "basalt",     14 },
        { DefaultRocks.ANDESITE,     "andesite",   15 },
        { DefaultRocks.RHYOLITE,     "granite",    21 }, // no GT rhyolite; granite is nearest felsic equivalent
        { DefaultRocks.DACITE,       "andesite",   22 }, // no GT dacite; andesite is nearest intermediate equivalent
        // Sedimentary
        { DefaultRocks.SHALE,        "stone",      23 },
        { DefaultRocks.CLAYSTONE,    "stone",      24 },
        { DefaultRocks.ROCKSALT,     "rock_salt",  18 },
        { DefaultRocks.LIMESTONE,    "calcite",    25 }, // limestone is CaCO3; calcite is nearest GT equivalent
        { DefaultRocks.CONGLOMERATE, "stone",      26 },
        { DefaultRocks.DOLOMITE,     "calcite",    27 }, // dolomite is CaMg(CO3)2; calcite is nearest GT equivalent
        { DefaultRocks.CHERT,        "flint",      19 },
        { DefaultRocks.CHALK,        "calcite",    28 }, // chalk is CaCO3; calcite is nearest GT equivalent
        // Metamorphic
        { DefaultRocks.QUARTZITE,    "quartzite",  17 },
        { DefaultRocks.SLATE,        "stone",      29 },
        { DefaultRocks.PHYLLITE,     "stone",      30 },
        { DefaultRocks.SCHIST,       "stone",      31 },
        { DefaultRocks.GNEISS,       "stone",      32 },
        { DefaultRocks.MARBLE,       "marble",     16 },
    };

    public static void register() {
        if (registered) return;
        registered = true;

        int count = 0;

        for (Object[] def : STONE_TYPE_DEFS) {
            ResourceLocation rockRL  = (ResourceLocation) def[0];
            String           gtName  = (String)           def[1];
            int              id      = (int)               def[2];

            Material material = GregTechAPI.materialManager.getMaterial(gtName);
            if (material == null) {
                FirmaBridge.LOGGER.warn("TFCStoneTypeHandler: GT material '{}' not found, skipping TFC stone type for {}",
                        gtName, rockRL);
                continue;
            }

            new StoneType(
                    id,
                    "tfc_" + rockRL.getPath(),
                    SoundType.STONE,
                    OrePrefix.ore,
                    material,
                    () -> findTFCRawState(rockRL),
                    state -> isTFCRock(state, rockRL),
                    true
            );

            count++;
        }

        FirmaBridge.LOGGER.info("TFCStoneTypeHandler: registered {} TFC stone types for GT ore rendering.", count);
    }

    private static IBlockState findTFCRawState(ResourceLocation rockRL) {
        for (Block block : Block.REGISTRY) {
            if (block instanceof BlockRockRaw) {
                BlockRockRaw raw = (BlockRockRaw) block;
                if (raw.getRock().getRegistryName().equals(rockRL)) {
                    return raw.getDefaultState();
                }
            }
        }
        FirmaBridge.LOGGER.warn("TFCStoneTypeHandler: could not find TFC raw block for {}, falling back to stone.", rockRL);
        return Blocks.STONE.getDefaultState();
    }

    private static boolean isTFCRock(IBlockState state, ResourceLocation rockRL) {
        if (!(state.getBlock() instanceof BlockRockRaw)) return false;
        return ((BlockRockRaw) state.getBlock()).getRock().getRegistryName().equals(rockRL);
    }

    /**
     * Hides GT ore variants for the given stone type names from the GT creative tab and JEI.
     *
     * GT's BlockOre.getSubBlocks() filters by StoneType.shouldBeDroppedAsItem. Setting it to
     * false causes those variants to be skipped when building creative/JEI item lists.
     *
     * Must be called in postInit, before the player first opens the creative inventory or JEI
     * initializes its ingredient list.
     */
    public static void hideFromCreativeAndJEI(String... stoneTypeNames) {
        Field shouldBeDroppedField;
        try {
            shouldBeDroppedField = StoneType.class.getDeclaredField("shouldBeDroppedAsItem");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(shouldBeDroppedField,
                    shouldBeDroppedField.getModifiers() & ~Modifier.FINAL);
            shouldBeDroppedField.setAccessible(true);
        } catch (Exception e) {
            FirmaBridge.LOGGER.error("TFCStoneTypeHandler: could not access shouldBeDroppedAsItem field: {}", e.getMessage());
            return;
        }

        for (String name : stoneTypeNames) {
            StoneType target = null;
            for (StoneType st : StoneType.STONE_TYPE_REGISTRY) {
                if (name.equals(st.name)) {
                    target = st;
                    break;
                }
            }
            if (target == null) {
                FirmaBridge.LOGGER.warn("TFCStoneTypeHandler: stone type '{}' not found in registry, skipping hide.", name);
                continue;
            }
            try {
                shouldBeDroppedField.setBoolean(target, false);
                FirmaBridge.LOGGER.info("TFCStoneTypeHandler: hid '{}' GT ore variants from creative/JEI.", name);
            } catch (Exception e) {
                FirmaBridge.LOGGER.warn("TFCStoneTypeHandler: failed to hide '{}': {}", name, e.getMessage());
            }
        }
    }

}
