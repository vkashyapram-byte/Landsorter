# LandStack Backend Architecture Guidelines

## Restricted State Mutations

Any status-changing write across the restricted tables (`building_permissions`, `parcel_encumbrances`, `parcel_tax_records`, or `parcel_registrations`) **MUST** go through a `SECURITY DEFINER` RPC with an internal role check.

**Rule:**
Never rely on a raw PostgREST `UPDATE` for restricted mutations, even if Row Level Security (RLS) is perfectly configured.

**Reasoning:**
When an unauthorized user (e.g. a Citizen) attempts to perform a raw REST `UPDATE` on a row they are blocked from modifying, PostgreSQL's RLS `USING` clause evaluates to false. This causes exactly 0 rows to match the `WHERE` filter of the update. PostgREST correctly interprets this as "Update successful on 0 rows" and returns an ambiguous HTTP `204 No Content`. 

Because a 204 is considered a success code, the Android client has no reliable way to detect that the update was actually rejected and silently dropped. 

By routing these mutations through an RPC:
1. We can execute an explicit role check as the first statement in the function body.
2. We can `RAISE EXCEPTION` to force the REST API to return a clear, interceptable HTTP `400 Bad Request` with a meaningful error payload.
3. We revoke `PUBLIC` execution rights and grant it exclusively to `authenticated` users, guaranteeing the function itself acts as an absolute security boundary independent of RLS quirks.

**Existing RPCs:**
- `record_ownership_mutation(ulpin, new_owners)`
- `approve_building_permit(p_permit_id, p_decision)`
- `resolve_parcel_encumbrance(p_encumbrance_id)`
- `mark_tax_record_paid(p_tax_record_id)`
