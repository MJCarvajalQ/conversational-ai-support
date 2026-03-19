# Conversational AI Support System

A Java CLI application featuring two collaborating AI agents — a **Technical Specialist** and a **Billing Specialist** — sharing a single conversation. The system routes each user message to the appropriate agent, supports multi-turn context, and dynamically switches between agents mid-conversation.

Built with Java 17 and the Claude API. No agentic frameworks — all orchestration is plain Java.

---

## Architecture

```
User input → CLI → Orchestrator → RouterClassifier (Claude, ~10 tokens)
                                         │
                       ┌─────────────────┼──────────────────┐
                  "technical"        "billing"         "out_of_scope"
                       │                 │                   │
               TechnicalAgent     BillingAgent          static msg
               (RAG + Claude)    (tool loop)
                       │                 │
                       └────────┬────────┘
                        ConversationHistory (shared)
```

### Key Components

| Component | Responsibility |
|---|---|
| `RouterClassifier` | Cheap Claude call classifies each message as `technical`, `billing`, or `out_of_scope` |
| `TechnicalAgent` | TF-IDF retrieval → injects top-3 doc chunks into system prompt → calls Claude |
| `BillingAgent` | Synchronous tool-calling loop (max 5 iterations) → returns final descriptive response |
| `ConversationHistory` | Shared `ArrayDeque<Message>`, enforces alternating user/assistant order, trimmed to last 10 turns |
| `EmbeddingService` | Builds TF-IDF vocabulary at startup, computes cosine similarity for RAG search |
| `VectorStore` | Stores embedded chunks, returns top-k matches for a query |
| `ToolExecutor` | Dispatches tool calls by name, validates inputs before execution |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- An [Anthropic API key](https://console.anthropic.com/)

---

## Setup & Running

```bash
# 1. Clone the repository
git clone https://github.com/MJCarvajalQ/conversational-ai-support
cd conversational-ai-support

# 2. Add your API key
cp config.properties.example config.properties
# Open config.properties and replace YOUR_ANTHROPIC_API_KEY with your real key

# 3. Build
mvn clean package -q

# 4. Run
java -jar target/conversational-ai-support-1.0-SNAPSHOT.jar
```

> **Note:** `config.properties` is listed in `.gitignore` and will never be committed.

---

## Project Structure

```
conversational-ai-support/
├── docs/                          # Documentation for the Technical Agent (RAG source)
│   ├── api-usage.md
│   ├── configuration.md
│   ├── setup-instructions.md
│   └── troubleshooting.md
├── src/main/java/com/support/
│   ├── Main.java                  # Bootstrap and wiring
│   ├── cli/ConversationCLI.java   # Scanner loop, input sanitization
│   ├── config/AppConfig.java      # Constants and API key loader
│   ├── orchestrator/
│   │   ├── Orchestrator.java      # Turn handler: classify → route → respond
│   │   └── RouterClassifier.java  # Claude-based message classifier
│   ├── agents/
│   │   ├── Agent.java             # Interface
│   │   ├── TechnicalAgent.java    # RAG-powered doc Q&A
│   │   └── BillingAgent.java      # Tool-calling billing assistant
│   ├── rag/
│   │   ├── DocumentLoader.java    # Loads docs/*.md safely
│   │   ├── DocumentChunker.java   # Splits docs into chunks
│   │   ├── EmbeddingService.java  # TF-IDF vocabulary + cosine similarity
│   │   ├── VectorStore.java       # In-memory chunk store + top-k search
│   │   └── DocumentChunk.java     # Chunk model
│   ├── tools/
│   │   ├── ToolDefinition.java    # Interface
│   │   ├── ToolResult.java        # Success/error wrapper
│   │   ├── ToolExecutor.java      # Registry and dispatcher
│   │   └── billing/               # 5 mocked billing tools
│   ├── conversation/
│   │   ├── Message.java
│   │   ├── ConversationHistory.java
│   │   └── ConversationSession.java
│   └── claude/
│       ├── ClaudeClient.java      # Raw HTTP client for Claude Messages API
│       ├── ClaudeResponse.java
│       └── ContentBlock.java
├── config.properties.example      # Safe placeholder — copy to config.properties
└── pom.xml
```

---

## Billing Tools

All tools are mocked (no real database). Each validates its inputs and returns a `ToolResult` — never throws.

| Tool | Description |
|---|---|
| `get_customer_plan` | Returns plan name, price, and renewal date |
| `open_refund_case` | Opens a refund case, returns a case ID |
| `send_refund_form` | Simulates sending a refund form by email |
| `get_billing_history` | Returns the last 3 invoices |
| `explain_refund_timeline` | Returns policy text by payment method |

---

## Security Measures

- **Prompt injection**: User input is wrapped in `<user_input>` XML tags and capped at 2000 characters
- **API key**: Loaded from gitignored `config.properties` — never logged or exposed in error messages
- **Path traversal**: `DocumentLoader` uses `toRealPath()` + `startsWith()` to block directory traversal
- **Tool input validation**: Each billing tool validates `customerId`, `amount`, and `email` before executing
- **Auth note**: This is a CLI demo — no user authentication is implemented. A production version would require identity verification before allowing billing tool access

---

## Example Conversations

### 1. Technical Question
```
You: I'm getting a 429 error when calling the API.

Agent: A 429 Too Many Requests error means you have exceeded the rate limit.
According to api-usage.md, the default rate limit is 100 requests per minute
for the Pro plan. To resolve this:
- Implement exponential backoff and retry logic in your client.
- Check the Retry-After response header for how long to wait.
- Cache GET responses where possible to reduce repeated calls.
If you need a higher rate limit, contact support to request a quota increase.
```

### 2. Billing Question (with tool calls)
```
You: I think I was charged twice last month. My customer ID is CUST-002.

[INFO] Router decision: billing
[INFO] BillingAgent handling turn (iteration cap: 5)

Agent: I've reviewed your billing history for customer CUST-002. I can see two
charges were recorded. I have opened refund case RF-A1B2C3D4 for the duplicate
charge of $99.00. You can expect resolution within 3–5 business days. Would you
like me to send the refund form to your email address?
```

### 3. Mid-Conversation Agent Switch
```
You: What is my current plan?

[INFO] Router decision: billing
[INFO] BillingAgent handling turn (iteration cap: 5)

Agent: Your current plan is the Pro Plan at $99/month. The next renewal date is 2026-04-15.

You: How do I configure the API timeout for the Pro Plan?

[INFO] Router decision: technical
[INFO] TechnicalAgent handling turn.

Agent: According to configuration.md, you can configure the HTTP timeout using
the http.timeout setting in config.yaml or the HTTP_TIMEOUT_MS environment
variable. For the Pro Plan's higher request volume, a value between 30,000 and
60,000 milliseconds is recommended...
```

### 4. Out-of-Scope Request
```
You: What's the weather like today?

[INFO] Router decision: out_of_scope

Agent: I'm sorry, I can only assist with technical or billing questions.
For other inquiries, please contact our general support team.
```

---

## Future Improvements

- **Semantic embeddings**: The current RAG implementation uses TF-IDF, which relies on exact keyword overlap. A production environment should use a semantic embedding model (such as `text-embedding-ada-002` or a similar model) to capture meaning across synonyms and paraphrases — for example, matching "authentication errors" to documentation that uses the term "login failures".
- **User authentication**: Add identity verification before exposing billing tools.
- **Persistent conversation history**: Store sessions to a database for multi-session continuity.
- **Streaming responses**: Use Claude's streaming API for a more responsive CLI experience.
- **Unit and integration tests**: Add JUnit tests for each agent, tool, and the routing classifier.
