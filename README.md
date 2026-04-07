<h1 align="center">FirmaBridge</h1>
<p align="center">
  A compatibility bridge between <b>TerraFirmaCraft</b> and <b>GregTech CEu</b> for Minecraft 1.12.2.
</p>
<p align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.12.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-14.23.5.2847-orange)
![Java](https://img.shields.io/badge/Java-25-blue)
![License](https://img.shields.io/badge/License-MIT-green)

</p>

---

## Overview

FirmaBridge makes TerraFirmaCraft and GregTech CEu work together cleanly.

TFC replaces vanilla ore generation, biomes, and materials with its own systems. GT adds an extensive material and machine recipe ecosystem built on top of vanilla assumptions. Without a bridge, the two mods largely ignore each other — GT veins don't generate in TFC rock, TFC ores have no GT processing recipes, and TFC metals have no GT material equivalents.

FirmaBridge fixes this by:

- Patching GT ore vein definitions to respect TFC rock layers
- Registering TFC metals and alloys into GT's OreDict system
- Adding GT machine recipes for TFC-unique alloys and ore grades

---

## Compatibility

| Mod | Version | Required |
| --- | ------- | -------- |
| TerraFirmaCraft | 1.7.23+ | Yes |
| GregTech CEu | 2.8.10+ | Yes |
| Minecraft Forge | 14.23.5.2847 | Yes |

---

## Features

### GT Vein Patcher
GT ore vein definitions target vanilla stone by default. FirmaBridge patches all 45 GT vein definitions at startup to include TFC rock layers, so GT veins generate correctly inside TFC terrain.

### Rock Registry
Maps TFC rock types to their GT material equivalents. Used internally by the vein patcher and recipe systems.

| TFC Rock | GT Material |
| -------- | ----------- |
| Granite | Granite |
| Diorite | Diorite |
| Gabbro | Gabbro |
| Marble | Marble |
| Quartzite | Quartzite |
| Schist | Schist |
| Phyllite | Phyllite |
| Gneiss | Gneiss |

### Material Bridge
Registers TFC metals and alloys into GT's OreDict system so they are recognized by GT machines and recipes. Covers 84 TFC→GT OreDict entries including ingots, dusts, nuggets, and blocks.

### Alloy Macerator Recipes
Adds GT macerator recipes for TFC-unique alloys that have no GT material counterpart, allowing them to be processed in GT machines.

| TFC Alloy | Output |
| --------- | ------ |
| Black Steel | Steel Dust |
| Blue Steel | Steel Dust |
| Red Steel | Steel Dust |
| Bismuth Bronze | Bronze Dust |
| Black Bronze | Bronze Dust |
| Rose Gold | Gold Dust |
| Sterling Silver | Silver Dust |

> **Note:** Alloy macerator recipes may be rebalanced or expanded in future versions as TFC/GT progression is refined.

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

---

## Installation

1. Install Minecraft 1.12.2 with Forge 14.23.5.2847.
2. Install TerraFirmaCraft and GregTech CEu.
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
| `enableGTIntegration` | `true` | Enables the GT/TFC bridge (vein patcher, material bridge, recipe compat, ore quality). Disable to turn off all FirmaBridge functionality. |

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
