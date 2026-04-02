package com.memoize.api.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record EventsByDate(@NotBlank String date, List<EventDto> events) {}
