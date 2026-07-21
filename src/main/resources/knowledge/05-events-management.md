# Events Management

## Overview

The Events feature provides CRUD operations for calendar events with support for recurring events (daily, weekly, monthly, yearly). Events are user-scoped and support multiple event types.

---

## 1. Entity: Event

| Field       | Type              | Description                              |
|-------------|-------------------|------------------------------------------|
| id          | UUID (PK)         | Auto-generated unique ID                 |
| title       | String(100)       | Event title                              |
| description | String(200)       | Optional event description               |
| location    | String            | Optional location                        |
| start       | OffsetDateTime    | Event start time (UTC)                   |
| end         | OffsetDateTime    | Event end time (UTC)                     |
| eventType   | EventType (Enum)  | EVENT, BIRTHDAY, MEETING, TASK, OTHER    |
| eventRepeat | EventRepeat (Enum)| NONE, YEARLY, MONTHLY, WEEKLY           |
| owner       | User (FK)         | Owner of the event                       |
| createdAt   | LocalDateTime     | Creation timestamp                       |
| updatedAt   | LocalDateTime     | Last update timestamp                    |

---

## 2. API Endpoints

| Method   | Endpoint                  | Description                                    |
|----------|--------------------------|------------------------------------------------|
| GET      | /events/all              | Fetch all events for user                      |
| GET      | /events/upcoming         | Get upcoming events within X days              |
| GET      | /events/monthly          | Get events for a specific month/year           |
| POST     | /events                  | Create a new event                             |
| PUT      | /events/{eventId}        | Update an existing event                       |
| DELETE   | /events/{eventId}        | Delete an event                                |

---

## 3. Operations Detail

### Fetch All Events

```
GET /events/all
```

Returns all events for the authenticated user, including recurring event templates.

### Fetch Upcoming Events

```
GET /events/upcoming?days=30
```

**Parameters:**
| Parameter | Type | Description                              |
|-----------|------|------------------------------------------|
| days      | int  | Number of days to look ahead (must be ≥ 1) |

**Returns:** Grouped by date, with recurring events expanded.

### Fetch Events by Month

```
GET /events/monthly?month=7&year=2026
```

**Parameters:**
| Parameter | Type   | Description                         |
|-----------|--------|-------------------------------------|
| month     | int    | Month (1-12)                        |
| year      | int    | Year (1900-3000)                    |

**Returns:** Events for the full calendar grid (Sunday to Saturday) of the month.

---

## 4. Recurring Events (EventRepeat)

Recurring events are **not stored as separate rows**. Instead, they are expanded dynamically at query time.

### Supported Recurrence Types

| Type     | Description                                               |
|----------|-----------------------------------------------------------|
| NONE     | Single occurrence (no recurrence)                         |
| YEARLY   | Repeats on the same month/day every year                  |
| MONTHLY  | Repeats on the same day of each month                     |
| WEEKLY   | Repeats on the same day of each week                      |

### Recurrence Expansion Logic

```
For each event in the user's collection:

  if eventRepeat == YEARLY:
      Iterate through years in the query range
      Create an occurrence at the same month+day for each year
      If occurrence falls within the query range → include

  if eventRepeat == MONTHLY:
      Iterate through months in the query range
      Create an occurrence at the same day (clamped to month length) of each month
      If occurrence falls within the query range → include

  if eventRepeat == WEEKLY:
      Iterate through weeks in the query range
      Create an occurrence on the same day of week
      If occurrence falls within the query range → include

  if eventRepeat == NONE:
      Single occurrence only
      If it falls within the query range → include
```

---

## 5. Events Grouping

The system returns events grouped by date using `EventsByDate`:

```json
[
  {
    "date": "2026-07-21T00:00:00.000Z",
    "events": [
      { "id": "uuid", "title": "Team Standup", ... },
      { "id": "uuid", "title": "Lunch with Sarah", ... }
    ]
  },
  {
    "date": "2026-07-22T00:00:00.000Z",
    "events": [
      { "id": "uuid", "title": "Gym Session", ... }
    ]
  }
]
```

Each date in the range is included — even if it has zero events (empty array).

---

## 6. Data Flow

### Create Event

```
[Client] -- POST /events --> [EventServiceImpl.createEventByUser()]
                              |
                              ├── Validate user exists
                              ├── Get User entity reference
                              ├── Build Event with all fields
                              ├── Save to database
                              └── Return EventDto
```

### Update Event

```
[Client] -- PUT /events/{eventId} --> [EventServiceImpl.updateEventByUser()]
                                       |
                                       ├── Find event by id + userId
                                       ├── Update all fields (title, start, end, type, repeat)
                                       ├── Conditionally update description & location (if provided)
                                       ├── Save
                                       └── Return EventDto
```

### Delete Event

```
[Client] -- DELETE /events/{eventId} --> [EventServiceImpl.deleteEventByUser()]
                                          |
                                          ├── Execute DELETE with id + userId
                                          ├── If 0 affected → throw IllegalArgumentException
                                          └── Return 1
```

---

## 7. DTOs

### EventModifyRequest (Input)

```json
{
  "title": "Team Standup",
  "start": "2026-07-21T09:00:00.000Z",
  "end": "2026-07-21T09:30:00.000Z",
  "eventType": "MEETING",
  "eventRepeat": "WEEKLY",
  "description": "Daily team sync-up",
  "location": "Conference Room A"
}
```

### EventDto (Output)

```json
{
  "id": "uuid",
  "title": "Team Standup",
  "start": "2026-07-21T09:00:00.000Z",
  "end": "2026-07-21T09:30:00.000Z",
  "eventType": "MEETING",
  "eventRepeat": "WEEKLY",
  "description": "Daily team sync-up",
  "location": "Conference Room A",
  "createdAt": "2026-07-01T10:00:00",
  "updatedAt": "2026-07-21T08:00:00"
}
```

---

## 8. Enums

### EventType

| Enum      | Value | Description              |
|-----------|-------|--------------------------|
| EVENT     | 1     | General event            |
| BIRTHDAY  | 2     | Birthday celebration     |
| MEETING   | 3     | Meeting/Appointment      |
| TASK      | 4     | Task/To-do               |
| OTHER     | 0     | Other type               |

### EventRepeat

| Enum    | Value | Description            |
|---------|-------|------------------------|
| NONE    | 0     | Single occurrence      |
| YEARLY  | 1     | Repeats every year     |
| MONTHLY | 2     | Repeats every month    |
| WEEKLY  | 3     | Repeats every week     |

---

## 9. Security

- All event endpoints require authentication
- Events are scoped to the authenticated user (userId from JWT)
- All queries filter by `owner.id = userId`

---

## 10. Database Table

### events

| Column       | Type         | Constraints                    |
|--------------|--------------|--------------------------------|
| id           | uuid         | PK                             |
| user_id      | uuid         | FK → users(id), ON DELETE CASCADE |
| title        | varchar(100) | NOT NULL                       |
| description  | varchar(200) | Optional                       |
| location     | varchar      | Optional                       |
| start        | timestamptz  | NOT NULL (OffsetDateTime UTC)  |
| end          | timestamptz  | NOT NULL (OffsetDateTime UTC)  |
| event_type   | varchar(50)  | NOT NULL                       |
| event_repeat | varchar      | NOT NULL, DEFAULT 'NONE'       |
| created_at   | timestamp    | NOT NULL                       |
| updated_at   | timestamp    | NOT NULL                       |

**Foreign Key:** `fk_events_user → users(id)`