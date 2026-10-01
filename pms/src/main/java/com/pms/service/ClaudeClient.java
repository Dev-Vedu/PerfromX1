package com.pms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.config.ClaudeProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper over the Claude messages API.
 * Returns an empty Optional-style blank string when no key is configured, so every
 * caller can fall back to its own offline heuristic and the app still runs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeClient {

    private final ClaudeProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private RestClient client;

    public boolean isConfigured() {
        return props.isEnabled() && props.getApiKey() != null && !props.getApiKey().isBlank();
    }

    private RestClient client() {
        if (client == null) {
            client = RestClient.builder()
                    .baseUrl(props.getBaseUrl())
                    .defaultHeader("x-api-key", props.getApiKey())
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .defaultHeader("content-type", MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
        return client;
    }

    /** Returns Claude's text, or a blank string if the call is unavailable or fails. */
    public String ask(String systemPrompt, String userPrompt) {
        if (!isConfigured()) return "";
        try {
            Map<String, Object> body = Map.of(
                    "model", props.getModel(),
                    "max_tokens", props.getMaxTokens(),
                    "system", systemPrompt,
                    "messages", List.of(Map.of("role", "user", "content", userPrompt)));

            String raw = client().post().body(body).retrieve().body(String.class);
            JsonNode root = mapper.readTree(raw);
            StringBuilder sb = new StringBuilder();
            for (JsonNode block : root.path("content")) {
                if ("text".equals(block.path("type").asText())) {
                    sb.append(block.path("text").asText());
                }
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("Claude call failed, falling back to local analysis: {}", e.getMessage());
            return "";
        }
    }

    /** Strips markdown fences so a JSON reply can be parsed. */
    public JsonNode askJson(String systemPrompt, String userPrompt) {
        String text = ask(systemPrompt, userPrompt);
        if (text.isBlank()) return null;
        String clean = text.replaceAll("(?s)```(json)?", "").trim();
        try {
            return mapper.readTree(clean);
        } catch (Exception e) {
            log.warn("Claude returned unparseable JSON: {}", e.getMessage());
            return null;
        }
    }
}
