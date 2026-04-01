package com.memoize.api.Dto;

import com.memoize.api.Entity.Event;
import com.memoize.api.Enum.EventRepeat;
import com.memoize.api.Enum.EventType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record EventDto(
        @NotBlank UUID id,
        @NotBlank String title,
        @NotBlank OffsetDateTime start,
        @NotBlank OffsetDateTime end,
        @NotBlank EventType eventType,
        @NotBlank EventRepeat eventRepeat,
        String description,
        String location,
        @NotBlank LocalDateTime createdAt,
        @NotBlank LocalDateTime updatedAt
) {
    public static EventDto fromEntity(Event event) {
        return EventDto.builder()
                .id(event.getId()).title(event.getTitle())
                .start(event.getStart()).end(event.getEnd())
                .eventType(event.getEventType()).eventRepeat(event.getEventRepeat())
                .description(event.getDescription()).location(event.getLocation())
                .createdAt(event.getCreatedAt()).updatedAt(event.getUpdatedAt()).build();
    }

    public String toString() {
        return "EventDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", eventType=" + eventType +
                ", eventRepeat=" + eventRepeat +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
