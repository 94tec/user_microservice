package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.UserDTO;
import com.warmUP.user_Auth.dto.UserUpdateDTO;
import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public UserController(
            UserService userService)
    {
        this.userService = userService;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userDetails) {
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

    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("Fetching users with page={}, size={}", page, size);

            // Fetch users with pagination
            List<UserDTO> users = userService.getAllUsers(page, size);

            // Return users with HTTP 200 OK
            return ResponseEntity.ok(users);

        } catch (InvalidPaginationParameterException ex) {
            logger.error("Invalid pagination parameters: page={}, size={}", page, size, ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));

        } catch (ResourceNotFoundException ex) {
            logger.error("No users found.", ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));

        } catch (ServiceException ex) {
            logger.error("Service error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));

        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage(), "status", HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        try {
            logger.info("Fetching user with ID: {}", id);

            // Fetch the user by ID
            UserDTO user = userService.findUserById(id, currentUser);

            // Return the user with HTTP 200 OK
            return ResponseEntity.ok(user);

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



}