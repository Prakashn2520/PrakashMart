package com.prakash.prakashmart.exception;

/**
 * Base custom checked exception for the PrakashMart application.
 * All application-specific exceptions extend this class and provide an errorCode.
 */
public class AppException extends Exception {

    private final String errorCode;

    public AppException(String message) {
        super(message);
        this.errorCode = "APP_ERROR";
    }

    public AppException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "APP_ERROR";
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "APP_ERROR";
    }

    public AppException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "APP_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
