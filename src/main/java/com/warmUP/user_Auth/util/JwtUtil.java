package com.warmUP.user_Auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;

    @Value("${jwt.expiration.time}")
    private long expirationTime;

    @Value("${jwt.refresh.expiration.time}")
    private long refreshExpirationTime;

    public JwtUtil() {
        // Automatically generate a secure key for HS256
        this.secretKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    }
    /**
     * Generates a JWT token for a user.
     *
     * @param userDetails The UserDetails object containing user information.
     * @return The generated JWT token.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities()); // Add user role to claims
        return createToken(claims, userDetails.getUsername(), expirationTime);
    }
    public String generateRefreshToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails.getUsername(), refreshExpirationTime);
    }
    /**
     * Generates a JWT token for email verification.
     *
     * @param email The email address to include in the token.
     * @return The generated JWT token.
     */
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, expirationTime);
    }

    /**
     * Creates a JWT token with claims and subject.
     *
     * @param claims  The claims to include in the token.
     * @param subject The subject (e.g., username or email) to include in the token.
     * @return The generated JWT token.
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date(System.currentTimeMillis());
        Date validity = new Date(now.getTime() + expiration);
        logger.debug("Generating token for subject: {}", subject);
        return Jwts.builder()
                .setClaims(claims) // Set custom claims (if any)
                .setSubject(subject) // Set the subject (username)
                .setIssuedAt(now) // Set the issue date
                .setExpiration(validity) // Set the expiration date
                .signWith(secretKey) // Sign the token with the secure key
                .compact(); // Build the token
    }

    // Validate JWT token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // Extract username from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from JWT token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract a specific claim from JWT token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from JWT token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token expired: {}", e.getMessage());
            throw new JwtException("Token has expired.");
        } catch (MalformedJwtException e) {
            logger.warn("Malformed token: {}", e.getMessage());
            throw new JwtException("Invalid token format.");
        } catch (IncorrectClaimException e) {
            logger.warn("Invalid token signature: {}", e.getMessage());
            throw new JwtException("Invalid token signature.");
        } catch (Exception e) {
            logger.error("Error parsing token: {}", e.getMessage());
            throw new JwtException("Failed to parse token.");
        }
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}