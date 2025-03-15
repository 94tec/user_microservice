package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.*;
import com.warmUP.user_Auth.repository.AuditLogRepository;
import com.warmUP.user_Auth.repository.TokenRepository;
import com.warmUP.user_Auth.repository.UserProfileRepository;
import com.warmUP.user_Auth.repository.UserRepository;
import com.warmUP.user_Auth.security.SecurityConfig;
import com.warmUP.user_Auth.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserProfileRepository userProfileRepository;
    private final AuditLogRepository auditLogRepository;
    private final TokenRepository tokenRepository;
    private final AuditLogService auditLogService;
    private final TokenService tokenService;
    private final UserActivityService userActivityService;

    @Autowired
    @Lazy // Add this annotation
    private SecurityConfig securityConfig;

    // ✅ Constructor-based dependency injection (Best Practice)
    public AuthService(
            UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            EmailService emailService, CustomUserDetailsService customUserDetailsService,
            UserProfileRepository userProfileRepository,
            AuditLogRepository auditLogRepository, TokenRepository tokenRepository,
            UserActivityService userActivityService, AuditLogService auditLogService,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.customUserDetailsService = customUserDetailsService;
        this.userProfileRepository = userProfileRepository;
        this.auditLogRepository = auditLogRepository;
        this.tokenRepository = tokenRepository;
        this.userActivityService = userActivityService;
        this.auditLogService = auditLogService;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        try {
            // ✅ Validate input
            validateUserRequest(userRequest);

            // ✅ Check if username or email already exists
            checkDuplicateUser(userRequest);

            // ✅ Create a new user
            User user = buildUserFromRequest(userRequest);

            // ✅ Save user to the database
            User savedUser = userRepository.save(user);

            // ✅ Assign role based on userRequest
            assignRoleToUser(savedUser, userRequest.getRole());

            // ✅ Create the UserProfile entity
            UserProfile profile = new UserProfile();
            profile.setUser(savedUser);
            profile.setProfilePictureUrl(userRequest.getProfilePictureUrl());
            profile.setFirstName(userRequest.getFirstName());
            profile.setLastName(userRequest.getLastName());
            profile.setBio(userRequest.getBio());
            userProfileRepository.save(profile);


            // ✅ Create the AuditLog entity
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(savedUser);
            auditLog.setUser_id(user.getId());
            auditLog.setUsername(user.getUsername());
            auditLog.setAction("USER_CREATED");
            auditLog.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(auditLog);

            // ✅ Send email verification link
            sendEmailVerification(savedUser);

            // ✅ Log the registration event
            logger.info("User registered successfully: {}", savedUser.getUsername());
            logger.info("Profile created successfully for user ID: {}", savedUser.getId());

            // ✅ Log the action in the audit log
            auditLogService.logAction(
                    "USER_CREATED",
                    "User " + savedUser.getUsername() + " was created"
            );

            // ✅ Return the user response
            return buildUserResponse(savedUser);

        } catch (DuplicateKeyException e) {
            logger.warn("User registration failed: Duplicate username or email - {}", e.getMessage());
            throw new DuplicateUserException("Username or email already exists");

        } catch (Exception e) {
            logger.error("Unexpected error occurred during user registration", e);
            throw new ServiceException("An error occurred during registration. Please try again.");
        }
    }


    private void validateUserRequest(UserRequest userRequest) {
        if (userRequest.getUsername() == null || userRequest.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRequest.getPassword() == null || userRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private void checkDuplicateUser(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateKeyException("Username already exists");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateKeyException("Email already exists");
        }
    }

    private User buildUserFromRequest(UserRequest userRequest) {
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Hash password
        user.setEmailVerified(false); // Email verification required
        user.setFirstName(userRequest.getFirstName()); // Set first name from request
        user.setLastName(userRequest.getLastName()); // Set last name from request
        user.setCreatedAt(LocalDateTime.now()); // Set creation timestamp
        user.setUpdatedAt(LocalDateTime.now()); // Set update timestamp
        user.setActive(false); // Set account as inactive by default

        // Optional: Handle provider and providerId if applicable
        user.setProvider(null); // Set to null or specify if using social login
        user.setProviderId(null); // Set to null or specify if using social login

        return user;
    }
    private void assignRoleToUser(User user, String roleString) {
        if (roleString != null) {
            try {
                // Convert the role string to the Role enum
                Role role = Role.valueOf(roleString);
                user.setRole(role);
            } catch (IllegalArgumentException ex) {
                // If the role is invalid, set the default role
                user.setRole(Role.ROLE_USER); // Default role
                logger.warn("Invalid role provided: {}. Defaulting to ROLE_USER.", roleString);
            }
        } else {
            // If no role is provided, set the default role
            user.setRole(Role.ROLE_USER); // Default role
        }
    }
    private void sendEmailVerification(User user) {
        String verificationToken = generateEmailVerificationToken(user);
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
    private UserResponse buildUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isEmailVerified()
        );
    }

    private String generateEmailVerificationToken(User user) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        return jwtUtil.generateToken(userDetails);
    }
    // ✅ Login a user and generate a JWT token
    @Transactional
    public String loginUser(String username, String password) {
        try {
            // Check if user exists in DB and password matches
            if (!isUserAvailable(username, password)) {
                logger.warn("Login attempt failed - user not found or password incorrect: {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username); // Load user details once
            User user = findUserByUsername(username); //Retrieve the full user object

            // Log the login action
            logger.info("User '{}' logged in successfully.", username);
            auditLogService.logAction("LOGIN", username );

            userActivityService.updateLastActivity(user.getId());

            // Check if the user's email is verified
            checkUserVerification(user);
            String accessToken = jwtUtil.generateToken(userDetails);
            String refreshToken = tokenService.generateRefreshToken(user);

            return accessToken + "," + refreshToken;

        } catch (BadCredentialsException e) {
            logger.warn("Login attempt failed for user: {}", username, e);
            throw new BadCredentialsException("Invalid username or password");
        }

    }

    public boolean isUserAvailable(String username, String password) {
        // Find user in DB
        User user = userRepository.findByUsername(username).orElse(null);

        // If user not found, return false
        if (user == null) {
            return false;
        }

        // Verify the password
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void checkUserVerification(User user) {
        if (!user.isEmailVerified()) {
            String message = String.format("Login attempt failed for user: %s - Email not verified", user.getUsername());
            log.warn(message);
            throw new IllegalStateException("Your email address has not been verified. Please check your inbox for a verification email.");
        }
    }
    public User findUserByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            logger.warn("User not found with username: {}", username);
            throw new UserNotFoundException("User not found with username: " + username);
        }
    }
    // ✅ Delete a user by ID
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            logger.warn("Attempted to delete non-existent user with id: {}", id);
            throw new UserNotFoundException("User not found with id: " + id);
        }
        try {
            userRepository.deleteById(id);
            logger.info("User with id: {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting user with id: {}", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage()); // Or a custom exception
        }
    }

    // ✅ Generate password reset token
    public void generatePasswordResetToken(String email) {
        try {
            // Find the user by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            // Generate a password reset token
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour

            // Save the updated user
            userRepository.save(user);
            // Send the password reset link
            emailService.sendPasswordResetEmail(user.getEmail(), token);

            // Log the event
            logger.info("Password Reset link sent to: {}", email);


        } catch (ResourceNotFoundException ex) {
            throw ex; // Re-throw ResourceNotFoundException
        } catch (Exception ex) {
            logger.error("Failed to generate password reset token: " + ex.getMessage());
            throw new PasswordResetException("Failed to generate password reset token: " + ex.getMessage());
        }
    }

    // ✅ Reset password securely
    public User resetPassword(String token, String newPassword) {
        logger.info("Attempting to reset password for token: {}", token);

        // Find the user by the reset token
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> {
                    logger.error("Invalid token: {}", token);
                    return new ResourceNotFoundException("Invalid token");
                });

        // Check if the token has expired
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            logger.warn("Token expired for user: {}", user.getEmail());
            throw new PasswordResetException("Token expired");
        }

        try {
            // Update the user's password and clear the reset token
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);

            // Save the updated user
            User savedUser = userRepository.save(user);
            logger.info("Password reset successfully for user: {}", savedUser.getEmail());
            // Log the reset password action
            auditLogService.logAction("PASSWORD_RESET", user.getUsername());
            return savedUser; // Return the updated user object
        } catch (Exception ex) {
            logger.error("Unexpected error resetting password: {}", ex.getMessage(), ex);
            throw new PasswordResetException("Failed to reset password: " + ex.getMessage());
        }
    }
    // ✅ Register user with social login
    public User registerWithSocialLogin(String email, String provider, String providerId) {
        User user = new User();
        user.setEmail(email);
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmailVerified(true); // Social logins are typically verified
        return userRepository.save(user);
    }

    // ✅ Send email verification link
    public void sendEmailVerificationLink(String email) {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate a secure JWT token for email verification
        String verificationToken = jwtUtil.generateToken(user.getEmail());

        // Set the verification token and expiry in the user entity
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
        userRepository.save(user);

        // Send the verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        // Log the event
        logger.info("Email verification link sent to: {}", email);
    }

    // ✅ Verify a user's email
    public void verifyEmail(String token) {
        try {
            // Validate the token
            if (token == null || token.isEmpty()) {
                logger.error("Invalid or empty token");
                throw new InvalidTokenException("Invalid or empty token");
            }

            // Extract the username from the token
            String username = jwtUtil.extractUsername(token);
            if (username == null || username.isEmpty()) {
                logger.error("Invalid token: Unable to extract username");
                throw new InvalidTokenException("Invalid token: Unable to extract username");
            }

            // Find the user by username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.error("User not found with username: {}", username);
                        return new UserNotFoundException("User not found with username: " + username);
                    });

            // Check if the email is already verified
            if (user.isEmailVerified()) {
                logger.warn("Email already verified for user: {}", user.getEmail());
                throw new EmailAlreadyVerifiedException("Email already verified for user: " + user.getEmail());
            }

            // Verify the email
            user.setEmailVerified(true);
            userRepository.save(user);
            logger.info("Email verified successfully for user: {}", user.getEmail());

            // Log the email verification action
            auditLogService.logAction("EMAIL_VERIFIED", username);
        } catch (JwtException ex) {
            logger.error("Token validation failed: {}", ex.getMessage());
            throw new InvalidTokenException("Token validation failed: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error during email verification: {}", ex.getMessage(), ex);
            throw new RuntimeException("Unexpected error during email verification: " + ex.getMessage());
        }
    }

    public void logoutCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                logger.warn("No user is currently authenticated.");
                throw new ResourceNotFoundException("No user is currently authenticated.");
            }

            String username = authentication.getName();

            // Invalidate refresh tokens
            tokenRepository.deleteByUserId(userRepository.findByUsername(username).get().getId());

            SecurityContextHolder.clearContext();
            logger.info("User '{}' has been logged out successfully.", username);
            auditLogService.logAction("LOGOUT", username);

        } catch (ResourceNotFoundException e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during logout: {}", e.getMessage(), e);
            throw new ServiceException("An unexpected error occurred during logout.", e);
        }
    }

    public void logoutSpecificUser(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));

            // Invalidate refresh tokens
            tokenRepository.deleteByUserId(user.getId());

            logger.info("User logged out successfully: {}", username);
            auditLogService.logAction("LOGOUT", username);

        } catch (UserNotFoundException e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            logger.error("Unexpected error during logout for user: {}", username, ex);
            throw new ServiceException("Unexpected error during logout: " + ex.getMessage(), ex);
        }
    }

}
