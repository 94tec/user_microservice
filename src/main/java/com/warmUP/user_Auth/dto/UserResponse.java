package com.warmUP.user_Auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean emailVerified;

    public UserResponse() {
    }

    public UserResponse(Long id, String username, String email, String role, boolean emailVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
    }

}
