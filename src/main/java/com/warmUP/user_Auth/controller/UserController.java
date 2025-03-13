package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.*;
import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.EmailService;
import com.warmUP.user_Auth.service.UserService;
import com.warmUP.user_Auth.util.JwtUtil;
import jakarta.validation.Valid;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public UserController(
            UserService userService,EmailService emailService, AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.userService = userService;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // ✅ POST: Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            logger.info("Attempting to register user: {}", userRequest.getEmail()); // ✅ Logging input
            UserResponse userResponse = userService.createUser(userRequest);
            logger.info("User registered successfully: {}", userResponse.getId()); // ✅ Logging success
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (DuplicateKeyException e) {
            logger.warn("Duplicate user found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Username or email already exists"));
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
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

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        try {
            // Call the logout service
            userService.logoutUser();

            // Return success response
            return ResponseEntity.ok("User logged out successfully.");

        } catch (ResourceNotFoundException e) {
            // Handle case where no user is authenticated
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (ServiceException e) {
            // Handle service-level errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        } catch (Exception e) {
            // Handle unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred during logout.");
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

    // ✅ DELETE: Delete a user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    // ✅ POST: Generate password reset token
    @PostMapping("/generate-password-reset-token")
    public ResponseEntity<?> generatePasswordResetToken(@RequestParam String email) {
        try {
            userService.generatePasswordResetToken(email);
            return ResponseEntity.ok(Map.of("message", "Password reset token generated successfully"));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.NOT_FOUND.toString()));
        } catch (PasswordResetException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDTO) {
        try {
            // Reset the password and get the updated user object
            User user = userService.resetPassword(passwordResetDTO.getToken(), passwordResetDTO.getNewPassword());

            // Send a confirmation email
            emailService.sendEmail(user.getEmail(), "Password Reset Confirmation", "Your password has been reset successfully.");

            // Return success response
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (ResourceNotFoundException ex) {
            logger.error("Resource not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.NOT_FOUND.toString()));
        } catch (PasswordResetException ex) {
            logger.error("Password reset error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));
        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage(), "status", HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        }
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

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        } catch (InvalidTokenException ex) {
            logger.error("Invalid token: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.BAD_REQUEST.toString()));
        } catch (UserNotFoundException ex) {
            logger.error("User not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.NOT_FOUND.toString()));
        } catch (EmailAlreadyVerifiedException ex) {
            logger.error("Email already verified: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage(), "status", HttpStatus.CONFLICT.toString()));
        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + ex.getMessage(), "status", HttpStatus.INTERNAL_SERVER_ERROR.toString()));
        }
    }

}