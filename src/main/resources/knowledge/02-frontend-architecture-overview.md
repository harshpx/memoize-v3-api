# Memoize v3 — Functional Documentation

## 1. Overview

Memoize is a modern, cross-platform "second brain" application that helps users capture notes, manage events/tasks, and interact with an AI assistant. It is built as a **React + TypeScript** frontend (Vite-based) with **Capacitor** wrappers for native mobile deployments (iOS & Android). The backend API is consumed via RESTful endpoints.

### Core Capabilities

1. **Notes** (Rich Text Editor) — Create, edit, save, soft-delete, restore, and permanently delete notes.
2. **Events / Calendar** — Create, edit, and delete events; weekly/monthly/yearly repeat support; multiple event types (Event, Meeting, Task, Birthday, Other).
3. **MemoAI** (LLM Chat) — Conversational AI with streaming responses, conversation history management.
4. **Trash** — Review and manage soft-deleted notes (restore or permanent delete).
5. **Theming** — Dark/Light mode toggle with 5 accent color options.
6. **Authentication** — Email/Password login & signup, OAuth2 (Google), JWT token refresh, session persistence.

---

## 2. Architecture

### 2.1 High-Level Structure

```
memoize-v3-client/
├─ public/            # Static assets, env template
├─ src/
│  ├── App.tsx        # Root Router & Providers
│  ├── main.tsx       # React DOM entry
│  ├── index.css      # Global styles, Tailwind, CSS variables, theme configuration
│  │
│  ├── context/
│  │   └── store.ts   # Zustand global store (auth, theme, data state)
│  │
│  ├── lib/
│  │   ├── commonTypes.ts   # Shared TypeScript interfaces (Note, Event, Chat, Auth, etc.)
│  │   ├── utils.ts         # Utility functions (cn, date helpers, etc.)
│  │   ├── validations.ts   # Zod schemas for form validation
│  │   ├── errors.ts        # Custom error classes (AuthError, AcceptableError)
│  │   └── dummyData.tsx    # Dummy/placeholder data for development
│  │
│  ├── services/
│  │   ├── apis.ts          # Raw API fetch functions (login, notes CRUD, events, AI chat)
│  │   └── services.ts      # Higher-level service orchestration with auto-refresh, store updates, toasts
│  │
│  ├── hooks/               # Custom React hooks
│  │   ├── useDebounce.ts
│  │   ├── useMediaQuery.ts
│  │   ├── useOutsideClick.ts
│  │   ├── useTiptapEditor.ts
│  │   ├── useMenuNavigation.ts
│  │   ├── useNativeBackButton.ts
│  │   ├── useNativeNetworkMonitor.ts
│  │   └── useComposedRef.ts
│  │
│  ├── pages/               # Route-level page components
│  │   ├── LandingPage.tsx
│  │   ├── Auth.tsx
│  │   ├── ResetPassword.tsx
│  │   ├── OAuth2Redirect.tsx
│  │   ├── HomeLayout.tsx       # App shell with sidebar/dock navigation
│  │   ├── HomePage.tsx         # Dashboard with greeting, quick actions, recent items
│  │   ├── NotePage.tsx         # Grid view of all active notes
│  │   ├── NoteEdit.tsx         # Rich text editor with TipTap
│  │   ├── EventLayout.tsx      # Event page + modal editor
│  │   ├── EventPage.tsx        # Calendar view of events
│  │   ├── EventEdit.tsx        # Event creation/editing form
│  │   ├── MemoAi.tsx           # AI chat interface
│  │   ├── Trash.tsx            # Deleted notes view
│  │   ├── NativeBackHandler.tsx
│  │   ├── NativeNetworkMonitor.tsx
│  │   └── NotFound.tsx
│  │
│  ├── components/
│  │   ├── custom/             # Custom reusable components
│  │   │   ├── LoginForm.tsx
│  │   │   ├── SignupForm.tsx
│  │   │   ├── NoteListItem.tsx
│  │   │   ├── ChatMessage.tsx
│  │   │   ├── ConversationDrawer.tsx
│  │   │   ├── ThemeSwitch.tsx
│  │   │   ├── BgIcons.tsx
│  │   │   ├── Logo.tsx
│  │   │   ├── Loader.tsx
│  │   │   ├── LoadingSkeletons.tsx
│  │   │   ├── CustomizableButton.tsx
│  │   │   └── calendar/
│  │   │       ├── Calendar.tsx
│  │   │       ├── MonthGrid.tsx
│  │   │       ├── MonthList.tsx
│  │   │       ├── EventCard.tsx
│  │   │       └── DateTimePicker.tsx
│  │   │
│  │   ├── wrapper/            # Higher-order layout/wrapper components
│  │   │   ├── AuthInit.tsx        # Initialize auth on app load
│  │   │   ├── ThemeInit.tsx       # Apply persisted theme
│  │   │   ├── ProtectedRoute.tsx  # Redirect unauthenticated users
│  │   │   └── PublicRoute.tsx     # Redirect authenticated users away from public pages
│  │   │
│  │   └── tiptap-ui/          # TipTap editor toolbar components
│  │   └── tiptap-ui-primitive/ # Lower-level TipTap UI primitives
│  │   └── tiptap-node/        # Custom TipTap node extensions
│  │   └── ui/                 # shadcn/ui primitives (Button, Input, Card, etc.)
│  │
│  └── styles/
│      ├── _variables.scss
│      └── _keyframe-animations.scss
│
├── capacitor.config.ts        # Capacitor mobile configuration
├── vite.config.ts             # Vite bundler config
├── docker-entrypoint.sh       # Docker deployment entrypoint
├── Dockerfile                 # Container image definition
├── nginx.conf                 # Nginx config for production serving
└── vercel.json                # Vercel deployment config
```

