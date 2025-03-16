package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.dto.UserUpdateRequest;
import com.warmUP.user_Auth.exception.ServiceException;
import com.warmUP.user_Auth.exception.UnauthorizedException;
import com.warmUP.user_Auth.exception.UserNotFoundException;
import com.warmUP.user_Auth.model.Role;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.UserRepository;
import com.warmUP.user_Auth.util.PasswordGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final AuthService authService;

    @Autowired
    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            EmailService emailService, AuditLogService auditLogService,
                            AuthService authService
    )
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.authService = authService;
    }

    @Transactional
    public UserResponse createUserByAdmin(UserRequest userRequest, String adminUsername) {
        try {
            // ✅ Check if the admin has permission to create users
            if (!hasPermissionToCreateUser(adminUsername)) {
                throw new UnauthorizedException("Admin does not have permission to create users");
            }

            // ✅ Create the user using the existing method
            UserResponse userResponse = authService.createUser(userRequest);

            // ✅ Log the admin's action in the audit log
            auditLogService.logAction(
                    "USER_CREATED_BY_ADMIN",
                    "Admin " + adminUsername + " created user " + userResponse.getUsername()
            );

            logger.info("Admin {} created user {}", adminUsername, userResponse.getUsername());

            return userResponse;

        } catch (UnauthorizedException e) {
            logger.warn("User with username {} attempted to create a user without permission", adminUsername);
            throw e;

        } catch (Exception e) {
            logger.error("Unexpected error occurred while admin created user", e);
            throw new ServiceException("An error occurred while creating the user. Please try again.");
        }
    }
    private boolean hasPermissionToCreateUser(String adminUsername) {
        // Fetch the user from the database
        User adminUser = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new UnauthorizedException("User not found: " + adminUsername));

        // Log the role for debugging
        logger.info("Admin role for {}: {}", adminUsername, adminUser.getRole());

        // Convert the enum to a string
        String roleString = adminUser.getRole().name(); // Convert Role enum to String
        // Check if the user has the ROLE_ADMIN role
        if (!Role.ROLE_ADMIN.name().equals(roleString)) {
            throw new UnauthorizedException("Admin does not have permission to create users");
        }

        return true;
    }
    public void adminUpdateUser(Long userId, UserUpdateRequest updateRequest) {
        logger.info("Starting adminUpdateUser for userId: {}", userId);
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

            // Generate a secure temporary password
            String temporaryPassword = PasswordGenerator.generateTemporaryPassword(12);

            // Update user details
            if (updateRequest.getUsername() != null) {
                user.setUsername(updateRequest.getUsername());
                logger.debug("Username updated for userId: {}", userId);
            }
            if (updateRequest.getEmail() != null) {
                user.setEmail(updateRequest.getEmail());
                logger.debug("Email updated for userId: {}", userId);
            }

            // Set temporary password and expiry time (e.g., 24 hours from now)
            user.setTemporaryPassword(passwordEncoder.encode(temporaryPassword));
            user.setTemporaryPasswordExpiry(LocalDateTime.now().plusHours(24));

            // Force password reset
            user.setForcePasswordReset(true);

            // Save the updated user
            userRepository.save(user);
            logger.info("User updated and saved successfully for userId: {}", userId);

            // Send email to the user with the temporary password
            emailService.sendPasswordResetEmailToTheUser(user.getEmail(), temporaryPassword);
            logger.info("Password reset email sent to user: {}", user.getEmail());

        } catch (UserNotFoundException e) {
            logger.error("User not found during update for userId: {}", userId, e);
            throw e; // Re-throw the exception to maintain the original exception handling flow.
        } catch (Exception e) {
            logger.error("An error occurred during adminUpdateUser for userId: {}", userId, e);
            throw new RuntimeException("Error updating user", e); // Wrap general exceptions for better error handling.
        }
        logger.info("Finished adminUpdateUser for userId: {}", userId);
    }
}