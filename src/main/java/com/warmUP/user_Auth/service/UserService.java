package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.exception.PasswordResetException;
import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.exception.UserNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.UserRepository;
import com.warmUP.user_Auth.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                log.warn("Login attempt failed - user not found or password incorrect: {}", username);
                throw new BadCredentialsException("Invalid username or password");
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            UserDetails userDetails = loadUserByUsername(username); // Load user details once
            User user = findUserByUsername(username); //Retrieve the full user object

            // Check if the user's email is verified
            checkUserVerification(user);

            return jwtUtil.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            log.warn("Login attempt failed for user: {}", username, e);
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
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Get a user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // ✅ Update a user by ID
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id); // Throws ResourceNotFoundException if user not found
        updateUserFields(user, userDetails); // Update user fields
        return userRepository.save(user); // Save the updated user
    }

    // 🔹 Helper method to update user fields
    private void updateUserFields(User user, User userDetails) {
        if (userDetails.getUsername() != null) {
            user.setUsername(userDetails.getUsername());
        }

        // 🔹 Ensure password is re-encoded if updated
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
            log.warn("User not found with username: {}", username);
            throw new UserNotFoundException("User not found with username: " + username);
        }
    }
    // ✅ Delete a user by ID
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent user with id: {}", id);
            throw new UserNotFoundException("User not found with id: " + id);
        }
        try {
            userRepository.deleteById(id);
            log.info("User with id: {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage()); // Or a custom exception
        }
    }

    // ✅ Verify a user's email
    public void verifyEmail(Long userId) {
        User user = getUserById(userId); // Throws ResourceNotFoundException if user not found
        user.setEmailVerified(true);
        userRepository.save(user);
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
        } catch (ResourceNotFoundException ex) {
            throw ex; // Re-throw ResourceNotFoundException
        } catch (Exception ex) {
            throw new PasswordResetException("Failed to generate password reset token: " + ex.getMessage());
        }
    }

    // ✅ Reset password securely
    public User resetPassword(String token, String newPassword) {
        try {
            logger.info("Attempting to reset password for token: {}", token);
            User user = userRepository.findByPasswordResetToken(token)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

            if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
                logger.warn("Token expired for user: {}", user.getEmail());
                throw new PasswordResetException("Token expired");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetToken(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);
            logger.info("Password reset successfully for user: {}", user.getEmail());
        } catch (ResourceNotFoundException | PasswordResetException ex) {
            logger.error("Error resetting password: {}", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error resetting password: {}", ex.getMessage());
            throw new PasswordResetException("Failed to reset password: " + ex.getMessage());
        }
        return null;
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

    public boolean verifyEmail(String token) {
        try {
            // Extract the username from the token
            String username = jwtUtil.extractUsername(token);

            // Find the user by username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Mark the email as verified
            user.setEmailVerified(true);
            user.setActive(true);
            userRepository.save(user);

            return true;
        } catch (Exception e) {
            // Log the error
            System.err.println("Email verification failed: " + e.getMessage());
            return false;
        }
    }

}