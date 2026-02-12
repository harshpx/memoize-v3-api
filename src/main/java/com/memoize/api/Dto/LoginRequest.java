package com.memoize.api.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {
}
