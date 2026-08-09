# ADR-007: AI Matching Engine Design

**Date:** 2026-08-09  
**Status:** Accepted  
**Deciders:** SkillBarter Engineering Team

---

## Context

The core value of SkillBarter is connecting the right peers for skill exchange. A naive keyword-match approach produces low-quality suggestions. Phase 3 requires a deterministic, explainable, multi-factor matching algorithm that respects multi-tenancy boundaries.

## Decision

Implement a **weighted multi-factor scoring engine** (`MatchingService`) with five dimensions:

| Dimension | Default Weight | Source |
|---|---|---|
| Skill Compatibility | 35% | Bidirectional skill/goal overlap |
| Goal Alignment | 25% | Learning goal ↔ teaching skill match |
| Availability Overlap | 20% | `AvailabilityOverlapService` score |
| Proficiency Balance | 10% | Level gap between teacher and learner |
| Trust Score | 10% | Normalized `trust_scores.overall_score` |

Weights are configurable via `application.yml` under `app.matching.weights` and bound to `MatchingWeightsConfig`.

**Algorithm:**
```
totalScore = Σ (dimensionScore × dimensionWeight)
```

All dimension scores are normalized `[0, 1]` before weighting. The final score is a percentage `[0, 100]`.

**Candidate Filtering:**
- Candidates must be in the **same tenant** (multi-tenancy hard boundary).
- The requesting user is excluded from their own candidate list.
- Minimum score threshold is configurable (default: 0.1).

## Consequences

- ✅ Deterministic — same inputs always produce the same score.
- ✅ Explainable — each dimension score can be shown to the user (implemented in `MatchesPage`).
- ✅ Configurable — product team can tune weights via config, not code.
- ✅ Tenant-isolated — no cross-tenant data leakage.
- ⚠️ Does not use ML — a learning-to-rank model could improve relevance in Phase 5.
