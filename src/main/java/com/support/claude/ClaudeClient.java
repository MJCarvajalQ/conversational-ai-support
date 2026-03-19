package com.support.claude;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.config.AppConfig;
import com.support.conversation.Message;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP client for the Claude Messages API.
 * Uses java.net.http.HttpClient + Jackson only — no third-party HTTP libraries.
 *
 * Security: Authorization headers are never logged or included in error messages.
 */
public class ClaudeClient {

    private static final String API_URL = AppConfig.CLAUDE_API_URL;
    private static final String MODEL   = AppConfig.CLAUDE_MODEL;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ClaudeClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Sends a request to the Claude Messages API.
     *
     * @param systemPrompt The system prompt for this call.
     * @param messages     Ordered conversation history (must alternate user/assistant).
     * @param tools        Tool schemas to expose to the model, or null/empty for none.
     * @param maxTokens    Maximum tokens in the response.
     * @return Parsed ClaudeResponse.
     * @throws RuntimeException on network error or non-200 response.
     */
    public ClaudeResponse sendMessage(
            String systemPrompt,
            List<Message> messages,
            List<Map<String, Object>> tools,
            int maxTokens) {

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("system", systemPrompt);
            requestBody.put("messages", messages);

            if (tools != null && !tools.isEmpty()) {
                requestBody.put("tools", tools);
            }

            String jsonBody = mapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", AppConfig.CLAUDE_API_KEY)
                .header("anthropic-version", AppConfig.ANTHROPIC_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                // Never log the request (it contains the API key in headers)
                throw new RuntimeException(
                    "Claude API returned HTTP " + response.statusCode() +
                    ". Check your API key and request format."
                );
            }

            return mapper.readValue(response.body(), ClaudeResponse.class);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Failed to communicate with Claude API: " + e.getMessage());
        }
    }
}
