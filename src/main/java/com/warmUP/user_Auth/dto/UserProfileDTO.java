package com.warmUP.user_Auth.dto;

import lombok.Data;

// UserProfileDTO.java
@Data
public class UserProfileDTO {
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private boolean isPublic;
}