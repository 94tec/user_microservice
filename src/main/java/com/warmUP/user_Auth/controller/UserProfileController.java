package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.UserProfileDTO;
import com.warmUP.user_Auth.exception.InvalidUserProfileException;
import com.warmUP.user_Auth.exception.UserProfileNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.UserProfileService;
import com.warmUP.user_Auth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private final UserProfileService userProfileService;
    private final UserService userService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService, UserService userService) {
        this.userProfileService = userProfileService;
        this.userService = userService;
    }

    // ✅ POST: Create or update user profile
    @PostMapping("/{userId}")
    public ResponseEntity<?> saveUserProfile(@PathVariable Long userId, @RequestBody UserProfileDTO userProfileDTO) {
        try {

            User user = userService.getUserById(userId);

            UserProfileDTO savedProfile = userProfileService.createProfile(user, userProfileDTO);
            return ResponseEntity.ok(savedProfile);
        } catch (InvalidUserProfileException ex) {
            logger.error("Invalid user profile data: {}", ex.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            errorResponse.put("status", HttpStatus.BAD_REQUEST.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception ex) {
            logger.error("An unexpected error occurred while saving user profile: {}", ex.getMessage(), ex);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✅ GET: Get user profile by user ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserProfileByUserId(@PathVariable Long userId) {
        try {
            UserProfileDTO profile = userProfileService.getUserProfileByUserId(userId);
            return ResponseEntity.ok(profile);
        } catch (UserProfileNotFoundException ex) {
            logger.error("User profile not found: {}", ex.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception ex) {
            logger.error("An unexpected error occurred while fetching user profile: {}", ex.getMessage(), ex);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ✅ PUT: update user profile
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId, @RequestBody UserProfileDTO userProfileDTO) {
        try {
            UserProfileDTO updatedProfile = userProfileService.updateProfile(userId, userProfileDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (UserProfileNotFoundException ex) {
            logger.error("User profile not found: {}", ex.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            errorResponse.put("status", HttpStatus.NOT_FOUND.toString());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception ex) {
            logger.error("An unexpected error occurred while updating user profile: {}", ex.getMessage(), ex);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}