---
name: navic-gnss-integration
description: Use this skill whenever writing, modifying, debugging, or explaining any GNSS, location, or NavIC/IRNSS-related code in the LandStack Android app. Covers reading raw GnssStatus and GnssMeasurementsEvent data, filtering for CONSTELLATION_IRNSS satellites, runtime ACCESS_FINE_LOCATION handling, and the required UI fallback copy when a device chipset doesn't expose IRNSS data or when running on an emulator.
---

# NavIC / Hardware GNSS Integration Skill

## Ground truth (do not contradict this)
Android has **no public API that can force device positioning to use NavIC alone** -- the chipset's GNSS HAL fuses all visible constellations (GPS, GLONASS, Galileo, BeiDou, NavIC/IRNSS) into one position fix. Any code or UI copy that implies "positioning by NavIC only" is inaccurate. What genuinely is available at hardware level, and what this app must actually use:
- Per-satellite constellation status via `GnssStatus`, filtered where `getConstellationType(i) == GnssStatus.CONSTELLATION_IRNSS`.
- Raw per-satellite measurements (pseudorange, carrier phase, Cn0) via `GnssMeasurementsEvent`, again filtered to IRNSS satellites.
- The actual displayed lat/long on the map still comes from `FusedLocationProviderClient` (fused, not NavIC-only) -- label it honestly.

## Instructions
1. Use `LocationManager.registerGnssStatusCallback()` to get live `GnssStatus` updates; expose the visible NavIC (IRNSS) satellite count, SVIDs, and Cn0 per satellite alongside the other constellations for comparison, not NavIC in isolation.
2. Implement `GnssMeasurementsEvent.Callback` to pull raw pseudorange/carrier-phase measurements specifically for IRNSS satellites -- this is what makes the feature genuinely hardware-level rather than a fused coordinate.
3. Use `FusedLocationProviderClient` only for the map's displayed position, and phrase any on-screen explanation as e.g. "NavIC satellites contributing to this fix: N" -- never "Position determined via NavIC."
4. Always request `ACCESS_FINE_LOCATION` at runtime (not just in the manifest) before touching any GNSS API, and handle both the "denied" and "denied forever" cases with a real UI state, not a crash.
5. If `GnssStatus` returns zero IRNSS satellites, show a clear, calm message (e.g. "No NavIC satellites detected -- this can be normal indoors, on this device's chipset, or on an emulator") -- never a blank screen, an infinite spinner, or a fabricated satellite count.

## Testing expectations
- **Emulator**: the NavIC/GNSS screen is *expected* to show zero satellites -- the emulator can fake a lat/long but not real per-satellite `GnssStatus`/`GnssMeasurementsEvent` data. "Empty but graceful" is a pass on the emulator, not a bug to chase.
- **Real device**: test outdoors with a clear sky. If it's still empty on a real device, that can legitimately mean the chipset doesn't expose IRNSS (common on non-Indian-market or older devices) -- surface that possibility in the fallback copy rather than assuming a bug.

## Constraints
- Never simulate or hardcode a fake NavIC satellite count "to make the demo look better."
- Never claim the displayed position was obtained "from NavIC" -- it's a fused position; only the constellation status/measurements panel is IRNSS-specific.
