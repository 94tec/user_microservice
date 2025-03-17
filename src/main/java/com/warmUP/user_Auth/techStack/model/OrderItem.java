package com.warmUP.user_Auth.techStack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @NotNull(message = "Order ID is required")
    private Long orderId; // Foreign key referencing Order

    @NotNull(message = "Part ID is required")
    private Long partId; // Foreign key referencing Part

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price must be non-negative")
    private Double unitPrice;

    @NotNull(message = "Subtotal is required")
    @Min(value = 0, message = "Subtotal must be non-negative")
    private Double subtotal;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    // Optional Fields
    @Column(nullable = true)
    private String notes;

    @Column(nullable = true)
    private Double discount;

}