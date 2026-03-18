package com.support.claude;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Deserializes the relevant fields from a Claude API response.
 * Unknown fields (id, model, usage, etc.) are safely ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResponse {

    @JsonProperty("stop_reason")
    private String stopReason;

    private List<ContentBlock> content;

    public ClaudeResponse() {}

    public String getStopReason() { return stopReason; }
    public void setStopReason(String stopReason) { this.stopReason = stopReason; }

    public List<ContentBlock> getContent() { return content; }
    public void setContent(List<ContentBlock> content) { this.content = content; }

    /**
     * Returns the first text block's content, or null if none exists.
     */
    public String getFirstTextContent() {
        if (content == null) return null;
        return content.stream()
            .filter(b -> "text".equals(b.getType()))
            .map(ContentBlock::getText)
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns true when the model requested one or more tool calls.
     */
    public boolean isToolUse() {
        return "tool_use".equals(stopReason);
    }
}
