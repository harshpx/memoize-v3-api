package com.memoize.api.Dto;

import com.memoize.api.Entity.Conversation;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ConversationDto(UUID id, UUID userId, String name, String summary, boolean isProperName, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ConversationDto fromEntity(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .name(conversation.getName())
                .summary(conversation.getSummary())
                .isProperName(conversation.isProperName())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
