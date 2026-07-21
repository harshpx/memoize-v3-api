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