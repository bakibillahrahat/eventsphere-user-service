package org.com.eventsphere.user.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${EMAIL_USERNAME}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String token) {

        try {
            String verificationLink = "http://localhost:8081/api/v1/auth/verify-email?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to EventSphere! Please Verify Your Email");
            message.setText(
                        "Thank you for registering with EventSphere!\n\n" +
                        "Please click the link below to verify your email address and activate your account:\n" +
                        verificationLink + "\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "Best regards,\nThe EventSphere Team"
        );
        mailSender.send(message);
        log.info("Verification email sent to {}", to);
        } catch (Exception e) {
            // In a production environment, you might want to re-queue this email or alert an admin.
            // For now, we just log the error without crashing the registration process.
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try{
            String resetLink = "http://localhost:8081/api/v1/auth/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("EventSphere Password Reset Request");
            message.setText(
                    "We received a request to reset your password for your EventSphere account.\n\n" +
                    "Please click the link below to reset your password:\n" +
                    resetLink + "\n\n" +
                    "If you did not request a password reset, please ignore this email. This link will expire in 1 hour.\n\n" +
                    "Best regards,\nThe EventSphere Team"
            );
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        }catch (Exception e){
            log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }
}
