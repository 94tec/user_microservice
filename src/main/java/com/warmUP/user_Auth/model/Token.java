package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "refresh_tokens")
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

    private  boolean revoked;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    public Token() {
    }

    public Token(Long id, String tokenValue, LocalDateTime expiryTime, Long user_id) {
        this.id = id;
        this.tokenValue = tokenValue;
        this.expiryTime = expiryTime;
        this.user_id = user_id;
    }


}