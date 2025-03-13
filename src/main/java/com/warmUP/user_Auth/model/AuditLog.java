package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action; // e.g., "LOGIN", "LOGOUT", "UPDATE_PROFILE"

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime timestamp;


    // Relationship with User (Many-to-One)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public AuditLog() {
    }

    public AuditLog(Long id, String action, String username, LocalDateTime timestamp) {
        this.id = id;
        this.action = action;
        this.username = username;
        this.timestamp = timestamp;
    }

}
