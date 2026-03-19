package com.support.agents;

import com.support.claude.ClaudeClient;
import com.support.claude.ClaudeResponse;
import com.support.conversation.ConversationSession;
import com.support.rag.DocumentChunk;
import com.support.rag.VectorStore;

import java.util.List;
import java.util.Objects;

/**
 * Answers technical questions strictly from the loaded documentation.
 *
 * On each turn:
 *   1. Retrieves the top-k most relevant documentation chunks via TF-IDF cosine similarity.
 *   2. Injects them into the system prompt.
 *   3. Calls Claude with the full shared conversation history.
 *   4. Returns the response text.
 *
 * The system prompt explicitly forbids guessing. If the answer is not in the docs,
 * the agent must say so and ask the user to clarify or contact support.
 */
public class TechnicalAgent implements Agent {

    private static final String SYSTEM_PROMPT_TEMPLATE =
        "You are a Technical Specialist for a software support service. " +
        "Your role is to answer technical questions STRICTLY using the documentation excerpts provided below.\n\n" +
        "Rules:\n" +
        "- Answer ONLY using information present in the documentation below. " +
        "Do NOT guess, invent, or draw on external knowledge.\n" +
        "- If the answer is not clearly covered by the documentation, say so honestly. " +
        "Ask the user to clarify, or advise them to contact support.\n" +
        "- Always mention which document or section the information comes from.\n" +
        "- Be concise, precise, and professional.\n\n" +
        "--- DOCUMENTATION ---\n" +
        "%s\n" +
        "--- END DOCUMENTATION ---";

    private final ClaudeClient claudeClient;
    private final VectorStore vectorStore;

    public TechnicalAgent(ClaudeClient claudeClient, VectorStore vectorStore) {
        this.claudeClient = Objects.requireNonNull(claudeClient, "claudeClient must not be null");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore must not be null");
    }

    @Override
    public String handle(String userMessage, ConversationSession session) {
        // Retrieve relevant documentation chunks
        List<DocumentChunk> relevantChunks = vectorStore.search(userMessage);
        String context = buildContext(relevantChunks);
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, context);

        // Call Claude with the full shared history (already ends with the user message)
        ClaudeResponse response = claudeClient.sendMessage(
            systemPrompt,
            session.getHistory().getMessages(),
            null,
            1024
        );

        String text = response.getFirstTextContent();
        if (text == null || text.isBlank()) {
            return "I was unable to generate a response. Please try rephrasing your question.";
        }
        return text;
    }

    private String buildContext(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) {
            return "[No relevant documentation found for this query.]";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            sb.append("[Source: ").append(chunk.getSourceFile()).append("]\n");
            sb.append(chunk.getText());
            if (i < chunks.size() - 1) sb.append("\n\n---\n\n");
        }
        return sb.toString();
    }
}
