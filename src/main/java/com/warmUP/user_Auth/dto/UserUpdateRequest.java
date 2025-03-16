package com.warmUP.user_Auth.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String username; // New username (optional)
    private String email;    // New email (optional)
    private String temporaryPassword; // Temporary password set by admin


    // toString method (optional, for debugging)
    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", temporaryPassword='" + temporaryPassword + '\'' +
                '}';
    }
}
