package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.dto.UserProfileDTO;
import com.warmUP.user_Auth.exception.ResourceNotFoundException;
import com.warmUP.user_Auth.exception.UserNotFoundException;
import com.warmUP.user_Auth.exception.UserProfileNotFoundException;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.model.UserProfile;
import com.warmUP.user_Auth.repository.UserProfileRepository;
import com.warmUP.user_Auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    private static final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserProfileService(UserProfileRepository userProfileRepository, UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    // ✅ Create or update user profile using DTO
    public UserProfileDTO createProfile(User user, UserProfileDTO profileDTO) {
        logger.info("Creating or updating profile for user: {}", user.getId());

        // Validate input
        validateUser(user);

        // Check if the user already has a profile
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElse(new UserProfile()); // Create a new profile if it doesn't exist

        // Update or set profile fields using DTO
        BeanUtils.copyProperties(profileDTO, profile);
        profile.setUser(user);

        // Save the profile
        UserProfile savedProfile = userProfileRepository.save(profile);

        logger.info("Successfully created or updated profile for user: {}", user.getId());
        return convertToDTO(savedProfile);
    }

    // Private method to validate the User object
    private void validateUser(User user) {
        if (user == null) {
            logger.error("User cannot be null");
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    public UserProfileDTO updateProfile(Long user_id, UserProfileDTO profileDTO) {
        logger.info("Updating profile for user_id: {}", user_id);

        // Fetch the user
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> {
                    logger.error("User not found with user_id: {}", user_id);
                    return new ResourceNotFoundException("User not found with user_id: " + user_id);
                });

        // Fetch the user profile
        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.error("UserProfile not found for user_id: {}", user_id);
                    return new ResourceNotFoundException("UserProfile not found for user_id: " + user_id);
                });

        // Update the profile fields using DTO
        BeanUtils.copyProperties(profileDTO, profile);

        // Save the updated profile
        UserProfile updatedProfile = userProfileRepository.save(profile);

        logger.info("Successfully updated profile for user_id: {}", user_id);
        return convertToDTO(updatedProfile);
    }

    public UserProfileDTO getUserProfileByUserId(Long user_id) {
        logger.info("Fetching user profile for user_id: {}", user_id);

        // Check if the user exists
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> {
                    logger.error("User not found with user_id: {}", user_id);
                    return new UserNotFoundException("User not found with user_id: " + user_id);
                });

        // Check if the user profile exists
        UserProfile userProfile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> {
                    logger.error("UserProfile not found for user_id: {}", user_id);
                    return new UserProfileNotFoundException("UserProfile not found for user_id: " + user_id);
                });

        logger.info("Successfully fetched user profile for user_id: {}", user_id);
        return convertToDTO(userProfile);
    }

    // Helper method to convert UserProfile to UserProfileDTO
    private UserProfileDTO convertToDTO(UserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        BeanUtils.copyProperties(profile, dto);
        return dto;
    }
}