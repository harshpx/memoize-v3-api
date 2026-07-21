# AI / LLM Integration

## Overview

Memoize integrates with **Google Gemini** models via the Spring AI framework for conversational AI features. It uses **pgvector** (PostgreSQL extension) for vector storage to enable memory and knowledge retrieval via RAG (Retrieval-Augmented Generation).

---

## 1. Spring AI Configuration

### Dependencies

The project uses Spring AI's Google GenAI integration (`spring-ai-google-genai`) along with pgvector for vector storage (`spring-ai-pgvector-store`).

---

## 2. Gemini Models

### LLM Configuration (LlmConfig.java)

Five `ChatClient` beans are configured with different combinations of chat memory and documentation knowledge:

| Bean Name                       | Model                     | Temperature | Chat Memory | Doc Knowledge | Purpose                        |
|---------------------------------|---------------------------|-------------|-------------|---------------|--------------------------------|
| `simple-gemini-3.1-flash-lite` | gemini-3.1-flash-lite     | 0.3         | ❌ No       | ❌ No         | Conversation name generation    |
| `memory-gemini-3.1-flash-lite` | gemini-3.1-flash-lite     | 0.3         | ✅ Yes      | ❌ No         | Main chat (no knowledge base)   |
| `memory-rag-gemini-3.1-flash-lite` | gemini-3.1-flash-lite | 0.3         | ✅ Yes      | ✅ Yes        | Chat with knowledge base (RAG)  |
| `memory-gemini-3.5-flash`      | gemini-3.5-flash          | 0.3         | ✅ Yes      | ❌ No         | Reserved / future use           |
| `memory-rag-gemini-3.5-flash`  | gemini-3.5-flash          | 0.3         | ✅ Yes      | ✅ Yes        | Reserved / future use (RAG)     |

### Model Parameters

- **Temperature**: 0.3 (low creativity, more deterministic responses)
- **Model**: gemini-3.1-flash-lite (fast, cost-effective)

---

## 3. Vector Store Configuration

### Two Vector Stores

| Bean Name                   | Table Name                   | Dimensions | Distance Type | Index Type | Purpose                    |
|-----------------------------|------------------------------|------------|---------------|------------|----------------------------|
| `chat-memory-vector-store`  | chat_memory_vector_store     | 768        | COSINE_DISTANCE | HNSW     | Store chat conversation memory |
| `knowledge-vector-store`    | knowledge_vector_store       | 768        | COSINE_DISTANCE | HNSW     | Store application knowledge base |

### Shared Settings

- **Dimensions**: 768 (matching the embedding model output)
- **Distance Metric**: COSINE_DISTANCE (best for semantic similarity)
- **Index Type**: HNSW (Hierarchical Navigable Small World - efficient approximate nearest neighbor search)
- **Initialize Schema**: false (schema managed externally)
- **Max Document Batch Size**: 10000

### Chat Memory Advisor

The `VectorStoreChatMemoryAdvisor` (used in `memory-gemini-3.1-flash-lite`, `memory-rag-gemini-3.1-flash-lite`, etc.):

```java
VectorStoreChatMemoryAdvisor memoryAdvisor = VectorStoreChatMemoryAdvisor
    .builder(chatMemoryVectorStore)
    .defaultTopK(5)
    .build();
```

This advisor automatically:
1. Stores each conversation turn as an embedding in the vector store
2. Retrieves relevant past exchanges (default: last 5) as context for new queries
3. Filters by `chat_memory_conversation_id` to keep memory scoped per conversation

### Question Answer Advisor (RAG)

The `QuestionAnswerAdvisor` (used in `memory-rag-gemini-3.1-flash-lite`, `memory-rag-gemini-3.5-flash`):

```java
QuestionAnswerAdvisor knowledgeAdvisor = QuestionAnswerAdvisor.builder(knowledgeVectorStore)
    .searchRequest(SearchRequest.builder().similarityThreshold(0.55).build())
    .build();
```

This advisor:
1. Retrieves relevant chunks from the `knowledge_vector_store` based on the user query
2. Injects them as context into the LLM prompt
3. Uses a similarity threshold of 0.55 for relevance filtering

---

## 4. RAG Service (RagService)

### Purpose

The `RagService` determines whether a user query requires knowledge base context and retrieves relevant documents from the app knowledge store.

### Interface

| Method | Returns | Description |
|--------|---------|-------------|
| `searchDocsFromKnowledgeStore(query)` | `List<Document>` | Searches the knowledge vector store for relevant documents |
| `requireKnowledgeStore(query)` | `boolean` | Returns true if relevant documents are found (threshold: 0.65) |

### Implementation (RagServiceImpl)

