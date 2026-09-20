package com.prakash.prakashmart.dto;

/**
 * Backward-compatible alias for LoginRequest.
 */
public class LoginRequestDTO extends LoginRequest {
    private static final long serialVersionUID = 1L;

    public LoginRequestDTO() {
        super();
    }

    public LoginRequestDTO(String email, String password) {
        super(email, password);
    }
}
