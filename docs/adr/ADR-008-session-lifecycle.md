# ADR-008: Session Lifecycle & Conflict Management

**Date:** 2026-08-09  
**Status:** Accepted  
**Deciders:** SkillBarter Engineering Team

---

## Context

Skill exchange sessions require strict state management and schedule conflict prevention. Concurrent double-bookings or invalid state transitions (e.g. completing a cancelled session) lead to credit misallocations and data corruption.

## Decision

Implement a **strict state machine** (`SessionStateValidator`) and **server-side conflict detection**:

### State Machine Transitions

```
SCHEDULED → IN_PROGRESS → COMPLETED
    ↓           ↓
CANCELLED   NO_SHOW
```

- `SCHEDULED`: Initial state upon session creation from an accepted Exchange Request.
- `IN_PROGRESS`: Transitioned when session starts.
- `COMPLETED`: Terminal state triggering automatic Skill Credit settlement (+1 to teacher, -1 to learner).
- `CANCELLED`: Terminal state prior to start.
- `NO_SHOW`: Marked if a participant fails to appear.
- `DISPUTED`: Can be flagged from `IN_PROGRESS` or `COMPLETED` for admin/support resolution.

### Server-Side Double-Booking Prevention

Before creating or confirming a session schedule, `SessionRepository.hasConflict(...)` executes a server-side overlap query checking both participants:

$$\text{Overlap} \iff (\text{start}_A < \text{end}_B) \land (\text{end}_A > \text{start}_B)$$

If a conflict exists for either user in state `SCHEDULED` or `IN_PROGRESS`, the creation is rejected with `SESSION_CONFLICT`.

## Consequences

- ✅ Guarantees credit transactions are only emitted on valid `COMPLETED` transitions.
- ✅ Prevents double-booking across both teachers and learners.
- ✅ Audit log tracking for all status changes.
