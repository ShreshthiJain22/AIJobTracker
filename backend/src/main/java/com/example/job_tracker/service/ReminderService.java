package com.example.job_tracker.service;

import com.example.job_tracker.model.Application;
import com.example.job_tracker.model.User;
import com.example.job_tracker.model.UserSettings;
import com.example.job_tracker.repository.ApplicationRepository;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReminderService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private GroqService groqService;

    @Autowired
    private EmailService emailService;

    private static final String SITE_URL = "https://job-tracking-with-ai.vercel.app";

    private static final Map<String, Integer> FREQUENCY_DAYS = Map.of(
        "Daily", 1,
        "Every 3 Days", 3,
        "Weekly", 7,
        "Monthly", 30
    );

    @Scheduled(fixedRate = 300000)
    public void checkReminders() {
        LocalTime now = LocalTime.now(ZoneId.of("Asia/Kolkata")).withSecond(0).withNano(0);
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            UserSettings settings = userSettingsRepository.findByUserId(user.getId()).orElse(null);

            if (settings == null || !settings.isRemindersEnabled()) {
                continue;
            }

            if (!isWithinCurrentWindow(settings.getReminderTime(), now)) {
                continue;
            }

            String frequency = settings.getReminderFrequency();
            int intervalDays = (frequency != null) ? FREQUENCY_DAYS.getOrDefault(frequency, 1) : 1;
            List<Application> userApplications = applicationRepository.findByUserId(user.getId());

            processUserReminder(user, settings, userApplications, intervalDays);
        }
    }

    private boolean isWithinCurrentWindow(String reminderTime, LocalTime now) {
        try {
            LocalTime target = LocalTime.parse(reminderTime, DateTimeFormatter.ofPattern("HH:mm"));
            long minutesDiff = Math.abs(java.time.Duration.between(target, now).toMinutes());
            return minutesDiff <= 5;
        } catch (Exception e) {
            return false;
        }
    }

    private void processUserReminder(User user, UserSettings settings, List<Application> applications, int intervalDays) {
        LocalDate today = LocalDate.now();
        List<String> visibleFields = settings.getVisibleFields();

        List<Application> activeApplications = new ArrayList<>();
        List<Application> rejectedApplications = new ArrayList<>();

        for (Application app : applications) {
            if ("Rejected".equals(app.getStatus())) {
                rejectedApplications.add(app);
            } else {
                activeApplications.add(app);
            }
        }

        List<Application> dueActive = new ArrayList<>();
        for (Application app : activeApplications) {
            if (isDueForNudge(app, today, intervalDays, visibleFields)) {
                dueActive.add(app);
            }
        }

        if (!dueActive.isEmpty()) {
            sendActiveApplicationNudge(user, settings, dueActive, applications, today, intervalDays);
            return;
        }

        boolean hasNoActiveApplications = activeApplications.isEmpty() && !rejectedApplications.isEmpty();
        if (hasNoActiveApplications && isDueForMotivation(rejectedApplications, today, intervalDays)) {
            sendMotivationalNudge(user, settings, rejectedApplications, today);
        }
    }

    private boolean isDueForNudge(Application app, LocalDate today, int intervalDays, List<String> visibleFields) {
        if (app.getAppliedDate() == null) {
            return false;
        }

        if (app.getLastNudgeSentDate() != null) {
            long daysSinceLastNudge = ChronoUnit.DAYS.between(app.getLastNudgeSentDate(), today);
            if (daysSinceLastNudge < intervalDays) {
                return false;
            }
        }

       return buildSituation(app, today, intervalDays, visibleFields) != null;
    }

    private boolean isDueForMotivation(List<Application> rejectedApplications, LocalDate today, int intervalDays) {
        LocalDate mostRecentNudge = rejectedApplications.stream()
            .map(Application::getLastNudgeSentDate)
            .filter(d -> d != null)
            .max(LocalDate::compareTo)
            .orElse(null);

        if (mostRecentNudge == null) {
            return true;
        }

        long daysSinceLastNudge = ChronoUnit.DAYS.between(mostRecentNudge, today);
        return daysSinceLastNudge >= intervalDays;
    }

    private String buildSituation(Application app, LocalDate today, int intervalDays, List<String> visibleFields) {
            long daysSinceApplied = ChronoUnit.DAYS.between(app.getAppliedDate(), today);
        boolean tracksReferral = visibleFields != null && visibleFields.contains("referralRequested");
        String status = app.getStatus();

        if (tracksReferral) {
            if (!app.isReferralRequested() && daysSinceApplied >= intervalDays) {
                return "hasn't asked for a referral yet at " + app.getCompany() + ", applied " + daysSinceApplied + " days ago";
            } else if (app.isReferralRequested() && !app.isReferralReceived()) {
                return "requested a referral at " + app.getCompany() + " but hasn't heard back yet";
            } else if (app.isReferralRequested() && app.isReferralReceived()) {
                return "got the referral at " + app.getCompany() + " and should just check in on the application status";
            }
        }

        if ("Applied".equals(status)) {
           if (daysSinceApplied < intervalDays) {
    return null;
}
            return "applied to " + app.getCompany() + " " + daysSinceApplied + " days ago and hasn't heard back — worth a polite follow-up";
        }

        if ("Interview".equals(status)) {
           if (daysSinceApplied < Math.max(intervalDays, 5)) {
    return null;
}
            return "interviewed at " + app.getCompany() + " a while back and is still waiting on the result — a good time to prep for the next round or follow up";
        }

        if ("Offer".equals(status)) {
            return "has an offer from " + app.getCompany() + " — worth reviewing the details and deciding soon";
        }

        return "should check in and update the status for " + app.getCompany() + ", applied " + daysSinceApplied + " days ago";
    }

