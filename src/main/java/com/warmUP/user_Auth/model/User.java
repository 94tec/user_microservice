package com.warmUP.user_Auth.model;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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
    @Setter
    @Getter
    private Long id;

    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private String password;
    @Setter
    @Getter
    private String role; // e.g., ROLE_USER, ROLE_ADMIN

    // New fields
    @Setter
    @Getter
    private String firstName;
    @Setter
    @Getter
    private String lastName;
    @Setter
    @Getter
    private String email;

    // Audit fields
    // Auto-generated fields
    @Setter
    @Getter
    private boolean emailVerified;
    @Setter
    @Getter
    private boolean active = false; // Default to true, set to false if account is deactivated

    @Setter
    @Getter
    @Column(updatable = false) // createdAt should not be updated after creation
    private LocalDateTime createdAt;

    @Setter
    @Getter
    private LocalDateTime updatedAt;
    //social login
    @Setter
    @Getter
    private String provider; // e.g., "google", "facebook"
    @Setter
    @Getter
    private String providerId; // Unique ID from the provider
    // Password reset fields
    @Setter
    @Getter
    private String passwordResetToken;

    @Setter
    @Getter
    private LocalDateTime passwordResetTokenExpiry;

    // Email verification fields
    @Setter
    @Getter
    private String emailVerificationToken;

    @Setter
    @Getter
    private LocalDateTime emailVerificationTokenExpiry;
    // Relationship with UserProfile (One-to-One)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    // Relationship with AuditLog (One-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AuditLog> auditLogs;
    // Default constructor (required by JPA)
    public User() {}

    // Parameterized constructor
    public User(Long id, String username, String password, String role, String firstName, String lastName, String email, boolean emailVerified, boolean active)
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
        if (role != null) {
            return Collections.singletonList(new SimpleGrantedAuthority(role));
        }
        return Collections.emptyList();
    }

    //✅ UserDetails methods
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