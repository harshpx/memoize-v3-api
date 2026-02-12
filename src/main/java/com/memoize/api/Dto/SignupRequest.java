package com.memoize.api.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
public record SignupRequest(
        @NotBlank String verificationCode,
        @NotBlank String name,
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password,
        String avatarUrl) {
}
