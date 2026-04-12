package net.zaltren.firmabridge.materials;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.MaterialRegistryEvent;
import gregtech.api.unification.material.info.MaterialIconSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.zaltren.firmabridge.FirmaBridge;
import net.zaltren.firmabridge.Tags;

/**
 * Registers custom GT CEu materials for TFC metals and minerals that have no
 * GT equivalent, enabling full GT machine processing for TFC-specific content.
 *
 * Materials are registered during GT's MaterialEvent (fires in GT preInit),
 * so they are available for GT's auto-recipe generation and OreDict output.
 *
 * Material IDs 24001–24006 are used (far above GT's own range, no conflicts).
 *
 * Chemical compositions are set so GT centrifuge/electrolyzer recipes are
 * auto-generated for each material dust, giving players meaningful byproducts.
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class FirmaBridgeMaterials {

    // Public references — available after MaterialEvent fires (GT preInit).
    public static Material PIG_IRON;
    public static Material LIGNITE;
    public static Material KAOLINITE;
    public static Material SYLVITE;
    public static Material CRYOLITE;
    public static Material SERPENTINE;

    @SubscribeEvent
    public static void createRegistry(MaterialRegistryEvent event) {
        GregTechAPI.materialManager.createRegistry(Tags.MOD_ID);
    }

    @SubscribeEvent
    public static void registerMaterials(MaterialEvent event) {
        // --- Iron family ---

        // TFC intermediate iron: iron ore → pig iron → (work/process) → wrought iron.
        // Fe₉C₁ composition — centrifuge yields iron + carbon (slight coke byproduct).
        PIG_IRON = new Material.Builder(24001, new ResourceLocation(Tags.MOD_ID, "pig_iron"))
                .ingot()
                .dust()
                .color(0xC0A080)
                .iconSet(MaterialIconSet.DULL)
                .components(Materials.Iron, 9, Materials.Carbon, 1)
                .build();

        // --- Coal family ---

        // TFC brown coal — lower grade than bituminous coal, higher than peat.
        // Half the burn value of coal. Centrifuge → carbon dust.
        LIGNITE = new Material.Builder(24002, new ResourceLocation(Tags.MOD_ID, "lignite"))
                .gem()
                .dust()
                .color(0x5C4033)
                .iconSet(MaterialIconSet.DULL)
                .burnTime(800)
                .components(Materials.Carbon, 1)
                .build();

        // --- Clay / aluminium silicate family ---

        // TFC kaolin clay ore. Al₂Si₂O₅ — centrifuge yields aluminium + silicon + oxygen,
        // giving players an aluminium source without bauxite (which TFC doesn't have).
        KAOLINITE = new Material.Builder(24003, new ResourceLocation(Tags.MOD_ID, "kaolinite"))
                .dust()
                .color(0xF5F0E8)
                .iconSet(MaterialIconSet.ROUGH)
                .components(Materials.Aluminium, 2, Materials.Silicon, 2, Materials.Oxygen, 5)
                .build();

        // --- Evaporite / halide minerals ---

        // TFC sylvite (KCl) — potassium chloride evaporite crystal.
        // KCl composition — electrolyzer yields potassium + chlorine gas.
        SYLVITE = new Material.Builder(24004, new ResourceLocation(Tags.MOD_ID, "sylvite"))
                .gem()
                .dust()
                .color(0xE8D0C8)
                .iconSet(MaterialIconSet.DULL)
                .components(Materials.Potassium, 1, Materials.Chlorine, 1)
                .build();

        // TFC cryolite (Na₃AlF₆) — sodium aluminium fluoride, historically used as
        // aluminium smelting flux. Na₃AlF₆ — electrolyzer yields sodium + aluminium + fluorine.
        CRYOLITE = new Material.Builder(24005, new ResourceLocation(Tags.MOD_ID, "cryolite"))
                .gem()
                .dust()
                .color(0xE8F0F8)
                .iconSet(MaterialIconSet.DULL)
                .components(Materials.Sodium, 3, Materials.Aluminium, 1, Materials.Fluorine, 6)
                .build();

        // --- Magnesium silicate family ---

        // TFC serpentine (Mg₃Si₂O₅) — altered ultramafic mineral, dark green.
        // Mg₃Si₂O₅ — centrifuge yields magnesium + silicon + oxygen.
        SERPENTINE = new Material.Builder(24006, new ResourceLocation(Tags.MOD_ID, "serpentine"))
                .dust()
                .color(0x4A7A50)
                .iconSet(MaterialIconSet.ROUGH)
                .components(Materials.Magnesium, 3, Materials.Silicon, 2, Materials.Oxygen, 5)
                .build();

        FirmaBridge.LOGGER.info("FirmaBridgeMaterials: registered 6 custom GT materials for TFC-specific content.");
    }
}
