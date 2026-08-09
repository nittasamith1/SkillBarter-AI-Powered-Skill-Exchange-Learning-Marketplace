# ADR-006: Availability Model & Overlap Detection

**Date:** 2026-08-09  
**Status:** Accepted  
**Deciders:** SkillBarter Engineering Team

---

## Context

Phase 3 requires peers to schedule skill exchange sessions. Without knowing each user's available times, session scheduling degrades to manual back-and-forth coordination. An availability model that supports overlap detection enables the AI matching engine to factor schedule compatibility into its ranking.

## Decision

Adopt a **weekly recurring slot model** stored in a `user_availability` table:

| Field | Type | Notes |
|---|---|---|
| `day_of_week` | `ENUM(MONDAY…SUNDAY)` | Recurring weekly anchor |
| `start_time` / `end_time` | `TIME` | Local time in the user's declared timezone |
| `timezone` | `VARCHAR(50)` | IANA tz database name (e.g. `Asia/Kolkata`) |

**Overlap Detection** (`AvailabilityOverlapService`):
1. Convert all slots for both users to a common reference timezone (UTC) using `java.time.ZonedDateTime`.
2. Compare day-of-week + time ranges to find minutes of weekly overlap.
3. Normalize the overlap score `[0, 1]` against the maximum possible weekly overlap.
4. A score of `0.0` means no shared availability; `1.0` means perfect alignment.

## Consequences

- ✅ Timezone-aware — avoids naive local-time comparisons across time zones.
- ✅ No calendar system dependency — recurring slots are simple and predictable.
- ✅ Overlap score feeds directly into the Matching Engine as a weighted dimension.
- ⚠️ Does not support one-off date-specific availability (deferred to Phase 4).
