package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
