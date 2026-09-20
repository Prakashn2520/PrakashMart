package com.prakash.prakashmart.exception;

/**
 * Thrown when input validation fails in the service or controller layer.
 */
public class ValidationException extends AppException {

    private final String field;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.field = null;
    }

    public ValidationException(String field, String message) {
        super(message, "VALIDATION_ERROR");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
