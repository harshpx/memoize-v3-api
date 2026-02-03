package com.memoize.api.Dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class LoginRequest {
    @NotNull
    @NotEmpty
    private String identifier;
    @NotNull
    @NotEmpty
    private String password;
}
