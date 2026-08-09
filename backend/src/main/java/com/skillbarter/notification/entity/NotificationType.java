package com.skillbarter.notification.entity;

/**
 * Enumeration of all notification event types in SkillBarter.
 */
public enum NotificationType {

    // Exchange Request events
    EXCHANGE_REQUEST_RECEIVED,
    EXCHANGE_REQUEST_ACCEPTED,
    EXCHANGE_REQUEST_REJECTED,

    // Session events
    SESSION_SCHEDULED,
    SESSION_REMINDER,
    SESSION_CANCELLED,
    SESSION_COMPLETED,

    // Credit events
    CREDIT_EARNED,
    CREDIT_SPENT,

    // Review events
    REVIEW_REQUESTED,
    REVIEW_RECEIVED,

    // Dispute events
    DISPUTE_CREATED,
    DISPUTE_RESOLVED
}