### 2.2 Data Flow

```
User Action → React Component → Service Layer (services.ts) → API Layer (apis.ts) → Backend API
                                  ↕                        ↕
                               Zustand Store            Auto-refresh JWT
                                  ↕                        ↕
                              UI Re-render             Error → Toast Notification
```

### 2.3 State Management (Zustand Store)

The global store (`src/context/store.ts`) is a single Zustand store organized into logical groups:

**Auth State**
| Field | Type | Description |
|---|---|---|
| `accessToken` | `string \| null` | JWT access token |
| `user` | `User \| null` | Authenticated user data |
| `init` | `boolean` | Whether auth initialization is complete |
| `setAuth(token, user)` | function | Update token & user |
| `setInit(init)` | function | Mark initialization done |
| `logout()` | function | Clear all state |

**Theme State**
| Field | Type | Description |
|---|---|---|
| `theme` | `"light" \| "dark"` | Current theme |
| `setTheme(theme)` | function | Update theme, persist to localStorage, apply CSS class |
| `accent` | `Accent` | Current accent color (default, teal, yellow, purple, pink) |
| `setAccent(accent)` | function | Update accent, persist to localStorage, set data attribute |

**Data State**
| Field | Type | Description |
|---|---|---|
| `notes` | `Record<"active" \| "deleted", PaginatedData<Note>>` | Paginated notes for active & deleted entities |
| `notesLoading` | `boolean` | Notes loading flag |
| `events` | `Event[]` | All user events |
| `eventsFetched` | `boolean` | Whether events have been fetched |
| `eventsLoading` | `boolean` | Events loading flag |
| `conversations` | `Conversation[]` | AI chat conversations |
| `selectedConversation` | `string` | Currently selected conversation ID |
| `conversationsLoading` | `boolean` | Conversations loading flag |
| `chats` | `Record<string, Chat[]>` | Map of conversationId → chat messages |
| `chatsLoading` | `boolean` | Chats loading flag |
| `chatStreaming` | `boolean` | Whether AI is currently streaming a response |

---

## 3. Routing Structure

