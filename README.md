<h1 align="center">FirmaBridge</h1>
<p align="center">
  A full integration layer between <b>TerraFirmaCraft</b> and <b>GregTech CEu</b> for Minecraft 1.12.2.
</p>
<p align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.12.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-14.23.5.2847-orange)
![Java](https://img.shields.io/badge/Java-25-blue)
![License](https://img.shields.io/badge/License-MIT-green)
[![CurseForge](https://img.shields.io/badge/CurseForge-FirmaBridge-orange?logo=curseforge)](https://legacy.curseforge.com/minecraft/mc-mods/firmabridge)

</p>

---

## Overview

FirmaBridge makes TerraFirmaCraft and GregTech CEu work together as a unified progression system.

TFC replaces vanilla ore generation, biomes, and materials with its own systems. GT adds an extensive material and machine recipe ecosystem built on top of vanilla assumptions. Without a bridge, the two mods largely ignore each other — GT veins don't generate in TFC rock, TFC ores have no GT processing recipes, and TFC metals have no GT material equivalents.

FirmaBridge solves this by:

- Patching GT ore vein generation to work inside TFC's geological world
- Applying geological weight rules so GT veins spawn in rock types that match their real-world formation
- Registering all 21 TFC stone types as GT StoneTypes so GT ores generate and render correctly in TFC terrain
- Defining new GT materials for TFC-specific minerals with accurate chemical compositions
- Registering TFC metals and alloys into GT's OreDict system
- Adding GT machine recipes that connect TFC and GT material processing chains
- Providing JEI information for TFC raw stone blocks that have GT material mappings

---

## Compatibility

| Mod | Version | Required |
| --- | ------- | -------- |
| TerraFirmaCraft | 1.7.23+ | Yes |
| GregTech CEu | 2.8.10+ | Yes |
| MixinBooter | 10.2+ | Yes |
| Minecraft Forge | 14.23.5.2847 | Yes |

---

## Features

### GT Vein Patcher
GT ore vein definitions target vanilla stone by default. FirmaBridge patches all GT vein definitions at startup to include TFC rock layers, so GT veins generate correctly inside TFC terrain.

### Geological Worldgen
GT ore veins spawn preferentially in the TFC rock types that match their real-world formation environment. A weight system controls how likely each vein is to generate when its center lands in a given rock type.

| Weight | Meaning |
| ------ | ------- |
| `1.0` | Primary habitat — always spawns |
| `0.5` | Secondary habitat — spawns roughly half the time |
| `0.0` | Geologically incompatible — never spawns |
| `0.05` | Default fallback for any unlisted rock — permissive so players are never locked out by local geology |

23 GT veins have explicit geological mappings. Examples:

| Vein | Primary rocks | Notes |
| ---- | ------------- | ----- |
| Magnetite | Gabbro, Basalt | Mafic igneous association |
| Copper | Diorite, Granite | Porphyry copper deposit |
| Galena | Limestone, Dolomite | MVT (Mississippi Valley Type) |
| Coal | Shale, Claystone | Sedimentary |
| Garnet | Schist, Gneiss | Metamorphic |
| Sapphire | Schist, Gneiss, Marble | High-pressure metamorphic |
| Lapis | Marble, Limestone | Contact metamorphic skarn |
| Diamond | Gneiss, Schist | Ultra-deep metamorphic |
| Redstone | Andesite, Rhyolite, Dacite | Volcanic hydrothermal |
| Salts | Rock Salt | Evaporite |

Veins not listed use the default weight for all rocks.

### Rock Registry
Maps all 21 TFC rock types to GT material equivalents. Rocks with no direct GT counterpart use a nearest geological approximation so ore block states exist for every layer.

| TFC Rock | GT Material | Notes |
| -------- | ----------- | ----- |
| Granite | Granite | Direct equivalent |
| Diorite | Diorite | Direct equivalent |
| Basalt | Basalt | Direct equivalent |
| Andesite | Andesite | Direct equivalent |
| Marble | Marble | Direct equivalent |
| Quartzite | Quartzite | Direct equivalent |
| Rock Salt | Rock Salt | Direct equivalent |
| Chert | Flint | Closest GT equivalent |
| Gabbro | Basalt | Approximation — no GT gabbro |
| Rhyolite | Granite | Approximation — no GT rhyolite |
| Dacite | Andesite | Approximation — no GT dacite |
| Limestone | Calcite | Approximation — no GT limestone |
| Dolomite | Calcite | Approximation — no GT dolomite |
| Chalk | Calcite | Approximation — no GT chalk |
| Shale | Stone | Approximation — no GT equivalent |
| Claystone | Stone | Approximation — no GT equivalent |
| Conglomerate | Stone | Approximation — no GT equivalent |
| Slate | Stone | Approximation — no GT equivalent |
| Phyllite | Stone | Approximation — no GT equivalent |
| Schist | Stone | Approximation — no GT equivalent |
| Gneiss | Stone | Approximation — no GT equivalent |

### Custom GT Materials
FirmaBridge registers four new GT materials for TFC-specific minerals that have no GT equivalent. Each has an accurate chemical composition so GT auto-generates centrifuge and electrolyzer recipes for them.

| Material | Formula | Properties |
| -------- | ------- | ---------- |
| Lignite | C | Dust, burnTime 800 — TFC brown coal, lower grade than bituminous |
| Sylvite | KCl | Gem + Dust — TFC evaporite crystal; electrolyzer yields potassium + chlorine |
| Cryolite | Na₃AlF₆ | Gem + Dust — aluminium smelting flux; electrolyzer yields sodium + aluminium + fluorine |
| Serpentine | Mg₃Si₂O₅ | Dust — altered ultramafic mineral; centrifuge yields magnesium + silicon + oxygen |

### Material Bridge
Registers TFC metals and alloys into GT's OreDict system so they are recognized by GT machines and recipes. Covers 84 TFC→GT OreDict entries including ingots, dusts, nuggets, and blocks.

### Pig Iron Bridge
Connects TFC's blast furnace workflow to GT's electric blast furnace:

```
TFC blast furnace → pig iron ingot
    → GT macerator  → pig iron dust
    → GT EBF 800°C  → iron ingot  (+25% carbon dust byproduct)
    → GT EBF (auto) → wrought iron → steel
```

### Alloy Macerator Recipes
Adds GT macerator recipes for TFC alloys that have no GT material counterpart, producing the correct GT material dust for each.

| TFC Alloy | Output |
| --------- | ------ |
| Black Steel | Black Steel Dust |
| Blue Steel | Blue Steel Dust |
| Red Steel | Red Steel Dust |
| Bismuth Bronze | Bismuth Bronze Dust |
| Black Bronze | Black Bronze Dust |
| Rose Gold | Rose Gold Dust |
| Sterling Silver | Sterling Silver Dust |

> **Note:** Recipe yields and GT material mappings may be rebalanced in future versions as TFC/GT progression is refined.

### Ore Quality System
Adds GT macerator recipes for TFC ore chunks and small ores at all three quality grades. Yields scale with ore grade.

| Grade | Output |
| ----- | ------ |
| Poor | 1x Dust |
| Normal | 2x Dust |
| Rich | 3x Dust |
| Small Ore | 1x Small Dust |

56 recipes total, covering all TFC metals supported by GT.

> **Note:** Ore quality yields may be rebalanced in future versions as TFC/GT progression is refined.

### JEI Integration
FirmaBridge's machine recipes (ore quality, alloy macerator, pig iron chain) are added directly to GT's RecipeMaps and appear automatically in GT's JEI categories. TFC stone-type GT ore variants appear in GT's creative tab and JEI item list automatically.

Additionally, TFC raw stone blocks that have GT material mappings display an info entry in JEI's information tab explaining the connection.

---

## Installation

1. Install Minecraft 1.12.2 with Forge 14.23.5.2847.
2. Install TerraFirmaCraft, GregTech CEu, and MixinBooter.
3. Drop `FirmaBridge-<version>.jar` into your `mods` folder.
4. Launch the game.

No additional configuration is required. FirmaBridge activates automatically on world load.

---

## Configuration

FirmaBridge generates a config file at:

```
config/firmabridge.cfg
```

| Option | Default | Description |
| ------ | ------- | ----------- |
| `enableGTIntegration` | `true` | Enables the GT/TFC bridge (vein patcher, geological worldgen, material bridge, recipe compat, ore quality). Disable to turn off all FirmaBridge functionality. |

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before submitting anything — it covers branch naming, commit conventions, code guidelines, and what to do before writing any code.

All pull requests are reviewed before being accepted or rejected. Feedback will always be provided regardless of outcome.

---

## Credits

- **TheZaltren** — mod author
- **TerraFirmaCraft Team** — for TFC and its APIs
- **GregTech CEu Team** — for GTCEu and its APIs
- **CleanroomMC** — for RetroFuturaGradle and the [ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv) template

---

## License

Licensed under the **MIT License**. See [LICENSE](LICENSE) for details.
