package com.support.cli;

import com.support.config.AppConfig;
import com.support.conversation.ConversationSession;
import com.support.orchestrator.Orchestrator;

import java.util.Scanner;

/**
 * Command-line interface for the conversational support system.
 *
 * Security:
 *   - User input is capped at MAX_INPUT_CHARS characters before being processed.
 *   - Input is wrapped in <user_input> XML tags when passed to the Orchestrator,
 *     clearly separating user data from system instructions (prompt injection defense).
 */
public class ConversationCLI {

    private static final String BANNER =
        "╔══════════════════════════════════════════════╗\n" +
        "║     Conversational AI Support System         ║\n" +
        "║  Type 'exit' or 'quit' to end the session.  ║\n" +
        "╚══════════════════════════════════════════════╝";

    private final Orchestrator orchestrator;

    public ConversationCLI(Orchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /** Starts the interactive conversation loop. Blocks until the user exits. */
    public void start() {
        System.out.println(BANNER);
        System.out.println();

        ConversationSession session = new ConversationSession();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("You: ");
            if (!scanner.hasNextLine()) break;

            String rawInput = scanner.nextLine().trim();
            if (rawInput.isEmpty()) continue;
            if (rawInput.equalsIgnoreCase("exit") || rawInput.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye! Have a great day.");
                break;
            }

            // Cap input length to prevent oversized API calls
            if (rawInput.length() > AppConfig.MAX_INPUT_CHARS) {
                System.out.println("[System] Input too long. Please limit your message to " +
                    AppConfig.MAX_INPUT_CHARS + " characters.");
                continue;
            }

            // Wrap in XML tags to separate user data from system instructions
            String sanitizedInput = "<user_input>" + rawInput + "</user_input>";

            try {
                String response = orchestrator.handleTurn(sanitizedInput, session);
                System.out.println();
                System.out.println("Agent: " + response);
                System.out.println();
            } catch (Exception e) {
                System.out.println("[Error] Something went wrong: " + e.getMessage());
                System.out.println("Please try again.");
                System.out.println();
            }
        }

        scanner.close();
    }
}
