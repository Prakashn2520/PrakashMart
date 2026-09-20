package com.prakash.prakashmart.exception;

/**
 * Thrown when an authenticated user attempts an action unauthorized for their role.
 */
public class AuthorizationException extends AppException {

    public AuthorizationException(String message) {
        super(message, "ACCESS_DENIED");
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, "ACCESS_DENIED", cause);
    }
}
