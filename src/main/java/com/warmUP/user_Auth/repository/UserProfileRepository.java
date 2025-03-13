package com.warmUP.user_Auth.repository;


import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // Find a user profile by user ID
    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByUser(User user); // Fetch profile by user
}
