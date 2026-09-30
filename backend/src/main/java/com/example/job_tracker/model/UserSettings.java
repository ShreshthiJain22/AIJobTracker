package com.example.job_tracker.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String reminderLanguage = "Hinglish";

    private boolean remindersEnabled = true;

    private String reminderTime = "21:00";

    private String reminderFrequency = "Daily";
    private String gender = "female";

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_goals", joinColumns = @JoinColumn(name = "user_settings_id"))
    @Column(name = "goal")
    private List<String> goals = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_visible_fields", joinColumns = @JoinColumn(name = "user_settings_id"))
    @Column(name = "field_name")
    private List<String> visibleFields = new ArrayList<>(List.of("jdLink", "source", "emailUsed", "contactPerson", "referralRequested"));

    public List<String> getVisibleFields() {
        return visibleFields;
    }
    public void setVisibleFields(List<String> visibleFields) {
        this.visibleFields = visibleFields;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getReminderLanguage() {
        return reminderLanguage;
    }
    public void setReminderLanguage(String reminderLanguage) {
        this.reminderLanguage = reminderLanguage;
    }

    public boolean isRemindersEnabled() {
        return remindersEnabled;
    }
    public void setRemindersEnabled(boolean remindersEnabled) {
        this.remindersEnabled = remindersEnabled;
    }

    public String getReminderTime() {
        return reminderTime;
    }
    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getReminderFrequency() {
        return reminderFrequency;
    }
    public void setReminderFrequency(String reminderFrequency) {
        this.reminderFrequency = reminderFrequency;
    }

    public List<String> getGoals() {
        return goals;
    }
    public void setGoals(List<String> goals) {
        this.goals = goals;
    }
}
