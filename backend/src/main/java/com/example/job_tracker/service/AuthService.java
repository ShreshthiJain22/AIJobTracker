package com.example.job_tracker.service;

import com.example.job_tracker.model.User;
import com.example.job_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${site.url}")
    private String siteUrl;

    public User signup(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());

        User saved = userRepository.save(user);

        String verificationLink = siteUrl + "/verify?token=" + saved.getVerificationToken();
        String subject = "Verify your Job Tracker account";
        String body = "Welcome to Job Tracker! Please verify your email by clicking the link below:\n\n"
            + verificationLink
            + "\n\nIf you didn't sign up for this, you can ignore this email.";

        emailService.sendNudge(saved.getEmail(), subject, body);

        return saved;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Please verify your email before logging in. Check your inbox for the verification link.");
        }

        return user;
    }

    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification link"));

        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return true;
    }

    public void forgotPassword(String email) {
    User user = userRepository.findByEmail(email).orElse(null);

    if (user == null) {
        return; 
    }

    String token = UUID.randomUUID().toString();
    user.setResetToken(token);
    user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
    userRepository.save(user);

    String resetLink = siteUrl + "/reset-password?token=" + token;
    String subject = "Reset your Job Tracker password";
    String body = "We received a request to reset your password. Click the link below to set a new one:\n\n"
        + resetLink
        + "\n\nThis link expires in 30 minutes. If you didn't request this, you can safely ignore this email.";

    emailService.sendNudge(user.getEmail(), subject, body);
}

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset link"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("This reset link has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}