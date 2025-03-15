package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Discount findByCode(String code);
}
