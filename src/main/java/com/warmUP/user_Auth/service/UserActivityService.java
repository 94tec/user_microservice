package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserActivityService {

    private static final Logger logger = LoggerFactory.getLogger(UserActivityService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Updates the last activity timestamp for a user.
     *
     * @param user_id The ID of the user.
     * @throws ResourceNotFoundException If the user is not found.
     */
    public void updateLastActivity(Long user_id) {
        try {
            logger.info("Updating last activity for user ID: {}", user_id);

            // Validate input
            if (user_id == null) {
                logger.error("User ID cannot be null");
                throw new IllegalArgumentException("User ID cannot be null");
            }

            // Fetch the user
            User user = userRepository.findById(user_id)
                    .orElseThrow(() -> {
                        logger.error("User not found with ID: {}", user_id);
                        return new ResourceNotFoundException("User not found with ID: " + user_id);
                    });

            // Update the last activity timestamp
            user.setLastActivity(LocalDateTime.now());
            userRepository.save(user);

            logger.info("Successfully updated last activity for user ID: {}", user_id);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e; // Re-throw the exception

        } catch (ResourceNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            throw e; // Re-throw the exception

        } catch (Exception e) {
            logger.error("Failed to update last activity for user ID: {}", user_id, e);
            throw new RuntimeException("Failed to update last activity. Please try again.");
        }
    }

    /**
     * Checks if a user's session is active.
     *
     * @param user_id The ID of the user.
     * @return True if the session is active, otherwise false.
     * @throws ResourceNotFoundException If the user is not found.
     */
    public boolean isSessionActive(Long user_id) {
        try {
            logger.info("Checking session activity for user ID: {}", user_id);

            // Validate input
            if (user_id == null) {
                logger.error("User ID cannot be null");
                throw new IllegalArgumentException("User ID cannot be null");
            }

            // Fetch the user
            User user = userRepository.findById(user_id)
                    .orElseThrow(() -> {
                        logger.error("User not found with ID: {}", user_id);
                        return new ResourceNotFoundException("User not found with ID: " + user_id);
                    });

            // Check if the session is active
            boolean isActive = user.getLastActivity().isAfter(LocalDateTime.now().minusMinutes(30)); // Session expires after 30 minutes of inactivity

            logger.info("Session activity result for user ID {}: {}", user_id, isActive);
            return isActive;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            throw e; // Re-throw the exception

        } catch (ResourceNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            throw e; // Re-throw the exception

        } catch (Exception e) {
            logger.error("Failed to check session activity for user ID: {}", user_id, e);
            throw new RuntimeException("Failed to check session activity. Please try again.");
        }
    }
}