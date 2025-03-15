package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.Invoice;
import com.warmUP.user_Auth.model.Order;
import com.warmUP.user_Auth.model.OrderItem;
import com.warmUP.user_Auth.repository.InvoiceRepository;
import com.warmUP.user_Auth.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private EmailService emailService;

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order createOrder(Long userId, List<OrderItem> items, String paymentMethod) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(new Date());
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING");

        double totalAmount = items.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        order.setTotalAmount(totalAmount);

        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        // Generate and send invoice
        generateInvoice(savedOrder);

        return savedOrder;
    }

    private void generateInvoice(Order order) {
        Invoice invoice = new Invoice();
        invoice.setOrderId(order.getId());
        invoice.setInvoiceDate(new Date());
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setEmailSent(false);

        invoiceRepository.save(invoice);

        // Send invoice email
        emailService.sendInvoiceEmail(order.getUserId(), invoice);
    }

    public void updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        orderRepository.save(order);
    }
}
