# Architecture Overview

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
│   ├── LlmConfig.java              # ChatClient beans for Gemini
│   ├── VectorStoreConfig.java      # pgvector configurations
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
│   ├── ChatService.java / Impl     # LLM queries & chat retrieval
│   ├── ConversationService.java / Impl # Conversation management
│   ├── ChatPersistanceService.java # Async chat persistence
│   ├── EventService.java / Impl    # Event CRUD & recurrence
│   ├── NoteService.java / Impl     # Note CRUD with soft-delete
│   ├── UserService.java / Impl     # User info retrieval
│   ├── JwtService.java / Impl      # JWT token generation & validation
│   ├── RefreshTokenService.java / Impl # Refresh token lifecycle
│   └── MailService.java / Impl     # Email sending via Mailtrap
├── Repository/                     # Spring Data JPA repositories
├── Entity/                         # JPA entities
├── Dto/                            # Data transfer objects / records
├── Enum/                           # Enumerations
└── Startup/                        # Startup logic
    └── AppKnowledgeLoader.java     # Loads markdown docs into vector store
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

## API Base URL Structure

| Prefix       | Purpose                     |
|-------------|------------------------------|
| `/auth/**`  | Authentication endpoints     |
| `/ai/**`    | AI chat & conversations      |
| `/notes/**` | Notes CRUD                   |
| `/events/**`| Calendar events              |
| `/user/**`  | User profile                 |

## Public Endpoints (No Auth Required)

| Endpoint            | Method | Description          |
|---------------------|--------|----------------------|
| `/`                 | GET    | Home/welcome page    |
| `/login`            | POST   | Login (from Spring)  |
| `/auth/**`          | ALL    | All auth endpoints   |