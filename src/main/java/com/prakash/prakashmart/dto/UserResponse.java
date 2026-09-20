package com.prakash.prakashmart.dto;

import com.prakash.prakashmart.model.User;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Safe public user representation for responses.
 * SECURITY: Never exposes password or password_hash.
 */
public class UserResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean isActive;
    private Timestamp createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, String role, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isActive = true;
        this.createdAt = createdAt;
    }

    public UserResponse(Long id, String name, String email, String role, Boolean isActive, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public UserResponse(Long id, String name, String email, com.prakash.prakashmart.model.Role role, Timestamp createdAt) {
        this(id, name, email, role != null ? role.name() : null, true, createdAt);
    }

    public UserResponse(Long id, String name, String email, com.prakash.prakashmart.model.Role role, Boolean isActive, Timestamp createdAt) {
        this(id, name, email, role != null ? role.name() : null, isActive, createdAt);
    }

    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getIsActive(),
                user.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
