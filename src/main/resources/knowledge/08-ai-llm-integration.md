# AI / LLM Integration

## Overview

Memoize integrates with **Google Gemini** models via the Spring AI framework for conversational AI features. It uses **pgvector** (PostgreSQL extension) for vector storage to enable memory and knowledge retrieval.

---

## 1. Spring AI Configuration

### Dependencies

The project uses Spring AI's Google GenAI integration (`spring-ai-google-genai`) along with pgvector for vector storage (`spring-ai-pgvector-store`).

---

## 2. Gemini Models

### LLM Configuration (LlmConfig.java)

Three `ChatClient` beans are configured:

| Bean Name                       | Model                     | Temperature | Vector Memory | Purpose                        |
|---------------------------------|---------------------------|-------------|---------------|--------------------------------|
| `vector-store-gemini-3.5-flash` | gemini-3.5-flash          | 0.3         | ✅ Yes         | Main chat (defined but unused) |
| `vector-store-gemini-3.1-flash-lite` | gemini-3.1-flash-lite | 0.3         | ✅ Yes         | Main chat (actually used)      |
| `simple-gemini-3.1-flash-lite` | gemini-3.1-flash-lite     | 0.3         | ❌ No          | Name generation                |

### Model Parameters

- **Temperature**: 0.3 (low creativity, more deterministic responses)
- **Model**: gemini-3.1-flash-lite (fast, cost-effective)
- **Memory**: VectorStoreChatMemoryAdvisor with pgvector

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

### Memory Advisor

```java
VectorStoreChatMemoryAdvisor memoryAdvisor = VectorStoreChatMemoryAdvisor
    .builder(vectorStore)
    .build();
```

This advisor automatically:
1. Stores each conversation turn as an embedding in the vector store
2. Retrieves relevant past exchanges (default: last 5) as context for new queries
3. Filters by `chat_memory_conversation_id` to keep memory scoped per conversation

---

## 4. Knowledge Base Loading (AppKnowledgeLoader)

### Startup Process

On `ApplicationReadyEvent`, the `AppKnowledgeLoader`:

1. **Truncates** the `knowledge_vector_store` table (fresh rebuild on each startup)
2. **Scans** `classpath:knowledge/*.md` for all markdown files
3. **Splits** each document into chunks using `TokenTextSplitter`
4. **Embeds** and stores chunks in the vector store with metadata:
   - `source`: filename of the markdown
   - `type`: "memoize-doc"

### Why Rebuild on Startup?

The knowledge base is rebuilt every time the application starts. This ensures:
- The vector store always reflects the latest documentation
- No manual sync required when docs change
- Simple deployment: just update the markdown files and restart

### Note

The knowledge vector store is populated but **not explicitly queried** in the current chat implementation. The chat service uses `chat_memory_vector_store` for conversation memory. The `knowledge_vector_store` is available for future RAG (Retrieval-Augmented Generation) features.

---

## 5. Embeddings

The embedding model is auto-configured by Spring AI (Google GenAI embedding model). Key properties:
- **Output Dimensions**: 768
- **Model**: Google's text-embedding model (auto-configured)

---

## 6. Async Architecture

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

## 7. Streaming Response Flow

```
[Client] -- POST /ai/chat/{id}?query=... --> [ChatControllerImpl]
                                                |
                                                v
                                        [ChatServiceImpl.queryLlmStream()]
                                                |
                            ┌───────────────────┴────────────────────┐
                            |    1. Save user query as Chat(QUESTION) |
                            |    2. Stream response from Gemini       |
                            |         via VectorStoreChatMemoryAdvisor|
                            |         (with last 5 exchanges context)  |
                            |    3. On complete: save answer &         |
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

## 8. Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Application Startup                         │
│                                                                   │
│  AppKnowledgeLoader                                               │
│  ┌─────────────────────────────────────────────┐                  │
│  │ 1. TRUNCATE knowledge_vector_store          │                  │
│  │ 2. Read all *.md from classpath:knowledge/  │                  │
│  │ 3. Split into chunks (TokenTextSplitter)    │                  │
│  │ 4. Embed + store in knowledge_vector_store  │                  │
│  └─────────────────────────────────────────────┘                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Chat Query Flow                              │
│                                                                   │
│  User Query                                                       │
│       │                                                           │
│       ▼                                                           │
│  Gemini Model (gemini-3.1-flash-lite)                             │
│       │                                                           │
│       ├── VectorStoreChatMemoryAdvisor                            │
│       │   ├── Retrieve past exchanges from chat_memory_vector_store│
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