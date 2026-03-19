package com.support;

import com.support.agents.BillingAgent;
import com.support.agents.TechnicalAgent;
import com.support.cli.ConversationCLI;
import com.support.claude.ClaudeClient;
import com.support.orchestrator.Orchestrator;
import com.support.orchestrator.RouterClassifier;
import com.support.rag.*;
import com.support.tools.ToolExecutor;
import com.support.tools.billing.*;

import java.util.List;

/**
 * Application entry point.
 *
 * Bootstrap sequence:
 *   1. Load all docs/*.md files
 *   2. Chunk each document
 *   3. Build TF-IDF vocabulary across all chunks
 *   4. Compute TF-IDF vectors and store chunks in VectorStore
 *   6. Instantiate Claude client, tools, agents, router, orchestrator
 *   7. Start the CLI conversation loop
 */
public class Main {

    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %5$s%n");
        try {
            System.out.println("[Boot] Starting Conversational AI Support System...");

            // --- Step 1 & 2: Load and chunk documentation ---
            DocumentLoader loader = new DocumentLoader();
            DocumentChunker chunker = new DocumentChunker();

            List<String[]> rawDocs = loader.loadAll();
            List<DocumentChunk> allChunks = new java.util.ArrayList<>();
            for (String[] doc : rawDocs) {
                String filename = doc[0];
                String content  = doc[1];
                List<DocumentChunk> chunks = chunker.chunk(filename, content);
                allChunks.addAll(chunks);
            }
            System.out.println("[Boot] Total chunks created: " + allChunks.size());

            // --- Step 3: Build TF-IDF vocabulary ---
            EmbeddingService embeddingService = new EmbeddingService();
            embeddingService.buildVocabulary(allChunks);

            // --- Step 4 & 5: Compute vectors and populate VectorStore ---
            VectorStore vectorStore = new VectorStore(embeddingService);
            vectorStore.addAll(allChunks);

            // --- Step 6: Wire up components ---
            ClaudeClient claudeClient = new ClaudeClient();

            ToolExecutor toolExecutor = new ToolExecutor();
            toolExecutor.register(new GetCustomerPlanTool());
            toolExecutor.register(new OpenRefundCaseTool());
            toolExecutor.register(new SendRefundFormTool());
            toolExecutor.register(new GetBillingHistoryTool());
            toolExecutor.register(new ExplainRefundTimelineTool());

            TechnicalAgent technicalAgent = new TechnicalAgent(claudeClient, vectorStore);
            BillingAgent   billingAgent   = new BillingAgent(claudeClient, toolExecutor);
            RouterClassifier router        = new RouterClassifier(claudeClient);

            Orchestrator orchestrator = new Orchestrator(router, technicalAgent, billingAgent);

            // --- Step 7: Start CLI ---
            System.out.println("[Boot] Ready.\n");
            ConversationCLI cli = new ConversationCLI(orchestrator);
            cli.start();

        } catch (Exception e) {
            System.err.println("[Fatal] Failed to start: " + e.getMessage());
            System.exit(1);
        }
    }
}
