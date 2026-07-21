# Authentication & Authorization

## Overview

Memoize supports two authentication methods:
1. **Email-based authentication** (signup/login with email + password)
2. **OAuth2 via Google** (and extensible to GitHub, Microsoft)

All authenticated endpoints require a **JWT Bearer token** in the `Authorization` header.
Refresh tokens are issued as **HTTP-only cookies** for secure token rotation.

---

## 1. Email-Based Authentication

### Signup Flow

```
[Client] -- POST /auth/signup --> [AuthServiceImpl.signup()]
              |
              ├── Validates verification code (6-char alphanumeric)
              ├── Encodes password with BCrypt (strength 10)
              ├── Saves User entity (role=USER, authSource=EMAIL)
              ├── Generates JWT (10 min expiry)
              ├── Generates Refresh Token (10 day expiry)
              └── Returns access token + Set-Cookie (refresh token)
```

**Signup Request:**
```json
{
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePass123",
  "verificationCode": "ABC123"
}
```

**Signup Response:**
```json
{
  "data": { "accessToken": "eyJhbGciOiJIUzI1NiJ9..." },
  "success": true,
  "timestamp": "2026-07-21T12:00:00"
}
```

### Login Flow

```
[Client] -- POST /auth/login --> [AuthServiceImpl.login()]
              |
              ├── Authenticates via AuthenticationManager (DaoAuthenticationProvider)
              ├── Loads user via UserDetailsServiceImpl (by username or email)
              ├── Validates password with BCryptPasswordEncoder
              ├── Generates JWT (10 min expiry)
              ├── Generates Refresh Token (10 day expiry)
              └── Returns access token + Set-Cookie (refresh token)
```

**Login Request:**
```json
{
  "identifier": "johndoe",
  "password": "securePass123"
}
```

### Verification Email Flow

```
[Client] -- POST /auth/verify-email --> [AuthServiceImpl.sendVerificationEmail()]
              |
              ├── Checks email availability (must be unused)
              ├── Checks for existing valid token (rate limit)
              ├── Generates 6-char random code
              ├── Encodes code in Base64, saves to verification_tokens table (10 min expiry)
              └── Sends email via Mailtrap API with verification code
```

### Password Reset Flow

```
Step 1: POST /auth/reset-password-send
        └── Sends verification code to email (must exist in system)

Step 2: POST /auth/reset-password-check
        └── Verifies the code (preserves token for next step)

Step 3: POST /auth/reset-password
        └── Validates code again + updates password with BCrypt
```

### Username/Email Availability

| Endpoint                  | Method | Description                |
|--------------------------|--------|----------------------------|
| `/auth/check-username`   | GET    | `?username=johndoe` → bool |
| `/auth/check-email`      | GET    | `?email=john@test.com` → bool |

---

## 2. OAuth2 Authentication (Google)

### Flow

```
[Browser] -- /oauth2/authorization/google --> [Google OAuth]
                |
                v
[Google Redirect] --> [OAuth2Config.successHandler]
                |
                ├── Extracts user info via OAuth2UserInfoGoogle
                │   ├── email, name, picture (avatar), sub (for username)
                │   └── Generates username from name + random suffix
                ├── Looks up user by email:
                │   ├── EXISTS: Validates auth source matches
                │   └── NOT EXISTS: Creates new User (authSource=GOOGLE)
                ├── Generates JWT + Refresh Token
                ├── Set-Cookie (refresh token, httpOnly, secure, SameSite=None)
                └── Redirects to: {clientUrl}/oauth2redirect
```

### OAuth2UserInfo Interface

Extensible to add more providers (GitHub, Microsoft):

```java
public interface OAuth2UserInfo {
    String email();
    String username();
    String name();
    String avatarUrl();
}
```

### Provider Support

| Provider  | Implementation          | Status     |
|-----------|------------------------|------------|
| Google    | OAuth2UserInfoGoogle   | ✅ Active  |
| GitHub    | Not implemented        | ❌ Planned |
| Microsoft | Not implemented        | ❌ Planned |

---

## 3. JWT Authentication

### Token Structure

