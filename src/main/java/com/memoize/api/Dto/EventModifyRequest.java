package com.memoize.api.Dto;

import com.memoize.api.Enum.EventRepeat;
import com.memoize.api.Enum.EventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record EventModifyRequest(
        @NotBlank String title,
        @NotBlank OffsetDateTime start,
        @NotBlank OffsetDateTime end,
        @NotBlank EventType eventType,
        @NotBlank EventRepeat eventRepeat,
        String description,
        String location
) {}
