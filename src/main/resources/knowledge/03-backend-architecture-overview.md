# Backend Architecture Overview

## Memoize V3 API

A Spring Boot REST API backend for the Memoize application — an AI-powered personal assistant for notes, events, and chat.

## Technology Stack

| Category       | Technology                                              |
|----------------|---------------------------------------------------------|
| Framework      | Spring Boot 3.x                                         |
| Language       | Java 21+                                                |
| Database       | PostgreSQL (with pgvector extension)                     |
| AI / LLM      | Spring AI (Google Gemini models)                        |
| Vector Store  | pgvector (PostgreSQL)                                   |
| Auth (JWT)    | jjwt (io.jsonwebtoken)                                  |
| Build Tool    | Maven                                                   |
| Async         | Spring @Async with custom ThreadPoolTaskExecutor        |
| ORM           | Spring Data JPA / Hibernate                             |
| Mail Service  | Mailtrap API (for dev/staging)                          |

## Application Overview

**Memoize** is a Spring Boot REST API that combines traditional CRUD operations (notes, events) with modern AI-powered conversational capabilities. Key highlights:

- **Dual Authentication**: Email/password + Google OAuth2
- **JWT + Refresh Token**: Secure, stateless authentication with token rotation
- **AI Chat**: Streaming responses via Google Gemini with vector-store memory and RAG (Retrieval-Augmented Generation) using documentation knowledge base
- **Notes**: Full CRUD with soft-delete and restore
- **Events**: Calendar events with yearly/monthly/weekly recurrence expansion
- **Vector Store**: Dual pgvector stores — one for chat memory, one for app knowledge base
- **RAG Knowledge Base**: App documentation loaded into vector store with incremental updates; used by chat to answer app-related questions
- **Async Processing**: Dedicated thread pool for LLM operations

## Configuration

### Required Environment Variables

| Variable           | Description                                 |
|--------------------|---------------------------------------------|
| `JWT_SECRET`       | Secret key for JWT signing (HMAC-SHA256)    |
| `MAIL_API_KEY`     | Mailtrap API key for sending emails         |
| `CLIENT_URL`       | Frontend client URL (for CORS & redirects)  |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL                    |
| `SPRING_DATASOURCE_USERNAME` | DB username                       |
| `SPRING_DATASOURCE_PASSWORD` | DB password                       |
| `SPRING_AI_GOOGLE_GENAI_API_KEY` | Google Gemini API key          |

### Database Requirements

- PostgreSQL with **pgvector** extension installed
- Tables are managed by JPA/Hibernate (ddl-auto)

### Key Application Properties

```yaml
spring:
  app:
    client-url: ${CLIENT_URL}
  jpa:
    hibernate:
      ddl-auto: update
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}

jwt:
  secret: ${JWT_SECRET}

mail:
  api:
    key: ${MAIL_API_KEY}
```

## Database Tables

| Table                    | Purpose                                    |
|--------------------------|--------------------------------------------|
| users                    | User accounts and authentication           |
| notes                    | Notes with soft-delete support             |
| events                   | Calendar events with recurrence            |
| conversations            | AI chat conversations                      |
| chats                    | Individual chat messages (Q&A pairs)       |
| refresh_tokens           | JWT refresh tokens                         |
| verification_tokens      | Email verification and password reset codes |
| chat_memory_vector_store | pgvector table for chat memory embeddings  |
| knowledge_vector_store   | pgvector table for app knowledge base      |

## Building & Running

```bash
# Build
./mvnw clean package

# Run with Docker
docker-compose up --build

# Run locally
./mvnw spring-boot:run
```

## Application Structure

