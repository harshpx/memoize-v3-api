package com.memoize.api.DevTesting;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @GetMapping("/1")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        var response = testService.testMethod1(4, 2026);
        return ResponseEntity.ok(response);
    }
}
