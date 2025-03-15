package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Invoice findByOrderId(Long orderId);
}