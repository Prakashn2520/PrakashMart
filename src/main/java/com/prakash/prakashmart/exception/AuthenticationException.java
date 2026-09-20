package com.prakash.prakashmart.exception;

/**
 * Thrown when user authentication fails (e.g. invalid email/password).
 */
public class AuthenticationException extends AppException {

    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}
