---
name: govtech-compose-ui
description: Use this skill whenever designing, building, or reviewing a Jetpack Compose screen in the LandStack Android app. Enforces a clean, practical, user-friendly civic/govtech Material 3 visual style -- plain-language labels, accessible tap targets, meaningful status indicators, full light/dark theme support, and explicit empty/loading/error states -- over decorative or "flashy" UI choices.
---

# Govtech Compose UI Skill

## Goal
LandStack will be used by ordinary citizens and government officers, not a tech-savvy audience by default. Every screen should read as clear and trustworthy first, polished second.

## Guidelines
- **Plain language over jargon**: don't put raw DB/legal field names (`khatauni`, `FAR`, `CRZ`) on screen without a short plain-language label or helper text explaining what it means to the person looking at it.
- **Accessible tap targets**: minimum 48dp touch targets; don't shrink buttons/icons to fit more on screen.
- **Meaningful status, not decoration**: colors/badges/steppers (e.g. for registration/mutation status) must map to real states with a consistent palette across the app -- don't reuse the same color for unrelated meanings, and don't add color purely for visual variety.
- **Dashboards lead with action, not data density**: Citizen and Officer/Admin dashboards should surface the 2-3 most relevant actions or pending items first; don't bury them under decorative summary tiles.
- **Explicit states, always**: every screen must have a real empty state, loading state, and error state -- never a blank Compose surface while something is loading or missing.
- **Theme parity**: every screen must be checked in both light and dark Material 3 themes; a screen that only looks right in one theme isn't done.

## Instructions
1. When building a new screen, write its empty/loading/error states before or alongside the "happy path" -- don't leave them as an afterthought.
2. When you introduce a new status concept (e.g. a new workflow stage), extend the existing status color/badge system rather than picking a new ad hoc color.
3. When labeling a field drawn from the parcel data model (Base/Essential/Additional layers), prefer a short human label plus an optional info affordance over the raw field name.

## Constraints
- Never ship a screen that hasn't been checked in dark theme.
- Never use color as the only signal for a status -- pair it with text or an icon too, for accessibility.
