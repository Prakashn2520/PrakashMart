package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.User;

import java.sql.Timestamp;

/**
 * Backward-compatible alias for UserResponse.
 */
public class UserResponseDTO extends UserResponse {
    private static final long serialVersionUID = 1L;

    public UserResponseDTO() {
        super();
    }

    public UserResponseDTO(Long id, String name, String email, String role, Timestamp createdAt) {
        super(id, name, email, role, createdAt);
    }

    public UserResponseDTO(Long id, String name, String email, String role, Boolean isActive, Timestamp createdAt) {
        super(id, name, email, role, isActive, createdAt);
    }

    public UserResponseDTO(Long id, String name, String email, com.prakash.prakashmart.model.Role role, Timestamp createdAt) {
        super(id, name, email, role, createdAt);
    }

    public UserResponseDTO(Long id, String name, String email, com.prakash.prakashmart.model.Role role, Boolean isActive, Timestamp createdAt) {
        super(id, name, email, role, isActive, createdAt);
    }

    public static UserResponseDTO fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}
