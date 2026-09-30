package com.example.job_tracker.controller;

import com.example.job_tracker.model.User;
import com.example.job_tracker.model.UserSettings;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class UserSettingsController {

    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationController applicationController;

    @GetMapping("/me")
    public Object getMySettings(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userSettingsService.getSettingsForUser(user.getId())
                .orElse(null);
    }

    @PostMapping("/me")
    public UserSettings saveMySettings(Authentication authentication, @RequestBody Map<String, Object> request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        @SuppressWarnings("unchecked")
        List<String> goals = (List<String>) request.get("goals");
        String language = (String) request.get("reminderLanguage");
        boolean remindersEnabled = (boolean) request.get("remindersEnabled");
        String reminderTime = (String) request.get("reminderTime");
        String reminderFrequency = (String) request.get("reminderFrequency");
        @SuppressWarnings("unchecked")
        List<String> visibleFields = (List<String>) request.get("visibleFields");
        String gender = (String) request.get("gender");

        UserSettings saved = userSettingsService.saveSettings(user, goals, language, remindersEnabled, reminderTime, reminderFrequency, visibleFields, gender);
        applicationController.clearCaptionCache();
        return saved;
    }
}