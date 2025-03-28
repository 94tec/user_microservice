package com.warmUP.user_Auth.repository;

import com.warmUP.user_Auth.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValue(String tokenValue);

    int deleteByTokenValue(String tokenValue);

    boolean existsByTokenValue(String tokenValue);

    void deleteByUserId(Long id);
}