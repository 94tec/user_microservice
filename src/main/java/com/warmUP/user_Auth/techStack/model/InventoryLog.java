package com.warmUP.user_Auth.techStack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @NotNull(message = "Part ID is required")
    private Long partId; // Foreign key referencing Part

    @NotNull(message = "Log date is required")
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime logDate;

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange; // Positive for increase, negative for decrease

    @NotNull(message = "Reason is required")
    private String reason; // e.g., "SALE", "PURCHASE", "ADJUSTMENT"

    @NotNull(message = "Current stock is required")
    private Integer currentStock; // Stock after the change

    // Optional fields for more context
    @Column(nullable = true)
    private Long orderId; // If the change is related to an order

    @Column(nullable = true)
    private Long userId; // If the change was made by a user

    @Column(nullable = true)
    private String notes; // Additional notes about the change

}
