package com.memoize.api;

import com.memoize.api.Dto.CommonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Home {
    @Value("${spring.profiles.active:default}")
    private String environment;
    @GetMapping
    public ResponseEntity<CommonResponse<String>> home() {
        var response = CommonResponse.success("Memoize API is running! on environment: " + environment);
        return ResponseEntity.ok(response);
    }
}
