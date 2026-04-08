# Changelog

All notable changes to FirmaBridge will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---
## [Unreleased]

### Fixed

- Trigger changelog on tags and always commit to FB-dev
- Move conditional relations blocks outside CurseGradle DSL closure

---## [v0.1.2-alpha] — 2026-04-08

### Added

- Add TFC stone macerator recipes and fix GT vein strata coverage
- Register all 21 TFC stone types as GT StoneTypes and fix worldgen NPE
- Add CurseForge publish workflow and set project ID

### Documentation

- Add FirmaBridge README
- Add automated changelog workflow with git-cliff
- Add GitHub issue templates for bugs, features, and compatibility
- Add compatibility request issue template
- Add pull request template
- Add CONTRIBUTING.md with commit conventions and guidelines
- Add restricted changes section to CONTRIBUTING.md
- Expand code guidelines in CONTRIBUTING.md
- Clarify commit convention rejection policy in CONTRIBUTING.md
- Add CleanroomMC build template info to CONTRIBUTING.md
- Add branch naming convention to CONTRIBUTING.md
- Add welcoming introduction to CONTRIBUTING.md
- Expand Before You Start section in CONTRIBUTING.md
- Expand CONTRIBUTING with dev setup, review process, and corrections
- Trim contributing section and correct CleanroomMC credit
- Update README with accurate 21-rock stone type table and approximations

### Fixed

- Use PAT for changelog workflow push permissions
- Skip auto changelog commit in git-cliff output
- Update git-cliff-action from v3 to v4
- Replace third-party actions with direct run steps for org policy compliance
- Install Java 25 in publish workflow for RFG compatibility

---## [v0.1.0-alpha] — 2026-04-07

### Added

- Initial mod structure and build system
- Implement GT/TFC material bridge and vein patcher
- Add alloy macerator recipes and ore quality system
- Add TFC surface finder and mob spawn API
- Add structure placement API

### Changed

- Defer mod integration and hide mob spawn config

---
