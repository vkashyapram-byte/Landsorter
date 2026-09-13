# Known Limitations

The following are known limitations and caveats of the current implementation. Please review these before evaluating or demoing the application.

## 1. Offline Sync Queuing
Offline sync queuing via `WorkManager` has been statically verified against correct configuration and framework wiring (e.g., Hilt `WorkerFactory` integration, process death survival policies). However, it has **not** been tested on a physical Android device or emulator across an actual OS-level process kill. 

**Recommendation:** Conduct a physical test (enable airplane mode, create/edit a parcel, force-kill the app process, reopen it, then disable airplane mode) before any live demo.

## 2. Offline Conflict Resolution
Conflict resolution is currently designed as **"last-write-wins" based on server arrival time**, not the time the edit was authored. 

If simultaneous edits occur from two offline-capable devices, they are not intelligently merged. The "current" state of the parcel will reflect whichever edit syncs to the server last, which means an older offline edit could overwrite a newer online edit if it connects later.

**Note:** The system's audit log (`audit_logs`) preserves the complete history of all changes. While the active state prioritizes the most recent sync, nothing is silently lost or deleted from the historical record.

## 3. Auth and Onboarding
For active development purposes, Supabase "Confirm email" is currently disabled. This allows accounts to sign in immediately after signup with no email confirmation or controlled onboarding process.
**Note:** This is a fine tradeoff for an internal app under active development, but before any real production rollout with actual citizens/officers, immediate sign-in without a real email validation or a controlled onboarding process must be reconsidered.
## 4. Maps Dependency
The app currently uses `osmdroid` for mapping. This library is largely unmaintained and has unresolved memory leak issues. The transition from Google Maps was a temporary measure.

## 5. Cadastral Map Accuracy
The underlying map imagery and tiles provided via OpenStreetMap are general-purpose street maps and do not represent official, survey-grade cadastral maps. While the parcel boundary overlays rendered on top of the map use actual geometry data stored in the database, the alignment between these boundaries and the base map features (roads, buildings) may not be perfectly accurate due to the general nature of the underlying street map.
