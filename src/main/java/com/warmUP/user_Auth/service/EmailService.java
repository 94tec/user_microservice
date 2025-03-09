package com.warmUP.user_Auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    public void sendVerificationEmail(String email, String verificationToken) {
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
    }
}