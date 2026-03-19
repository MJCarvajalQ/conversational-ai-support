package com.support.agents;

import com.support.claude.ClaudeClient;
import com.support.claude.ClaudeResponse;
import com.support.claude.ContentBlock;
import com.support.config.AppConfig;
import com.support.conversation.ConversationSession;
import com.support.conversation.Message;
import com.support.tools.ToolExecutor;
import com.support.tools.ToolResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Handles billing questions using a synchronous tool-calling loop.
 *
 * Design:
 *   - Maintains a local workingHistory for the multi-turn tool loop.
 *     This keeps intermediate tool_use / tool_result messages out of the
 *     shared ConversationSession, which stays clean for other agents.
 *   - Only the final text response is returned to the Orchestrator,
 *     which then adds it to the shared history.
 *   - Responses are written to be self-contained and descriptive
 *     (e.g. "Your current plan is the Pro Plan") so that context is
 *     preserved if the Technical Agent takes over on the next turn.
 *   - Capped at MAX_TOOL_ITERATIONS to prevent runaway API usage.
 */
public class BillingAgent implements Agent {

    private static final String SYSTEM_PROMPT =
        "You are a Billing Specialist for a software support service. " +
        "Your role is to assist customers with billing questions and requests " +
        "using the tools available to you.\n\n" +
        "Rules:\n" +
        "- Use the provided tools to look up customer information, open refund cases, " +
        "send refund forms, retrieve billing history, and explain refund policies.\n" +
        "- Always produce complete, descriptive responses. For example, say " +
        "'Your current plan is the Pro Plan at $99/month' rather than just 'It's the Pro Plan'. " +
        "This ensures full context is preserved across the conversation.\n" +
        "- Be professional, empathetic, and clear.\n" +
        "- If a tool returns an error, explain the issue to the customer and suggest next steps.\n" +
        "- If you need information from the customer (such as their customer ID or email), ask for it.";

    private static final Logger logger = Logger.getLogger(BillingAgent.class.getName());

    private static final String CONTENT_TYPE_TOOL_USE = "tool_use";
    private static final String ROLE_ASSISTANT        = "assistant";
    private static final String ROLE_USER             = "user";

    private final ClaudeClient claudeClient;
    private final ToolExecutor toolExecutor;

    public BillingAgent(ClaudeClient claudeClient, ToolExecutor toolExecutor) {
        this.claudeClient = Objects.requireNonNull(claudeClient, "claudeClient must not be null");
        this.toolExecutor = Objects.requireNonNull(toolExecutor, "toolExecutor must not be null");
    }

    @Override
    public String handle(String userMessage, ConversationSession session) {
        logger.info("BillingAgent handling turn (iteration cap: " + AppConfig.MAX_TOOL_ITERATIONS + ")");
        // Start working history from the shared session history.
        // The session history already ends with the user message (added by Orchestrator).
        List<Message> workingHistory = new ArrayList<>(session.getHistory().getMessages());
        List<java.util.Map<String, Object>> toolSchemas = toolExecutor.getToolSchemas();

        for (int i = 0; i < AppConfig.MAX_TOOL_ITERATIONS; i++) {
            ClaudeResponse response = claudeClient.sendMessage(
                SYSTEM_PROMPT,
                workingHistory,
                toolSchemas,
                AppConfig.AGENT_MAX_TOKENS
            );

            if (response.isToolUse()) {
                // Add the assistant's tool_use response to working history
                workingHistory.add(new Message(ROLE_ASSISTANT, response.getContent()));

                // Execute each tool call and collect results
                List<ContentBlock> toolResults = new ArrayList<>();
                for (ContentBlock block : response.getContent()) {
                    if (CONTENT_TYPE_TOOL_USE.equals(block.getType())) {
                        logger.fine("Tool call: " + block.getName() + " | input: " + block.getInput());
                        ToolResult result = toolExecutor.execute(block.getName(), block.getInput());
                        logger.fine("Tool result: " + result.getContent());
                        toolResults.add(ContentBlock.toolResult(block.getId(), result.getContent()));
                    }
                }

                // Add tool results as a user message to working history
                workingHistory.add(new Message(ROLE_USER, toolResults));

            } else {
                // stop_reason == "end_turn" — extract the final text response
                String finalResponse = response.getFirstTextContent();
                if (finalResponse == null || finalResponse.isBlank()) {
                    logger.warning("Empty final response from Claude in BillingAgent.");
                    return "I was unable to complete your billing request. Please try again.";
                }
                return finalResponse;
            }
        }

        logger.warning("Max tool iterations reached without end_turn response.");
        return "I was unable to complete your request within the allowed steps. " +
               "Please contact our support team directly for assistance.";
    }
}
