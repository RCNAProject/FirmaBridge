package net.zaltren.firmabridge.compat;

import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Metal;
import net.dries007.tfc.objects.items.metal.ItemMetal;
import net.dries007.tfc.types.DefaultMetals;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.zaltren.firmabridge.FirmaBridge;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers TFC metal items into GT CEu OreDict entries so GT machines
 * accept TFC material inputs wherever OreDict is used.
 *
 * Covers: ingots, nuggets, dusts, and sheets (→ plates).
 * Called during init phase, after both TFC and GT have registered their items.
 */
public class MaterialBridgeHandler {

    // TFC metal ResourceLocation → GT OreDict name suffix
    // e.g. COPPER → "Copper" produces "ingotCopper", "nuggetCopper", etc.
    private static final Map<ResourceLocation, String> METAL_OREDICT_NAMES =
            new LinkedHashMap<ResourceLocation, String>();

    static {
        // Base metals
        METAL_OREDICT_NAMES.put(DefaultMetals.COPPER,          "Copper");
        METAL_OREDICT_NAMES.put(DefaultMetals.TIN,             "Tin");
        METAL_OREDICT_NAMES.put(DefaultMetals.GOLD,            "Gold");
        METAL_OREDICT_NAMES.put(DefaultMetals.SILVER,          "Silver");
        METAL_OREDICT_NAMES.put(DefaultMetals.LEAD,            "Lead");
        METAL_OREDICT_NAMES.put(DefaultMetals.NICKEL,          "Nickel");
        METAL_OREDICT_NAMES.put(DefaultMetals.BISMUTH,         "Bismuth");
        METAL_OREDICT_NAMES.put(DefaultMetals.ZINC,            "Zinc");
        METAL_OREDICT_NAMES.put(DefaultMetals.PLATINUM,        "Platinum");

        // Iron progression
        METAL_OREDICT_NAMES.put(DefaultMetals.PIG_IRON,        "PigIron");
        METAL_OREDICT_NAMES.put(DefaultMetals.WROUGHT_IRON,    "Iron");     // TFC wrought iron = GT iron

        // Steel progression — key progression handoff point
        METAL_OREDICT_NAMES.put(DefaultMetals.STEEL,           "Steel");
        METAL_OREDICT_NAMES.put(DefaultMetals.BLACK_STEEL,     "BlackSteel");
        METAL_OREDICT_NAMES.put(DefaultMetals.BLUE_STEEL,      "BlueSteel");
        METAL_OREDICT_NAMES.put(DefaultMetals.RED_STEEL,       "RedSteel");

        // Alloys
        METAL_OREDICT_NAMES.put(DefaultMetals.BRONZE,          "Bronze");
        METAL_OREDICT_NAMES.put(DefaultMetals.BISMUTH_BRONZE,  "BismuthBronze");
        METAL_OREDICT_NAMES.put(DefaultMetals.BLACK_BRONZE,    "BlackBronze");
        METAL_OREDICT_NAMES.put(DefaultMetals.BRASS,           "Brass");
        METAL_OREDICT_NAMES.put(DefaultMetals.ROSE_GOLD,       "RoseGold");
        METAL_OREDICT_NAMES.put(DefaultMetals.STERLING_SILVER, "SterlingSilver");
    }

    public static void register() {
        int registered = 0;

        for (Map.Entry<ResourceLocation, String> entry : METAL_OREDICT_NAMES.entrySet()) {
            Metal metal = TFCRegistries.METALS.getValue(entry.getKey());
            if (metal == null) {
                FirmaBridge.LOGGER.warn("MaterialBridgeHandler: TFC metal not found: {}", entry.getKey());
                continue;
            }

            String suffix = entry.getValue();

            registered += registerItemType(metal, Metal.ItemType.INGOT,  "ingot"  + suffix);
            registered += registerItemType(metal, Metal.ItemType.NUGGET, "nugget" + suffix);
            registered += registerItemType(metal, Metal.ItemType.DUST,   "dust"   + suffix);
            registered += registerItemType(metal, Metal.ItemType.SHEET,  "plate"  + suffix); // TFC sheet = GT plate
        }

        FirmaBridge.LOGGER.info("MaterialBridgeHandler: registered {} TFC→GT OreDict entries.", registered);
    }

    private static int registerItemType(Metal metal, Metal.ItemType type, String oreName) {
        Item item = ItemMetal.get(metal, type);
        if (item == null) return 0;
        OreDictionary.registerOre(oreName, new ItemStack(item));
        return 1;
    }

}