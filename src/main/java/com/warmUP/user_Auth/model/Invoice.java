package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Date invoiceDate;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private boolean emailSent;

    // Getters and setters
}
