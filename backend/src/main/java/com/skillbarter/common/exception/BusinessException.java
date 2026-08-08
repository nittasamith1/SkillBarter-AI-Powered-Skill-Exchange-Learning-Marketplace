package com.skillbarter.common.exception;

/**
 * Thrown when a business rule is violated (e.g., duplicate email, disabled account).
 */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String message) {
        this("BAD_REQUEST", message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
