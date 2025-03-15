package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String discountType; // e.g., PERCENTAGE, FIXED

    @Column(nullable = false)
    private double value;

    @Column(nullable = false)
    private Date expiryDate;

    // Getters and setters
}
