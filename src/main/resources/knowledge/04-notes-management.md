# Notes Management

## Overview

The Notes feature provides CRUD operations with soft-delete support. Notes are user-scoped, paginated, and support content + preview fields.

---

## 1. Entity: Note

| Field       | Type           | Description                           |
|-------------|----------------|---------------------------------------|
| id          | UUID (PK)      | Auto-generated unique ID              |
| content     | String (TEXT)  | Full note content                     |
| preview     | String         | Short preview/summary text            |
| isArchived  | Boolean        | Archive flag (default: false)         |
| isDeleted   | Boolean        | Soft-delete flag (default: false)     |
| deletedAt   | LocalDateTime  | When the note was soft-deleted        |
| owner       | User (FK)      | Owner of the note                     |
| createdAt   | LocalDateTime  | Creation timestamp                    |
| updatedAt   | LocalDateTime  | Last update timestamp                 |

---

## 2. API Endpoints

| Method   | Endpoint              | Description                              |
|----------|-----------------------|------------------------------------------|
| GET      | /notes                | Fetch notes (paginated, filter by deleted status) |
| POST     | /notes                | Create a new note                        |
| PUT      | /notes/{id}           | Update an existing note                  |
| DELETE   | /notes/{id}           | Soft-delete a note                       |
| PUT      | /notes/{id}/restore   | Restore a soft-deleted note              |
| DELETE   | /notes/{id}/permanent | Permanently delete a note                |

---

## 3. Operations Detail

### Fetch Notes

```
GET /notes?deleted=false&page=0&size=50&sort=updatedAt,desc
```

**Parameters:**
| Parameter | Type    | Default | Description                    |
|-----------|---------|---------|--------------------------------|
| deleted   | boolean | false   | Filter by soft-delete status   |
| page      | int     | 0       | Page number (0-indexed)        |
| size      | int     | 50      | Page size                      |
| sort      | String  | updatedAt,desc | Sort field and direction |

**Response:**
```json
{
  "data": {
    "content": [
      {
        "id": "uuid",
        "content": "Full note content here...",
        "preview": "Short preview",
        "isArchived": false,
        "isDeleted": false,
        "createdAt": "2026-07-21T10:00:00",
        "updatedAt": "2026-07-21T11:00:00",
        "deletedAt": null
      }
    ],
    "pageable": { ... },
    "totalPages": 1,
    "totalElements": 1,
    "last": true,
    ...
  },
  "success": true,
  "timestamp": "2026-07-21T12:00:00"
}
```

### Create Note

```
POST /notes
Content-Type: application/json

{
  "content": "Full note content...",
  "preview": "Short preview text"
}
```

### Update Note

```
PUT /notes/{id}
Content-Type: application/json

{
  "content": "Updated content...",
  "preview": "Updated preview"
}
```

### Soft-Delete Note

```
DELETE /notes/{id}
```

- Marks `isDeleted = true`, sets `deletedAt` timestamp
- Throws `IllegalStateException` if already deleted
- Returns the updated NoteDto

### Restore Note

```
PUT /notes/{id}/restore
```

- Sets `isDeleted = false`, clears `deletedAt`
- Throws `IllegalStateException` if already active
- Returns the restored NoteDto

### Permanent Delete

```
DELETE /notes/{id}/permanent
```

- Permanently removes the note from the database
- Only allowed if the note is already soft-deleted
- Returns count of deleted rows (1 = success, 0 = failure/not found)

---

## 4. Data Flow

### Create Note

```
[Client] -- POST /notes --> [NoteControllerImpl.createNoteByUser()]
                              |
                              ├── Validate user exists
                              ├── Get User entity reference (not full load)
                              ├── Build Note with content + preview + owner
                              ├── Save to database
                              └── Return NoteDto
```

### Soft-Delete Flow

```
[Client] -- DELETE /notes/{id} --> [NoteControllerImpl.deleteNoteByUser()]
                                     |
                                     ├── Find note by id + userId
                                     ├── Check if already deleted → throw IllegalStateException
                                     ├── Set isDeleted = true
                                     ├── Set deletedAt = now
                                     ├── Save
                                     └── Return updated NoteDto
```

### Permanent Delete Flow

```
[Client] -- DELETE /notes/{id}/permanent --> [NoteControllerImpl.permanentDeleteNoteByUser()]
                                               |
                                               ├── Execute DELETE query with id + userId
                                               ├── If 0 rows affected → throw IllegalArgumentException
                                               └── Return 1
```

---

## 5. Repository Queries

```java
public interface NoteRepository extends JpaRepository<Note, UUID> {
    // Find by composite key (id + owner)
    Optional<Note> findByIdAndOwnerId(UUID id, UUID ownerId);

    // Paginated fetch with deleted filter
    @Query("SELECT n FROM Note n WHERE n.owner.id = :userId AND n.isDeleted = :isDeleted")
    Page<Note> findNotesForUser(UUID userId, boolean isDeleted, Pageable pageable);

    // Permanent delete
    @Modifying
    int deleteByIdAndOwnerId(UUID noteId, UUID userId);
}
```

---

## 6. DTOs

### NoteModifyRequest (Input)

```json
{
  "content": "Full note content...",
  "preview": "Short preview"
}
```

### NoteDto (Output)

```json
{
  "id": "uuid",
  "content": "...",
  "preview": "...",
  "isArchived": false,
  "isDeleted": false,
  "createdAt": "2026-07-21T10:00:00",
  "updatedAt": "2026-07-21T11:00:00",
  "deletedAt": null
}
```

---

## 7. Security

- All note endpoints require authentication
- Notes are scoped to the authenticated user (userId from JWT)
- All queries filter by `owner.id = userId`

---

## 8. Database Table

### notes

| Column       | Type      | Constraints                    |
|--------------|-----------|--------------------------------|
| id           | uuid      | PK                             |
| user_id      | uuid      | FK → users(id), ON DELETE CASCADE |
| content      | text      | NOT NULL                       |
| preview      | varchar   |                                |
| is_archived  | boolean   | DEFAULT false                  |
| is_deleted   | boolean   | DEFAULT false                  |
| deleted_at   | timestamp |                                |
| created_at   | timestamp | NOT NULL                       |
| updated_at   | timestamp | NOT NULL                       |

**Foreign Key:** `fk_notes_user → users(id)`