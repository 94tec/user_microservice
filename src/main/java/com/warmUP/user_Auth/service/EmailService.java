package com.warmUP.user_Auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

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
            System.out.println("Verification email sent to " + email);
        } catch (Exception e) {
            System.err.println("Failed to send verification email to " + email + ": " + e.getMessage());
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
            System.out.println("Email sent to " + email + ": " + subject);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + email + ": " + e.getMessage());
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
            System.out.println("Password reset email sent to " + email);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to " + email + ": " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}