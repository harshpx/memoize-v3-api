package com.memoize.api.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
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
                .baseUrl("https://api.brevo.com/v3/smtp/email")
                .defaultHeaders(h -> {
                    h.add("api-key", mailApiKey);
                    h.add("content-type", "application/json");
                })
                .build();
    }

    @Override
    public void sendVerificationEmail(String email, String verificationCode) {
        if (email == null || email.isBlank() || verificationCode == null || verificationCode.isBlank()) {
            throw new RuntimeException("Recipient email address and verification code is required.");
        }
        var body = Map.of(
                "sender", Map.of("email", "support@memoize.in", "name", "Memoize Team"),
                "to", List.of(Map.of("email", email)),
                "subject", "Memoize email verification",
                "htmlContent", "<div style='font-size:16px;'>Your email verification code is: <strong>" + verificationCode + "</strong></div>" + "<div>This code will only be active for 10 minutes (you will only be able to generate a new code once this expires)</div>"
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
