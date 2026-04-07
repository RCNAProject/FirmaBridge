# Contributing to FirmaBridge

FirmaBridge is a community-driven project and contributions are genuinely welcomed. Whether you are fixing a bug, improving compatibility, or helping document the mod, your help makes FirmaBridge better for everyone who uses it.

That said, this project has a clear purpose and a standard for how contributions are made. The guidelines in this document are not here to make contributing difficult — they exist to keep the codebase clean, organized, and maintainable as the mod grows. A well-organized project is easier to debug, easier to build on, and easier for new contributors to understand.

Please take the time to read through this document before contributing. Following these guidelines from the start makes the review process smoother for everyone and gives your PR the best chance of being accepted.

---

## Before You Start

FirmaBridge is focused on one thing — bridging TerraFirmaCraft and GregTech CEu. Every contribution, no matter how small, should serve that purpose. If your change does not directly improve how TFC and GT work together, it is likely out of scope.

Before writing any code:
- Check the open issues and pull requests to make sure someone is not already working on the same thing. Duplicate work wastes everyone's time and duplicate PRs will be closed.
- If you are adding a new feature or compatibility, open a Feature Request or Compatibility Request issue first and wait for feedback before starting work. Starting work before getting approval risks having your PR rejected entirely.
- If you are fixing a bug, check if there is already an open issue for it. If there is not, open one before submitting a fix so it can be tracked and acknowledged.
- If you are unsure whether your idea fits the scope of the mod, ask first. It is always better to discuss before spending time on code that may not be accepted.

Do not submit a PR for work that has not been discussed and acknowledged first, unless it is a clear and straightforward bug fix. This protects your time as much as ours — there is nothing more frustrating than putting effort into a contribution only for it to be rejected because it was never discussed.

All pull requests are reviewed before being accepted or rejected. Every submission will receive feedback regardless of outcome. We appreciate the effort contributors put in, and even rejected PRs help shape the direction of the mod.

---

## Getting Started

1. Fork the repository.
2. Create a feature branch from `FB-dev` using the following naming convention:

```
FB-dev-yourname-what-you-added
```

The name portion must be your GitHub username or the name you are widely known by in the Minecraft community. This keeps branch ownership clear and traceable. Do not use a generic or made-up name.

**Examples:**
```
FB-dev-TheZaltren-gt-copper-recipes
FB-dev-sarah-fix-cinnabar-generation
FB-dev-alex-tfc-alloy-bridge
```

Branches that do not follow this naming convention will be asked to be renamed before the PR is reviewed.

3. Make your changes.
4. Verify the build compiles cleanly.

```
gradlew build
```

5. Open a pull request targeting `FB-dev`.

---

## Build Requirements

FirmaBridge is built on the [CleanroomMC 1.12.2 development template](https://github.com/RCNAProject/RCNA-1.12.2-template), which uses RetroFuturaGradle to provide a modern build environment for legacy Minecraft versions. If you are unfamiliar with this setup, it is recommended to review the CleanroomMC documentation before contributing.

| Component         | Version      |
| ----------------- | ------------ |
| Java              | 25           |
| Gradle            | 9.2.1        |
| RetroFuturaGradle | 2.0.2        |
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

Pull requests with commits that do not follow this convention will be rejected and the author will be asked to amend their commit messages before the PR can be reconsidered. If the commit messages are not corrected within a reasonable amount of time the PR will remain rejected and will be closed. Please make sure your commits follow the convention before submitting.

---

## Code Guidelines

**Keep changes focused.**
One feature or fix per PR. Do not bundle unrelated changes together. If you find an unrelated bug while working on something, open a separate issue or PR for it.

**Do not over-engineer.**
Only add what is necessary for your change. Do not add extra abstractions, helper classes, or configuration options that are not required. Do not refactor surrounding code that is not directly related to your change.

**Do not add dead code.**
Do not leave commented-out code, unused imports, or placeholder methods in your PR. If something is being deferred, note it in the issue tracker instead.

**Follow existing patterns.**
FirmaBridge has established patterns for how things like recipe registration, ore generation, and config handling are done. Follow those patterns rather than introducing new ones without discussion.

**World gen must stay within chunk bounds.**
Any block placement during world generation must be clamped to the chunk currently being populated. Accessing blocks in adjacent chunks during generation causes cascading worldgen lag and is not acceptable. Always verify your world gen code does not cross chunk boundaries.

**Test before submitting.**
All changes must be tested in-game with both TerraFirmaCraft and GregTech CEu loaded. Verify the build compiles cleanly, there are no crash logs on world load, and your change behaves as expected in a real world.

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
