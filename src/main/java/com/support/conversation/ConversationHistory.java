package com.support.conversation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Sliding-window conversation history.
 * Enforces the alternating user/assistant order required by the Claude API.
 * Trims to the last maxTurns pairs (1 turn = 1 user + 1 assistant message).
 */
public class ConversationHistory {

    private final ArrayDeque<Message> messages = new ArrayDeque<>();
    private final int maxTurns;

    public ConversationHistory(int maxTurns) {
        this.maxTurns = maxTurns;
    }

    /**
     * Adds a message, enforcing alternating role order.
     * Throws if two consecutive messages share the same role.
     */
    public void add(Message message) {
        if (!messages.isEmpty()) {
            String lastRole = messages.peekLast().getRole();
            if (lastRole.equals(message.getRole())) {
                throw new IllegalStateException(
                    "Cannot add two consecutive '" + message.getRole() + "' messages. " +
                    "Claude API requires strict user/assistant alternation."
                );
            }
        }
        messages.addLast(message);
        trim();
    }

    /** Returns an ordered snapshot of the current history. */
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public String getLastRole() {
        return messages.isEmpty() ? null : messages.peekLast().getRole();
    }

    private void trim() {
        int maxMessages = maxTurns * 2;
        while (messages.size() > maxMessages) {
            messages.pollFirst();
        }
        // After trimming, history must not start with an assistant message
        if (!messages.isEmpty() && "assistant".equals(messages.peekFirst().getRole())) {
            messages.pollFirst();
        }
    }
}
