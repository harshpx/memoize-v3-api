# Memoize v3 — User Manual (Process Documentation)

> **Your Second Brain — Capture Notes, Manage Events, Chat with AI.**

---

## Table of Contents

1. [What is Memoize?](#1-what-is-memoize)
2. [Getting Started (Onboarding)](#2-getting-started-onboarding)
    - [2.1 Accessing Memoize](#21-accessing-memoize)
    - [2.2 Creating an Account](#22-creating-an-account)
    - [2.3 Logging In](#23-logging-in)
    - [2.4 OAuth2 / Google Sign-In](#24-oauth2--google-sign-in)
    - [2.5 Understanding the Interface](#25-understanding-the-interface)
3. [Managing Notes](#3-managing-notes)
    - [3.1 Creating a Note](#31-creating-a-note)
    - [3.2 The Rich Text Editor](#32-the-rich-text-editor)
    - [3.3 Editing a Note](#33-editing-a-note)
    - [3.4 Auto-Save & Manual Save](#34-auto-save--manual-save)
    - [3.5 Deleting a Note (Soft Delete)](#35-deleting-a-note-soft-delete)
    - [3.6 Restoring a Note from Trash](#36-restoring-a-note-from-trash)
    - [3.7 Permanently Deleting a Note](#37-permanently-deleting-a-note)
4. [Managing Events](#4-managing-events)
    - [4.1 Creating an Event](#41-creating-an-event)
    - [4.2 Editing an Event](#42-editing-an-event)
    - [4.3 Deleting an Event](#43-deleting-an-event)
    - [4.4 Understanding Event Types & Repeating Events](#44-understanding-event-types--repeating-events)
    - [4.5 Using the Calendar View](#45-using-the-calendar-view)
5. [Using MemoAI](#5-using-memoai)
    - [5.1 Starting a Conversation](#51-starting-a-conversation)
    - [5.2 Asking Questions](#52-asking-questions)
    - [5.3 Conversation Management](#53-conversation-management)
    - [5.4 Streaming Responses & Timing](#54-streaming-responses--timing)
6. [Using the Trash](#6-using-the-trash)
7. [Customizing the Look & Feel](#7-customizing-the-look--feel)
    - [7.1 Switching Between Light & Dark Mode](#71-switching-between-light--dark-mode)
    - [7.2 Changing the Accent Color](#72-changing-the-accent-color)
8. [The Dashboard (Home Screen)](#8-the-dashboard-home-screen)
9. [Available Platforms & System Requirements](#9-available-platforms--system-requirements)
10. [Tips & Best Practices](#10-tips--best-practices)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. What is Memoize?

Memoize is a **modern productivity application** designed to be your "second brain." It combines three core tools into one seamless experience:

- **📝 Notes** — A full-featured rich text editor for capturing ideas, code snippets, meeting notes, and more.
- **📅 Events** — A calendar system for managing events, tasks, birthdays, meetings, and other time-based items.
- **🤖 MemoAI** — An AI assistant you can have natural conversations with, powered by a large language model.

The application works across **Web (Desktop & Mobile Browser)** and **Native Mobile (iOS & Android via Capacitor)** platforms, with your data synced in real-time through a cloud backend.

---

## 2. Getting Started (Onboarding)

### 2.1 Accessing Memoize

| Platform | How to Access |
|---|---|
| **Web (Production)** | Visit [memoize.in](https://memoize.in) (or the deployed URL) |
| **Web (Development)** | Run `npm run dev` locally → `http://localhost:5173` |
| **iOS** | Download from App Store (if published) |
| **Android** | Download from Play Store (if published) |

### 2.2 Creating an Account

**Step-by-step signup process:**

1. Go to the **Landing Page**. Click the **"Get Started"** button.
2. On the **Auth Page**, switch to the **"Signup"** tab.
3. Fill in the following fields:
    - **Name** — Your full name (min. 3 characters).
    - **Username** — A unique identifier (3–10 characters, letters/numbers/underscores/dots only).  
      → The system checks availability in real-time after you stop typing for 1 second.
    - **Email** — A valid email address.  
      → The system also checks email availability in real-time.
    - **Password** — Min. 3, max. 20 characters.
    - **Confirm Password** — Must match the password.
4. Click **"Send OTP"** to receive a 6-digit verification code at your email.
5. Enter the OTP code in the input field.
6. Click **"Signup"** to complete registration.
7. On success, you will be automatically logged in and redirected to the **Dashboard**.

> **Note:** If username or email is already taken, you'll see a red error message and won't be able to proceed until you provide unique values.

### 2.3 Logging In

1. On the **Auth Page**, stay on the **"Login"** tab.
2. Enter your **Username or Email** in the first field.
3. Enter your **Password**.
4. Click **"Login"**.
5. On success, you'll see a welcome toast and be redirected to the **Dashboard**.

> **Forgot your password?** Click the "Forgot password?" link below the password field to navigate to the password reset page.

### 2.4 OAuth2 / Google Sign-In

1. On the **Auth Page** (Login or Signup), scroll to the bottom of the form card.
2. Click **"Continue with Google"**.
3. You'll be redirected to Google's OAuth consent screen.
4. After authorizing, you'll be redirected back to Memoize and automatically logged in.

> **Platform note:** The Google sign-in button is only available on web browsers. On native mobile apps (iOS/Android), OAuth2 is handled natively through Capacitor.

### 2.5 Understanding the Interface

After logging in, you land on the **Dashboard**. The interface adapts based on your screen size:

**Desktop Layout (≥768px):**
```
┌──────────────────────────────────────────────────────┐
│ [Sidebar Panel - 180-240px] │  [Main Content Area]  │
│                             │                        │
│  Logo                       │  Greeting              │
│  [Add Note] [Add Event]     │  Quick Actions         │
│                             │  Recent Chats          │
│  Home                       │  Recent Notes          │
│  MemoAI                     │  Upcoming Events        │
│  Notes                      │                        │
│  Events                     │                        │
│  Trash                      │                        │
│                             │                        │
│  [User Avatar - Logout]     │                        │
└──────────────────────────────────────────────────────┘
```
- The sidebar is **resizable** (drag the edge) and can be **collapsed** to icon-only mode using the arrow button.
- Each sidebar item highlights when you're on that page.

**Mobile Layout (<768px):**
```
┌────────────────────────────────────────┐
│                                        │
│           Main Content Area            │
│                                        │
│                                        │
│                                        │
├────────────────────────────────────────┤
│  Home  │  MemoAI  │  Notes  │  Events  │  Trash  │  👤  │  ← Bottom Dock
└────────────────────────────────────────┘
```
- Navigation is via a **bottom dock** with icons and labels.
- The Editor pages (Notes Editor, Event Editor) hide the dock for a full-screen experience.

---

## 3. Managing Notes

### 3.1 Creating a Note

There are **three ways** to create a new note:

1. **From the Dashboard**: Click the **"Add Note"** button in the Quick Actions section.
2. **From the Sidebar/Dock**: Click the **"Add Note"** button in the left sidebar (desktop) or navigate to Notes and use the action.
3. **From the Notes Page**: Click the **"+" (plus icon)** card at the top of the notes grid.

All three methods open the **Note Editor** with a blank canvas.

Then you can click on the save button in the bottom bar, or else it will be autosaved once you go back, or automatically after 15 seconds. 

### 3.2 The Rich Text Editor

The Note Editor uses **TipTap** — a powerful, ProseMirror-based rich text editor. The toolbar at the top provides:

| Tool Group | Options |
|---|---|
| **Undo/Redo** | Undo, Redo |
| **Heading** | Heading 1, 2, 3, 4 |
| **Lists** | Bullet List, Ordered List, Task List |
| **Blocks** | Blockquote, Code Block |
| **Text Formatting** | Bold, Italic, Underline, Strike |
| **Script** | Superscript, Subscript |
| **Text Alignment** | Left, Center, Right, Justify |

**Pro Tips:**
- **Task Lists**: Use the Task List option to create interactive checkboxes.
- **Code Blocks**: Perfect for documenting code snippets with preserved formatting.
- **Keyboard shortcuts** also work (Ctrl+B for bold, Ctrl+I for italic, etc.).

### 3.3 Editing a Note

1. **From the Dashboard/Notes Page**: Click on any note card in the masonry grid.
2. The note opens in the **Note Editor** with its existing content loaded.
3. Make your changes using the toolbar.
4. The editor automatically marks the note as "dirty" (unsaved changes) when you start typing.

### 3.4 Auto-Save & Manual Save

Memoize uses a **three-tier save system**:

| Save Trigger | Description |
|---|---|
| **Auto-Save (Every 15 seconds)** | The editor automatically saves your changes every 15 seconds while you're typing. |
| **Manual Save** | Click the **Save** button in the bottom bar to save immediately. |
| **Save on Unmount** | If you navigate away, the editor attempts to save your changes before closing. |

**Save Status Indicators (bottom bar):**
- *"Type something to save"* — When the note is brand new and empty.
- *"Save"* — When there are unsaved changes.
- *"Saving..."* — While the save operation is in progress.
- *"Saved"* — When all changes have been persisted.

### 3.5 Deleting a Note (Soft Delete)

1. Open the note you want to delete.
2. Click the **Delete** button (trash icon) in the bottom bar.
3. The note is **soft-deleted** — it moves to the **Trash** and can be restored later.
4. A success toast confirms the deletion.

> **What happens?** The note is removed from the active notes list and appears in the Trash. It is NOT permanently deleted at this stage.

### 3.6 Restoring a Note from Trash

1. Navigate to the **Trash** page from the sidebar/dock.
2. Click on the note card you want to restore.
3. In the editor, click the **Restore** button (restore icon) in the bottom bar.
4. The note is moved back to active notes.
5. You'll be redirected to the Trash page, and a success toast confirms the restore.

### 3.7 Permanently Deleting a Note

1. Navigate to **Trash** and open the note you want to permanently delete.
2. Click the **Delete** button again (now highlighted in red).
3. The note is **permanently erased** from both Trash and active notes.
4. A success toast confirms permanent deletion.

> ⚠️ **Warning:** Permanent deletion cannot be undone. The note and its content are gone forever.

---

## 4. Managing Events

### 4.1 Creating an Event

1. From the **Dashboard**: Click the **"Add Event"** button in the Quick Actions section.
2. From the **Sidebar/Dock**: Click the **"Add Event"** button.
3. From the **Events Page**: Click a date on the calendar or use the add button.

The **Event Editor** opens as a modal/overlay with the following fields:

| Field | Description | Required |
|---|---|---|
| **Title** | Name of the event | ✅ Yes |
| **Description** | Additional notes about the event | ❌ No |
| **Start** | Date and time the event begins | ✅ Yes (auto-set) |
| **End** | Date and time the event ends | ✅ Yes (auto-set) |
| **Full Day Event** | Toggle to make it an all-day event | ❌ Optional toggle |
| **Repeat** | How often the event repeats | ❌ Default: "None" |
| **Type** | Category of the event | ✅ Default: "Event" |

**Steps:**
1. Enter a **Title** (this is required — the Save button won't activate without it).
2. Optionally add a **Description**.
3. Set the **Start** date/time using the date-time picker.
4. Set the **End** date/time (must be after Start).
5. Toggle **"Full day event"** if it spans an entire day.
6. Choose a **Repeat** interval: None, Weekly, Monthly, or Yearly.
7. Choose an **Event Type**: Event, Meeting, Task, Birthday, or Other.
8. Click **Save**. The editor closes and the event appears on your calendar.

> **Validation:** The Save button only activates when:
> - Title is not empty
> - End date/time is not before Start date/time
> - Changes have been made

### 4.2 Editing an Event

1. Navigate to the **Events** page (Calendar view).
2. Click on the event card you want to edit.
3. The **Event Editor** opens with the existing data pre-filled.
4. Make your changes.
5. Click **Save** to update, or **Cancel** to discard changes.

### 4.3 Deleting an Event

1. Open the event in the editor.
2. Click the **Delete** button.
3. The event is permanently removed from your calendar.
4. A toast notification confirms the deletion.

### 4.4 Understanding Event Types & Repeating Events

**Event Types** (visually distinguished with icons):
| Type | Icon | Use Case |
|---|---|---|
| **Event** | 🎫 | General events |
| **Meeting** | 🚪 | Work meetings, appointments |
| **Task** | 📋 | To-do items with deadlines |
| **Birthday** | 🎂 | Birthdays and anniversaries |
| **Other** | ❓ | Anything else |

**Repeat Options:**
| Repeat | Behavior |
|---|---|
| **None** | One-time event |
| **Weekly** | Repeats on the same day of the week |
| **Monthly** | Repeats on the same day of the month |
| **Yearly** | Repeats on the same date every year |

The calendar view automatically expands repeating events and displays them on their respective dates.

### 4.5 Using the Calendar View

The Calendar view (EventPage) provides:
- **Month Navigation** — Browse through months using navigation buttons.
- **Month Grid** — Overview of days in the current month with event indicators.
- **Event Cards** — Click any event card to edit it.
- **Date click** — Clicking a date may initiate adding a new event (context-dependent).

---

## 5. Using MemoAI

MemoAI is your AI-powered conversational assistant. It uses a large language model (LLM) to answer questions, help with tasks, brainstorm ideas, and more — with awareness of your notes and context (when integrated).

### 5.1 Starting a Conversation

1. Navigate to **MemoAI** from the sidebar/dock.
2. If you have no conversations, you'll see a welcome screen: *"Hi [name]! Ask Memo AI anything."*
3. Click the **"New Chat"** button (or the "+" icon) to start a new conversation.
4. A new conversation appears in the conversation list.

You can also start a chat directly from the **Dashboard** by clicking the **"Ask MemoAI"** button.

### 5.2 Asking Questions

1. Type your question in the **text input box** at the bottom of the chat screen.
2. Press **Enter** (or click the **Send** button) to submit your question.
3. The AI processes your query and streams the response in real-time.
4. You can use **Shift+Enter** to add a new line without sending.

**Tip:** You can ask about anything — from summarizing ideas and explaining concepts to drafting content and generating plans.

### 5.3 Conversation Management

**Viewing Conversations:**
- Use the **Conversation Drawer** (hamburger menu icon at the top left) to see all your conversations.
- Each conversation is named automatically by the AI after the first exchange.

**Switching Between Conversations:**
- Click any conversation name in the drawer to switch to it.
- The selected conversation is highlighted.

**Deleting a Conversation:**
- Conversations can be deleted from the conversation drawer (delete action).
- A toast confirms the deletion.

**Creating a New Conversation:**
- Use the **"New Chat"** button.
- If an unsaved "new" conversation already exists, the system switches to it instead of creating another.

### 5.4 Streaming Responses & Timing

When MemoAI is generating a response:
- The response text appears **character-by-character** in real-time (streaming).
- A timer shows how long the AI has been thinking: *"3s ..."* with animated dots.
- While streaming, the input box is **disabled** — wait for completion or cancellation.
- The conversation name may update automatically after the first response once the backend generates a proper title.

---

## 6. Using the Trash

The **Trash** page displays all notes that have been soft-deleted.

**Viewing Deleted Notes:**
1. Navigate to **Trash** from the sidebar/dock.
2. Deleted notes appear in a masonry grid, similar to the Notes page.
3. Each card shows the note preview and last updated date.

**Actions Available in Trash:**
| Action | How | Result |
|---|---|---|
| **Restore a Note** | Click the note → Click **Restore** | Note moves back to active notes |
| **Permanently Delete** | Click the note → Click **Delete** (red) | Note is erased forever |

**Empty State:** When Trash is empty, you'll see a trash can icon with "Trash empty" message.

---

## 7. Customizing the Look & Feel

Memoize offers a robust theming system: **Light/Dark mode** and **5 accent colors**.

### 7.1 Switching Between Light & Dark Mode

The **ThemeSwitch** button (sun/moon icon) is accessible from:
- The landing page (top-right corner)
- The Dashboard (top-right corner)
- The Note Editor (toolbar area)
- The MemoAI page (top-right corner)
- The Auth page (various positions)

**To toggle:**
1. Click the sun/moon button → A popover opens.
2. In the **"Toggle theme"** section, click the **Switch** (toggle).
3. The interface instantly transitions between Light and Dark mode.

> Your theme preference is saved automatically and persists across sessions.

### 7.2 Changing the Accent Color

Accent colors affect buttons, highlights, links, and decorative elements throughout the app.

1. Open the **ThemeSwitch** popover (sun/moon icon).
2. In the **"Set Accent"** section, you'll see 5 colored circles:

| Circle Color | Accent Name |
|---|---|
| 🟢 Green (Default) | **Default** |
| 🔵 Teal | **Teal** |
| 🟡 Yellow | **Yellow** |
| 🟣 Purple | **Purple** |
| 🩷 Pink | **Pink** |

3. Click any color circle to apply it.
4. The accent changes instantly across all UI elements.

> Your accent preference is saved and persists across sessions.

---

## 8. The Dashboard (Home Screen)

The Dashboard is your command center, providing an at-a-glance overview of your recent activity.

**Elements of the Dashboard:**

1. **Time-based Greeting**
    - Morning (before 12 PM): *"Good morning, [Name]"*
    - Afternoon (12–6 PM): *"Good afternoon, [Name]"*
    - Evening (after 6 PM): *"Good evening, [Name]"*

2. **Quick Action Buttons**
    - **Ask MemoAI** — Start a new AI chat session
    - **Add Note** — Create a new note
    - **Add Event** — Create a new event

3. **Recent MemoAI Chats** (Collapsible)
    - Shows your most recent AI conversations.
    - Click **"New Chat"** to start a fresh conversation.
    - Click any conversation to jump to it.

4. **Recent Notes** (Collapsible)
    - Shows the last 4 notes you've created/edited.
    - Click any note card to open it for editing.

5. **Upcoming Events** (Collapsible)
    - Shows events scheduled for the next 7 days.
    - Events are grouped by date with day headers.
    - Click any event to edit it.

**Section states:**
- **Loading**: Skeleton placeholders appear while data is being fetched.
- **Empty**: Encouraging CTAs invite you to add content.
- **Populated**: Content cards in a responsive masonry layout.

---

## 9. Available Platforms & System Requirements

### Supported Platforms

| Platform | Status | Notes |
|---|---|---|
| **Web (Desktop Browser)** | ✅ Fully Supported | Chrome, Firefox, Safari, Edge (latest 2 versions) |
| **Web (Mobile Browser)** | ✅ Fully Supported | Responsive design adapts to mobile |
| **Android (Native App)** | ✅ Supported via Capacitor | Requires Android 8+ |

### Technical Requirements

- **Modern web browser** with JavaScript enabled
- **Internet connection** (required for all features — notes, events, and AI all sync to the cloud)
- **Screen resolution**: Optimized for 360px width and above

### Security

- **Authentication**: JWT access tokens (in-memory) + HTTP-only cookie refresh tokens
- **Data in transit**: All API communication over HTTPS in production
- **Session persistence**: Automatic login restoration on returning visits

---

## 10. Tips & Best Practices

### Notes

- **Use Task Lists** for to-do items within notes — they render as interactive checkboxes.
- **Code Blocks** are great for saving code snippets with proper formatting.
- **The editor auto-saves**, but clicking "Save" before navigating away gives you extra peace of mind.
- **Empty notes won't be saved** — type at least something before the auto-save kicks in.

### Events

- **Use the "Full day event" toggle** for events without specific time constraints (birthdays, holidays).
- **Repeating events** (Weekly, Monthly, Yearly) automatically appear on future dates in the calendar.
- **Event types** help visually distinguish between meetings, tasks, birthdays, and general events.

### MemoAI

- **Start a new conversation** for each distinct topic to keep responses organized.
- **Be specific in your questions** to get better, more focused answers.
- **Conversations are automatically named** by the AI after the first exchange.
- **Use Shift+Enter** to insert line breaks in your query without sending.

### General

- **Toggling themes** doesn't affect your data — play freely.
- **All data syncs automatically** — no manual save needed for most operations.
- **Use the collapsible sections** on the Dashboard to focus on what matters most.
- **Logout** from the user profile popover in the sidebar/dock.

---

## 11. Troubleshooting

### Login Issues

| Problem | Likely Cause | Solution |
|---|---|---|
| "Invalid credentials" | Wrong username/email or password | Double-check your credentials. Use "Forgot password?" to reset. |
| "Username/Email not available" | Already taken | Try a different username or email. |
| Can't sign in after signup | Session not established | Try logging in directly with your credentials. |

### Note-Related Issues

| Problem | Likely Cause | Solution |
|---|---|---|
| Note not saving | Network issue or empty content | Check internet connection. Ensure the note has content. |
| "Failed to create note" toast | Backend error or auth timeout | Try again. If persistent, re-login. |
| Note shows as deleted | Soft-deleted | Go to Trash and restore it. |
| Editor not loading | Corrupted note content | Safe parsing handles most cases. Reload the page. |

### Event-Related Issues

| Problem | Likely Cause | Solution |
|---|---|---|
| Save button inactive | Title empty or End before Start | Ensure title is filled and end time is after start time. |
| Event not appearing in calendar | Refresh needed or repeat expansion issue | Navigate away and back. If using repeat, verify correct setup. |
| "Failed to create event" toast | Backend error | Try again. Check data validity. |

### MemoAI Issues

| Problem | Likely Cause | Solution |
|---|---|---|
| Response not streaming | Network issue or backend timeout | Wait a moment. If stuck, refresh and try again. |
| "Failed to get response" message | Backend error | Try rephrasing your query. If persistent, try a new conversation. |
| Input box disabled after response | UI state issue | Wait a moment for the state to reset. |
| Conversation not naming | Backend hasn't processed naming | The app polls for the name automatically. It should update shortly. |

### General Issues

| Problem | Likely Cause | Solution |
|---|---|---|
| Theme not persisting | localStorage issue | Re-apply your theme preference. It should stick on next visit. |
| Login flash on page reload | Session restore in progress | Wait a moment. The app automatically attempts session restore. |
| App stuck on loader | Auth initialization taking time | Check internet connection. Clear browser cookies and try again. |
| OAuth2 redirect issues | Cookie/redirect configuration | Try using email/password login as an alternative. |

### Contact & Support

If you continue to experience issues, please contact the Memoize administration team or open an issue on the project repository.

---

*Documentation version: v3.0 — July 2026*