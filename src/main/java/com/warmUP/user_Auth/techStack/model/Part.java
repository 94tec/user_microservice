package com.warmUP.user_Auth.techStack.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class) // Enable auditing
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Part name is required")
    @Size(min = 2, max = 255, message = "Part name must be between 2 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be a positive value")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotBlank(message = "Model compatibility is required")
    private String modelCompatibility;

    @Size(max = 255, message = "Image URL cannot exceed 255 characters")
    private String imageUrl;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Additional fields for security and tracking
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true; // Soft delete or availability flag

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isFeatured = false;

    @Column(nullable = true)
    private String partNumber; // Manufacturer part number

    @Column(nullable = true)
    private String supplier;

}