package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserRegistrationRequest;
import com.warmUP.user_Auth.dto.UserResponse;
import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.UserRepository;
import com.warmUP.user_Auth.util.JwtUtil;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
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
    public UserResponse createUser(UserRegistrationRequest userRequest) {
        // Validate input
        if (userRequest.getUsername() == null || userRequest.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (userRequest.getEmail() == null || userRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRequest.getPassword() == null || userRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Check if username or email already exists
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new DuplicateKeyException("Username already exists");
        }
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateKeyException("Email already exists");
        }

        // Create a new user
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        user.setEmailVerified(false); // Email verification required
        user.setRole("ROLE_USER"); // Assign default role

        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Send email verification link
        UserDetails userDetails = loadUserByUsername(savedUser.getUsername());
        String verificationToken = jwtUtil.generateToken(userDetails);
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        // Log the registration event
        logger.info("User registered successfully: {}", savedUser.getUsername());

        // Return the user response
        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.isEmailVerified()
        );
    }

    // ✅ Login a user and generate a JWT token
    public String loginUser(String username, String password) {
        try {
            // Authenticate the user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            // Load user details
            final UserDetails userDetails = loadUserByUsername(username);

            // Generate a JWT token
            return jwtUtil.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
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

    // ✅ Delete a user by ID
    public void deleteUser(Long id) {
        User user = getUserById(id); // Throws ResourceNotFoundException if user not found
        userRepository.delete(user);
    }

    // ✅ Verify a user's email
    public void verifyEmail(Long userId) {
        User user = getUserById(userId); // Throws ResourceNotFoundException if user not found
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    // ✅ Generate password reset token
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
    }

    // ✅ Reset password securely
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        // 🔹 Encrypt the new password before saving
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
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
            userRepository.save(user);

            return true;
        } catch (Exception e) {
            // Log the error
            System.err.println("Email verification failed: " + e.getMessage());
            return false;
        }
    }

}