```java
@Service
public class RagServiceImpl implements RagService {
    
    public List<Document> searchDocsFromKnowledgeStore(String query) {
        return knowledgeVectorStore.similaritySearch(
            SearchRequest.builder()
                .query(query)
                .similarityThreshold(0.65)
                .topK(3)
                .build()
        );
    }

    public boolean requireKnowledgeStore(String query) {
        return !searchDocsFromKnowledgeStore(query).isEmpty();
    }
}
```

### How RAG is Used in Chat Flow

In `ChatServiceImpl.queryLlmStream()`:

```java
ChatClient chatClient = ragService.requireKnowledgeStore(query) 
    ? docAndMemoryChatClient   // memory + RAG
    : memoryChatClient;        // memory only
```

- If the query is **app-related** (e.g., "How do I create a note?"), relevant docs are found in the knowledge store → the `memory-rag-gemini-3.1-flash-lite` client is used (memory + documentation context)
- If the query is **general** (e.g., "Explain Java Streams"), no relevant docs are found → the `memory-gemini-3.1-flash-lite` client is used (memory only)

### Similarity Thresholds

| Advisor / Service | Threshold | Purpose |
|-------------------|-----------|---------|
| `RagServiceImpl` | 0.65 | Determines if query needs knowledge base context |
| `QuestionAnswerAdvisor` (in LlmConfig) | 0.55 | Filters chunks actually injected into the prompt |

---

## 5. Knowledge Base Loading (AppKnowledgeLoader)

### Startup Process

On `ApplicationReadyEvent`, the `AppKnowledgeLoader`:

1. **Scans** `classpath:knowledge/*.md` for all markdown files
2. For each file:
   - Computes **SHA-256 hash** of the content
   - Compares against stored hash in the vector store metadata
   - If **unchanged** → skips (incremental update)
   - If **changed or new** → deletes old embeddings, splits into chunks via `TokenTextSplitter`, and re-indexes
3. **Removes** embeddings for files that no longer exist in the classpath

### Why Incremental Instead of Rebuild?

The knowledge base uses **incremental updates** to avoid full truncation on every startup:
- Performance: only changed files are re-indexed
- The stored hash is kept in `metadata->>'hash'` within the pgvector table
- Deleted file cleanup ensures stale data doesn't accumulate

### Chunking

- Uses `TokenTextSplitter` (Spring AI default) to split each markdown document into smaller chunks
- Each chunk stores metadata: `source` (filename), `type` ("memoize-doc"), `hash` (SHA-256)

### SQL Queries Used

**Get stored hash for a file:**
```sql
SELECT metadata->>'hash' FROM knowledge_vector_store WHERE metadata->>'source'=?
```

**Delete embeddings for a file:**
```sql
DELETE FROM knowledge_vector_store WHERE metadata->>'source'=?
```

**Clean up deleted files:**
```sql
DELETE FROM knowledge_vector_store WHERE metadata->>'source' IN (?, ?, ...)
```

### Knowledge Base Files

The following files are loaded from `classpath:knowledge/`:

| File | Description |
|------|-------------|
| `00-memoize.md` | App overview and introduction |
| `01-architecture-overview.md` | High-level architecture |
| `02-authentication.md` | Auth flows summary |
| `03-ai-chat.md` | AI chat features summary |
| `04-notes-management.md` | Notes features summary |
| `05-events-management.md` | Events features summary |
| `06-user-management.md` | User management summary |
| `07-email-service.md` | Email service summary |
| `08-ai-llm-integration.md` | LLM integration summary |

---

## 6. Embeddings

The embedding model is auto-configured by Spring AI (Google GenAI embedding model). Key properties:
- **Output Dimensions**: 768
- **Model**: Google's text-embedding model (auto-configured)

---

## 7. Async Architecture

### LLM Task Executor

```java
@Bean("llmTaskExecutor")
public Executor llmTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(16);
    executor.setQueueCapacity(100);
    executor.setKeepAliveSeconds(30);
    executor.setThreadNamePrefix("LLMAsync-");
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(15);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    return executor;
}
```

### Why Async?

| Task                      | Sync/Async | Reason                                          |
|---------------------------|------------|-------------------------------------------------|
| Streaming LLM response    | Async (Reactor) | Non-blocking SSE stream to client          |
| Saving chat to DB         | Async (CompletableFuture) | Don't block response stream          |
| Generating conversation name | Async (CompletableFuture) | Don't delay response to user       |
| Vector store operations   | Synchronous | Part of Spring AI advisor pipeline              |

---

## 8. Streaming Response Flow

```
[Client] -- POST /ai/chat/{id}?query=... --> [ChatControllerImpl]
                                                |
                                                v
                                        [ChatServiceImpl.queryLlmStream()]
                                                |
                            ┌───────────────────┴────────────────────┐
                            |    1. Validate conversation ownership   |
                            |    2. Save user query as Chat(QUESTION) |
                            |    3. Check RAG need via RagService     |
                            |       ├── Need docs? → docAndMemoryChatClient
                            |       └── No docs?  → memoryChatClient |
                            |    4. Stream response from Gemini       |
                            |         via VectorStoreChatMemoryAdvisor|
                            |         (with last 5 exchanges context)  |
                            |    5. On complete: save answer &         |
                            |       optionally generate name           |
                            └─────────────────────────────────────────┘
                                                |
                                                v
                                Flux<String> (SSE stream)
```

