package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.UserDTO;
import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.EmailService;
import com.warmUP.user_Auth.service.UserService;
import org.hibernate.annotations.Parameter;
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
    public ResponseEntity<List<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<UserDTO> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
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