package com.memoize.api.Service;

import com.memoize.api.Dto.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MailServiceImpl implements MailService {
    private final RestClient restClient;

    ;

    public MailServiceImpl(
            @Value("${mailjet.api.key}") String mailjetApiKey,
            @Value("${mailjet.api.secret}") String mailjetApiSecret
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.mailjet.com/v3/send")
                .defaultHeaders(h -> h.setBasicAuth(mailjetApiKey, mailjetApiSecret))
                .build();
    }

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        if (emailRequest.getRecipients() == null || emailRequest.getRecipients().isEmpty()) {
            throw new RuntimeException("At least one recipient email address is required.");
        }
        List<Map<String, Object>> recipients = emailRequest.getRecipients()
                .stream().map(mail -> Map.of("Email", (Object) mail)).toList();
        Map<String, Object> body = new HashMap<>();
        body.put("FromEmail", "support@memoize.in");
        body.put("FromName", "Memoize Team");
        body.put("Subject", emailRequest.getSubject());
        body.put("Html-part", emailRequest.getBody());
        body.put("Recipients", recipients);

        restClient.post()
                .body(body).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Failed to send email: " + res.getStatusCode().value());
                }).onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Mail service is currently unavailable. Please try again later.");
                });
    }

    @Override
    public void sendVerificationEmail(String email, String verificationCode) {
        if (email == null || email.isBlank() || verificationCode == null || verificationCode.isBlank()) {
            throw new RuntimeException("Recipient email address and verification code is required.");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("FromEmail", "support@memoize.in");
        body.put("FromName", "Memoize Team");
        body.put("Subject", "Your email verification code for Memoize");
        body.put("Html-part", "<div style='font-size:16px;'>Your email verification code is: <strong>" + verificationCode + "</strong></div>");
        body.put("Recipients", List.of(Map.of("Email", (Object) email)));

        String response = restClient.post()
                .body(body).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new RuntimeException("Failed to send verification email: " + res.getStatusCode().value());
                }).onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new RuntimeException("Mail service is currently unavailable. Please try again later.");
                }).body(String.class);
        System.out.println("Verification email sent successfully: " + response);
    }
}
