package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Data
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tokenValue; // Token value (e.g., JWT or UUID)

    @Column(nullable = false)
    private LocalDateTime expiryTime; // Token expiry time

    @Column(nullable = false)
    private Long userId; // ID of the user associated with the token

    private String sessionId;

    // Getters and setters
}
