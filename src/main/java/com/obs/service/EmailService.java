package com.obs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private MailProperties mailProperties;

    public void sendWelcomeEmail(String to, String username) {
        logger.info("Attempting to send welcome email to: {}", to);
        
        try {
            // Check if mail sender is configured
            if (mailSender == null) {
                throw new RuntimeException("JavaMailSender is not configured");
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(to);
            message.setSubject("Welcome to OBS Banking System");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Welcome to OBS Banking System!\n\n" +
                "Your account has been successfully created. You can now log in to your new OBS account using your credentials.\n\n" +
                "Login URL: http://localhost:3000\n" +
                "Username: %s\n\n" +
                "Thank you for choosing OBS Banking System!\n\n" +
                "Best regards,\n" +
                "OBS Banking Team",
                username, username
            ));
            
            mailSender.send(message);
            logger.info("Welcome email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    public void sendAccountLockedEmail(String to, String username) {
        logger.info("Attempting to send account locked email to: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(to);
            message.setSubject("Account Locked - OBS Banking System");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your OBS Banking account has been locked due to multiple failed login attempts.\n\n" +
                "For security reasons, please wait 24 hours before trying again, or contact our support team for immediate assistance.\n\n" +
                "Support Email: support@obs-banking.com\n" +
                "Support Phone: 1-800-OBS-BANK\n\n" +
                "Best regards,\n" +
                "OBS Banking Security Team",
                username
            ));
            
            mailSender.send(message);
            logger.info("Account locked email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send account locked email to {}: {}", to, e.getMessage(), e);
        }
    }

    public void sendPasswordResetEmail(String to, String username, String tempPassword) {
        logger.info("Attempting to send password reset email to: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(to);
            message.setSubject("Password Reset - OBS Banking System");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your password has been reset successfully. Your temporary password is: %s\n\n" +
                "Please log in to your OBS account and change your password immediately for security.\n\n" +
                "Login URL: http://localhost:3000\n\n" +
                "If you did not request this password reset, please contact our support team immediately.\n\n" +
                "Best regards,\n" +
                "OBS Banking Security Team",
                username, tempPassword
            ));
            
            mailSender.send(message);
            logger.info("Password reset email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
        }
    }

    public void sendOTPEmail(String to, String username, String otp) {
        logger.info("Attempting to send OTP email to: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(to);
            message.setSubject("OTP Verification - OBS Banking System");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "Your One-Time Password (OTP) for secure access is: %s\n\n" +
                "This OTP is valid for 10 minutes only. Please do not share this code with anyone.\n\n" +
                "If you did not request this OTP, please contact our support team immediately.\n\n" +
                "Best regards,\n" +
                "OBS Banking Security Team",
                username, otp
            ));
            
            mailSender.send(message);
            logger.info("OTP email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
        }
    }

    public void sendTransactionAlert(String to, String username, String transactionDetails) {
        logger.info("Attempting to send transaction alert email to: {}", to);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailProperties.getUsername());
            message.setTo(to);
            message.setSubject("Transaction Alert - OBS Banking System");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "A transaction has been processed on your account:\n\n" +
                "%s\n\n" +
                "If you did not authorize this transaction, please contact our support team immediately.\n\n" +
                "Support Email: security@obs-banking.com\n" +
                "Support Phone: 1-800-OBS-BANK\n\n" +
                "Best regards,\n" +
                "OBS Banking Team",
                username, transactionDetails
            ));
            
            mailSender.send(message);
            logger.info("Transaction alert email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send transaction alert email to {}: {}", to, e.getMessage(), e);
        }
    }
}
