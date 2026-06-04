package com.memoize.api.Service;

import com.memoize.api.Enum.VerificationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class MailServiceImpl implements MailService {
    private final RestClient restClient;

    public MailServiceImpl(
            @Value("${mail.api.key}") String mailApiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://send.api.mailtrap.io/api/send")
                .defaultHeaders(h -> {
                    h.add("Authorization", "Bearer " + mailApiKey);
                    h.add("Content-Type", "application/json");
                })
                .build();
    }

    @Override
    public void sendVerificationCodeEmail(String email, String verificationCode, VerificationType verificationType) {
        if (email == null || email.isBlank() || verificationCode == null || verificationCode.isBlank()) {
            throw new RuntimeException("Recipient email address and verification code is required.");
        }
        
        String subject = "";
        if (verificationType == VerificationType.VERIFY_EMAIL) {
            subject = "Memoize Email Verification";
        }  else if (verificationType == VerificationType.RESET_PASSWORD) {
            subject = "Memoize Forgot Password Code";
        }

        String text = "";
        if (verificationType == VerificationType.VERIFY_EMAIL) {
            text = """
                    Your email verification code is: %s
                    This code will only be active for 10 minutes.
                    (You will only be able to generate new code once this expires)
                   """.formatted(verificationCode);
        } else if (verificationType == VerificationType.RESET_PASSWORD) {
            text = """
                    Your Password Reset verification code is: %s
                    This code will only be active for 10 minutes.
                    (You will only be able to generate new code once this expires)
                   """.formatted(verificationCode);
        }

        var body = Map.of(
                "from", Map.of("email", "support@memoize.in", "name", "Memoize Team"),
                "to", List.of(Map.of("email", email)),
                "subject", subject,
                "text", text
        );

        String response = restClient.post()
                .body(body).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Failed to send verification email: " + res.getStatusCode().value());
                }).onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Mail service is currently unavailable. Please try again later.");
                }).body(String.class);
    }
}
