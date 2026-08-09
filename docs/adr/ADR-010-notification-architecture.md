# ADR-010: Notification Architecture & Event Decoupling

**Date:** 2026-08-09  
**Status:** Accepted  
**Deciders:** SkillBarter Engineering Team

---

## Context

Phase 3 introduces critical user interactions (exchange request decisions, session scheduling updates, credit settlements, reviews, disputes). Users require real-time/in-app notifications to remain responsive.

## Decision

Implement an **in-app notification module** (`com.skillbarter.notification`) with support for event-driven creation:

### Domain Event Triggers

Notifications are generated synchronously by domain services during lifecycle operations:
1. `SessionService` $\rightarrow$ SESSION_SCHEDULED, SESSION_STARTED, SESSION_COMPLETED, SESSION_CANCELLED.
2. `CreditService` $\rightarrow$ CREDIT_EARNED (+1.0), CREDIT_SPENT (-1.0).
3. `ReviewService` $\rightarrow$ REVIEW_RECEIVED.
4. `DisputeService` $\rightarrow$ DISPUTE_OPENED, DISPUTE_RESOLVED.

### In-App UI Integration
- Frontend polls `/api/v1/notifications` periodically via `NotificationDropdown`.
- Unread badge counter updates dynamically.
- Mark as read single and bulk actions (`/notifications/{id}/read`, `/notifications/read-all`).

## Consequences

- ✅ Clear visibility into exchange workflow events for users.
- ✅ Multi-tenant isolated notification logs.
- ⚠️ In Phase 3 notifications are emitted synchronously; Phase 4 will transition to async Spring `@EventListener` or Kafka event broker.
