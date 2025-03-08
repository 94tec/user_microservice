package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.model.UserProfile;
import com.warmUP.user_Auth.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {
    @Autowired
    private UserProfileService userProfileService;

    // ✅ POST: Create or update user profile
    @PostMapping
    public ResponseEntity<UserProfile> saveUserProfile(@RequestBody UserProfile userProfile) {
        UserProfile savedProfile = userProfileService.saveUserProfile(userProfile);
        return ResponseEntity.ok(savedProfile);
    }

    // ✅ GET: Get user profile by user ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getUserProfileByUserId(@PathVariable Long userId) {
        UserProfile profile = userProfileService.getUserProfileByUserId(userId);
        return ResponseEntity.ok(profile);
    }
}
