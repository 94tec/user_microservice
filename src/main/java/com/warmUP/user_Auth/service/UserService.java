package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.*;
import com.warmUP.user_Auth.repository.UserRepository;
import com.warmUP.user_Auth.security.SecurityConfig;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final TokenService tokenService;

    @Autowired
    @Lazy // Add this annotation
    private SecurityConfig securityConfig;

    // ✅ Constructor-based dependency injection (Best Practice)
    public UserService(
            UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.tokenService = tokenService;
    }

    // ✅ Get all users
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers(int page, int size) {
        logger.info("Fetching users with page={} and size={}", page, size);
        try {
            // 5. Pagination to handle large datasets
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);

            // 2. Check for empty database
            if (userPage.isEmpty()) {
                logger.warn("No users found.");
                throw new ResourceNotFoundException("No users found.");
            }

            // 7. Use DTOs to avoid serialization issues (not shown here, but recommended)
            List<User> users = userPage.getContent();

            return users;

        } catch (DataAccessException e) {
            // 1. Database connection issues
            logger.error("Error fetching users from the database.\", e");
            throw new ServiceException("Error fetching users from the database.", e);

        } catch (AccessDeniedException e) {
            // 4. Permission or authorization issues
            logger.error("You do not have permission to access this resource.", e);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.", e);

        } catch (IllegalArgumentException e) {
            // 8. Invalid user input (e.g., invalid pagination parameters)
            logger.error("Invalid pagination parameters.", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters.", e);

        } catch (ResourceNotFoundException e) {
            // 2. Empty database
            logger.error("NO CONTENT.", e);
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, e.getMessage());

        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ✅ Get a user by ID
    public User getUserById(Long id) {
        // Validate the ID
        if (id == null || id <= 0) {
            logger.error("Invalid user ID: {}", id);
            throw new InvalidUserIdException("Invalid user ID: " + id);
        }

        // Retrieve the user from the repository
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UserNotFoundException("User not found with ID: " + id);
                });
    }

    public User updateUser(Long id, User userDetails) {
        // Validate the user ID
        if (id == null || id <= 0) {
            logger.error("Invalid user ID: {}", id);
            throw new InvalidUserIdException("Invalid user ID: " + id);
        }

        // Validate the user details
        if (userDetails == null) {
            logger.error("User details are null");
            throw new InvalidUserDetailsException("User details cannot be null");
        }

        // Retrieve the user
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        // Update user fields
        updateUserFields(user, userDetails);

        // Save the updated user
        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", id);
        // Log the email verification action
        auditLogService.logAction("USER_UPDATED", user.getUsername());
        return updatedUser;
    }

    private void updateUserFields(User user, User userDetails) {
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }

        // Ensure password is re-encoded if updated
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (userDetails.getFirstName() != null) {
            user.setFirstName(userDetails.getFirstName());
        }

        if (userDetails.getLastName() != null) {
            user.setLastName(userDetails.getLastName());
        }

        if (userDetails.getEmail() != null) {
            user.setEmail(userDetails.getEmail());
        }

        if (userDetails.getRole() != null) {
            user.setRole(userDetails.getRole());
        }
    }

}