- **Algorithm**: HMAC-SHA256 (HS256)
- **Claims**: `sub` (userId UUID), `role` (USER/ADMIN/MOD), `iat`, `exp`
- **Expiry**: 10 minutes
- **Secret**: Configured via `jwt.secret` property

### Filter Chain (JwtFilter)

```
[Incoming Request]
     |
     ├── Public endpoint? → Skip filter, continue chain
     ├── Authorization header present with "Bearer " prefix?
     │   ├── NO → Return 401 Unauthorized
     │   └── YES → Extract token
     ├── Token valid? (signature + expiry)
     │   ├── NO → Return 401 Unauthorized
     │   └── YES → Extract claims
     ├── Create AuthPrincipal(userId, role)
     ├── Set SecurityContext (authorities: ROLE_USER/ROLE_ADMIN)
     └── Continue filter chain
```

### AuthPrincipal Record

```java
public record AuthPrincipal(UUID userId, String userRole) {}
```

Used across all controllers via `@AuthenticationPrincipal AuthPrincipal principal`.

---

## 4. Refresh Token Management

### Lifecycle

| Operation | Endpoint            | Description                              |
|-----------|---------------------|------------------------------------------|
| Issue     | /auth/login, /auth/signup, OAuth2 success | Generated and sent as Set-Cookie |
| Refresh   | /auth/refresh       | Validates cookie, issues new JWT + new refresh token (rotation) |
| Logout    | /auth/logout        | Deletes refresh token from DB + clears cookie |

### Refresh Token Cookie

- **Name**: `refreshToken`
- **Path**: `/auth/refresh`
- **HttpOnly**: true
- **Secure**: true
- **SameSite**: None
- **Max-Age**: 10 days

### Token Structure (stored)

- `token_id` (UUID, primary key)
- `token_hash` (BCrypt hash of the secret portion)
- `user_id` (UUID, reference)
- `expires_at` (timestamp, 10 days)

### Client-Side Format

The cookie value is `{tokenId}.{random64CharSecret}`, where:
- `tokenId` identifies the DB record
- `random64CharSecret` is verified against the BCrypt hash

---

## 5. Security Configuration

### SecurityConfig

```java
- CSRF: DISABLED
- CORS: Enabled (configured origins)
- Session: STATELESS (no HTTP sessions)
- Public endpoints: "/", "/login", "/auth/**"
- All other endpoints: AUTHENTICATED
- JWT Filter: Before UsernamePasswordAuthenticationFilter
- OAuth2 Login: Custom success/failure handlers
- HTTP Basic: DISABLED
- Form Login: DISABLED
- Logout: DISABLED (handled via /auth/logout endpoint)
```

### CORS Configuration

```yaml
Allowed Origins: {client-url}, https://localhost
Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
Allowed Headers: Authorization, Content-Type
Allow Credentials: true
```

---

## 6. Enums

| Enum             | Values                      | Purpose                                   |
|------------------|-----------------------------|-------------------------------------------|
| `Role`           | ADMIN, MOD, USER            | User role for authorization                |
| `AuthSource`     | EMAIL, GOOGLE, GITHUB, MICROSOFT | Where the user account originated     |
| `VerificationType` | VERIFY_EMAIL, RESET_PASSWORD | Purpose of the email verification code |

---

## 7. Error Handling

| Exception                      | HTTP Status | Description                                 |
|-------------------------------|-------------|---------------------------------------------|
| BadCredentialsException       | 401         | Invalid login credentials or verification code |
| JwtException / ServletException | 401       | Missing/invalid/expired JWT                  |
| EntityNotFoundException       | 404         | User or resource not found                   |
| EntityExistsException         | 409         | Duplicate resource (e.g., conversation)      |
| IllegalArgumentException      | 400         | Invalid input or bad request                 |
| IllegalStateException         | 400         | Invalid state (e.g., deleting already-deleted note) |
| BindException                 | 400         | Validation errors                            |
| Generic Exception             | 500         | Internal server error                        |

Error response format:
```json
{
  "error": "Error message description",
  "status": 401,
  "timestamp": "2026-07-21T12:00:00"
}