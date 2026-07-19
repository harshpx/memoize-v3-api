package com.memoize.api.Dto;

import com.memoize.api.Entity.Conversation;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ConversationDto(UUID id, UUID userId, String name, boolean isProperName, boolean isNew, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ConversationDto fromEntity(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .name(conversation.getName())
                .isProperName(conversation.isProperName())
                .isNew(conversation.isNew())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
