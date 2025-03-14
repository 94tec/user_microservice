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

    @Column(nullable = false, insertable = false, updatable = false)
    private Long user_id;

    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Token() {
    }

    public Token(Long id, String tokenValue, LocalDateTime expiryTime, String sessionId, Long user_id) {
        this.id = id;
        this.tokenValue = tokenValue;
        this.expiryTime = expiryTime;
        this.sessionId = sessionId;
        this.user_id = user_id;
    }

    // Getters and setters (handled by Lombok @Data)
}