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

The `sendVerificationEmail()` method accepts a `VerificationType` parameter to distinguish between:
- `VERIFY_EMAIL` — Used during signup; validates the email is not already in use
- `RESET_PASSWORD` — Used during password reset; validates the email exists in the system

### Password Reset Flow

The password reset is a **3-step process**:

```
Step 1: POST /auth/reset-password-send
        Body: { "email": "john@example.com" }
        └── Calls sendVerificationEmail(email, RESET_PASSWORD)
            ├── Validates email exists in system
            ├── Checks for existing valid token (rate limit)
            ├── Generates 6-char random code
            ├── Saves to verification_tokens table (10 min expiry)
            └── Sends email with verification code

Step 2: POST /auth/reset-password-check
        Body: { "email": "john@example.com", "verificationCode": "ABC123" }
        └── Calls verifyPasswordResetCode()
            ├── Validates the code (preserves token for next step)
            └── Returns success/failure

Step 3: POST /auth/reset-password
        Body: { "email": "john@example.com", "verificationCode": "ABC123", "newPassword": "newSecurePass" }
        └── Calls resetPassword()
            ├── Validates code again
            ├── Encodes new password with BCrypt
            └── Updates user password in database
```

**Key difference between Step 2 and Step 3:**
- Step 2 (`verifyPasswordResetCode`) uses `toCheckAgain=true` — the verification token is **preserved** so it can be reused in Step 3
- Step 3 (`resetPassword`) uses `toCheckAgain=false` — the verification token is **deleted** after successful validation

### Verification Code Helper

```java
@Transactional
protected boolean verifyCode(String email, String code, boolean toCheckAgain) {
    String encodedCode = Common.encodeBase64(code);
    var token = verificationTokenRepository.findByEmailAndToken(email, encodedCode);
    if (token.isEmpty()) return false;
    if (token.get().getExpiresAt().isBefore(LocalDateTime.now())) {
        verificationTokenRepository.delete(token.get());
        return false;
    }
    if (!toCheckAgain) {
        verificationTokenRepository.delete(token.get());
    }
    return true;
}
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
| `VerificationType` | VERIFY_EMAIL(0), RESET_PASSWORD(1) | Purpose of the email verification code |

The `VerificationType` enum is used in `AuthServiceImpl.sendVerificationEmail()` to:
- `VERIFY_EMAIL`: Validate email is not already in use (for signup)
- `RESET_PASSWORD`: Validate email exists in the system (for password reset)

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
```

---

## 8. Frontend Integration

### Auth Page UI

The frontend provides a unified **Auth Page** with two tabs: **Login** and **Signup**.

**Signup Tab:**
- Fields: Name, Username, Email, Password, Confirm Password
- Real-time availability checks: Username and email are checked against `/auth/check-username` and `/auth/check-email` endpoints with a 1-second debounce after the user stops typing
- If username/email is taken, a red error message is displayed and the form cannot be submitted
- "Send OTP" button triggers `/auth/verify-email` to send a 6-digit verification code
- OTP input field for entering the code
- "Signup" button submits to `/auth/signup` with all fields + verification code
- On success: auto-login, welcome toast, redirect to Dashboard

**Login Tab:**
- Fields: Username or Email, Password
- "Forgot password?" link navigates to password reset flow
- "Login" button submits to `/auth/login`
- On success: welcome toast, redirect to Dashboard

**Password Reset Flow (Frontend):**
1. User clicks "Forgot password?" → enters email → calls `/auth/reset-password-send`
2. User receives email with verification code → enters code → calls `/auth/reset-password-check`
3. User enters new password + confirm password → calls `/auth/reset-password`
4. On success: redirect to Login page with success message

**OAuth2 / Google Sign-In:**
- "Continue with Google" button at the bottom of the form card
- On web: redirects to Google's OAuth consent screen, then back to `{clientUrl}/oauth2redirect`
- On native mobile (iOS/Android via Capacitor): OAuth2 is handled natively
- After OAuth2 success: auto-login, redirect to Dashboard

### Session Management (Frontend)

- **JWT Access Token**: Stored in-memory (not localStorage) for security. Sent as `Authorization: Bearer <token>` header.
- **Refresh Token**: Stored in an HTTP-only cookie (not accessible via JavaScript). Automatically sent on `/auth/refresh` requests.
- **Session Restore**: On page reload, the frontend attempts to call `/auth/refresh` to restore the session. A brief loading state is shown during this process.
- **Auto-Logout**: When the refresh token expires (10 days), the user is redirected to the Auth page.
- **Logout**: Calls `/auth/logout` which clears the refresh token cookie server-side, then redirects to the Auth page.

### UI States

| State | Frontend Behavior |
|-------|-------------------|
| **Loading** | Skeleton placeholders or spinner shown during auth check |
| **Error (Invalid Credentials)** | Red error message: "Invalid credentials" |
| **Error (Username/Email Taken)** | Red inline error next to the field |
| **Error (Network)** | Toast notification with error message |
| **Success** | Welcome toast + redirect to Dashboard |
| **Session Restore** | Brief loading screen while `/auth/refresh` is called |