### Timeout

- **Stream timeout**: 30 seconds
- If the LLM doesn't respond within 30 seconds, the stream errors out

### Error Handling

```java
.onErrorResume((error) -> {
    String fullAnswer = "Error generating response";
    // Save error response to DB asynchronously
    return Flux.just(fullAnswer);
});
```

---

## 9. Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Application Startup                         │
│                                                                   │
│  AppKnowledgeLoader                                               │
│  ┌─────────────────────────────────────────────┐                  │
│  │ 1. Scan classpath:knowledge/*.md            │                  │
│  │ 2. For each file:                           │                  │
│  │   - Compute SHA-256 hash                    │                  │
│  │   - Compare with stored hash                │                  │
│  │   - If changed/new: split, embed, store     │                  │
│  │   - If unchanged: skip                      │                  │
│  │ 3. Remove embeddings for deleted files      │                  │
│  └─────────────────────────────────────────────┘                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Chat Query Flow                              │
│                                                                   │
│  User Query                                                       │
│       │                                                           │
│       ▼                                                           │
│  RagService.requireKnowledgeStore(query)                          │
│       │                                                           │
│       ├── YES → Use memory-rag-gemini-3.1-flash-lite              │
│       │         (memory + knowledge base context)                  │
│       │                                                           │
│       └── NO  → Use memory-gemini-3.1-flash-lite                  │
│                 (memory only)                                      │
│                                                                   │
│       ▼                                                           │
│  Gemini Model (gemini-3.1-flash-lite)                             │
│       │                                                           │
│       ├── VectorStoreChatMemoryAdvisor                            │
│       │   ├── Retrieve past exchanges from chat_memory_vector_store│
│       │   └── Inject as context to the prompt                     │
│       │                                                           │
│       ├── [If RAG] QuestionAnswerAdvisor                          │
│       │   ├── Retrieve docs from knowledge_vector_store           │
│       │   └── Inject as context to the prompt                     │
│       │                                                           │
│       ▼                                                           │
│  Stream Response [SSE]                                            │
│       │                                                           │
│       ▼                                                           │
│  Save Chat (Async) → chat table (QUESTION + ANSWER)               │
│  Save Memory → chat_memory_vector_store (auto by advisor)         │
│  Generate Name (Async, if first exchange) → conversation table   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 10. Frontend Integration

### How the Frontend Consumes AI

The frontend interacts with the AI backend through two primary mechanisms:

**1. REST API Calls (Conversation CRUD):**
- `POST /ai/conversation` — Create new conversation
- `POST /ai/conversation/all` — Fetch all conversations (creates one if none exists)
- `GET /ai/conversation/{id}` — Get specific conversation
- `DELETE /ai/conversation/{id}` — Delete conversation
- `GET /ai/chat/{conversationId}` — Fetch chat history for a conversation

**2. SSE Stream (Chat Query):**
- `POST /ai/chat/{conversationId}?query=...` — Send a query and receive a streaming SSE response
- The response is consumed as a **ReadableStream** (EventSource) in the browser
- Each SSE `data:` chunk is appended character-by-character to the chat UI in real-time

### SSE Consumption Pattern (Frontend)

```
[Frontend] -- POST /ai/chat/{id}?query=... --> [Backend]

[Backend streams response via SSE]:
    data: H
    data:  hello
    data:  how
    data:  can
    data:  I
    data:  help?
    
[Frontend accumulates chunks and renders progressively]
    "H" → "He" → "Hel" → "Hell" → "Hello" → "Hello how" → "Hello how can" → ...
```

### User Experience

- **Real-time streaming**: Text appears character-by-character as the AI generates it
- **Timer**: A *"3s ..."* indicator with animated dots shows while the AI is processing
- **Input disabled**: The text input is disabled during streaming to prevent concurrent requests
- **Completion**: Input re-enables, conversation name may auto-update via polling

### Knowledge Base Context

The `knowledge_vector_store` is populated from `classpath:knowledge/*.md` files at startup. The RAG system automatically detects if a user query relates to the app documentation (e.g., "How do I create a note?") and enriches the LLM response with relevant documentation context.

### UI States

| State | Frontend Behavior |
|-------|-------------------|
| **Streaming** | Character-by-character text appearing + disabled input + timer |
| **Complete** | Full response rendered, input re-enabled, name may auto-update |
| **Error** | "Error generating response" message shown in chat |
| **Timeout (30s)** | Stream errors out, error fallback displayed |
| **Conversations Loading** | Skeleton placeholders in conversation drawer |
| **Chat History Loading** | Skeleton placeholders while fetching past messages |