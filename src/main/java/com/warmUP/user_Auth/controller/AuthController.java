package com.warmUP.user_Auth.controller;

import com.warmUP.user_Auth.dto.*;
import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.service.AuthService;
import com.warmUP.user_Auth.service.EmailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/users")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final EmailService emailService;

    // ✅ Constructor-based dependency injection (Best Practice)
    @Autowired
    public AuthController(
            AuthService authService, EmailService emailService)
    {
        this.authService = authService;
        this.emailService = emailService;
    }

    // ✅ POST: Register a new user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            logger.info("Attempting to register user: {}", userRequest.getEmail()); // ✅ Logging input
            UserResponse userResponse = authService.createUser(userRequest);
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
            String tokens = authService.loginUser(loginRequestDto.getUsername(), loginRequestDto.getPassword());

            // Split the returned string to get access and refresh tokens
            String[] tokenArray = tokens.split(",");
            String jwt = tokenArray[0];
            String refreshToken = tokenArray[1];

            LoginResponseDto response = new LoginResponseDto();
            response.setToken(jwt);
            response.setRefreshToken(refreshToken);
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of(
                    "error", "Email not verified: " + e.getMessage(),
                    "status", HttpStatus.FORBIDDEN.value(),
                    "timestamp", Instant.now().toString()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
             .body(Map.of(
                    "error", "Invalid username or password: " + e.getMessage(),
                    "status", HttpStatus.UNAUTHORIZED.value(),
                    "timestamp", Instant.now().toString()
            ));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody PasswordChangeRequest changeRequest) {
        try {
            authService.changePassword(changeRequest);
            return ResponseEntity.ok(Map.of(
                    "message", "Password changed successfully.",
                    "status", HttpStatus.OK.value()
            ));
        } catch (UserNotFoundException ex) {
            logger.error("User not found: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "User not found: " + ex.getMessage(),
                            "status", HttpStatus.NOT_FOUND.value(),
                            "timestamp", Instant.now().toString()
                    ));
        } catch (InvalidPasswordException ex) {
            logger.error("Invalid old password: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "Invalid old password: " + ex.getMessage(),
                            "status", HttpStatus.UNAUTHORIZED.value(),
                            "timestamp", Instant.now().toString()
                    ));
        } catch (PasswordMismatchException ex) {
            logger.error("New passwords do not match: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "New passwords do not match: " + ex.getMessage(),
                            "status", HttpStatus.BAD_REQUEST.value(),
                            "timestamp", Instant.now().toString()
                    ));
        } catch (Exception ex) {
            logger.error("Unexpected error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Unexpected error: " + ex.getMessage(),
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "timestamp", Instant.now().toString()
                    ));
        }
    }


    // ✅ DELETE: Delete a user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            logger.warn("Attempted to delete non-existent user with id: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error deleting user with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }


    // ✅ POST: Generate password reset token
    @PostMapping("/generate-password-reset-token")
    public ResponseEntity<?> generatePasswordResetToken(@RequestParam String email) {
        try {
            authService.generatePasswordResetToken(email);
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
            User user = authService.resetPassword(passwordResetDTO.getToken(), passwordResetDTO.getNewPassword());

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
    // reset password forced by Admin
    @PostMapping("/password-reset")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetRequest resetRequest) {
        authService.resetPasswordForcedByAdmin(resetRequest);
        return ResponseEntity.ok("Password reset successfully.");
    }
    // ✅ POST: Register with social login
    @PostMapping("/register/social")
    public ResponseEntity<User> registerWithSocialLogin(
            @RequestParam String email,
            @RequestParam String provider,
            @RequestParam String providerId) {
        User user = authService.registerWithSocialLogin(email, provider, providerId);
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
            authService.sendEmailVerificationLink(email);

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
            authService.verifyEmail(token);
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
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam String username) {
        try {
            authService.logoutSpecificUser(username);
            return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
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
