package com.prakash.prakashmart.exception;

/**
 * Thrown when an underlying database / SQL error occurs in the DAO layer.
 */
public class DatabaseException extends AppException {

    public DatabaseException(String message) {
        super(message, "DATABASE_ERROR");
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, "DATABASE_ERROR", cause);
    }
}
