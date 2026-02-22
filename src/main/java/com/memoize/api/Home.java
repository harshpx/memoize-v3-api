package com.memoize.api;

import com.memoize.api.Dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/")
public class Home {
    @Value("${spring.profiles.active:default}")
    private String environment;
    @GetMapping
    public ResponseEntity<CommonResponse<Map<String, Object>>> home() {
        var data = Map.of(
                "message", (Object)"Memoize API is running!",
                "environment", environment
        );
        var response = CommonResponse.success(data);
        return ResponseEntity.ok(response);
    }
}
