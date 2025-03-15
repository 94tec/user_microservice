// TokenService.java
package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.Token;
import com.warmUP.user_Auth.model.User;
import com.warmUP.user_Auth.repository.TokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AuditLogService auditLogService;
    @Value("${token.expiry.hours:1}") // Default to 1 hour
    private int tokenExpiryHours;

    public String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        // LocalDateTime expiryTime = LocalDateTime.now().plusHours(tokenExpiryHours);

        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(tokenValue);
        token.setExpiryTime(LocalDateTime.now().plusHours(1));
        token.setUser_id(user.getId());
        token.setRevoked(false);

        // Save the refresh token
        tokenRepository.save(token);

        // Log the token generation action
        logger.info("Refresh token generated for user: {}", user.getUsername());
        auditLogService.logAction("TOKEN_GENERATED", user.getUsername());
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
        try {
            int rowsAffected = tokenRepository.deleteByTokenValue(tokenValue);
            if(rowsAffected > 0){
                auditLogService.logAction("TOKEN_REVOKED", "Token revoked: " + tokenValue);
            } else {
                logger.warn("Attempted to revoke non-existent token: {}", tokenValue);
            }

        } catch (DataAccessException e) {
            logger.error("Error revoking token: {}", tokenValue, e);
            // Handle database-related errors
        } catch (Exception e) {
            logger.error("An unexpected error occurred revoking token: {}", tokenValue, e);
        }
    }

}
