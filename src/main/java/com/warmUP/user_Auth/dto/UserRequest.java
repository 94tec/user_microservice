package com.warmUP.user_Auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    // Setters
    // Getters
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Role is required")
    private String role;

    @NotBlank(message = "Username is required")
    private String firstName;

    @NotBlank(message = "Username is required")
    private String lastName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private boolean isAdmin;
    // Default constructor
    public UserRequest() {
    }
    // Parameterized constructor
    public UserRequest(String username, String email, String role, String firstName, String lastName, String password, boolean isAdmin) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isAdmin = isAdmin;
    }
    

    @Override
    public String toString() {
        return "UserRegistrationRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isAdmin=" + isAdmin +
                '}';
    }

}
