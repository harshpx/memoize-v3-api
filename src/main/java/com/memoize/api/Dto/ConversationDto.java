package com.memoize.api.Dto;

import com.memoize.api.Entity.Conversation;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ConversationDto(UUID id, UUID userId, String name, String summary, String recentChats, boolean isProperName, boolean isNew, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static ConversationDto fromEntity(Conversation conversation) {
        return ConversationDto.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .name(conversation.getName())
                .summary(conversation.getSummary())
                .recentChats(conversation.getRecentChats())
                .isProperName(conversation.isProperName())
                .isNew(conversation.isNew())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}
