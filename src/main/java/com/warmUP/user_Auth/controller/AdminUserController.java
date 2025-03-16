package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.UserRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.dto.UserUpdateRequest;
import com.warmUP.user_Auth.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest userRequest,
            @RequestHeader("Admin-Username") @NotBlank String adminUsername
    ) {
        logger.info("Received request to create user by admin: {}", adminUsername);
        UserResponse userResponse = adminUserService.createUserByAdmin(userRequest, adminUsername);
        logger.info("User created successfully by admin: {}", adminUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUserByAdmin(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest updateRequest) {
        try {
            adminUserService.adminUpdateUser(userId, updateRequest);
            return ResponseEntity.ok("User updated successfully.");
        } catch (com.warmUP.user_Auth.exception.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user: " + e.getMessage());
        }
    }
}