| Route | Access | Component(s) | Description |
|---|---|---|---|
| `/` | Public | `LandingPage` | Welcome/landing page |
| `/auth` | Public | `Auth` → `LoginForm` / `SignupForm` | Authentication page |
| `/oauth2redirect` | Public | `OAuth2Redirect` | OAuth2 callback handler |
| `/reset-password` | Public | `ResetPassword` | Password reset flow (UI scaffold) |
| `/home` | Protected | `HomeLayout` → `HomePage` | Dashboard with greeting, recents, actions |
| `/home/notes` | Protected | `HomeLayout` → `NotePage` | All active notes in masonry grid |
| `/home/notes/editor` | Protected | `HomeLayout` → `NoteEdit` | Rich text note editor |
| `/home/events` | Protected | `HomeLayout` → `EventLayout` → `EventPage` | Calendar view |
| `/home/events/editor` | Protected | `EventLayout` → `EventPage` (overlaid) + `EventEdit` | Event editor modal |
| `/home/ai` | Protected | `HomeLayout` → `MemoAi` | AI chat interface |
| `/home/trash` | Protected | `HomeLayout` → `Trash` | Deleted notes |
| `*` | Public | `NotFound` | 404 page |

### Route Guards

- **PublicRoute**: If user is authenticated and tries to visit `/`, `/auth`, or `/oauth2redirect`, they are redirected to `/home`.
- **ProtectedRoute**: If user is not authenticated, redirects to `/`.
- **AuthInit**: Shows a loader until the app has attempted to restore a session from the persisted refresh token.

---

## 4. Feature Breakdown

### 4.1 Authentication

**Login**
- Form fields: Identifier (username or email) + Password
- Validation: Zod schema (`loginSchema`)
- Flow: `LoginForm.onSubmit` → `loginAndFetchUserInfo()` → `login()` API → `getUserInfo()` → store token & user
- Error handling: Toast notifications, inline error messages
- Additional: "Forgot password?" link to `/reset-password`

**Signup**
- Form fields: Name, Username, Email, Password, Confirm Password
- Real-time availability checks: Debounced (1s) API calls to check username & email availability
- OTP Verification: Send OTP button → `sendVerificationCode()` → OTP code input → signup with `verificationCode`
- Flow: `SignupForm.onSubmit` → `signupAndFetchUserInfo()` → `signup()` API → `getUserInfo()` → store

**OAuth2 (Google)**
- Button redirects to `${BASE_URL}/oauth2/authorization/google`
- On callback, `OAuth2Redirect` calls `initialAuthRefresh()` to establish session

**Session Persistence**
- `AuthInit` wrapper calls `initialAuthRefresh()` on mount
- `initialAuthRefresh` uses `retryWithRefresh(getUserInfo, [])` which first attempts to call refresh token API
- If refresh succeeds, token & user are restored → user stays logged in

**Token Refresh Mechanism**
- `retryWithRefresh()` wraps every authenticated API call
- On `AuthError` (401), it attempts to refresh the token
- On second auth failure, logs the user out

### 4.2 Notes

**Data Model**
```typescript
interface Note {
  id: string;
  content: string;      // TipTap JSON string
  preview: string;      // HTML preview for rendering in list
  createdAt: string;    // ISO 8601
  updatedAt: string;    // ISO 8601
  isArchived: boolean;
  isDeleted: boolean;
  deletedAt?: string;
}
```

**Operations**
| Operation | API Endpoint | Service Handler | Description |
|---|---|---|---|
| Fetch notes | `GET /notes?deleted=&page=&size=` | `notesFetchHandler(entityState)` | Paginated fetch (50 per page) |
| Create note | `POST /notes` | `noteCreateHandler()` | Sends TipTap content + preview HTML |
| Update note | `PUT /notes/:id` | `noteUpdateHandler()` | Auto-saves every 15s, on unmount, on manual save |
| Soft delete | `DELETE /notes/:id` | `noteSoftDeleteHandler()` | Moves note to "deleted" entity state |
| Restore | `PUT /notes/:id/restore` | `noteRestoreHandler()` | Moves note back to "active" entity state |
| Permanent delete | `DELETE /notes/:id/permanent` | `notePermanentDeleteHandler()` | Removes from both active & deleted lists |

