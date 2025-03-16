package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserDTO;
import com.warmUP.user_Auth.dto.UserProfileDTO;
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


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    public List<UserDTO> getAllUsers(int page, int size) {
        logger.info("Fetching users with page={}, size={}", page, size);

        try {
            // Validate pagination parameters
            if (page < 0 || size <= 0) {
                logger.error("Invalid pagination parameters: page={}, size={}", page, size);
                throw new IllegalArgumentException("Page must be >= 0 and size must be > 0.");
            }

            // Pagination
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);

            // Check for empty database
            if (userPage.isEmpty()) {
                logger.warn("No users found.");
                throw new ResourceNotFoundException("No users found.");
            }

            // Map User entities to UserDTOs
            List<UserDTO> userDTOs = userPage.getContent().stream()
                    .map(this::mapToUserDTO)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} users from the database.", userPage.getNumberOfElements());
            return userDTOs;

        } catch (DataAccessException e) {
            logger.error("Error fetching users from the database.", e);
            throw new ServiceException("Error fetching users from the database.", e);

        } catch (AccessDeniedException e) {
            logger.error("You do not have permission to access this resource.", e);
            throw new CustomException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");

        } catch (IllegalArgumentException e) {
            logger.error("Invalid pagination parameters.", e);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters.");

        } catch (ResourceNotFoundException e) {
            logger.error("NO CONTENT.", e);
            throw new CustomException(HttpStatus.NO_CONTENT, e.getMessage());

        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            throw e;
        }
    }
    // ✅ Get a user by ID
    public UserDTO findUserById(Long id) {
        // Validate the ID
        if (id == null || id <= 0) {
            logger.error("Invalid user ID: {}", id);
            throw new InvalidUserIdException("Invalid user ID: " + id);
        }

        // Retrieve the user from the repository
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UserNotFoundException("User not found with ID: " + id);
                });

        // Map the User entity to UserDTO
        return mapToUserDTO(user);
    }
    private UserDTO mapToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setEmailVerified(user.isEmailVerified());
        userDTO.setActive(user.isActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setLastActivity(user.getLastActivity());
        userDTO.setProvider(user.getProvider());
        userDTO.setProviderId(user.getProviderId());

        // Map UserProfile to UserProfileDTO
        if (user.getUserProfile() != null) {
            UserProfileDTO userProfileDTO = new UserProfileDTO();
            userProfileDTO.setFirstName(user.getUserProfile().getFirstName());
            userProfileDTO.setLastName(user.getUserProfile().getLastName());
            userProfileDTO.setProfilePictureUrl(user.getUserProfile().getProfilePictureUrl());
            userProfileDTO.setBio(user.getUserProfile().getBio());
            userProfileDTO.setPublic(user.getUserProfile().isPublic());
            userDTO.setUserProfile(userProfileDTO);
        }

        return userDTO;
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
    // Get a user by ID
    public User getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);

        // Validate the ID
        if (id == null || id <= 0) {
            logger.error("Invalid user ID: {}", id);
            throw new InvalidUserIdException("Invalid user ID: " + id);
        }

        // Fetch the user from the repository
        Optional<User> userOptional = userRepository.findById(id);

        // Check if the user exists
        if (userOptional.isEmpty()) {
            logger.error("User not found with ID: {}", id);
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        return userOptional.get();
    }

}