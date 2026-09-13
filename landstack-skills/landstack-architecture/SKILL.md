---
name: landstack-architecture
description: Use this skill whenever adding, refactoring, reviewing, or explaining code in the LandStack Android app (Kotlin/Jetpack Compose GIS land-governance prototype). Enforces the MVVM + Repository + Hilt architecture, feature-based package structure, offline-first Room caching, and the rule that every data domain (cadastral, RoR, registration, planning, taxation, utilities) is accessed only through a swappable Repository interface so a real government backend can later replace the bundled mock JSON with zero UI changes.
---

# LandStack Architecture Skill

## Goal
Keep every change to the LandStack codebase consistent with its original architecture, so the project stays swappable (mock data -> real departmental APIs), auditable, and free of "God classes" as it grows past the initial one-shot build.

## Architecture Rules
- **Layers**: UI (Compose screens) -> ViewModel (Hilt-injected, per screen/feature) -> Repository interface -> Data source (currently: bundled mock JSON via a Retrofit-shaped repository impl; future: real REST API).
- **Repository-interface-first**: Every one of the six data domains (cadastral, RoR/registration, planning/zoning, building permission, taxation, utilities) has its own Repository interface. ViewModels and Composables must never read mock JSON assets directly -- always go through the interface, even though today's implementation is backed by local mock data.
- **Package structure**: feature-based (e.g. `feature/parceldetail`, `feature/gismap`, `feature/dashboard`, `feature/servicerequest`), not layer-based dumping grounds like one giant `viewmodels/` or `utils/` package. No file should try to do two features' worth of work.
- **Offline-first**: Room is the source of truth for what's on screen; network/mock-repository calls populate Room, screens observe Room via Flow. A screen must still render useful content with no network.
- **RBAC**: role checks (Citizen / Land Officer / Admin) are enforced in the ViewModel layer, not just hidden in the UI -- a ViewModel must refuse to expose officer-only actions/data to a citizen role even if a screen is somehow reached.
- **Audit trail**: any view or edit of parcel data must write a row (who, what, when) to the local Room audit-trail table. Don't add a new sensitive read/write path without wiring this.

## Instructions
1. Before adding a new feature, identify which existing Repository interface(s) it should use. Only create a new interface if the feature genuinely introduces a new data domain.
2. Put new screens under their own feature package with `ui/`, `viewmodel/` (and `state/` if the screen has a non-trivial UI state class) inside it.
3. If a screen needs data from more than one domain (e.g. Parcel Detail needs cadastral + RoR + taxation), inject multiple repository interfaces into one ViewModel rather than one repository reaching into another's data.
4. When touching anything role-restricted, verify the ViewModel enforces the role check -- don't rely on the Composable simply "not showing" a button.
5. When touching parcel data, log the audit-trail entry as part of the same ViewModel action, not as an afterthought.

## Constraints
- Never let a Composable call a DAO, a mock JSON asset, or a Retrofit service directly.
- Never collapse two feature packages together "to save time."
- Never skip the audit-trail write for a parcel view/edit to save effort on a small change.
