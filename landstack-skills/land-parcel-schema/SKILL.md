---
name: land-parcel-schema
description: Use this skill whenever adding or modifying parcel-related fields, screens, mock data entries, or data models in the LandStack Android app. Enforces the three-layer land parcel data model (Base, Essential, Additional/Use-Case layers) so no layer or field defined in the original spec is silently dropped, and enforces the State Profile pattern (e.g. Chandigarh vs Tamil Nadu) for swapping field labels/units by state.
---

# Land Parcel Schema Skill

## The three layers (every parcel must expose all of these)

**Base Layer** -- ULPIN, state, district, village/ward, survey/sub-division number, cadastral boundary geometry (GeoJSON polygon) + computed area, parcel type (rural/urban).

**Essential Layer**
- Record of Rights (RoR): owner name(s), khata/khatauni number, right type, ownership share %, mutation history log
- Registration: deed number, registration date, transaction type, stamp duty paid, registering sub-office
- Master Plan / Zoning: zone classification, permissible FAR/FSI, permitted use
- Building Permission: sanction number, approved built-up area & floors, occupancy certificate status
- Encumbrance & Mortgage: lender, loan amount, lien status, encumbrance certificate validity window

**Additional / Use-Case Layer**
- Utility infrastructure: water & electricity connection IDs, sewage access, road-access type
- Property taxation: assessed value, annual tax, payment status, arrears
- Valuation: government guideline/circle rate vs. estimated market value
- Environmental/restriction zones: flood-zone flag, CRZ flag, heritage-zone flag, other overlays

## Instructions
1. Before adding a new screen or feature that touches parcel data, check which layer(s) it belongs to and pull the relevant fields from the list above rather than inventing a subset.
2. When adding or editing mock data, keep it realistic and keep the existing spread (roughly 10-15 parcels spanning a Chandigarh-style urban block and a Tamil Nadu-style village) -- don't reduce the sample set or flatten the variety to save time.
3. Respect the **State Profile** selector: field labels and units come from the active state profile (e.g. Chandigarh vs Tamil Nadu), not hardcoded per-screen. If a new field is state-specific, add it to the state-profile config rather than branching UI code on the state name.
4. If asked to "simplify" or "clean up" the parcel model, push back and confirm with the user before removing a layer or field -- completeness of the three-layer model is a stated project requirement, not an incidental detail.

## Constraints
- Never ship a parcel-related screen that only shows the Base layer "for now" and calls it done.
- Never hardcode Chandigarh-only or Tamil-Nadu-only labels into a shared component -- route them through the state profile.
