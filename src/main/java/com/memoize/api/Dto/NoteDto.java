package com.memoize.api.Dto;

import com.memoize.api.Entity.Note;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NoteDto(UUID id, String content, String preview, Boolean isArchived, Boolean isDeleted,
                      LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
    public static NoteDto fromEntity(Note note) {
        return NoteDto.builder()
                .id(note.getId())
                .content(note.getContent())
                .preview(note.getPreview())
                .isArchived(note.getIsArchived())
                .isDeleted(note.getIsDeleted())
                .updatedAt(note.getUpdatedAt())
                .createdAt(note.getCreatedAt())
                .deletedAt(note.getDeletedAt())
                .build();
    }
}
