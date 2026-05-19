package com.memoize.api.Dto;

import com.memoize.api.Entity.AiChat;
import com.memoize.api.Enum.ChatType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record AiChatDto(UUID id, UUID userId, String content, ChatType type, LocalDateTime createdAt) {
    public static AiChatDto fromEntity(AiChat entity) {
        return AiChatDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
