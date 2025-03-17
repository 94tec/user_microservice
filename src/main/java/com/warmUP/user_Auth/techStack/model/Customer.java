package com.warmUP.user_Auth.techStack.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 255, message = "Customer name must be between 2 and 255 characters")
    private String customerName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String contactPhone;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String contactEmail;

    @Size(max = 1000, message = "Address cannot exceed 1000 characters")
    private String address;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Optional Fields
    @Column(nullable = true)
    private String customerCode;

    @Column(nullable = true)
    private String notes;

    @Column(nullable = true)
    private String preferredContactMethod; // Email, Phone, etc.

}