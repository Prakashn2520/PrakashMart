package com.prakash.prakashmart.dto;

import java.io.Serializable;

/**
 * Standard API response envelope supporting both success and error payloads.
 *
 * Success format:
 * {
 *   "success": true,
 *   "message": "Operation completed successfully",
 *   "data": { ... }
 * }
 *
 * Error format:
 * {
 *   "success": false,
 *   "message": "Product not found",
 *   "errorCode": "PRODUCT_NOT_FOUND"
 * }
 *
 * @param <T> Payload data type
 */
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private ApiError error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, String errorCode, T data, ApiError error) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", null, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, message, errorCode, null, new ApiError(errorCode, message));
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, Object details) {
        return new ApiResponse<>(false, message, errorCode, null, new ApiError(errorCode, message, details));
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, "ERROR", null, new ApiError("ERROR", message));
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
        if (error != null) {
            this.errorCode = error.getCode();
            if (this.message == null) {
                this.message = error.getMessage();
            }
        }
    }
}
