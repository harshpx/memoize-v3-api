# User Management

## Overview

The User Management feature provides user profile retrieval. The User entity serves as the core authentication principal for the entire application.

---

## 1. Entity: User

| Field       | Type              | Description                              |
|-------------|-------------------|------------------------------------------|
| id          | UUID (PK)         | Auto-generated unique ID                 |
| name        | String(100)       | Full display name                        |
| username    | String(50)        | Unique username                          |
| email       | String(100)       | Unique email address                     |
| password    | String            | BCrypt-hashed password (empty for OAuth users) |
| authSource  | AuthSource (Enum) | EMAIL, GOOGLE, GITHUB, MICROSOFT        |
| avatarUrl   | String            | Profile picture URL (default: placeholder) |
| role        | Role (Enum)       | ADMIN, MOD, USER                        |
| createdAt   | LocalDateTime     | Account creation timestamp               |
| updatedAt   | LocalDateTime     | Profile last updated timestamp           |
| notes       | Set<Note>         | One-to-many relationship with notes      |
| events      | Set<Event>        | One-to-many relationship with events     |

### User as Spring Security Principal

The `User` entity implements `UserDetails`:

```java
public class User implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    // getPassword() returns the BCrypt hashed password
    // getUsername() returns the email (used as the identifier along with username)
}
```

### PrePersist Defaults

```java
@PrePersist
public void prePersist() {
    if (role == null) role = Role.USER;
    if (authSource == null) authSource = AuthSource.EMAIL;
    if (avatarUrl == null || avatarUrl.isBlank())
        avatarUrl = "https://i.imgur.com/8GO2mo5.png";
}
```

---

## 2. API Endpoints

| Method   | Endpoint  | Description                       |
|----------|-----------|-----------------------------------|
| GET      | /user/me  | Get authenticated user's profile  |

### Profile Response

```
GET /user/me
Authorization: Bearer <jwt-token>
```

```json
{
  "data": {
    "id": "uuid",
    "name": "John Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "avatarUrl": "https://i.imgur.com/8GO2mo5.png",
    "role": "USER"
  },
  "success": true,
  "timestamp": "2026-07-21T12:00:00"
}
```

---

## 3. User Lookup

### UserDetailsServiceImpl

The `UserDetailsServiceImpl.loadUserByUsername()` method accepts either a **username** or **email** as the identifier:

```java
@Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
Optional<User> findByIdentifier(@Param("identifier") String identifier);
```

This dual lookup enables login with either username or email.

---

## 4. User Repository

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    // Login lookup (username OR email)
    Optional<User> findByIdentifier(String identifier);

    // Username availability check
    Optional<User> findByUsername(String username);

    // Email availability check
    Optional<User> findByEmail(String email);

    // Check auth source for OAuth linking
    Optional<AuthSource> getAuthSourceByEmail(String email);
}
```

---

## 5. DTOs

### UserDto (Output)

```json
{
  "id": "uuid",
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "avatarUrl": "https://i.imgur.com/8GO2mo5.png",
  "role": "USER"
}
```

---

## 6. Roles

| Role    | Value | Description      |
|---------|-------|------------------|
| ADMIN   | 1     | System admin     |
| MOD     | 2     | Moderator        |
| USER    | 3     | Regular user     |

Note: Role-based authorization is set up in the JWT and SecurityContext, but granular permission checks per endpoint are not yet implemented.

---

## 7. Auth Sources

| Source    | Value | Description                  |
|-----------|-------|------------------------------|
| EMAIL     | 1     | Email + password registration |
| GOOGLE    | 2     | Google OAuth2 login          |
| GITHUB    | 3     | GitHub OAuth2 (future)        |
| MICROSOFT | 4     | Microsoft OAuth2 (future)     |

---

## 8. Database Table

### users

| Column      | Type         | Constraints                           |
|-------------|--------------|---------------------------------------|
| id          | uuid         | PK, default gen_random_uuid()         |
| name        | varchar(100) | NOT NULL                              |
| username    | varchar(50)  | NOT NULL, UNIQUE                      |
| email       | varchar(100) | NOT NULL, UNIQUE                      |
| password    | varchar      | (nullable for OAuth users)            |
| auth_source | varchar(20)  | NOT NULL, DEFAULT 'EMAIL'             |
| avatar_url  | text         | DEFAULT placeholder URL               |
| role        | varchar(10)  | NOT NULL, DEFAULT 'USER'              |
| created_at  | timestamp    | NOT NULL                              |
| updated_at  | timestamp    | NOT NULL                              |

**Indexes:**
- `idx_user_username` on `username`
- `idx_user_email` on `email`