package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.Cart;
import com.warmUP.user_Auth.model.CartItem;
import com.warmUP.user_Auth.model.Discount;
import com.warmUP.user_Auth.model.Product;
import com.warmUP.user_Auth.repository.CartRepository;
import com.warmUP.user_Auth.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private DiscountRepository discountRepository;

    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public Cart addToCart(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setTotalPrice(0.0);
        }

        // Fetch product details
        Product product = productService.getProductById(productId);
        double itemPrice = product.getPrice() * quantity;

        // Add item to cart
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProductId(productId);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice());

        cart.getItems().add(cartItem);
        cart.setTotalPrice(cart.getTotalPrice() + itemPrice);

        return cartRepository.save(cart);
    }

    public Cart updateCartItemQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        Optional<CartItem> cartItemOptional = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            double oldItemPrice = cartItem.getPrice() * cartItem.getQuantity();
            cartItem.setQuantity(quantity);
            double newItemPrice = cartItem.getPrice() * quantity;
            cart.setTotalPrice(cart.getTotalPrice() - oldItemPrice + newItemPrice);
            return cartRepository.save(cart);
        } else {
            throw new RuntimeException("Item not found in cart");
        }
    }

    public Cart removeCartItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        Optional<CartItem> cartItemOptional = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            double itemPrice = cartItem.getPrice() * cartItem.getQuantity();
            cart.getItems().remove(cartItem);
            cart.setTotalPrice(cart.getTotalPrice() - itemPrice);
            return cartRepository.save(cart);
        } else {
            throw new RuntimeException("Item not found in cart");
        }
    }

    public Cart applyDiscount(Long userId, String discountCode) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new RuntimeException("Cart not found");
        }

        Discount discount = discountRepository.findByCode(discountCode);
        if (discount == null || discount.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Invalid or expired discount code");
        }

        double discountValue = discount.getValue();
        if (discount.getDiscountType().equals("PERCENTAGE")) {
            discountValue = cart.getTotalPrice() * (discountValue / 100);
        }

        cart.setTotalPrice(cart.getTotalPrice() - discountValue);
        cart.setDiscountCode(discountCode);
        return cartRepository.save(cart);
    }
}