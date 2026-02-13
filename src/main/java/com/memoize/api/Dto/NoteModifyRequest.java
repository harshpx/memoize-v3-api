package com.memoize.api.Dto;

import jakarta.validation.constraints.NotBlank;

public record NoteModifyRequest(@NotBlank String content, @NotBlank String preview) {
}
