package com.cts.servicenow.services.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.cts.servicenow.config.DiscordWebhookUrl;
import com.cts.servicenow.services.SendNotification;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SendNotificationImpl implements SendNotification {
    
    private final DiscordWebhookUrl discordWebhookUrl;

    private final RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper;

    public SendNotificationImpl(RestTemplate restTemplate, DiscordWebhookUrl discordWebhookUrl, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.discordWebhookUrl = discordWebhookUrl ;
        this.objectMapper = objectMapper;
    }

    @Override
    public String sendDiscordNotification(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create the payload using ObjectMapper
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("content", message);
        String payload;
        try {
            payload = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("Failed to create JSON payload", e);
            return "Failed to create JSON payload: " + e.getMessage();
        }
        log.info("Payload: {}", payload);

        HttpEntity<String> entity = new HttpEntity<>(payload, headers);

        int maxRetries = 5;
        int retryCount = 0;
        long waitTime = 1000; // Initial wait time in milliseconds

        while (retryCount < maxRetries) {
            try {
                log.info("Sending request to Discord webhook URL: {}", discordWebhookUrl.getDiscordWebhookUrl());
                ResponseEntity<String> response = restTemplate.exchange(discordWebhookUrl.getDiscordWebhookUrl(), HttpMethod.POST, entity, String.class);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Notification sent successfully!");
                    return "Notification sent successfully!";
                } else if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    String retryAfter = response.getHeaders().getFirst("Retry-After");
                    if (retryAfter != null) {
                        long retryAfterMillis = (long) (Double.parseDouble(retryAfter) * 1000);
                        log.warn("Rate limited. Retry after {} milliseconds", retryAfterMillis);
                        TimeUnit.MILLISECONDS.sleep(retryAfterMillis);
                    } else {
                        log.warn("Rate limited. Retry after {} milliseconds", waitTime);
                        TimeUnit.MILLISECONDS.sleep(waitTime);
                        waitTime *= 2; // Exponential backoff
                    }
                } else {
                    log.error("Failed to send notification: {}", response.getStatusCode());
                    return "Failed to send notification: " + response.getStatusCode();
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.warn("Rate limited. Retry after {} milliseconds", waitTime);
                    try {
                        TimeUnit.MILLISECONDS.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "Failed to send notification: Interrupted during wait";
                    }
                    waitTime *= 2; // Exponential backoff
                } else {
                    log.error("Exception occurred while sending notification", e);
                    return "Failed to send notification: " + e.getMessage();
                }
            } catch (Exception e) {
                log.error("Exception occurred while sending notification", e);
                return "Failed to send notification: " + e.getMessage();
            }
            retryCount++;
        }
        return "Failed to send notification after multiple attempts";
    }

}


