package com.prakash.prakashmart.dto;

import java.io.Serializable;

/**
 * DTO for user login requests.
 */
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        // SECURITY: Never include password in toString()
        return "LoginRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
