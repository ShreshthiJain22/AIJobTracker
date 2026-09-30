package com.example.job_tracker.controller;

import com.example.job_tracker.model.Application;
import com.example.job_tracker.model.User;
import com.example.job_tracker.model.UserSettings;
import com.example.job_tracker.repository.ApplicationRepository;
import com.example.job_tracker.repository.UserRepository;
import com.example.job_tracker.repository.UserSettingsRepository;
import com.example.job_tracker.service.EmailService;
import com.example.job_tracker.service.GroqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private GroqService groqService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private UserRepository userRepository;

    private static final List<String> VALID_STATUSES = List.of(
        "Applied", "Referral Requested", "Interview", "Offer", "Rejected"
    );

    private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+\\..+");



    private final Map<Long, List<String>> captionCache = new HashMap<>();

    private final Map<String, String> cachedQuotesByLanguage = new HashMap<>();

    private void validateApplication(Application application) {
        if (application.getCompany() == null || application.getCompany().trim().isEmpty()) {
            throw new RuntimeException("Company is required.");
        }
        if (application.getRole() == null || application.getRole().trim().isEmpty()) {
            throw new RuntimeException("Role is required.");
        }
        if (application.getAppliedDate() == null) {
            throw new RuntimeException("Applied date is required.");
        }
        if (application.getAppliedDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Applied date cannot be in the future.");
        }
        if (application.getStatus() == null || !VALID_STATUSES.contains(application.getStatus())) {
            throw new RuntimeException("Status must be one of: " + String.join(", ", VALID_STATUSES));
        }
        if (application.getJdLink() != null && !application.getJdLink().trim().isEmpty()) {
            if (!URL_PATTERN.matcher(application.getJdLink().trim()).matches()) {
                throw new RuntimeException("JD Link must be a valid URL starting with http:// or https://");
            }
        }
    }

    @GetMapping("/quote-of-the-day")
public String getQuoteOfTheDay(Authentication authentication) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    UserSettings settings = userSettingsRepository.findByUserId(user.getId()).orElse(null);
    String language = settings != null ? settings.getReminderLanguage() : "English";
    String gender = settings != null && settings.getGender() != null ? settings.getGender() : "female";

    LocalDate today = LocalDate.now();
    String cacheKey = language + "-" + gender + "-" + today;

    if (!cachedQuotesByLanguage.containsKey(cacheKey)) {
        String prompt = "Write in " + language + ". The quote is for a " + gender + " actively job hunting and applying to companies. "
            + "Write ONE short, punchy motivational quote (under 20 words). "
            + "It should feel like something a supportive friend would text — warm, energetic, and genuinely encouraging, "
            + "with light, gentle humour if it fits naturally. "
            + "Do NOT be sarcastic, dismissive, or joke about rejection, failure, or how many applications have gone unanswered — "
            + "that reads as discouraging, not motivating. "
            + "The quote must make clear, literal sense as a standalone sentence — don't invent random details "
            + "(coffee, bragging rights, imaginary scenarios) just to sound witty; every word should genuinely relate "
            + "to encouraging someone to keep applying. "
            + "If writing in Hindi, use grammar matching a " + gender + " speaker/subject. "
            + "No corporate language. "
            + "Reply with ONLY the quote, nothing else.";
        String generated = groqService.generateNudge(prompt);
        cachedQuotesByLanguage.put(cacheKey, generated != null ? generated : "Every application is a step closer to your dream job.");
    }

    return cachedQuotesByLanguage.get(cacheKey);
}
    @GetMapping
    public List<Application> getAllApplications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return applicationRepository.findByUserId(user.getId());
    }

    @PostMapping
    public Application createApplication(@RequestBody Application application, Authentication authentication) {
        validateApplication(application);

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        application.setUser(user);
        return applicationRepository.save(application);
    }

    @PutMapping("/{id}")
    public Application updateApplication(@PathVariable Long id, @RequestBody Application updatedApplication, Authentication authentication) {
        validateApplication(updatedApplication);

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Application existing = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id " + id));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to edit this application");
        }

        existing.setCompany(updatedApplication.getCompany());
        existing.setRole(updatedApplication.getRole());
        existing.setStatus(updatedApplication.getStatus());
        existing.setJdLink(updatedApplication.getJdLink());
        existing.setAppliedDate(updatedApplication.getAppliedDate());
        existing.setSource(updatedApplication.getSource());
        existing.setContactPerson(updatedApplication.getContactPerson());
        existing.setReferralRequested(updatedApplication.isReferralRequested());
        existing.setReferralRequestedDate(updatedApplication.getReferralRequestedDate());
        existing.setReferralReceived(updatedApplication.isReferralReceived());
        existing.setEmailUsed(updatedApplication.getEmailUsed());

        return applicationRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void deleteApplication(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Application existing = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found with id " + id));

        if (!existing.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to delete this application");
        }

        applicationRepository.deleteById(id);
    }

   
    

   @GetMapping("/{id}/stage-captions")
public List<String> getStageCaptions(@PathVariable Long id, Authentication authentication) {
    if (captionCache.containsKey(id)) {
        return captionCache.get(id);
    }

    Application app = applicationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Application not found"));

    String email = authentication.getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    UserSettings settings = userSettingsRepository.findByUserId(user.getId())
            .orElse(null);

    List<String> goals = settings != null ? settings.getGoals() : List.of();
    String language = settings != null ? settings.getReminderLanguage() : "Hinglish";
    String gender = settings != null && settings.getGender() != null ? settings.getGender() : "female";
    List<String> visibleFields = settings != null ? settings.getVisibleFields() : null;
    boolean tracksReferral = visibleFields != null && visibleFields.contains("referralRequested");

    List<String> stages = tracksReferral
        ? List.of("Applied", "Referral Requested", "Interview", "Offer")
        : List.of("Applied", "Interview", "Offer");

    int currentIndex = stages.indexOf(app.getStatus());
    if (currentIndex < 0) currentIndex = 0;
    List<String> reachedStages = stages.subList(0, currentIndex + 1);

    StringBuilder promptBuilder = new StringBuilder();
    promptBuilder.append("Write in ").append(language).append(". This is for a ").append(gender).append(" job seeker. ")
            .append("For a job application to ").append(app.getCompany())
            .append(" for the role ").append(app.getRole())
            .append(", write one short, warm, motivating sentence for each stage below, ")
            .append("each tying the stage to the given personal goal. If writing in Hindi, use grammar matching a ")
            .append(gender).append(" subject. ")
            .append("Reply with exactly one line per stage, no numbering, no extra text.\n");

    for (int i = 0; i < reachedStages.size(); i++) {
        String goal = goals.isEmpty() ? "their dreams" : goals.get(i % goals.size());
        promptBuilder.append("Stage: ").append(reachedStages.get(i)).append(", Goal: ").append(goal).append("\n");
    }

    String result = groqService.generateNudge(promptBuilder.toString());
    List<String> captions;
    if (result != null) {
        captions = Arrays.asList(result.split("\n"));
    } else {
        captions = reachedStages.stream().map(s -> "One step closer to your goal.").toList();
    }

    captionCache.put(id, captions);
    return captions;
}
    public void clearCaptionCache() {
        captionCache.clear();
    }
}