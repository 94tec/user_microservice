package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.model.Order;
import com.warmUP.user_Auth.model.OrderItem;
import com.warmUP.user_Auth.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/history/{userId}")
    public List<Order> getOrderHistory(@PathVariable Long userId) {
        return orderService.getOrderHistory(userId);
    }

    @PostMapping("/create")
    public Order createOrder(@RequestParam Long userId, @RequestBody List<OrderItem> items, @RequestParam String paymentMethod) {
        return orderService.createOrder(userId, items, paymentMethod);
    }

    @PutMapping("/update-status")
    public void updateOrderStatus(@RequestParam Long orderId, @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
    }
}