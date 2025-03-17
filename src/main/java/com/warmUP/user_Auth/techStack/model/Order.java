package com.warmUP.user_Auth.techStack.model;

import com.warmUP.user_Auth.model.OrderItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(name = "orders") // Avoid conflict with SQL reserved word "order"
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @NotNull(message = "Order date is required")
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime orderDate;

    @Column(nullable = true)
    private Long supplierId; // Nullable if it's a customer order

    @Column(nullable = true)
    private Long customerId; // Nullable if it's a supplier order

    @NotNull(message = "Order type is required")
    private String orderType; // e.g., "SUPPLIER", "CUSTOMER"

    @NotNull(message = "Order status is required")
    private String orderStatus; // e.g., "PENDING", "PROCESSING", "SHIPPED", "COMPLETED"

    @NotNull(message = "Total amount is required")
    private Double totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
