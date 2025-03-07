package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ POST: Create a new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
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
}