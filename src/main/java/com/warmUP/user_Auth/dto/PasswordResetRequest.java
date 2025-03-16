package com.warmUP.user_Auth.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    private String temporaryPassword; // Temporary password provided by the admin

    @Size(min = 8, message = "Temporary password must be at least 8 characters")
    private String newPassword;

    // toString method (optional, for debugging)
    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "username='" + username + '\'' +
                ", temporaryPassword='" + temporaryPassword + '\'' +
                ", newPassword='" + newPassword + '\'' +
                '}';
    }
}