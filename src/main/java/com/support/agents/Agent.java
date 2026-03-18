package com.support.agents;

import com.support.conversation.ConversationSession;

/**
 * Contract for all agent implementations.
 * The Orchestrator calls handle() after routing a user message to the correct agent.
 *
 * At the time handle() is called, the user message has already been added to the
 * shared ConversationSession history by the Orchestrator.
 */
public interface Agent {

    /**
     * Processes a user message and returns the agent's text response.
     *
     * @param userMessage The raw user input for this turn.
     * @param session     The shared conversation session (history already updated).
     * @return The agent's response, ready to be added to shared history.
     */
    String handle(String userMessage, ConversationSession session);
}
