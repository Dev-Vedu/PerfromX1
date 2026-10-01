package com.pms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "claude")
public class ClaudeProperties {
    private String apiKey = "";
    private String baseUrl = "https://api.anthropic.com/v1/messages";
    private String model = "claude-sonnet-4-6";
    private int maxTokens = 1200;
    private boolean enabled = true;
}

