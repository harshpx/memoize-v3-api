package com.memoize.api;

import com.memoize.api.Config.Common;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

public class MailApiTests {
    private final RestClient restClient = RestClient.create();

    @Test
    public void testMailApi() {
        String mailjetApiKey = "USE_YOUR_KEY";
        String mailjetApiSecret = "USE_YOUR_SECRET";
        String url = "https://api.mailjet.com/v3/send";
        Map<String, Object> body = Map.of(
                "FromEmail", "support@memoize.in",
                "FromName", "Memoize Team",
                "Subject", "Your email verification code",
//                "Text-part", "Your email verification code is: " + Common.generateRandomString(6),
                "Html-part", "<div style='font-size:16px;'>Your email verification code is: <strong>" + Common.generateRandomString(6) + "</strong></div>",
                "Recipients", List.of(Map.of("Email", "harsh.rzf@gmail.com"))
        );

        String response = restClient.post()
                .uri(url).headers(h -> h.setBasicAuth(mailjetApiKey, mailjetApiSecret))
                .body(body).retrieve().body(String.class);
        System.out.println(response);
    }
}
