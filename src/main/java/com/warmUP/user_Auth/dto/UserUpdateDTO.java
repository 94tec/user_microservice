package com.warmUP.user_Auth.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    // Getters and setters

    // ... (getters and setters) ...

    @Override
    public String toString() {
        return "UserUpdateRequestDTO{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}