# Memoize Application Documentation

> AI-powered personal assistant backend — Notes, Events, and AI Chat

## Table of Contents

| #  | Document                                   | Description                                      |
|----|--------------------------------------------|--------------------------------------------------|
| 01 | [Architecture Overview](01-architecture-overview.md) | Tech stack, project structure, design patterns   |
| 02 | [Authentication & Authorization](02-authentication.md) | Email signup/login, OAuth2 (Google), JWT, refresh tokens |
| 03 | [AI Chat & Conversations](03-ai-chat.md)   | Streaming LLM chat, conversation management, memory |
| 04 | [Notes Management](04-notes-management.md) | CRUD, soft-delete, restore, permanent delete      |
| 05 | [Events Management](05-events-management.md) | Calendar events, recurring events, date grouping |
| 06 | [User Management](06-user-management.md)   | User entity, roles, auth sources                  |
| 07 | [Email Service](07-email-service.md)       | Mailtrap integration, verification codes          |
| 08 | [AI / LLM Integration](08-ai-llm-integration.md) | Google Gemini, pgvector, embeddings, knowledge base |

## Application Overview

**Memoize** is a Spring Boot REST API that combines traditional CRUD operations (notes, events) with modern AI-powered conversational capabilities. Key highlights:

- **Dual Authentication**: Email/password + Google OAuth2
- **JWT + Refresh Token**: Secure, stateless authentication with token rotation
- **AI Chat**: Streaming responses via Google Gemini with vector-store memory
- **Notes**: Full CRUD with soft-delete and restore
- **Events**: Calendar events with yearly/monthly/weekly recurrence expansion
- **Vector Store**: pgvector for chat memory and knowledge base (RAG-ready)
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