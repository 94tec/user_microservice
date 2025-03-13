package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.Token;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    public String generateToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1); // Token expires in 1 hour

        Token token = new Token();
        token.setTokenValue(tokenValue);
        token.setExpiryTime(expiryTime);
        token.setUserId(user.getId());

        tokenRepository.save(token);
        return tokenValue;
    }

    public boolean validateToken(String tokenValue) {
        Optional<Token> tokenOptional = tokenRepository.findByTokenValue(tokenValue);
        if (tokenOptional.isPresent()) {
            Token token = tokenOptional.get();
            return token.getExpiryTime().isAfter(LocalDateTime.now());
        }
        return false;
    }

    public void revokeToken(String tokenValue) {
        tokenRepository.deleteByTokenValue(tokenValue);
    }
}