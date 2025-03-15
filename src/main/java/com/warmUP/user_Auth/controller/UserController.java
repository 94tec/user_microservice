package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.EmailService;
import com.warmUP.user_Auth.service.UserService;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public UserController(
            UserService userService,EmailService emailService)
    {
        this.userService = userService;
        this.emailService = emailService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (InvalidUserIdException ex) {
            logger.error("Invalid user ID: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));
        } catch (ResourceNotFoundException ex) {
            logger.error("User not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.NOT_FOUND.toString()));
        } catch (InvalidUserDetailsException ex) {
            logger.error("Invalid user details: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));
        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage(), "status", HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        }
    }

    // ✅ Get (Access list users)
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Fetch users with pagination
            List<User> users = userService.getAllUsers(page, size);

            // Return users with HTTP 200 OK
            return ResponseEntity.ok(users);

        } catch (ResourceNotFoundException e) {
            // Handle empty database case (HTTP 204 No Content)
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());

        } catch (AccessDeniedException e) {
            // Handle permission issues (HTTP 403 Forbidden)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());

        } catch (IllegalArgumentException e) {
            // Handle invalid input (HTTP 400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (ServiceException e) {
            // Handle database or service errors (HTTP 500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        } catch (Exception e) {
            // Handle any other unexpected errors (HTTP 500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    // ✅ GET: Retrieve a user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (InvalidUserIdException ex) {
            logger.error("Invalid user ID: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));
        } catch (UserNotFoundException ex) {
            logger.error("User not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.NOT_FOUND.toString()));
        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage(), "status", HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        }
    }



}