package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by username
    Optional<User> findByUsername(String username);

    // Find a user by email
    Optional<User> findByEmail(String email);

    // Find a user by password reset token
    Optional<User> findByPasswordResetToken(String token);
    // ✅ Find a user by email verification token
    Optional<User> findByEmailVerificationToken(String token);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

