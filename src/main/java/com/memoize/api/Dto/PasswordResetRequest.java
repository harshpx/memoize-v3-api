package com.memoize.api.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record PasswordResetRequest(
        @NotBlank String email,
        @NotBlank String verificationCode,
        @NotBlank String newPassword) {
}
