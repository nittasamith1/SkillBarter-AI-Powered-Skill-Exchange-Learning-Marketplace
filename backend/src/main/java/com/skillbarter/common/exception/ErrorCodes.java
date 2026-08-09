package com.skillbarter.common.exception;

/**
 * Centralised error codes for all Phase 3 business exceptions.
 *
 * <p>Use these constants with {@link BusinessException} and
 * {@link ResourceNotFoundException} rather than hard-coding strings.
 */
public final class ErrorCodes {

    private ErrorCodes() {}

    // ── Availability ──────────────────────────────────────────
    public static final String AVAILABILITY_NOT_FOUND   = "AVAILABILITY_NOT_FOUND";
    public static final String INVALID_AVAILABILITY     = "INVALID_AVAILABILITY";
    public static final String AVAILABILITY_OVERLAP     = "AVAILABILITY_OVERLAP";

    // ── Matching ──────────────────────────────────────────────
    public static final String MATCH_NOT_FOUND          = "MATCH_NOT_FOUND";
    public static final String NO_ELIGIBLE_MATCH        = "NO_ELIGIBLE_MATCH";

    // ── Session ───────────────────────────────────────────────
    public static final String SESSION_NOT_FOUND                = "SESSION_NOT_FOUND";
    public static final String INVALID_SESSION_STATE            = "INVALID_SESSION_STATE";
    public static final String SESSION_CONFLICT                 = "SESSION_CONFLICT";
    public static final String SESSION_IN_PAST                  = "SESSION_IN_PAST";
    public static final String EXCHANGE_REQUEST_NOT_ACCEPTED    = "EXCHANGE_REQUEST_NOT_ACCEPTED";

    // ── Credits ───────────────────────────────────────────────
    public static final String CREDIT_WALLET_NOT_FOUND      = "CREDIT_WALLET_NOT_FOUND";
    public static final String INSUFFICIENT_CREDITS         = "INSUFFICIENT_CREDITS";
    public static final String CREDIT_ALREADY_SETTLED       = "CREDIT_ALREADY_SETTLED";
    public static final String INVALID_CREDIT_TRANSACTION   = "INVALID_CREDIT_TRANSACTION";

    // ── Reviews ───────────────────────────────────────────────
    public static final String REVIEW_NOT_ALLOWED       = "REVIEW_NOT_ALLOWED";
    public static final String REVIEW_ALREADY_EXISTS    = "REVIEW_ALREADY_EXISTS";
    public static final String INVALID_RATING           = "INVALID_RATING";

    // ── Notifications ─────────────────────────────────────────
    public static final String NOTIFICATION_NOT_FOUND   = "NOTIFICATION_NOT_FOUND";

    // ── Disputes ──────────────────────────────────────────────
    public static final String DISPUTE_NOT_FOUND        = "DISPUTE_NOT_FOUND";
    public static final String INVALID_DISPUTE_STATE    = "INVALID_DISPUTE_STATE";

    // ── Tenant ────────────────────────────────────────────────
    public static final String CROSS_TENANT_ACCESS_DENIED = "CROSS_TENANT_ACCESS_DENIED";
}
