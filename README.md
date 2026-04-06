
<p align="center">
  <img src="docs/RCNAProject.png" width="200">
</p>
<h1 align="center">RCNA 1.12.2 Mod Development Template</h1>
<p align="center">
Development template for mods used in the <b>RunicCraft: New Ascension</b> ecosystem.
</p>
<p align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.12.2-brightgreen)
![Forge](https://img.shields.io/badge/Forge-14.23.5.2847-orange)
![Java](https://img.shields.io/badge/Java-25-blue)
![License](https://img.shields.io/badge/License-MIT-green)

</p>

---

# Overview

This repository provides a **modernized development environment for Minecraft 1.12.2 modding**.

It is based on the CleanroomMC template and configured specifically for **RunicCraft: New Ascension (RCNA)** development.

The template includes support for:

* RetroFuturaGradle
* Forge 1.12.2
* Mixins
* Coremods
* Modern Gradle workflow for legacy Minecraft versions
---

# Toolchain

This template runs on the following development stack:

| Component         | Version      |
| ----------------- | ------------ |
| Minecraft         | 1.12.2       |
| Forge             | 14.23.5.2847 |
| Java              | 25           |
| Gradle            | 9.2.1        |
| RetroFuturaGradle | 2.0.2        |

---

# Creating a New Mod

1. Click **Use this template** at the top of the repository.
2. Clone the generated repository.

```
git clone <your-new-repo>
```

3. Open the project in **IntelliJ IDEA**.

Before syncing the project ensure Gradle uses **Java 25**:

```
Settings → Build Tools → Gradle → Gradle JVM
```

4. When IntelliJ detects the `build.gradle`, choose:

```
Load Gradle Project
```

5. Refresh Gradle in the right-side Gradle panel.

---

## Important: Updating Mod Information

Before developing your mod you must update the mod metadata in `gradle.properties`.

Locate the **Mod Information** section and change:

```
root_package = net.yourname
mod_id = yourmodid
mod_name = Your Mod Name
```

After editing these values regenerate the project sources:

```
./gradlew clean
./gradlew build
```

This will regenerate the `Tags` class used by the mod entrypoint.


# Development Workflow

Common development tasks:

### Run the Minecraft client

```
gradlew runClient
```

### Run the dedicated server

```
gradlew runServer
```

### Build the mod jar

```
gradlew build
```

The compiled mod will be located in:

```
build/libs/
```

---

# Project Structure

```
src/
 └ main/
    ├ java/
    │  └ net/zaltren/rcnatemplate
    │     └ ModMain.java
    └ resources/
       ├ assets/
       ├ mixins.modid.json
       └ modid.info
```

---

# Notes

Dependency configuration:

```
gradle/scripts/dependencies.gradle
```

Publishing configuration:

```
gradle/scripts/publishing.gradle
```

For mixin development in IntelliJ it is recommended to use:

https://github.com/eigenraven/MinecraftDev/releases

---

# Credits

This template is based on the CleanroomMC development template.

Special thanks to the CleanroomMC team for modernizing the Minecraft 1.12.2 development environment.

---

# License

Licensed under the **MIT License**.
