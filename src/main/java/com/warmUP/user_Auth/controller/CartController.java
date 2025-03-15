package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.model.Cart;
import com.warmUP.user_Auth.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    public Cart getCart(@PathVariable Long userId) {
        return cartService.getCartByUserId(userId);
    }

    @PostMapping("/{userId}/add")
    public Cart addToCart(@PathVariable Long userId, @RequestParam Long productId, @RequestParam int quantity) {
        return cartService.addToCart(userId, productId, quantity);
    }

    @PutMapping("/{userId}/update")
    public Cart updateCartItemQuantity(@PathVariable Long userId, @RequestParam Long itemId, @RequestParam int quantity) {
        return cartService.updateCartItemQuantity(userId, itemId, quantity);
    }

    @DeleteMapping("/{userId}/remove")
    public Cart removeCartItem(@PathVariable Long userId, @RequestParam Long itemId) {
        return cartService.removeCartItem(userId, itemId);
    }

    @PostMapping("/{userId}/apply-discount")
    public Cart applyDiscount(@PathVariable Long userId, @RequestParam String discountCode) {
        return cartService.applyDiscount(userId, discountCode);
    }
}
