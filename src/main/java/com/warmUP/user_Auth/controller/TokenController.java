package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.UserService;
import com.warmUP.user_Auth.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/generate")
    public ResponseEntity<String> generateToken(@RequestParam Long userId) {
        try {
            logger.info("Received request to generate token for user ID: {}", userId);

            if (userId == null) {
                logger.error("User ID cannot be null");
                return ResponseEntity.badRequest().body("User ID cannot be null");
            }

            User user = userService.getUserById(userId);

            String token = jwtUtil.generateToken(user);

            logger.info("Successfully generated token for user ID: {}", userId);
            return ResponseEntity.ok(token);

        } catch (ResourceNotFoundException e) {
            logger.error("User not found with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        } catch (Exception e) {
            logger.error("Failed to generate token for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate token");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        try {
            logger.info("Received request to validate token");

            if (token == null || token.trim().isEmpty()) {
                logger.error("Token cannot be null or empty");
                return ResponseEntity.badRequest().body(false);
            }

            // In a real application, you would typically validate the token using a Spring Security filter.
            // This is just a basic example.
            boolean isValid = jwtUtil.validateToken(token, userService.getUserById(1L)); // replace 1L with the user details.

            logger.info("Token validation result: {}", isValid);
            return ResponseEntity.ok(isValid);

        } catch (Exception e) {
            logger.error("Failed to validate token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}