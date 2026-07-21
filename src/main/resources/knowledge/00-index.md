# Memoize Application Documentation

> AI-powered personal assistant backend — Notes, Events, and AI Chat

Memoize is a **modern productivity application** designed to be your "second brain." It combines three core tools into one seamless experience:

- **📝 Notes** — A full-featured rich text editor for capturing ideas, code snippets, meeting notes, and more.
- **📅 Events** — A calendar system for managing events, tasks, birthdays, meetings, and other time-based items.
- **🤖 MemoAI** — An AI assistant you can have natural conversations with, powered by a large language model.

## Table of Contents

| #  | Document                                                               | Description                                      |
|----|------------------------------------------------------------------------|--------------------------------------------------|
| 01 | [User Manual](01-user-manual.md)                                       | How to use the application |
| 02 | [Frontend Architecture Overview](02-frontend-architecture-overview.md) | Routing, Context, States, API management, Service layer, Hooks, Components etc.
| 03 | [Backend Architecture Overview](03-backend-architecture-overview.md)   | Tech stack, project structure, design patterns   |
| 04 | [Authentication & Authorization](04-authentication.md)                 | Email signup/login, OAuth2 (Google), JWT, refresh tokens |
| 05 | [AI Chat & Conversations](05-ai-chat.md)                               | Streaming LLM chat, conversation management, memory |
| 06 | [Notes Management](06-notes-management.md)                             | CRUD, soft-delete, restore, permanent delete      |
| 07 | [Events Management](07-events-management.md)                           | Calendar events, recurring events, date grouping |
| 08 | [User Management](08-user-management.md)                               | User entity, roles, auth sources                  |
| 09 | [Email Service](09-email-service.md)                                   | Mailtrap integration, verification codes          |
| 10 | [AI / LLM Integration](10-ai-llm-integration.md)                       | Google Gemini, pgvector, embeddings, knowledge base |