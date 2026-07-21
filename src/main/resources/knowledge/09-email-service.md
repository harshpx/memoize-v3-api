# Email Service

## Overview

The Email Service handles sending verification codes and password reset codes via email using the **Mailtrap API**. It is triggered during signup and password reset flows.

---

## 1. Mailtrap Integration

### Configuration

```yaml
mail:
  api:
    key: ${MAIL_API_KEY}
```

The API key is set via environment variable `MAIL_API_KEY`.

### HTTP Client

A `RestClient` is configured with:
- **Base URL**: `https://send.api.mailtrap.io/api/send`
- **Authorization**: Bearer token with the API key
- **Content-Type**: `application/json`

---

## 2. Email Types

### Verification Email (Signup)

Sent when a new user signs up to verify their email address.

| Property   | Value                                   |
|------------|-----------------------------------------|
| Subject    | "Memoize Email Verification"            |
| From       | support@memoize.in (Memoize Team)       |
| Content    | 6-character verification code + expiry info |

**Template:**
```
Your email verification code is: ABC123
This code will only be active for 10 minutes.
(You will only be able to generate new code once this expires)
```

### Password Reset Email

Sent when a user requests to reset their password.

| Property   | Value                                     |
|------------|-------------------------------------------|
| Subject    | "Memoize Forgot Password Code"            |
| From       | support@memoize.in (Memoize Team)         |
| Content    | 6-character verification code + expiry info |

**Template:**
```
Your Password Reset verification code is: ABC123
This code will only be active for 10 minutes.
(You will only be able to generate new code once this expires)
```

---

## 3. API Request Format

```json
{
  "from": {
    "email": "support@memoize.in",
    "name": "Memoize Team"
  },
  "to": [
    { "email": "user@example.com" }
  ],
  "subject": "Memoize Email Verification",
  "text": "Your email verification code is: ABC123\n..."
}
```

---

## 4. Error Handling

| Status Code | Error Message                                      |
|-------------|----------------------------------------------------|
| 4xx         | "Failed to send verification email: {statusCode}"   |
| 5xx         | "Mail service is currently unavailable. Please try again later." |
| Null/Empty  | "Recipient email address and verification code is required." |

---

## 5. Verification Token Storage

Each verification code is stored in the `verification_tokens` table:

| Column      | Type      | Description                           |
|-------------|-----------|---------------------------------------|
| id          | uuid (PK) | Auto-generated unique ID              |
| token       | varchar   | Base64-encoded verification code      |
| email       | varchar   | Recipient email address               |
| expires_at  | timestamp | Expiration time (default: 10 minutes) |

### Rate Limiting

```java
if (verificationTokenRepository.existsValidTokenByEmail(email)) {
    return; // Silently skip if a valid token already exists
}
```

A new verification code will only be sent if no existing valid (unexpired) token exists for that email. This prevents abuse/spam.

---

## 6. Verification Code Properties

| Property          | Value            |
|-------------------|------------------|
| Length            | 6 characters     |
| Character Set     | A-Z, a-z, 0-9    |
| Storage           | Base64-encoded   |
| Expiry            | 10 minutes       |
| Rate Limit        | 1 per expiry window per email |

### Code Generation (Common.java)

```java
private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                                    "abcdefghijklmnopqrstuvwxyz" +
                                    "0123456789";
private static final SecureRandom random = new SecureRandom();

public static String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
    }
    return sb.toString();
}
```

---

## 7. Frontend Integration

### Email Trigger Flows

The email service is triggered by two user-facing flows on the frontend:

**1. Signup OTP Flow:**
```
[Auth Page - Signup Tab]
    │
    ├── User fills: Name, Username, Email, Password, Confirm Password
    ├── User clicks "Send OTP" button
    │   └── POST /auth/verify-email → Backend sends verification email
    ├── User receives 6-digit code at their email inbox
    ├── User enters code in the OTP input field
    └── User clicks "Signup" to complete registration
```

- The "Send OTP" button is disabled while the request is in progress
- A success message indicates the code was sent
- If the email is already registered, an inline error is shown and the OTP is not sent
- Rate limiting: If a valid token already exists (within 10 min window), the backend silently skips sending a new one

**2. Password Reset Flow:**
```
[Auth Page - "Forgot password?" link]
    │
    ├── Step 1: User enters email → POST /auth/reset-password-send
    │   └── Verification code sent to email
    │
    ├── Step 2: User enters code → POST /auth/reset-password-check
    │   └── Code verified (token preserved for next step)
    │
    └── Step 3: User enters new password → POST /auth/reset-password
        └── Password updated
```

- The password reset flow is a multi-step process with dedicated UI pages/steps
- Each step validates before proceeding to the next
- If the email does not exist in the system, the user is notified

### UI States

| State | Frontend Behavior |
|-------|-------------------|
| **Sending OTP** | Button shows loading state, disabled |
| **OTP Sent** | Success message, OTP input field enabled |
| **OTP Error (Network)** | Toast notification: "Failed to send verification email" |
| **OTP Rate Limited** | Silently handled (backend skips if valid token exists); no duplicate sent |
| **Password Reset - Step 1** | Email input form |
| **Password Reset - Step 2** | OTP input form |
| **Password Reset - Step 3** | New password + confirm password form |
| **Password Reset Success** | Redirect to Login page with success message |