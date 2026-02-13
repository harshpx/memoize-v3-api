package com.memoize.api.Dto;

import com.memoize.api.Entity.Note;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NoteDto(UUID id, String content, String preview, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static NoteDto fromEntity(Note note) {
        return NoteDto.builder()
                .id(note.getId())
                .content(note.getContent())
                .preview(note.getPreview())
                .updatedAt(note.getUpdatedAt())
                .createdAt(note.getCreatedAt())
                .build();
    }
}
