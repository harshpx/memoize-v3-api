package com.memoize.api.Dto;

import com.memoize.api.Enum.ChatType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatDto(UUID id, UUID conversationId, String content, ChatType type, LocalDateTime createdAt) {}
