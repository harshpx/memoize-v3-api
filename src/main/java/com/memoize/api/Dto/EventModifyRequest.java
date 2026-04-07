package com.memoize.api.Dto;

import com.memoize.api.Enum.EventRepeat;
import com.memoize.api.Enum.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record EventModifyRequest(
        @NotBlank String title,
        @NotNull OffsetDateTime start,
        @NotNull OffsetDateTime end,
        @NotNull EventType eventType,
        @NotNull EventRepeat eventRepeat,
        String description,
        String location
) {}