**Rich Text Editor (TipTap)**
- Extensions: StarterKit, Placeholder, HorizontalRule, TextAlign, TaskList/TaskItem, Highlight, Typography, Superscript, Subscript, Selection
- Toolbar: Undo/Redo, Heading (1-4), Lists (bullet/ordered/task), Blockquote, Code Block, Bold, Italic, Underline, Strike, Superscript, Subscript, Text Align (left/center/right/justify)
- Auto-save: Triggered every 15 seconds
- Save-on-unmount: Handled via useEffect cleanup
- Edit lock: Deleted notes display a warning and cannot be edited until restored

### 4.3 Events

**Data Model**
```typescript
interface Event {
  id: string;
  title: string;
  start: string;          // ISO 8601
  end: string;            // ISO 8601
  eventType: "EVENT" | "TASK" | "BIRTHDAY" | "MEETING" | "OTHER";
  eventRepeat: "NONE" | "WEEKLY" | "MONTHLY" | "YEARLY";
  description?: string;
  location?: string;
  createdAt: string;
  updatedAt: string;
}
```

**Operations**
| Operation | API Endpoint | Service Handler |
|---|---|---|
| Fetch all events | `GET /events/all` | `eventsFetchHandler()` |
| Create event | `POST /events` | `eventCreateHandler()` |
| Update event | `PUT /events/:id` | `eventUpdateHandler()` |
| Delete event | `DELETE /events/:id` | `eventDeleteHandler()` |

**Calendar View**
- Month-by-month calendar grid (`Calendar.tsx`)
- Support for repeating events (Weekly, Monthly, Yearly) — expansion logic in `utils.ts:populateEventsInRange`
- Event cards display title, time, type icon
- Click event → opens editor modal

**Event Editor**
- Fields: Title, Description, Start/End DateTime, Full-day toggle, Repeat selector, Event Type selector
- Validation: Title required, End must be after Start
- Create/Update/Delete support

### 4.4 MemoAI (LLM Chat)

**Data Models**
```typescript
interface Chat {
  id: string;
  content: string;
  type: "QUESTION" | "ANSWER";
  createdAt: string;
}

interface Conversation {
  id: string;
  name: string;
  summary: string;
  isProperName: boolean;  // Whether AI has generated a proper title
  isNew: boolean;
  createdAt: string;
  updatedAt: string;
}
```

**Operations**
| Operation | API Endpoint | Service Handler |
|---|---|---|
| List conversations | `POST /ai/conversation/all` | `conversationsFetchHandler()` |
| Get conversation | `GET /ai/conversation/:id` | `conversationInfoFetchHandler()` |
| Create conversation | `POST /ai/conversation` | `conversationCreateHandler()` |
| Delete conversation | `DELETE /ai/conversation/:id` | `conversationDeleteHandler()` |
| List chats | `GET /ai/chat/:conversationId` | `chatsFetchHandler()` |
| Ask LLM | `POST /ai/chat/:conversationId?query=...` | `llmQueryHandler()` |

**Streaming**
- The `askLLM` function uses the Fetch API with `response.body.getReader()` for streaming
- Server-Sent Events (SSE) format: lines starting with `data:`
- Streamed chunks are accumulated in real-time in the Zustand store
- A timer shows elapsed response time (e.g., "3s ...")

**Conversation Naming**
- After the first AI response, the app polls the backend until `isProperName` is `true`
- The backend generates a conversation name/title based on the conversation content

### 4.5 Trash

- Shows all soft-deleted notes in a masonry grid
- Notes in trash can be restored or permanently deleted via the NoteEdit page
- Empty state: Trash can icon with "Trash empty" message

### 4.6 Dashboard (HomePage)

