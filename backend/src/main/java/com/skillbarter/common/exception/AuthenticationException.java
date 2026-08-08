package com.skillbarter.common.exception;

/**
 * Thrown when an authentication attempt fails (invalid credentials, token issues).
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
