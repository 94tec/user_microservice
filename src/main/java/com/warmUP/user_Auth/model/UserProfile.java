package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Setter
@Getter
@Data
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private boolean isPublic; // Profile privacy setting

    // Relationship with User (One-to-One)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserProfile() {}
    public UserProfile(Long id, String firstName, String lastName, String profilePictureUrl, String bio, boolean isPublic, User user) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePictureUrl = profilePictureUrl;
        this.bio = bio;
        this.isPublic = isPublic;
        this.user = user;
    }

}
