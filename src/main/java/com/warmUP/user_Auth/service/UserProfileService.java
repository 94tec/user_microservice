package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.exception.UserNotFoundException;
import com.warmUP.user_Auth.exception.UserProfileNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.model.UserProfile;
import com.warmUP.user_Auth.repository.UserProfileRepository;
import com.warmUP.user_Auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);
    @Autowired
    private final UserProfileRepository userProfileRepository;
    private final   UserRepository userRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    // ✅ Create or update user profile
    public UserProfile createProfile(User user, String profilePictureUrl, String bio) {
        logger.info("Creating or updating profile for user: {}", user.getId());

        // Validate input
        validateUser(user);
        validateProfilePictureUrl(profilePictureUrl, user.getId());
        validateBio(bio, user.getId());

        // Check if the user already has a profile
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile()); // Create a new profile if it doesn't exist

        // Update or set profile fields
        profile.setUser(user);
        profile.setProfilePictureUrl(profilePictureUrl);
        profile.setBio(bio);

        // Save the profile
        UserProfile savedProfile = userProfileRepository.save(profile);

        logger.info("Successfully created or updated profile for user: {}", user.getId());
        return savedProfile;
    }

    // Private method to validate the User object
    private void validateUser(User user) {
        if (user == null) {
            logger.error("User cannot be null");
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    // Private method to validate the profile picture URL
    private void validateProfilePictureUrl(String profilePictureUrl, Long userId) {
        if (profilePictureUrl == null || profilePictureUrl.trim().isEmpty()) {
            logger.warn("Profile picture URL is null or empty for user: {}", userId);
        }
    }

    // Private method to validate the bio
    private void validateBio(String bio, Long userId) {
        if (bio == null || bio.trim().isEmpty()) {
            logger.warn("Bio is null or empty for user: {}", userId);
        }
    }

    public UserProfile updateProfile(Long userId, String profilePictureUrl, String bio) {
        logger.info("Updating profile for userId: {}", userId);

        // Fetch the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with userId: {}", userId);
                    return new ResourceNotFoundException("User not found with userId: " + userId);
                });

        // Fetch the user profile
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.error("UserProfile not found for userId: {}", userId);
                    return new ResourceNotFoundException("UserProfile not found for userId: " + userId);
                });

        // Update the profile fields
        profile.setProfilePictureUrl(profilePictureUrl);
        profile.setBio(bio);

        // Save the updated profile
        UserProfile updatedProfile = userProfileRepository.save(profile);

        logger.info("Successfully updated profile for userId: {}", userId);
        return updatedProfile;
    }

    public UserProfile getUserProfileByUserId(Long userId) {
        logger.info("Fetching user profile for userId: {}", userId);

        // Check if the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with userId: {}", userId);
                    return new UserNotFoundException("User not found with userId: " + userId);
                });

        // Check if the user profile exists
        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.error("UserProfile not found for userId: {}", userId);
                    return new UserProfileNotFoundException("UserProfile not found for userId: " + userId);
                });

        logger.info("Successfully fetched user profile for userId: {}", userId);
        return userProfile;
    }
}
