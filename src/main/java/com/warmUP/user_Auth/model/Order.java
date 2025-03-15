package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Date orderDate;

    @Column(nullable = false)
    private double totalAmount;

    @Column(nullable = false)
    private String status; // e.g., PENDING, SHIPPED, DELIVERED

    @Column(nullable = false)
    private String paymentMethod; // e.g., Card, PayPal, M-Pesa

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    // Getters and setters
}
