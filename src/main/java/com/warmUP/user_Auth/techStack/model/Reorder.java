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
public class Reorder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reorderId;

    @NotNull(message = "Part ID is required")
    private Long partId; // Foreign key referencing Part

    @NotNull(message = "Reorder date is required")
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime reorderDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Status is required")
    private String status; // e.g., "PENDING", "ORDERED", "RECEIVED"

    // Optional fields
    @Column(nullable = true)
    private Long supplierId; // If reorder is to a specific supplier

    @Column(nullable = true)
    private String notes; // Any notes regarding the reorder

    @Column(nullable = true)
    private LocalDateTime expectedDeliveryDate;

}
