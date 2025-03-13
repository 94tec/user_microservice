package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.TokenService;
import com.warmUP.user_Auth.service.UserService;
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
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    // ✅ Generate a token for a user
    @PostMapping("/generate")
    public ResponseEntity<String> generateToken(@RequestParam Long userId) {
        try {
            logger.info("Received request to generate token for user ID: {}", userId);

            // Validate input
            if (userId == null) {
                logger.error("User ID cannot be null");
                return ResponseEntity.badRequest().body("User ID cannot be null");
            }

            // Fetch the user
            User user = userService.getUserById(userId);

            // Generate the token
            String token = tokenService.generateToken(user);

            logger.info("Successfully generated token for user ID: {}", userId);
            return ResponseEntity.ok(token);

        } catch (ResourceNotFoundException e) {
            logger.error("User not found with ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + userId);

        } catch (Exception e) {
            logger.error("Failed to generate token for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate token. Please try again.");
        }
    }

    // ✅ Validate a token
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        try {
            logger.info("Received request to validate token");

            // Validate input
            if (token == null || token.trim().isEmpty()) {
                logger.error("Token cannot be null or empty");
                return ResponseEntity.badRequest().body(false);
            }

            // Validate the token
            boolean isValid = tokenService.validateToken(token);

            logger.info("Token validation result: {}", isValid);
            return ResponseEntity.ok(isValid);

        } catch (Exception e) {
            logger.error("Failed to validate token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    // ✅ Revoke a token
    @PostMapping("/revoke")
    public ResponseEntity<Void> revokeToken(@RequestParam String token) {
        try {
            logger.info("Received request to revoke token");

            // Validate input
            if (token == null || token.trim().isEmpty()) {
                logger.error("Token cannot be null or empty");
                return ResponseEntity.badRequest().build();
            }

            // Revoke the token
            tokenService.revokeToken(token);

            logger.info("Successfully revoked token");
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Failed to revoke token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