private void sendActiveApplicationNudge(User user, UserSettings settings, List<Application> dueApplications, List<Application> allApplications, LocalDate today, int intervalDays) {    List<String> visibleFields = settings.getVisibleFields();
    Application focusApp = dueApplications.get(0);
String situation = buildSituation(focusApp, today, intervalDays, visibleFields);
    List<String> goals = settings.getGoals();
    long daysSinceApplied = focusApp.getAppliedDate() != null
        ? ChronoUnit.DAYS.between(focusApp.getAppliedDate(), today)
        : 0;
    String goal = (goals == null || goals.isEmpty())
        ? "their dreams"
        : goals.get((int) (daysSinceApplied % goals.size()));
    String language = settings.getReminderLanguage();
    String gender = settings.getGender() != null ? settings.getGender() : "female";

    String prompt = "You're texting a close friend who's job hunting. The friend you're texting is " + gender + ". "
        + "Write in " + language + ". "
        + "\n\nContext: They " + situation + ". "
        + "\n\nTheir personal goal: \"" + goal + "\"."
        + "\n\nWrite ONE short WhatsApp-style message (under 25 words) that does two things clearly: "
        + "1) mentions the specific situation above in a natural way, and "
        + "2) connects it to their goal with a clear, logical link — explain HOW this job situation moves them toward that goal, don't just mention both things side by side. "
        + "The connection must make real sense — for example, if their goal is about money or a trip, tie it to the fact that getting this job/offer means income toward that goal. "
        + "Make it warm and gently funny, never sarcastic or dismissive. "
        + "Do NOT invent random unrelated details (food, drinks, activities) just to sound casual — every word should relate directly to their actual situation and goal. "
        + "If writing in Hindi, use natural, grammatically correct Hindi in Devanagari script, with verb forms and grammar correctly matching the friend's gender (" + gender + ") — not a literal word-by-word translation from English/Hinglish phrasing. "
        + "If writing in Hinglish, write it the way young Indians actually text on WhatsApp — Hindi and English words casually mixed together, using Roman/English script throughout (not Devanagari), informal tone, like 'yaar', 'bhai'/'behen' as fits the friend's gender, casual contractions — NOT formal Hindi with English words swapped in. "
        + "No corporate language, no emojis, no hashtags. "
        + "Write it as ONE coherent sentence, not two disconnected clauses stuck together. "
        + "Reply with ONLY the message, nothing else.";

    String message = groqService.generateNudge(prompt);
    String personalizedMessage = (message != null && !message.trim().isEmpty())
        ? message
        : "Time to check in on your applications.";

    String emailBody = buildEmailBody(personalizedMessage, allApplications, language);

    System.out.println("[NUDGE for " + user.getEmail() + "] " + personalizedMessage);
    emailService.sendNudge(user.getEmail(), "Your Job Tracker Update", emailBody);

    for (Application app : dueApplications) {
        app.setLastNudgeSentDate(today);
        applicationRepository.save(app);
    }
}
private void sendMotivationalNudge(User user, UserSettings settings, List<Application> rejectedApplications, LocalDate today) {
    List<String> goals = settings.getGoals();
    String goal = (goals == null || goals.isEmpty()) ? "their dreams" : goals.get(0);
    String language = settings.getReminderLanguage();
    String gender = settings.getGender() != null ? settings.getGender() : "female";

    String prompt = "You're texting a close friend who just got rejected from a job. The friend you're texting is " + gender + ". "
        + "Write in " + language + ". "
        + "\n\nThey're feeling a bit low about it right now."
        + "\n\nTheir personal goal: \"" + goal + "\"."
        + "\n\nWrite ONE short WhatsApp-style message (under 25 words) that does three things: "
        + "1) briefly acknowledges the rejection with warmth, not dismissively, "
        + "2) reminds them their goal is still reachable — explain HOW continuing to apply gets them closer, don't just mention the goal, "
        + "3) gently pushes them to try again. "
        + "Light humour is okay, but the emotional acknowledgment and the goal connection must both make real sense together as one coherent thought. "
        + "Do NOT invent random unrelated details (food, drinks, activities) just to sound casual — every word should relate directly to their actual situation and goal. "
        + "If writing in Hindi, use natural, grammatically correct Hindi in Devanagari script, with verb forms and grammar correctly matching the friend's gender (" + gender + ") — not a literal word-by-word translation from English/Hinglish phrasing. "
        + "If writing in Hinglish, write it the way young Indians actually text on WhatsApp — Hindi and English words casually mixed together, using Roman/English script throughout (not Devanagari), informal tone, like 'yaar', 'bhai'/'behen' as fits the friend's gender, casual contractions — NOT formal Hindi with English words swapped in. "
        + "No corporate language, no toxic positivity, no emojis. "
        + "Reply with ONLY the message, nothing else.";

    String message = groqService.generateNudge(prompt);
    String personalizedMessage = (message != null && !message.trim().isEmpty())
        ? message
        : "This one didn't work out, but your next application could be the one. Keep going.";

    String emailBody = personalizedMessage
        + "\n\nReady to try again? Add a new application here: " + SITE_URL;

    System.out.println("[MOTIVATION for " + user.getEmail() + "] " + personalizedMessage);
    emailService.sendNudge(user.getEmail(), "Keep Going — Your Job Tracker", emailBody);

    for (Application app : rejectedApplications) {
        app.setLastNudgeSentDate(today);
        applicationRepository.save(app);
    }
}

    private String buildEmailBody(String personalizedMessage, List<Application> allApplications, String language) {
        String snapshotHeader;
        String linkText;

        if ("Hindi".equals(language)) {
            snapshotHeader = "यहाँ आपकी सभी एप्लीकेशन्स की स्थिति है:";
            linkText = "अपना ट्रैकर खोलें: ";
        } else {
            snapshotHeader = "Here's where things stand across all your applications:";
            linkText = "Open your tracker: ";
        }

        StringBuilder body = new StringBuilder();
        body.append(personalizedMessage).append("\n\n");
        body.append(snapshotHeader).append("\n\n");

        for (Application app : allApplications) {
            body.append("- ")
                .append(app.getCompany())
                .append(" — ")
                .append(app.getRole())
                .append(" (")
                .append(app.getStatus())
                .append(")\n");
        }

        body.append("\n").append(linkText).append(SITE_URL);
        return body.toString();
    }
}