```
com.memoize.api
├── MemoizeV3ApiApplication.java    # Main entry point
├── Home.java                       # Home controller
├── Config/                         # App configuration
│   ├── Common.java                 # Shared constants & utilities
│   ├── AsyncConfig.java            # Thread pool for LLM tasks
│   ├── CorsConfig.java             # CORS configuration
│   ├── ErrorHandler.java           # Global exception handler
│   ├── LlmConfig.java              # 5 ChatClient beans for Gemini (memory, RAG, simple)
│   ├── VectorStoreConfig.java      # 2 pgvector stores (chat memory & knowledge base)
│   └── Security/                   # Security layer
│       ├── SecurityConfig.java     # Spring Security filter chain
│       ├── JwtFilter.java          # JWT authentication filter
│       ├── AuthPrincipal.java      # Authenticated user info record
│       ├── UserDetailsServiceImpl.java # Loads users for auth
│       └── OAuth2/                 # OAuth2 login support
│           ├── OAuth2Config.java
│           ├── OAuth2UserInfo.java
│           └── OAuth2UserInfoGoogle.java
├── Controller/                     # REST controllers (Interface + Impl)
│   ├── AuthController.java / Impl  # Authentication endpoints
│   ├── ChatController.java / Impl  # AI chat & conversations
│   ├── EventController.java / Impl # Calendar events
│   ├── NoteController.java / Impl  # Notes CRUD
│   └── UserController.java / Impl  # User profile
├── Service/                        # Business logic layer
│   ├── AuthService.java / Impl     # Signup, login, email verification, password reset
│   ├── ChatService.java / Impl     # LLM queries & chat retrieval (with RAG routing)
│   ├── ConversationService.java / Impl # Conversation management
│   ├── ChatPersistanceService.java # Async chat persistence
│   ├── EventService.java / Impl    # Event CRUD & recurrence
│   ├── NoteService.java / Impl     # Note CRUD with soft-delete
│   ├── UserService.java / Impl     # User info retrieval
│   ├── JwtService.java / Impl      # JWT token generation & validation
│   ├── RefreshTokenService.java / Impl # Refresh token lifecycle
│   ├── MailService.java / Impl     # Email sending via Mailtrap
│   ├── RagService.java / Impl      # Knowledge base search & RAG query detection
├── Repository/                     # Spring Data JPA repositories
├── Entity/                         # JPA entities
├── Dto/                            # Data transfer objects / records
├── Enum/                           # Enumerations (Role, AuthSource, ChatType, EventRepeat, EventType, ModifyAction, VerificationType)
└── Startup/                        # Startup logic
    └── AppKnowledgeLoader.java     # Incrementally loads markdown docs into vector store
```

## Key Design Patterns

1. **Interface + Impl Pattern**: Controllers and Services define interfaces with their implementation separately.
2. **Record DTOs**: Immutable data transfer objects using Java records.
3. **Global Error Handler**: `@ControllerAdvice` maps exceptions to HTTP status codes.
4. **JWT Stateless Auth**: No sessions; every request carries a Bearer token.
5. **OAuth2 + Email Auth**: Dual authentication sources.
6. **Async LLM Processing**: Uses a dedicated thread pool for blocking LLM calls.
7. **Soft Delete for Notes**: Notes are marked as deleted (not removed immediately).
8. **Soft Delete for Conversations**: Conversations are deletable; vector store memory is also cleaned.
9. **RAG-based Chat Routing**: `RagService` determines if a query needs knowledge base context; the system selects the appropriate `ChatClient` (with or without RAG) dynamically.
10. **Incremental Knowledge Base Loading**: `AppKnowledgeLoader` uses SHA-256 hashing to detect changed files and only re-indexes modified/new content.

## API Base URL Structure

| Prefix       | Purpose                    |
|-------------|----------------------------|
| `/auth/**`  | Authentication endpoints   |
| `/ai/**`    | AI chat & conversations    |
| `/notes/**` | Notes management           |
| `/events/**`| Calendar events management |
| `/user/**`  | User profile               |

## Public Endpoints (No Auth Required)

| Endpoint            | Method | Description        |
|---------------------|--------|--------------------|
| `/`                 | GET    | Home/welcome page  |
| `/login`            | POST   | Login (for OAuth)  |
| `/auth/**`          | ALL    | All auth endpoints |

## Vector Stores

| Store                   | Table                    | Purpose                                  |
|-------------------------|--------------------------|------------------------------------------|
| `chat-memory-vector-store` | chat_memory_vector_store | Stores conversation embeddings for chat memory |
| `knowledge-vector-store`   | knowledge_vector_store   | Stores app documentation for RAG         |

Both stores use:
- **Dimensions**: 768
- **Distance Type**: COSINE_DISTANCE
- **Index Type**: HNSW
- **Max Document Batch Size**: 10000