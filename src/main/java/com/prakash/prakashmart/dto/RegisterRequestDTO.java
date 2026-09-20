package com.prakash.prakashmart.dto;

/**
 * Backward-compatible alias for RegisterRequest.
 */
public class RegisterRequestDTO extends RegisterRequest {
    private static final long serialVersionUID = 1L;

    public RegisterRequestDTO() {
        super();
    }

    public RegisterRequestDTO(String name, String email, String password, String role) {
        super(name, email, password, role);
    }
}
