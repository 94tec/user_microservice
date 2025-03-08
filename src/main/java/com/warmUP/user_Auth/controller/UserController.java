package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.UserService;
import com.warmUP.user_Auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ✅ POST: Register a new user
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    // ✅ POST: Login and generate JWT token
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody User user) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        final UserDetails userDetails = userService.loadUserByUsername(user.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return ResponseEntity.ok(response);
    }

    // ✅ POST: Logout (invalidate token)
    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser() {
        // In a stateless system, logout is handled client-side by discarding the token
        return ResponseEntity.ok().build();
    }

    // ✅ GET: Retrieve all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ✅ GET: Retrieve a user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id); // Throws ResourceNotFoundException if user not found
        return ResponseEntity.ok(user);
    }

    // ✅ PUT: Update a user by ID
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails); // Throws ResourceNotFoundException if user not found
        return ResponseEntity.ok(updatedUser);
    }

    // ✅ DELETE: Delete a user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id); // Throws ResourceNotFoundException if user not found
        return ResponseEntity.noContent().build();
    }

    // ✅ POST: Verify a user's email
    @PostMapping("/{id}/verify-email")
    public ResponseEntity<Void> verifyEmail(@PathVariable Long id) {
        userService.verifyEmail(id); // Throws ResourceNotFoundException if user not found
        return ResponseEntity.ok().build();
    }

    // ✅ POST: Reset a user's password
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        userService.resetPassword(token, newPassword); // Throws ResourceNotFoundException if token is invalid or expired
        return ResponseEntity.ok().build();
    }
    // ✅ POST: Register with social login
    @PostMapping("/register/social")
    public ResponseEntity<User> registerWithSocialLogin(
            @RequestParam String email,
            @RequestParam String provider,
            @RequestParam String providerId) {
        User user = userService.registerWithSocialLogin(email, provider, providerId);
        return ResponseEntity.ok(user);
    }

    // ✅ POST: Send email verification link
    @PostMapping("/send-verification-link")
    public ResponseEntity<Void> sendEmailVerificationLink(@RequestParam String email) {
        userService.sendEmailVerificationLink(email);
        return ResponseEntity.ok().build();
    }

    // ✅ POST: Verify email using token
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok().build();
    }
}