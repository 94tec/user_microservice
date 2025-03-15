package com.warmUP.user_Auth.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String brand;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private int stockQuantity;

    public Product(Long id, String name, String description, double price, String imageUrl, String brand, Category category, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }
}
