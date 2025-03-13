package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.exception.*;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.UserRepository;
import com.warmUP.user_Auth.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // ✅ Constructor-based dependency injection (Best Practice)
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy AuthenticationManager authenticationManager, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }
    // ✅ Load user by username (required by UserDetailsService)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), // Username
                user.getPassword(), // Password (already encoded)
                user.getAuthorities() // Roles/authorities
        );
    }
    // ✅ Create a new user with an encoded password
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        try {
            // ✅ Validate input
            validateUserRequest(userRequest);

            // ✅ Check if username or email already exists
            checkDuplicateUser(userRequest);

            // ✅ Create a new user
            User user = new User();
            user.setUsername(userRequest.getUsername());
            user.setEmail(userRequest.getEmail());
            user.setPassword(passwordEncoder.encode(userRequest.getPassword())); // Hash password
            user.setEmailVerified(false); // Email verification required
            user.setRole(userRequest.getRole()); // Assign default role
            user.setFirstName(userRequest.getFirstName()); // Set first name from request
            user.setLastName(userRequest.getLastName()); // Set last name from request
            user.setCreatedAt(LocalDateTime.now()); // Set creation timestamp
            user.setUpdatedAt(LocalDateTime.now()); // Set update timestamp
            user.setActive(false); // Set account as inactive by default

            // Optional: Handle provider and providerId if applicable
            user.setProvider(null); // Set to null or specify if using social login
            user.setProviderId(null); // Set to null or specify if using social login

            // Save the user to the database
            userRepository.save(user);


            // ✅ Save user to the database
            User savedUser = userRepository.save(user);

            // ✅ Assign role based on isAdmin flag
            if (userRequest.getRole() != null && (userRequest.getRole().equals("ROLE_ADMIN") || userRequest.getRole().equals("ROLE_USER"))) {
                user.setRole(userRequest.getRole());
            } else {
                user.setRole("ROLE_USER"); // Default role
            }

            // ✅ Send email verification link
            String verificationToken = generateEmailVerificationToken(savedUser);
            emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

            // ✅ Log the registration event
            logger.info("User registered successfully: {}", savedUser.getUsername());

            // ✅ Return the user response
            return new UserResponse(
                    savedUser.getId(),
                    savedUser.getUsername(),
                    savedUser.getEmail(),
                    savedUser.getRole(),
                    savedUser.isEmailVerified()
            );

        } catch (DuplicateKeyException e) {
            logger.warn("User registration failed: Duplicate username or email - {}", e.getMessage());
            throw e; // Re-throw so it can be handled in the controller

        } catch (Exception e) {
            logger.error("Unexpected error occurred during user registration", e);
            throw new RuntimeException("An error occurred during registration. Please try again.");
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
    private String generateEmailVerificationToken(User user) {
        UserDetails userDetails = loadUserByUsername(user.getUsername());
        return jwtUtil.generateToken(userDetails);
    }
    // ✅ Login a user and generate a JWT token
    public String loginUser(String username, String password) {
        try {
            // Check if user exists in DB and password matches
            if (!isUserAvailable(username, password)) {
                logger.warn("Login attempt failed - user not found or password incorrect: {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            UserDetails userDetails = loadUserByUsername(username); // Load user details once
            User user = findUserByUsername(username); //Retrieve the full user object

            // Check if the user's email is verified
            checkUserVerification(user);

            return jwtUtil.generateToken(userDetails);
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

    // ✅ Get all users
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
        return updatedUser;
    }

    /**
     * Helper method to update user fields.
     *
     * @param user        The user to update.
     * @param userDetails The updated user details.
     */
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
            user.setActive(true);
            userRepository.save(user);
            logger.info("Email verified successfully for user: {}", user.getEmail());
        } catch (JwtException ex) {
            logger.error("Token validation failed: {}", ex.getMessage());
            throw new InvalidTokenException("Token validation failed: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error during email verification: {}", ex.getMessage(), ex);
            throw new RuntimeException("Unexpected error during email verification: " + ex.getMessage());
        }
    }

    public void logoutUser() {
        try {
            // Get the current authentication object
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                logger.warn("No user is currently authenticated.");
                throw new ResourceNotFoundException("No user is currently authenticated.");
            }

            // Perform logout (invalidate session, clear security context, etc.)
            SecurityContextHolder.clearContext();
            logger.info("User '{}' has been logged out successfully.", authentication.getName());

        } catch (ResourceNotFoundException e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw e; // Re-throw for global exception handling
        } catch (Exception e) {
            logger.error("An unexpected error occurred during logout: {}", e.getMessage(), e);
            throw new ServiceException("An unexpected error occurred during logout.", e);
        }
    }

}