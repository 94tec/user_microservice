package com.warmUP.user_Auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends a verification email to the user.
     *
     * @param email             The user's email address.
     * @param verificationToken The JWT token for email verification.
     */
    @Async
    public void sendVerificationEmail(String email, String verificationToken) {
        try {
            // Create the verification URL
            String verificationUrl = "http://localhost:8080/api/users/verify-email?token=" + verificationToken;

            // Create the email content
            String subject = "Verify Your Email Address";
            String body = "Thank you for registering! Please click the link below to verify your email address:\n\n"
                    + verificationUrl + "\n\n"
                    + "If you did not register, please ignore this email.";

            // Create and send the email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Verification email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Sends a generic email to the user.
     *
     * @param email   The user's email address.
     * @param subject The subject of the email.
     * @param message The content of the email.
     */
    @Async
    public void sendEmail(String email, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            logger.info("Email sent successfully to {}: {}", email, subject);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Sends a password reset email to the user.
     *
     * @param email The user's email address.
     * @param token The password reset token.
     */
    @Async
    public void sendPasswordResetEmail(String email, String token) {
        try {
            // Create the password reset URL
            String resetUrl = "http://localhost:8080/api/users/reset-password?token=" + token;

            // Create the email content
            String subject = "Password Reset Request";
            String body = "You have requested to reset your password. Please click the link below to reset your password:\n\n"
                    + resetUrl + "\n\n"
                    + "If you did not request this, please ignore this email.";

            // Create and send the email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Sends a welcome email to the user after successful registration.
     *
     * @param email The user's email address.
     * @param name  The user's name.
     */
    @Async
    public void sendWelcomeEmail(String email, String name) {
        try {
            // Create the email content
            String subject = "Welcome to Our Platform!";
            String body = "Hello " + name + ",\n\n"
                    + "Welcome to our platform! We're excited to have you on board.\n\n"
                    + "If you have any questions, feel free to reach out to our support team.\n\n"
                    + "Best regards,\n"
                    + "The Team";

            // Create and send the email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    /**
     * Sends a notification email to the user.
     *
     * @param email   The user's email address.
     * @param subject The subject of the email.
     * @param message The content of the email.
     */
    @Async
    public void sendNotificationEmail(String email, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            mailSender.send(mailMessage);
            logger.info("Notification email sent successfully to {}: {}", email, subject);
        } catch (Exception e) {
            logger.error("Failed to send notification email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send notification email", e);
        }
    }
}