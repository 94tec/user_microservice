package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
