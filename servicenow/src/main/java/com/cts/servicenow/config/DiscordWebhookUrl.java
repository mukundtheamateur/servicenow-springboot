package com.cts.servicenow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class DiscordWebhookUrl {

    @Value("${discord.webhook.url}")
    private String discordWebhookUrl;
}