- **Greeting**: Time-of-day greeting with user's first name
- **Quick Actions**: "Ask MemoAI", "Add Note", "Add Event" buttons
- **Recent MemoAI Chats**: Collapsible section showing recent conversations
- **Recent Notes**: Collapsible section showing last 4 notes (masonry layout)
- **Upcoming Events**: Collapsible section showing events for the next 7 days
- Loading states: Skeleton loaders for each section
- Empty states: Encouraging CTAs when no data exists

### 4.7 Theming

**Theme (Light/Dark)**
- Toggled via `ThemeSwitch` component
- Persisted to `localStorage` key `"theme"`
- Applied as CSS class on `<html>` element

**Accent Colors**
- 5 options: Default (green), Teal, Yellow, Purple, Pink
- Persisted to `localStorage` key `"accent"`
- Applied as `data-accent` attribute on `<html>` element
- CSS variables `--accent-light` and `--accent-dark` drive accent-colored UI elements

**ThemeSwitch Component**
- Popover with theme toggle (Switch) and accent color picker (5 colored circles)
- Accessible from landing page, home sidebar, note editor, AI chat

### 4.8 Navigation Layout (HomeLayout)

**Desktop (≥768px)**
- Resizable sidebar panel (180–240px, collapsible to 80px)
- Main content panel
- Sidebar contains: Logo, Add Note/Event buttons, navigation items (Home, MemoAI, Notes, Events, Trash), User avatar with logout popover

**Mobile (<768px)**
- Bottom dock with navigation icons
- Main content area above
- Note editor and Event editor render full-screen (no dock shown during editing)

---

## 5. API Layer Architecture

**Base URL Resolution**
```typescript
// Determined at runtime:
- Native platform (Capacitor) or PROD env → "https://api.memoize.in"
- VITE_ENV=PROD → "https://api-ts.memoize.in"
- Default (dev) → "http://localhost:8086"
```

**Common API Response Envelope**
```typescript
type ApiResponse<T> = 
  | { success: true; data: T; timestamp: string }
  | { success: false; data: string; timestamp: string };  // error message
```

**Error Handling**
- `AuthError` — custom error for 401 responses → triggers token refresh
- `AcceptableError` — custom error for expected failures (e.g., 409 conflict)
- All service handlers catch errors and show toast notifications via `sonner`

---

## 6. Technology Stack

| Layer | Technology |
|---|---|
| **UI Framework** | React 19 |
| **Language** | TypeScript 5.9 |
| **Bundler** | Vite 7 |
| **Styling** | Tailwind CSS v4, SCSS, `tw-animate-css` |
| **UI Components** | shadcn/ui (Radix UI primitives), Custom components |
| **State Management** | Zustand 5 |
| **Routing** | React Router v7 |
| **Form Handling** | React Hook Form + Zod validation |
| **Rich Text Editor** | TipTap 3 (ProseMirror-based) |
| **Date Handling** | Dayjs |
| **Animations** | Motion (Framer Motion) |
| **Icons** | React Icons (Lu) |
| **Notifications** | Sonner (toast) |
| **Mobile** | Capacitor 8 (iOS & Android) |
| **Linting** | ESLint |
| **Formatting** | Prettier |
| **Deployment** | Docker, Vercel |

---

## 7. Mobile-Specific Features

- **Native Back Button**: `NativeBackHandler` intercepts Android hardware back button
- **Network Monitoring**: `NativeNetworkMonitor` uses Capacitor Network plugin to detect connectivity changes
- **OAuth2**: Google OAuth button is hidden on native platforms (uses native SSO instead)
- **Deep Linking**: Capacitor handles deep links for OAuth redirects

---

## 8. Security & Error Resilience

- **JWT Token**: Access token stored in Zustand (memory), refresh token in HTTP-only cookie
- **Credentials**: All API calls include `credentials: "include"` for cookie-based refresh
- **Token Auto-Refresh**: `retryWithRefresh` pattern ensures seamless re-authentication
- **Input Validation**: Zod schemas validate all form inputs before submission
- **API Error Handling**: Structured error responses, typed error classes
- **Safe Content Parsing**: `safeParseForEditor` gracefully handles malformed note content JSON