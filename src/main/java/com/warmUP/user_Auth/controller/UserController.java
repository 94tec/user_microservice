package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.LoginRequestDto;
import com.warmUP.user_Auth.dto.LoginResponseDto;
import com.warmUP.user_Auth.dto.UserRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.UserService;
import com.warmUP.user_Auth.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ✅ POST: Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            log.info("Attempting to register user: {}", userRequest.getEmail()); // ✅ Logging input
            UserResponse userResponse = userService.createUser(userRequest);
            log.info("User registered successfully: {}", userResponse.getId()); // ✅ Logging success
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (DuplicateKeyException e) {
            log.warn("Duplicate user found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username or email already exists"));
        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An error occurred during registration"));
        }
    }

    // ✅ POST: Login and generate JWT token
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            // ✅ Use loginUser service method
            String jwt = userService.loginUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());

            LoginResponseDto response = new LoginResponseDto();
            response.setToken(jwt);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email not verified");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
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
        userService.deleteUser(id);
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

    /**
     * Sends an email verification link to the user.
     *
     * @param email The email address of the user.
     * @return A ResponseEntity with HTTP status 200 (OK) if successful,
     *         or HTTP status 404 (Not Found) if the user is not found.
     */
    @PostMapping("/send-verification-link")
    public ResponseEntity<?> sendEmailVerificationLink(@RequestParam String email) {
        try {
            // Call the service method to send the verification email
            userService.sendEmailVerificationLink(email);

            // Return HTTP 200 OK
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            // Return HTTP 404 Not Found with an error message
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Return HTTP 500 Internal Server Error with an error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred. Please try again later."));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        boolean isVerified = userService.verifyEmail(token);

        if (isVerified) {
            return ResponseEntity.ok("Email verified successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token.");
        }
    }

}