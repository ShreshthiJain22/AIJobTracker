package com.example.job_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateNudge(String prompt) {
    String url = "https://api.groq.com/openai/v1/chat/completions";

    Map<String, Object> requestBody = Map.of(
        "model", "openai/gpt-oss-20b",
        "messages", List.of(
            Map.of("role", "user", "content", prompt)
        )
    );

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);
    HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

    try {
        Map response = restTemplate.postForObject(url, request, Map.class);
        List choices = (List) response.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");
        String result = (String) message.get("content");
        return cleanMarkdown(result);
    } catch (Exception e) {
        System.out.println("Groq API call failed: " + e.getMessage());
        return null;
    }
}

private String cleanMarkdown(String text) {
    if (text == null) return null;
    return text
        .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
        .replaceAll("\\*(.*?)\\*", "$1")
        .replaceAll("__(.*?)__", "$1")
        .replaceAll("_(.*?)_", "$1")
        .trim();
}
}