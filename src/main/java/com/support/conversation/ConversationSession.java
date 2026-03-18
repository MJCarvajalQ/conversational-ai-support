package com.support.conversation;

import com.support.config.AppConfig;

/**
 * Holds the shared conversation state for a single user session.
 * Passed through the Orchestrator to all agents.
 */
public class ConversationSession {

    private final ConversationHistory history;
    private String lastActiveAgent; // "technical", "billing", or null

    public ConversationSession() {
        this.history = new ConversationHistory(AppConfig.MAX_HISTORY_TURNS);
    }

    public ConversationHistory getHistory() { return history; }

    public String getLastActiveAgent() { return lastActiveAgent; }

    public void setLastActiveAgent(String agent) { this.lastActiveAgent = agent; }
}
