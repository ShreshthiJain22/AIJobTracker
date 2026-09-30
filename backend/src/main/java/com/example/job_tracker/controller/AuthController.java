package com.example.job_tracker.controller;

import com.example.job_tracker.model.User;
import com.example.job_tracker.service.AuthService;
import com.example.job_tracker.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public Map<String, String> signup(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        authService.signup(email, password);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account created! Please check your email to verify your account before logging in.");
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        User user = authService.login(email, password);

        String token = jwtUtil.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", user.getEmail());
        response.put("id", user.getId());

        return response;
    }

    @GetMapping("/verify")
    public Map<String, String> verify(@RequestParam String token) {
        authService.verifyEmail(token);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Email verified successfully! You can now log in.");
        return response;
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.forgotPassword(email);

        Map<String, String> response = new HashMap<>();
        response.put("message", "If an account exists with that email, a reset link has been sent.");
        return response;
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        authService.resetPassword(token, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully! You can now log in with your new password.");
        return response;
    }
}