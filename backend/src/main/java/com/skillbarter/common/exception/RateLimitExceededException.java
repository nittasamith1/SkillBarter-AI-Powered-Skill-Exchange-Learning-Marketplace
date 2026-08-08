package com.skillbarter.common.exception;

/**
 * Thrown when an IP address exceeds its allowed request rate.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
