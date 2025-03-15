package com.warmUP.user_Auth.service;

import com.warmUP.user_Auth.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final AuthService authService;
    private final AuditLogService auditLogService;

    public CustomOAuth2UserService(AuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.info("Starting OAuth2 user loading process.");
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            String email = oAuth2User.getAttribute("email");
            String providerId = oAuth2User.getAttribute("sub");

            if (email == null) {
                logger.error("Email not found in OAuth2 response. Unable to process user.");
                throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
            }
            if (providerId == null) {
                logger.error("Provider ID (sub) not found in OAuth2 response. Unable to process user.");
                throw new OAuth2AuthenticationException("Provider ID not found in OAuth2 response");
            }

            logger.info("OAuth2 user email: {}", email);
            logger.info("OAuth2 user providerId: {}", providerId);

            User user = authService.findByEmail(email);

            if (user == null) {
                logger.info("User with email {} not found. Creating new user.", email);
                user = new User();
                user.setEmail(email);
                user.setRole(user.getRole());
                user.setVerified(true);
                user.setProviderId(providerId);

                try {
                    authService.save(user);
                    auditLogService.logAction("LOGIN", email );
                    logger.info("New user created and saved successfully: {}", user);
                } catch (Exception e) {
                    logger.error("Error saving new user: {}", e.getMessage(), e);
                    // Create an OAuth2Error object
                    OAuth2Error oauth2Error = new OAuth2Error("oauth2_error", "Error saving new user", null);
                    throw new OAuth2AuthenticationException(oauth2Error, e);
                }

            } else {
                logger.info("User with email {} found.", email);
                if(user.getProviderId() == null || !user.getProviderId().equals(providerId)){
                    logger.info("Updating socialId for user: {}", email);
                    user.setProviderId(providerId);
                    try {
                        authService.save(user);
                        logger.info("SocialId updated successfully for user: {}", user);
                    } catch (Exception e) {
                        logger.error("Error updating socialId for user: {}", e.getMessage(), e);
                        // Create an OAuth2Error object
                        OAuth2Error oauth2Error = new OAuth2Error("oauth2_error", "Error updating socialId for user", null);
                        throw new OAuth2AuthenticationException(oauth2Error, e);
                    }
                }
            }

            return oAuth2User;

        } catch (OAuth2AuthenticationException e) {
            logger.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during OAuth2 user loading: {}", e.getMessage(), e);
            // Create an OAuth2Error object
            OAuth2Error oauth2Error = new OAuth2Error("oauth2_error", "Error during OAuth2 user loading", null);
            throw new OAuth2AuthenticationException(oauth2Error, e);
        } finally {
            logger.info("Finished OAuth2 user loading process.");
        }
    }
}
