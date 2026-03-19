package com.support.orchestrator;

import com.support.claude.ClaudeClient;
import com.support.claude.ClaudeResponse;
import com.support.conversation.ConversationSession;
import com.support.conversation.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Classifies each user message into one of three routes:
 *   "technical"    — technical errors, API, configuration, setup
 *   "billing"      — payments, invoices, refunds, subscription plans
 *   "out_of_scope" — anything else
 *
 * Uses a cheap, constrained Claude call (~10 tokens response).
 * Operates on a SEPARATE minimal message list — never reads from or
 * writes to the shared ConversationSession history.
 */
public class RouterClassifier {

    public static final String ROUTE_TECHNICAL    = "technical";
    public static final String ROUTE_BILLING      = "billing";
    public static final String ROUTE_OUT_OF_SCOPE = "out_of_scope";

    private static final int CLASSIFIER_MAX_TOKENS   = 10;
    private static final int CLASSIFIER_CONTEXT_TURNS = 2;
    private static final int SHORT_MESSAGE_MAX_LENGTH = 10;

    private static final String SYSTEM_PROMPT =
        "You are a message routing classifier for a software support service.\n\n" +
        "Classify the user's message into exactly one of these categories:\n" +
        "- technical: questions about errors, API usage, configuration, setup, integration, or troubleshooting\n" +
        "- billing: questions about payments, invoices, refunds, subscription plans, or billing history\n" +
        "- out_of_scope: anything unrelated to technical support or billing\n\n" +
        "Respond with EXACTLY one word: technical, billing, or out_of_scope.\n" +
        "No explanation, no punctuation, no extra text.";

    private final ClaudeClient claudeClient;

    public RouterClassifier(ClaudeClient claudeClient) {
        this.claudeClient = Objects.requireNonNull(claudeClient, "claudeClient must not be null");
    }

    /**
     * Classifies the current user message using up to the last 2 messages
     * from history for context (helps with follow-up questions).
     *
     * @return "technical", "billing", or "out_of_scope"
     */
    public String classify(String userMessage, ConversationSession session) {
        // Build a minimal message list — do not modify shared history
        List<Message> allMessages = session.getHistory().getMessages();
        List<Message> classifierMessages = new ArrayList<>();

        // Include the previous assistant message (if any) for follow-up context,
        // then the current user message (always last in shared history)
        int size = allMessages.size();
        if (size >= CLASSIFIER_CONTEXT_TURNS) {
            classifierMessages.add(allMessages.get(size - 2)); // previous assistant turn
        }
        classifierMessages.add(allMessages.get(size - 1)); // current user message

        ClaudeResponse response = claudeClient.sendMessage(
            SYSTEM_PROMPT,
            classifierMessages,
            null,
            CLASSIFIER_MAX_TOKENS
        );

        String classification = response.getFirstTextContent();
        if (classification == null) return ROUTE_OUT_OF_SCOPE;

        classification = classification.trim().toLowerCase();
        classification = switch (classification) {
            case "technical" -> ROUTE_TECHNICAL;
            case "billing"   -> ROUTE_BILLING;
            default          -> ROUTE_OUT_OF_SCOPE;
        };

        // Fallback: short messages with no topic keywords (e.g. "No", "Yes", "Ok")
        // stay with the last active agent instead of triggering the out-of-scope wall.
        if (ROUTE_OUT_OF_SCOPE.equals(classification) && isShortMessage(userMessage)) {
            String lastAgent = session.getLastActiveAgent();
            if (lastAgent != null) return lastAgent;
        }

        return classification;
    }

    private boolean isShortMessage(String message) {
        // Strip the XML wrapper added by ConversationCLI before measuring length
        String stripped = message
            .replace("<user_input>", "")
            .replace("</user_input>", "")
            .trim();
        return stripped.length() <= SHORT_MESSAGE_MAX_LENGTH;
    }
}
