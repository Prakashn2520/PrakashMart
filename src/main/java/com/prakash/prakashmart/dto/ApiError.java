package com.prakash.prakashmart.dto;

import java.io.Serializable;

/**
 * Standard error shape for API error responses.
 */
public class ApiError implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private Object details;

    public ApiError() {
    }

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiError(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}
