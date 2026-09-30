package com.example.job_tracker.service;

import com.example.job_tracker.model.User;
import com.example.job_tracker.model.UserSettings;
import com.example.job_tracker.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserSettingsService {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    private static final List<String> VALID_LANGUAGES = List.of("English", "Hindi", "Hinglish");
    private static final List<String> VALID_GENDERS = List.of("male", "female");

    public Optional<UserSettings> getSettingsForUser(Long userId) {
        return userSettingsRepository.findByUserId(userId);
    }

    public UserSettings saveSettings(User user, List<String> goals, String language, boolean remindersEnabled, String reminderTime, String reminderFrequency, List<String> visibleFields, String gender) {
        if (goals == null || goals.size() < 2 || goals.size() > 5) {
            throw new RuntimeException("You must set between 2 and 5 goals.");
        }

        if (!VALID_LANGUAGES.contains(language)) {
            throw new RuntimeException("Language must be English, Hindi, or Hinglish.");
        }

        UserSettings settings = userSettingsRepository.findByUserId(user.getId())
                .orElse(new UserSettings());

        settings.setUser(user);
        settings.setGoals(goals);
        settings.setReminderLanguage(language);
        settings.setRemindersEnabled(remindersEnabled);
        settings.setReminderTime(reminderTime != null ? reminderTime : "21:00");
        settings.setReminderFrequency(reminderFrequency != null ? reminderFrequency : "Daily");
        if (visibleFields != null) {
            settings.setVisibleFields(visibleFields);
        }
        if (gender != null && VALID_GENDERS.contains(gender)) {
            settings.setGender(gender);
        }

        return userSettingsRepository.save(settings);
    }
}