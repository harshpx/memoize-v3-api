package com.memoize.api.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Builder
public record EmailRequest(@NotBlank @Email String email) {
}
