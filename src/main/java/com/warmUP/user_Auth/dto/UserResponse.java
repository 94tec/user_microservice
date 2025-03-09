package com.warmUP.user_Auth.dto;

public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;

    // Constructors, GetterS and SetterS
    public UserResponse() {
    }

    public UserResponse(Long id, String username, String email, boolean emailVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.emailVerified = emailVerified;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
