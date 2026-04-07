# Contributing to FirmaBridge

Thank you for your interest in contributing to FirmaBridge. This document outlines the guidelines for contributing to the project.

---

## Before You Start

FirmaBridge is focused on one thing — bridging TerraFirmaCraft and GregTech CEu. Before opening a PR or starting work on a feature, please make sure your contribution fits within that scope. If you are unsure, open a Feature Request or Compatibility Request issue first and discuss it before writing any code.

All pull requests are reviewed before being accepted or rejected. Contributions that don't align with the mod's goals may not be merged, but feedback will always be provided.

---

## Getting Started

1. Fork the repository.
2. Create a feature branch from `FB-dev`.

```
git checkout -b feat/your-feature-name
```

3. Make your changes.
4. Verify the build compiles cleanly.

```
gradlew build
```

5. Open a pull request targeting `FB-dev`.

---

## Build Requirements

| Component         | Version      |
| ----------------- | ------------ |
| Java              | 25           |
| Gradle            | 9.2.1        |
| Minecraft         | 1.12.2       |
| Forge             | 14.23.5.2847 |

---

## Commit Message Convention

FirmaBridge uses conventional commits to automate changelog generation. All commit messages must use one of the following prefixes:

| Prefix | Description |
| ------ | ----------- |
| `feat:` | A new feature or addition |
| `fix:` | A bug fix |
| `change:` | A change or refactor to existing functionality |
| `remove:` | Removing a feature or file |
| `docs:` | Documentation only changes |

**Examples:**
```
feat: add GT macerator recipe for TFC bismuth bronze
fix: cinnabar ore not generating below Y 20
change: rebalance ore quality dust yields
docs: update compatibility table in README
```

Commits that do not follow this convention will be asked to be amended before merging.

---

## Code Guidelines

- Keep changes focused. One feature or fix per PR.
- Do not add features or refactor code beyond what is necessary for your change.
- All world gen code must stay within chunk bounds to avoid cascading worldgen lag.
- Test in-game with both TerraFirmaCraft and GregTech CEu loaded before submitting.

---

## Restricted Changes

Certain types of changes require explicit approval from the project lead or an authorized maintainer before a PR will be considered. Submitting a PR for these without prior approval will result in it being closed.

**Requires maintainer approval:**

- **Refactors** — any change that restructures, renames, or reorganizes existing code without adding new functionality. Refactors carry a high risk of introducing regressions and will only be approved if there is a clear and justified reason.
- **Dependency changes** — adding, removing, upgrading, or downgrading any dependency in `dependencies.gradle` or `gradle.properties`. Dependency changes can break the build environment for all users and must be reviewed carefully before being accepted.
- **Build system changes** — any modification to Gradle scripts, build configuration, or the toolchain setup.

If you believe a refactor or dependency change is necessary, open a Feature Request issue first and explain your reasoning. Do not submit code until it has been discussed and approved.

---

## Reporting Issues

If you find a bug or want to request a feature, please use the appropriate issue template on GitHub:

- **Bug Report** — unexpected behavior or crashes
- **Feature Request** — new functionality
- **Compatibility Issue** — conflict with another mod
- **Compatibility Request** — request support for a specific mod

---

## License

By contributing to FirmaBridge you agree that your contributions will be licensed under the [MIT License](LICENSE).
