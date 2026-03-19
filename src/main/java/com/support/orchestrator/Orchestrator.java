package com.support.orchestrator;

import com.support.agents.Agent;
import com.support.conversation.ConversationSession;
import com.support.conversation.Message;
import java.util.Objects;

/**
 * Central coordinator for each conversation turn.
 *
 * Turn flow:
 *   1. Add the user message to shared history.
 *   2. Classify the message via RouterClassifier.
 *   3. Route to TechnicalAgent, BillingAgent, or return an out-of-scope message.
 *   4. Add the assistant response to shared history.
 *   5. Return the response to the CLI.
 *
 * The shared history always maintains strict alternating user/assistant order.
 * ConversationHistory.add() enforces this and will throw if violated.
 */
public class Orchestrator {

    private static final String OUT_OF_SCOPE_RESPONSE =
        "I'm sorry, I can only assist with technical or billing questions. " +
        "For other inquiries, please contact our general support team.";

    private final RouterClassifier router;
    private final Agent technicalAgent;
    private final Agent billingAgent;

    public Orchestrator(RouterClassifier router, Agent technicalAgent, Agent billingAgent) {
        this.router = Objects.requireNonNull(router, "router must not be null");
        this.technicalAgent = Objects.requireNonNull(technicalAgent, "technicalAgent must not be null");
        this.billingAgent = Objects.requireNonNull(billingAgent, "billingAgent must not be null");
    }

    /**
     * Processes one conversation turn and returns the agent's response.
     *
     * @param userMessage The raw user input (already sanitized by ConversationCLI).
     * @param session     The shared conversation session.
     * @return The assistant's response text.
     */
    public String handleTurn(String userMessage, ConversationSession session) {
        // Step 1: Add user message to shared history
        session.getHistory().add(Message.user(userMessage));

        // Step 2: Classify
        String route = router.classify(userMessage, session);
        System.out.println("[Router] → " + route);

        // Step 3: Route to the appropriate agent
        String response;
        switch (route) {
            case "technical" -> {
                session.setLastActiveAgent("technical");
                response = technicalAgent.handle(userMessage, session);
            }
            case "billing" -> {
                session.setLastActiveAgent("billing");
                response = billingAgent.handle(userMessage, session);
            }
            default -> response = OUT_OF_SCOPE_RESPONSE;
        }

        // Step 4: Add assistant response to shared history
        session.getHistory().add(Message.assistant(response));

        return response;
    }
}
