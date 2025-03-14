package com.warmUP.user_Auth.model;

import lombok.Data;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Role of the user

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private boolean emailVerified;

    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false) // createdAt should not be updated after creation
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastActivity;
    //social login
    private String provider; // e.g., "google", "facebook"

    private String providerId; // Unique ID from the provider

    // Password reset fields
    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

    // Email verification fields
    private String emailVerificationToken;

    private LocalDateTime emailVerificationTokenExpiry;

    // Relationship with UserProfile (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Token> tokens;

    // Relationship with AuditLog (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;
    // Default constructor (required by JPA)
    public User() {}


    // Parameterized constructor
    public User(Long id, String username, String password, Role role, String firstName, String lastName, String email, boolean emailVerified, boolean active)
    {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.emailVerified = emailVerified;
        this.active = active;
        this.createdAt = LocalDateTime.now(); // Set createdAt to the current time
        this.updatedAt = LocalDateTime.now(); // Set updatedAt to the current time
    }
    // ✅ Convert role to GrantedAuthority
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    // ✅ UserDetails methods
    @Override
    public boolean isAccountNonExpired() {
        return true; // Account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials never expire
    }

    @Override
    public boolean isEnabled() {
        return active; // Account is enabled if active is true
    }

        // Override toString() for better logging
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", emailVerified=" + emailVerified +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", passwordResetToken='" + passwordResetToken + '\'' +
                ", passwordResetTokenExpiry=" + passwordResetTokenExpiry +
                '}';
    }

}