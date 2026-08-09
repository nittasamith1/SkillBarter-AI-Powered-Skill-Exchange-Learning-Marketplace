# ADR-009: Reputation & Dynamic Trust Score Calculation

**Date:** 2026-08-09  
**Status:** Accepted  
**Deciders:** SkillBarter Engineering Team

---

## Context

Peer-to-peer marketplaces depend heavily on trust. Users need visibility into a peer's reliability, rating history, and session behavior before committing to exchange sessions.

## Decision

Implement a **dynamic multi-component Trust Score algorithm** (`TrustScoreService`):

### Score Equation

$$\text{TrustScore} = (W_r \cdot S_r) + (W_c \cdot S_c) + (W_b \cdot S_b) + (W_p \cdot S_p) + (W_x \cdot (1 - P_x))$$

Where:
- $S_r$: Average star rating score normalized to $[0, 100]$ ($W_r = 0.40$).
- $S_c$: Session completion rate $[0, 100]$ ($W_c = 0.20$).
- $S_b$: Reliability score based on no-show avoidance $[0, 100]$ ($W_b = 0.20$).
- $S_p$: Response rate to exchange requests $[0, 100]$ ($W_p = 0.10$).
- $P_x$: Cancellation penalty rate $[0, 100]$ ($W_x = 0.10$).

Default weights are configurable via `app.trust.weights` in `application.yml`.

### Rules & Integrity
- Peer reviews can only be submitted for completed sessions in which both parties participated.
- Self-reviews are strictly forbidden and enforced by `ReviewService`.
- Trust scores re-calculate dynamically upon review submission or session completion.

## Consequences

- ✅ Provides holistic reputation metrics beyond simple star averages.
- ✅ Penalizes frequent cancellations and no-shows automatically.
- ✅ Feeds directly into AI matching candidate ranking.
