package com.memoize.api.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VerificationCodeCheckRequest(@NotBlank @Email String email, @NotBlank String verificationCode) {
}
